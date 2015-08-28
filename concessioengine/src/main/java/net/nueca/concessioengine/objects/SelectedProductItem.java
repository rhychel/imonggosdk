package net.nueca.concessioengine.objects;

import android.util.Log;

import net.nueca.concessioengine.lists.ValuesList;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.tools.ProductListTools;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public class SelectedProductItem {
    private Product product;
    private String total_quantity = "0";
    private String TAG = "SelectedProductItem";
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
        Log.e(TAG, "index: " + index + " values: " + value.toString());
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
                Log.e(TAG, "Index is 1 setting the value");
                setValues(0, value.getQuantity(), value.getUnit());
                return -1;
            }
        }
        value.setLine_no(ProductListTools.getLineNo());

        this.values.add(value);
        this.total_quantity = values.getQuantity();

        Log.e(TAG, "Index is -1 setting the value");
        return -1;
    }

    public Double getRetailPriceWithTax() {

        int quantity = Integer.parseInt(total_quantity);
        double retail_price = product.getRetail_price();
        //ProductTaxRateAssoc pTRate =

        if(quantity == 0) { // if quantity is zero | return 0
            return 0.0;
        } else {
            if(product.isTax_exempt()) { // if tax excempted return retail price
                Log.e(TAG, "Product is tax exempted. returning default retail price");
                return retail_price;
            }
            if(isMultiline) {
                return retail_price;
            } else {
                return retail_price * quantity;
            }
        }
    }

    public void setValues(int position, Values values) {
        this.values.get(position).setValue(values.getQuantity(), values.getUnit(), values.getExtendedAttributes());
        setValues();
    }

    public void setValues(int position, String quantity, Unit unit) {
        this.values.get(position).setValue(quantity, unit);
        setValues();
    }

    private void setValues() {
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

    @Override
    public String toString() {
        return "SelectedProductItem{" +
                "product=" + product.getName() +
                ", total_quantity='" + total_quantity + '\'' +
                ", values=" + values +
                ", isMultiline=" + isMultiline +
                '}';
    }
}