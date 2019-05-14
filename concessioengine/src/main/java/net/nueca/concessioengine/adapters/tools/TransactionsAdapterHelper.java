package net.nueca.concessioengine.adapters.tools;

import android.util.Log;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.DocumentTypeCode;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.objects.order.OrderLine;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 8/5/15.
 * imonggosdk2 (c)2015
 */
public class TransactionsAdapterHelper {

    // Detailed offline data transaction type
    public static String getTransactionType(ImonggoDBHelper2 dbHelper, OfflineData offlineData) {
        if(offlineData.getType() == OfflineData.ORDER)
            return "Order";
        else if(offlineData.getType() == OfflineData.INVOICE)
            return "Sales";
        else if(offlineData.getType() == OfflineData.CUSTOMER)
            return "Customer";
//        ConcessioModule concessioModule = offlineData.getObjectFromData(Document.class)
//                .getDocument_type_code()
//                .getConcessioModule();
        try {
            ModuleSetting moduleSetting = dbHelper.fetchObjects(ModuleSetting.class).queryBuilder().where().eq("module_type", offlineData.getConcessioModule()).queryForFirst();
            if(moduleSetting != null)
                return moduleSetting.getLabel();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Document";
    }

    public static String getTransactionType(int type) {
        if(type == OfflineData.ORDER)
            return "Order";
        else if(type == OfflineData.INVOICE)
            return "Sales";
        return "Document";
    }

    public static int getStatus(OfflineData offlineData, boolean isDefault) {
        if(offlineData.isSynced()) {
            if(isDefault) {
                if (offlineData.isCancelled())
                    return R.drawable.ic_cancel_black;
                return R.drawable.ic_check_black;
            }
            if(offlineData.isCancelled())
                return R.drawable.ic_alert_red;
            return R.drawable.ic_check_round_teal;
        }
        if(offlineData.isSyncing() || offlineData.isQueued())
            return R.drawable.ic_sync_black;
//        if(offlineData.isQueued())
//            return R.drawable.ic_sync_black;
        if(offlineData.getOfflineDataTransactionType().isVoiding())
            return R.drawable.ic_readytosync_black_void;
        return R.drawable.ic_readytosync_black;
    }

    public static List<Product> generateTransactionItems(OfflineData offlineData, ImonggoDBHelper2 dbHelper) {
        List<Product> transactionLines = new ArrayList<>();

        try {
            if(offlineData.getType() == OfflineData.ORDER) {
                Order order = Order.fromJSONObject(offlineData.getData());
                for(OrderLine orderLine : order.getOrder_lines()) {
                    Product product = dbHelper.fetchObjects(Product.class).queryBuilder().where().eq("id", orderLine.getProduct_id()).and().isNull("status").queryForFirst();
                    if(product != null)
                        transactionLines.add(product);
                    else
                        continue;

                    Values values = new Values();
                    values.setQuantity(String.valueOf(orderLine.getQuantity()));
                    values.setLine_no(orderLine.getLine_no());

                    Unit unit = new Unit();
                    unit.setId(-1);
                    unit.setName(product.getBase_unit_name());
                    if(orderLine.getUnit_id() != null) {
                        unit = new Unit();
                        unit.setName(orderLine.getUnit_name());
                        unit.setId(orderLine.getUnit_id());

                        values.setUnit_content_quantity(orderLine.getUnit_content_quantity());
                        values.setUnit_name(orderLine.getUnit_name());
                        values.setUnit_quantity(String.valueOf(orderLine.getUnit_quantity()));
                        values.setUnit_retail_price(orderLine.getUnit_retail_price());
                    }

                    values.setUnit(unit);

                    int indexOfSelectedItem = ProductsAdapterHelper.getSelectedProductItems().indexOf(product);
                    SelectedProductItem selectedProductItem = ProductsAdapterHelper.getSelectedProductItems().getSelectedProductItem(product);
                    if(selectedProductItem == null) {
                        selectedProductItem = new SelectedProductItem();
                        selectedProductItem.setProduct(product);
                    }
                    selectedProductItem.addValues(values);

                    if(indexOfSelectedItem > -1)
                        ProductsAdapterHelper.getSelectedProductItems().get(indexOfSelectedItem).addValues(values);
                    else
                        ProductsAdapterHelper.getSelectedProductItems().add(selectedProductItem);

                }
            }
            else if(offlineData.getType() == OfflineData.INVOICE) {

            }
            else if(offlineData.getType() == OfflineData.DOCUMENT) {

            }
        } catch (JSONException | SQLException e) {
            e.printStackTrace();
        }

        return transactionLines;
    }
}
