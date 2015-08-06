package net.nueca.concessioengine.adapters.tools;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
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

    public static String getTransactionType(int type) {
        if(type == OfflineData.ORDER)
            return "Order";
        else if(type == OfflineData.INVOICE)
            return "Sales";
        return "Document";
    }

    public static int getStatus(OfflineData offlineData) {
        if(offlineData.isSynced()) {
            if(offlineData.isCancelled())
                return R.drawable.ic_cancel_black;
            return R.drawable.ic_check_black;
        }
        if(offlineData.isSyncing())
            return R.drawable.ic_sync_black;
        if(offlineData.isQueued())
            return R.drawable.ic_warning_black;
        return R.drawable.ic_readytosync_black;
    }

    public static List<Product> generateTransactionItems(OfflineData offlineData, ImonggoDBHelper dbHelper) {
        List<Product> transactionLines = new ArrayList<>();

        try {
            if(offlineData.getType() == OfflineData.ORDER) {
                Order order = Order.fromJSONString(offlineData.getData());
                for(OrderLine orderLine : order.getOrderLines()) {
                    Product product = dbHelper.getProducts().queryBuilder().where().eq("id", orderLine.getProduct_id()).and().isNull("status").queryForFirst();
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
                    if(orderLine.getUnitId() != null) {
                        unit = new Unit();
                        unit.setName(orderLine.getUnitName());
                        unit.setId(orderLine.getUnitId());

                        values.setUnit_content_quantity(orderLine.getUnitContentQuantity());
                        values.setUnit_name(orderLine.getUnitName());
                        values.setUnit_quantity(String.valueOf(orderLine.getUnitQuantity()));
                        values.setUnit_retail_price(orderLine.getUnitRetailPrice());
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
