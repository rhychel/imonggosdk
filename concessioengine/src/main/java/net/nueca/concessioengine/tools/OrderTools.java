package net.nueca.concessioengine.tools;

import android.content.Context;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.OrderTypeCode;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.objects.order.OrderLine;
import net.nueca.imonggosdk.tools.ProductListTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymartmanchus on 17/05/2016.
 */
public class OrderTools {

    public static Order generateOrder(Context context, int deviceId, int servingBranchId, OrderTypeCode orderType) {
        Order.Builder order = new Order.Builder();
        for(SelectedProductItem selectedProductItem : ProductsAdapterHelper.getSelectedProductItems()) {
            for(Values value : selectedProductItem.getValues()) {
                OrderLine orderLine = new OrderLine.Builder()
                        .line_no(value.getLine_no())
                        .product_id(selectedProductItem.getProduct().getId())
                        .quantity(Double.valueOf(value.getActualQuantity()))
                        .retail_price(value.getRetail_price())
                        .build();
                if(value.isValidUnit()) {
                    orderLine.setUnit_id(value.getUnit().getId());
                    orderLine.setUnit_name(value.getUnit_name());
                    orderLine.setUnit_content_quantity(value.getUnit_content_quantity());
                    orderLine.setUnit_quantity(Double.valueOf(value.getUnit_quantity()));
                    orderLine.setUnit_retail_price(value.getUnit_retail_price());
                }
                else {
                    orderLine.setUnit_retail_price(value.getUnit_retail_price());
                    orderLine.setRetail_price(value.getRetail_price());
                }
                order.addOrderLine(orderLine);
            }
        }
        order.order_type_code(orderType.toString());
        order.serving_branch_id(servingBranchId);
        order.generateReference(context, deviceId);
        return order.build();
    }

    public static List<Product> generateSelectedItemList(ImonggoDBHelper2 dbHelper, Order order) throws SQLException {
        return generateSelectedItemList(dbHelper, order, false);
    }

    public static List<Product> generateSelectedItemList(ImonggoDBHelper2 dbHelper, Order order, boolean isMultiLine) throws SQLException {
        List<Product> productList = new ArrayList<>();

        List<OrderLine> orderLines = order.getOrder_lines();
        for(OrderLine orderLine : orderLines) {
            Product product = dbHelper.fetchIntId(Product.class).queryForId(orderLine.getProduct_id());
            if(productList.indexOf(product) == -1)
                productList.add(product);

            SelectedProductItem selectedProductItem = ProductsAdapterHelper.getSelectedProductItems().initializeItem(product);
            selectedProductItem.setIsMultiline(isMultiLine);
            String quantity = "0";
            Unit unit = null;
            if(orderLine.getUnit_id() != null)
                unit = dbHelper.fetchIntId(Unit.class).queryForId(orderLine.getUnit_id());
            if(unit != null) {
                quantity = orderLine.getUnit_quantity().toString();
                unit.setRetail_price(orderLine.getUnit_retail_price());
            }
            else {
                unit = new Unit();
                unit.setId(-1);
                unit.setName(product.getBase_unit_name());
                unit.setRetail_price(orderLine.getUnit_retail_price());
                quantity = String.valueOf(orderLine.getQuantity());
            }
            Values values = new Values(unit, quantity);
            values.setLine_no(orderLine.getLine_no());
            selectedProductItem.addValues(values);
            ProductsAdapterHelper.getSelectedProductItems().add(selectedProductItem);

            ProductListTools.getLineNo();
        }

        return productList;
    }

}
