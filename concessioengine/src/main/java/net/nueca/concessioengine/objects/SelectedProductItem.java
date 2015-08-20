package net.nueca.concessioengine.objects;

import android.util.Log;
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

    /**
     *
     * @param value
     * @return position
     */
    public int addValues(Values value) {
        int index = values.indexOf(value);
        if(index > -1) {
            setValues(index, value);
            return index;
        }
        if(isMultiline) {
            value.setLine_no(ProductListTools.getLineNo());
            this.values.add(value);
            this.total_quantity = values.getQuantity();
            return -1;
        }
        else {
            if(values.size() == 1) {
                setValues(0, value.getQuantity(), value.getUnit());
                return -1;
            }
        }
        value.setLine_no(ProductListTools.getLineNo());
        this.values.add(value);
        this.total_quantity = values.getQuantity();
        return -1;
    }

    public void setValues(int position, Values values) {
        this.values.get(position).setValue(values.getQuantity(), values.getUnit(), values.getExtendedAttributes());
        this.total_quantity = values.getQuantity();
    }

    public void setValues(int position, String quantity, Unit unit) {
        this.values.get(position).setValue(quantity, unit);
        this.total_quantity = values.getQuantity();
    }

    public void setValues(ValuesList values) {
        this.values = values;
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

    public String getQuantity(int valuePosition) {
        if(values.get(valuePosition) == null)
            throw new NullPointerException("values list has no index of "+valuePosition+".");
        return values.get(valuePosition).getQuantity();
    }

    public void remove(int valuePosition) {
        if(valuePosition > -1)
            if(values.get(valuePosition) != null)
                values.remove(valuePosition);
    }

    public void removeAll() {
        values.clear();
    }

    @Override
    public boolean equals(Object o) {
        return product.getId() == ((SelectedProductItem) o).getProduct().getId();
    }
}
