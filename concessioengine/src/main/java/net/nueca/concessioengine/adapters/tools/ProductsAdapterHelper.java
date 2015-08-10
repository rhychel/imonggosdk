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
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.disklrucache.DiskLruCache;

import net.nueca.concessioengine.lists.SelectedProductItemList;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.ProductListTools;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public class ProductsAdapterHelper {

    private static RequestQueue imageRequestQueue;
    private static ImageLoader imageLoader;
    private static ImonggoDBHelper dbHelper;
    private static Session session;
    private static SelectedProductItemList selectedProductItems = null;

    public static ImageLoader getImageLoaderInstance(Context context) {
        return getImageLoaderInstance(context, false);
    }

    public static ImageLoader getImageLoaderInstance(Context context, final boolean hasHeader) {
        if(imageRequestQueue == null)
            imageRequestQueue = ImageVolley.newRequestQueue(context);
        if(imageLoader == null)
            imageLoader = new ImageLoader(imageRequestQueue,
                    DiskImageLruCache.getInstance(context)){
                @Override
                protected Request<Bitmap> makeImageRequest(String requestUrl, int maxWidth, int maxHeight, ImageView.ScaleType scaleType, final String cacheKey) {
                    return new ImageRequest(requestUrl, new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
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

    public static void setDbHelper(ImonggoDBHelper dbHelper) {
        ProductsAdapterHelper.dbHelper = dbHelper;
    }

    public ImonggoDBHelper getDbHelper() {
        return dbHelper;
    }

    public static Session getSession() {
        try {
            if(AccountTools.isLoggedIn(dbHelper))
                session = dbHelper.getSessions().queryForAll().get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return session;
    }

    public static void clearSelectedProductItemList() {
        selectedProductItems.clear();
        ProductListTools.restartLineNo();
    }

    public static void destroySelectedProductItemList() {
        selectedProductItems.clear();
        selectedProductItems = null;
    }

    public static void destroyProductAdapterHelper() {
        dbHelper = null;
        session = null;
        imageLoader = null;
        imageRequestQueue = null;
        selectedProductItems = null;
    }
}
