package net.nueca.concessioengine.adapters.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.adapters.interfaces.ImageLoaderListener;
import net.nueca.concessioengine.lists.SelectedProductItemList;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.Settings;
import net.nueca.imonggosdk.objects.base.DBTable;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.document.DocumentPurpose;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.Configurations;
import net.nueca.imonggosdk.tools.ProductListTools;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public class ProductsAdapterHelper {

    private static RequestQueue imageRequestQueue;
    private static ImageLoader imageLoader;
    private static ImonggoDBHelper2 dbHelper;
    private static Session session;
    private static DocumentPurpose reason;
    private static SelectedProductItemList selectedReturnProductItems = null;
    private static SelectedProductItemList selectedProductItems = null;
    public static ImageLoaderListener imageLoaderListener = null;
    public static boolean isDuplicating = false;

    private static Customer selectedCustomer;
    private static CustomerGroup selectedCustomerGroup;
    private static Branch selectedBranch;
    private static Branch source, destination;
    private static int warehouse_id = -1, parent_document_id = -1;

    private static int decimalPlace = 2;

    public static ImageLoader getImageLoaderInstance(Context context) {
        return getImageLoaderInstance(context, false);
    }

    public static ImageLoader getImageLoaderInstance(Context context, final boolean hasHeader) {
        if(imageRequestQueue == null)
            imageRequestQueue = ImageVolley.newRequestQueue(context);
        if(imageLoader == null)
            imageLoader = new ImageLoader(imageRequestQueue,
                    DiskImageLruCache.getInstance(context, imageLoaderListener)){
                @Override
                protected Request<Bitmap> makeImageRequest(String requestUrl, int maxWidth, int maxHeight, ImageView.ScaleType scaleType, final String cacheKey) {
                    return new ImageRequest(requestUrl, new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            if(imageLoaderListener != null)
                                imageLoaderListener.imageLoaded(response);
                            onGetImageSuccess(cacheKey, response);
                        }
                    }, maxWidth, maxHeight, scaleType, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            onGetImageError(cacheKey, error);
                        }
                    }){
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            if(hasHeader) {
                                HashMap params = new HashMap();
                                String auth = "Basic "+getSession().getApiAuthentication();
                                params.put("Authorization", auth);
                                return params;
                            }
                            return super.getHeaders();
                        }
                    };
                }
            };
        return imageLoader;
    }

    public static SelectedProductItemList getSelectedProductItems() {
        if(selectedProductItems == null)
            selectedProductItems = new SelectedProductItemList();
        return selectedProductItems;
    }

    public static SelectedProductItemList getSelectedReturnProductItems() {
        if(selectedReturnProductItems == null)
            selectedReturnProductItems = new SelectedProductItemList();
        if(!selectedReturnProductItems.isReturns())
            selectedReturnProductItems.setReturns(true);
        return selectedReturnProductItems;
    }

    public static void setDbHelper(ImonggoDBHelper2 dbHelper) {
        ProductsAdapterHelper.dbHelper = dbHelper;

        /** set the decimal place! **/
        List<Settings> settingsList = Settings.fetchWithConditionInt(ProductsAdapterHelper.dbHelper,
                Settings.class, new DBTable.ConditionsWindow<Settings,Integer>() {
                    @Override
                    public Where<Settings, Integer> renderConditions(Where<Settings, Integer> where) throws SQLException {
                        return where.eq("name", Configurations.SETTINGS_NAME.get(SettingsName.FORMAT_NO_OF_DECIMALS));
                    }
                });
        if(settingsList.size() > 0)
            decimalPlace = Integer.parseInt(settingsList.get(0).getValue());
    }

    public static ImonggoDBHelper2 getDbHelper() {
        return dbHelper;
    }

    public static void setImageLoaderListener(ImageLoaderListener imageLoaderListener) {
        ProductsAdapterHelper.imageLoaderListener = imageLoaderListener;
    }

    public static Session getSession() {
        try {
            if(AccountTools.isLoggedIn(dbHelper))
                session = dbHelper.fetchObjectsList(Session.class).get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return session;
    }

    public static boolean hasSelectedProductItems() {
        if(selectedProductItems == null)
            return false;
        return !selectedProductItems.isEmpty();
    }

    public static boolean hasSelectedReturnProductItems() {
        if(selectedReturnProductItems == null)
            return false;
        return !selectedReturnProductItems.isEmpty();
    }

    public static void clearSelectedProductItemList(boolean includeCustomer) {
        clearSelectedProductItemList(includeCustomer, true);
    }
    public static void clearSelectedProductItemList(boolean includeCustomer, boolean includeReason) {
        if(isDuplicating)
            return;
        if(selectedProductItems != null)
            selectedProductItems.clear();
        if(includeCustomer) {
            selectedCustomer = null;
            selectedCustomerGroup = null;
        }
        selectedBranch = null;
        if(includeReason)
            reason = null;
        warehouse_id = -1;
        parent_document_id = -1;
        ProductListTools.restartLineNo();
        Log.e("ProductAdapterHelper", "clearSelectedProductItemList");
    }

    public static void clearSelectedReturnProductItemList() {
        if(isDuplicating)
            return;
        if(selectedReturnProductItems != null)
            selectedReturnProductItems.clear();
    }

    public static void destroySelectedProductItemList() {
        if(selectedProductItems != null)
            selectedProductItems.clear();
        selectedProductItems = null;
        selectedCustomer = null;
        selectedCustomerGroup = null;
        selectedBranch = null;
        reason = null;
        warehouse_id = -1;
        parent_document_id = -1;
        Log.e("ProductAdapterHelper", "destroySelectedProductItemList");
    }

    public static void destroySelectedReturnProductItemList() {
        if(selectedReturnProductItems != null)
            selectedReturnProductItems.clear();
        selectedReturnProductItems = null;
    }

    public static void destroyProductAdapterHelper() {
        dbHelper = null;
        session = null;
        imageLoader = null;
        imageRequestQueue = null;
        selectedProductItems = null;
        selectedReturnProductItems = null;
        selectedCustomer = null;
        selectedCustomerGroup = null;
        selectedBranch = null;
        reason = null;
        warehouse_id = -1;
        parent_document_id = -1;
        Log.e("ProductAdapterHelper", "destroyProductAdapterHelper");
    }

    public static void setSelectedCustomer(Customer selectedCustomer) {
        ProductsAdapterHelper.selectedCustomer = selectedCustomer;
    }

    public static Customer getSelectedCustomer() {
        return selectedCustomer;
    }

    public static CustomerGroup getSelectedCustomerGroup() {
        return selectedCustomerGroup;
    }

    public static void setSelectedCustomerGroup(CustomerGroup selectedCustomerGroup) {
        ProductsAdapterHelper.selectedCustomerGroup = selectedCustomerGroup;
    }

    public static Branch getSelectedBranch() {
        return selectedBranch;
    }

    public static void setSelectedBranch(Branch selectedBranch) {
        ProductsAdapterHelper.selectedBranch = selectedBranch;
    }

    public static DocumentPurpose getReason() {
        return reason;
    }

    public static void setReason(DocumentPurpose reason) {
        ProductsAdapterHelper.reason = reason;
    }

    public static int getDecimalPlace() {
        return decimalPlace;
    }

    public static Branch getDestination() {
        return destination;
    }

    public static void setDestination(Branch destination) {
        ProductsAdapterHelper.destination = destination;
    }

    public static Branch getSource() {
        return source;
    }

    public static void setSource(Branch source) {
        ProductsAdapterHelper.source = source;
    }

    public static int getWarehouse_id() {
        return warehouse_id;
    }

    public static void setWarehouse_id(int warehouse_id) {
        ProductsAdapterHelper.warehouse_id = warehouse_id;
    }

    public static int getParent_document_id() {
        return parent_document_id;
    }

    public static void setParent_document_id(int parent_document_id) {
        ProductsAdapterHelper.parent_document_id = parent_document_id;
    }
}
