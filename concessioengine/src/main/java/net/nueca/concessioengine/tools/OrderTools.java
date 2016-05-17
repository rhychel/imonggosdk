package net.nueca.concessioengine.tools;

import android.content.Context;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.enums.OrderTypeCode;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.objects.order.OrderLine;

/**
 * Created by rhymartmanchus on 17/05/2016.
 */
public class OrderTools {

    public Order generateOrder(Context context, int deviceId, int servingBranchId, OrderTypeCode orderType) {
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

}
