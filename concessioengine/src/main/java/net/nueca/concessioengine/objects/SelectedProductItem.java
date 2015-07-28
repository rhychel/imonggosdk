package net.nueca.concessioengine.objects;

import net.nueca.concessioengine.lists.ValuesList;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.tools.ProductListTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public class SelectedProductItem {

    private Product product;
    private String total_quantity = "0";
    private ValuesList values = new ValuesList();
    private boolean isMultiline = false;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public ValuesList getValues() {
        return values;
    }

    public void setValues(ValuesList values) {
        this.values = values;
    }

    public void addValues(Values value) {
        value.setLine_no(ProductListTools.getLineNo());
        this.values.add(value);
        this.total_quantity = values.getQuantity();
    }

    public void setValues(int position, String quantity, Unit unit) {
        this.values.get(position).setValue(quantity, unit);
        this.total_quantity = values.getQuantity();
    }

    public boolean isMultiline() {
        return isMultiline;
    }

    public void setIsMultiline(boolean isMultiline) {
        this.isMultiline = isMultiline;
    }

    public String getQuantity() {
        return total_quantity;
    }

    @Override
    public boolean equals(Object o) {
        return product.getId() == ((Integer)o);
    }
}