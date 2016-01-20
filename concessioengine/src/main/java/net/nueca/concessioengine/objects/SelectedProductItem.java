package net.nueca.concessioengine.objects;

import android.util.Log;

import net.nueca.concessioengine.lists.ValuesList;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.tools.NumberTools;
import net.nueca.imonggosdk.tools.ProductListTools;

import java.math.BigDecimal;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public class SelectedProductItem {
    private Inventory inventory;
    private Product product;
    private String total_quantity = "0";
    private String total_return = "0";
    private String total_discrepancy = "0";
    private Double retail_price;

    private String TAG = "SelectedProductItem";
    private ValuesList valuesList = new ValuesList();
    private boolean isMultiline = false;

    public SelectedProductItem() {
    }

    public SelectedProductItem(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public ValuesList getValues() {
        return valuesList;
    }

    /**
     *
     * @param value
     * @return position
     */
    public int addValues(Values value) {
        int index = valuesList.indexOf(value);
        if(value.getRetail_price() == null)
            value.setRetail_price(getRetail_price());
        //Log.e(TAG, "index: " + index + " valuesList: " + value.toString());
        if(index > -1) {
            setValues(index, value);
            return index;
        }
        /*if(isMultiline) {
            value.setLine_no(ProductListTools.getLineNo());
            this.valuesList.add(value);
            setValues();
            return -1;
        }*/
        if(!isMultiline) {
            if(valuesList.size() == 1) {
                //Log.e(TAG, "Index is 1 setting the value");
                if(value.getExtendedAttributes() == null) {
                    if(value.getRetail_price() != null) {
                        setValues(0, value.getQuantity(), value.getUnit(), value.getRetail_price());
                    } else
                        setValues(0, value.getQuantity(), value.getUnit());
                }
                else {
                    if(value.getRetail_price() != null)
                        setValues(0, value.getQuantity(), value.getUnit(), value.getExtendedAttributes(), value.getRetail_price());
                    else
                        setValues(0, value.getQuantity(), value.getUnit(), value.getExtendedAttributes());
                }
                return -1;
            }
        }
        if(value.getLine_no() <= -1)
            value.setLine_no(ProductListTools.getLineNo());
        this.valuesList.add(value);
        setValues();

        //Log.e(TAG, "Index is -1 setting the value");
        return -1;
    }

    public Double getRetailPriceWithTax() {

        int quantity = Integer.parseInt(total_quantity);
        /**double retail_price = product.getRetail_price();**/
        //ProductTaxRateAssoc pTRate =

        if(quantity == 0) { // if quantity is zero | return 0
            return 0.0;
        } else {
            if(product.isTax_exempt()) { // if tax excempted return retail price
                Log.e(TAG, "Product is tax exempted. returning default retail price");
                return getRetail_price();
            }
            if(isMultiline) {
                return getRetail_price();
            } else {
                return getRetail_price() * quantity;
            }
        }
    }

    public void setValues(int position, Values values) {
        //Log.e("SELECTED_PRODUCT_ITEM", "setValues(int position, Values values)");
        if(values.getRetail_price() != null) {
            if(values.getPrice() != null)
                this.valuesList.get(position).setValue(values.getQuantity(),values.getPrice(),values.getExtendedAttributes());
            else {
                this.valuesList.get(position).setValue(values.getQuantity(), values.getUnit(), values.getRetail_price());
                this.valuesList.get(position).setExtendedAttributes(values.getExtendedAttributes());
            }
        } else {
            this.valuesList.get(position).setValue(values.getQuantity(), values.getUnit(), values.getExtendedAttributes());
        }
        setValues();
    }

    public void setValues(int position, String quantity, Unit unit) {
        //Log.e("SELECTED_PRODUCT_ITEM", "setValues(int position, String quantity, Unit unit)");
        this.valuesList.get(position).setValue(quantity, unit);
        setValues();
    }
    public void setValues(int position, String quantity, Unit unit, ExtendedAttributes extendedAttributes) {
        //Log.e("SELECTED_PRODUCT_ITEM", "setValues(int position, String quantity, Unit unit, ExtendedAttributes extendedAttributes)");
        this.valuesList.get(position).setValue(quantity, unit, extendedAttributes);
        setValues();
    }
    public void setValues(int position, String quantity, Unit unit, double retail_price) {
        Log.e("SELECTED_PRODUCT_ITEM", "setValues(position="+position+", quantity="+quantity+", unit="+(unit!=null?unit.getName():"null")+", " +
                "retail_price="+retail_price+")");
        this.valuesList.get(position).setValue(quantity, unit, retail_price);
        setValues();
    }

    public void setValues(int position, String quantity, Unit unit, ExtendedAttributes extendedAttributes, double retail_price) {
        //Log.e("SELECTED_PRODUCT_ITEM", "setValues(int position, String quantity, Unit unit, ExtendedAttributes extendedAttributes, double " +
        //        "retail_price)");
        this.valuesList.get(position).setValue(quantity, unit, retail_price);
        this.valuesList.get(position).setExtendedAttributes(extendedAttributes);
        setValues();
    }

    private void setValues() {
        this.total_quantity = valuesList.getQuantity();
        this.total_discrepancy = valuesList.getDiscrepancy();
        this.total_return = valuesList.getOutrightReturn();
    }

    public void setValues(ValuesList values) {
        this.valuesList = values;
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

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public String updatedInventory(boolean shouldAdd) {
        if(inventory == null)
            return valuesList.getActualQuantity();
        BigDecimal currentInventory = new BigDecimal(inventory.getQuantity());
        BigDecimal totalQuantity = new BigDecimal(valuesList.getActualQuantity());
        if(shouldAdd)
            return currentInventory.add(totalQuantity).toPlainString();
        return currentInventory.subtract(totalQuantity).toPlainString();
    }

    public String getQuantity(int valuePosition) {
        if(valuesList.get(valuePosition) == null)
            throw new NullPointerException("values list has no index of "+valuePosition+".");
        return valuesList.get(valuePosition).getQuantity();
    }

    public String getReturn() {
        return total_return;
    }
    public String getReturn(int valuePosition) {
        Values values = valuesList.get(valuePosition);
        if(values == null)
            throw new NullPointerException("values list has no index of "+valuePosition+".");
        if(values.getExtendedAttributes() == null || values.getExtendedAttributes().getOutright_return() == null)
            return "0";

        return valuesList.get(valuePosition).getExtendedAttributes().getOutright_return();
    }

    public String getDiscrepancy() {
        return total_discrepancy;
    }
    public String getDiscrepancy(int valuePosition) {
        Values values = valuesList.get(valuePosition);
        if(values == null)
            throw new NullPointerException("values list has no index of "+valuePosition+".");
        if(values.getExtendedAttributes() == null || values.getExtendedAttributes().getDiscrepancy() == null)
            return "0";

        return valuesList.get(valuePosition).getExtendedAttributes().getDiscrepancy();
    }

    public String getOriginalQuantity() {
        Double originalQty = NumberTools.toDouble(total_discrepancy) + NumberTools.toDouble(total_quantity) +
                NumberTools.toDouble(total_return);
        return NumberTools.separateInCommas(originalQty);
    }

    public String getOriginalQuantity(int valuePosition) {
        Values values = valuesList.get(valuePosition);
        if(values == null)
            throw new NullPointerException("values list has no index of "+valuePosition+".");

        Double originalQty = NumberTools.toDouble(getDiscrepancy(valuePosition)) +
                NumberTools.toDouble(getQuantity(valuePosition)) +
                NumberTools.toDouble(getReturn(valuePosition));

        return NumberTools.separateInCommas(originalQty);
    }

    public void remove(int valuePosition) {
        if(valuePosition > -1) {
            if (valuesList.get(valuePosition) != null)
                valuesList.remove(valuePosition);

            setValues();
        }
    }

    public void removeAll() {
        valuesList.clear();
        setValues();
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
                ", valuesList=" + valuesList +
                ", isMultiline=" + isMultiline +
                '}';
    }

    public Double getRetail_price() {
        if(retail_price == null)
            retail_price = product.getRetail_price();
        return retail_price;
    }

    public void setRetail_price(Double retail_price) {
        this.retail_price = retail_price;
    }

    public String getValuesRetailPrices(char delimiter) {
        if(valuesList == null || valuesList.size() == 0)
            return "";
        String retailPrices = "";
        for(Values values : valuesList) {
            retailPrices += values.getRetail_price();
            if(valuesList.indexOf(values) < valuesList.size()-1)
                retailPrices += delimiter;
        }
        return retailPrices;
    }

    public Double getValuesSubtotal() {
        Double subtotal = 0d;
        for (Values values : valuesList) {
            subtotal += values.getSubtotal();
        }
        return subtotal;
    }

    @Deprecated
    public int addReceiveValues(Values value) {
        int index = valuesList.indexOf(value);
        //Log.e(TAG, "index: " + index + " valuesList: " + value.toString());
        if(index > -1) {
            setValues(index, value);
            return index;
        }
        if(!isMultiline) {
            if(valuesList.size() == 1) {
                //Log.e(TAG, "Index is 1 setting the value");
                if(value.getExtendedAttributes() == null)
                    setValues(0, value.getQuantity(), value.getUnit());
                else
                    setValues(0, value.getQuantity(), value.getUnit(),value.getExtendedAttributes());
                return -1;
            }
        }
        this.valuesList.add(value);
        setValues();

        //Log.e(TAG, "Index is -1 setting the value");
        return -1;
    }
}