package net.nueca.concessioengine.objects;

import android.util.Log;

import net.nueca.concessioengine.lists.ValuesList;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.tools.NumberTools;
import net.nueca.imonggosdk.tools.ProductListTools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public class SelectedProductItem {
    private Inventory inventory;
    private Product product;
    private String total_quantity = "0";
    private String total_actual_quantity = "0";
    private String total_return = "0";
    private String total_discrepancy = "0";
    private Double retail_price;

    private String TAG = "SelectedProductItem";
    private ValuesList valuesList = new ValuesList();
    private boolean isMultiline = false;

    private boolean isReturns = false;

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
        Log.e(TAG, "Quantity="+value.getQuantity()+" |index: " + index + " valuesList: " + value.toString());
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
            Log.e("SelectedProductItem", "is not multi line");
            if(valuesList.size() == 1) {
                setValues(0, value, true);
                //Log.e(TAG, "Index is 1 setting the value");
                // DEPRECATED
//                if(value.getExtendedAttributes() == null) {
//                    if(value.getPrice() != null) {
//                        setValues(0, value.getQuantity(), value.getPrice());
//                    } else {
//                        if (value.getRetail_price() != null) {
//                            setValues(0, value.getQuantity(), value.getUnit(), value.getRetail_price());
//                        } else
//                            setValues(0, value.getQuantity(), value.getUnit());
//                    }
//                }
//                else {
//                    if(value.getPrice() != null) {
//                        setValues(0, value.getQuantity(), value.getPrice(), value.getExtendedAttributes());
//                    } else {
//                        if (value.getRetail_price() != null)
//                            setValues(0, value.getQuantity(), value.getUnit(), value.getExtendedAttributes(), value.getRetail_price());
//                        else
//                            setValues(0, value.getQuantity(), value.getUnit(), value.getExtendedAttributes());
//                    }
//                }
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
        setValues(position, values, false);
//        setValues();
    }

    public void setValues(int position, Values value, boolean isAdvanced) {
        String qty = fixQuantity(value.getQuantity());

        this.valuesList.get(position).setBadStock(value.isBadStock());
        this.valuesList.get(position).setInvoicePurpose(value.getInvoicePurpose());
        this.valuesList.get(position).setExpiry_date(value.getExpiry_date());
        if(isAdvanced) {
            if(value.getPrice() != null) {
                this.valuesList.get(position).setValue(qty, value.getPrice(), value.getExtendedAttributes());
            }
            else {
                if(value.getExtendedAttributes() == null) {
                    if(value.getRetail_price() != null)
                        this.valuesList.get(position).setValue(qty, value.getUnit(), value.getRetail_price());
                    else
                        this.valuesList.get(position).setValue(qty, value.getUnit());
                }
                else {
                    if(value.getRetail_price() != null) {
                        this.valuesList.get(position).setValue(qty, value.getUnit(), value.getRetail_price());
                        this.valuesList.get(position).setExtendedAttributes(value.getExtendedAttributes());
                    }
                    else
                        this.valuesList.get(position).setValue(qty, value.getUnit(), value.getExtendedAttributes());
                }
            }
        }
        else {
//            String qty = fixQuantity(values.getQuantity());
            if(value.getRetail_price() != null) {
                if(value.getPrice() != null)
                    this.valuesList.get(position).setValue(qty, value.getPrice(), value.getExtendedAttributes());
                else {
                    this.valuesList.get(position).setValue(qty, value.getUnit(), value.getRetail_price());
                    this.valuesList.get(position).setExtendedAttributes(value.getExtendedAttributes());
                }
            } else {
                this.valuesList.get(position).setValue(qty, value.getUnit(), value.getExtendedAttributes());
            }
        }
        setValues();
    }

    // UNIT
    @Deprecated
    public void setValues(int position, String quantity, Unit unit) {
        //Log.e("SELECTED_PRODUCT_ITEM", "setValues(int position, String quantity, Unit unit)");
        quantity = fixQuantity(quantity);
        this.valuesList.get(position).setValue(quantity, unit);
        setValues();
    }
    @Deprecated
    public void setValues(int position, String quantity, Unit unit, ExtendedAttributes extendedAttributes) {
        //Log.e("SELECTED_PRODUCT_ITEM", "setValues(int position, String quantity, Unit unit, ExtendedAttributes extendedAttributes)");
        quantity = fixQuantity(quantity);
        this.valuesList.get(position).setValue(quantity, unit, extendedAttributes);
        setValues();
    }
    @Deprecated
    public void setValues(int position, String quantity, Unit unit, double retail_price) {
        quantity = fixQuantity(quantity);
        Log.e("SELECTED_PRODUCT_ITEM", "setValues(position="+position+", quantity="+quantity+", unit="+(unit!=null?unit.getName():"null")+", " +
                "retail_price="+retail_price+")");
        this.valuesList.get(position).setValue(quantity, unit, retail_price);
        setValues();
    }
    @Deprecated
    public void setValues(int position, String quantity, Unit unit, ExtendedAttributes extendedAttributes, double retail_price) {
        //Log.e("SELECTED_PRODUCT_ITEM", "setValues(int position, String quantity, Unit unit, ExtendedAttributes extendedAttributes, double " +
        //        "retail_price)");
        quantity = fixQuantity(quantity);
        this.valuesList.get(position).setValue(quantity, unit, retail_price);
        this.valuesList.get(position).setExtendedAttributes(extendedAttributes);
        setValues();
    }

    // PRICE
    @Deprecated
    public void setValues(int position, String quantity, Price price) {
        setValues(position, quantity, price, null);
    }
    @Deprecated
    public void setValues(int position, String quantity, Price price, ExtendedAttributes extendedAttributes) {
        quantity = fixQuantity(quantity);
        this.valuesList.get(position).setValue(quantity, price, extendedAttributes);
        setValues();
    }


    private void setValues() {
        this.total_actual_quantity = valuesList.getActualQuantity();
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

    public boolean isReturns() {
        return isReturns;
    }

    public String getActualQuantity() {
        if(!product.isAllow_decimal_quantities())
            return Double.valueOf(total_actual_quantity).intValue()+"";
        return total_actual_quantity;
    }

    public void setReturns(boolean returns) {
        if(isReturns != returns) {
            isReturns = returns;
            for (Values values : valuesList) {
                setValues(valuesList.indexOf(values), values);
            }
        }
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
        Log.e("updatedInventory", "shouldAdd="+shouldAdd);
        double inventoryQty = 0;
        if(inventory != null)
            inventoryQty = inventory.getQuantity();
        BigDecimal currentInventory = new BigDecimal(inventoryQty);
        BigDecimal totalQuantity = new BigDecimal(valuesList.getActualQuantity());
        Log.e("currentInventory", currentInventory.toString());
        Log.e("totalQuantity", totalQuantity.toString());
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
        Log.e("getOriginalQuantity", "total_discrepancy="+total_discrepancy+" | total_quantity="+total_quantity+" | total_return="+total_return);
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

    public Double getRetail_price(int valuePosition) {
        if(valuesList == null || valuesList.size() == 0 || valuesList.size() <= valuePosition || valuePosition < 0)
            return getRetail_price();
        Unit unit = valuesList.get(valuePosition).getUnit();
        if(unit != null && unit.getId() != -1)
            return unit.getRetail_price();
        else
            return product.getRetail_price();
    }


    public void setRetail_price(Double retail_price) {
        this.retail_price = retail_price;
    }

    public String getValuesRetailPrices() {
        // char delimiter
        if(valuesList == null || valuesList.size() == 0)
            return "";
        List<String> unitNames = new ArrayList<>();
        String retailPrices = "";
        boolean hasOne = false;
        for(Values values : valuesList) {
            if(unitNames.contains(values.getUnit_name()))
                continue;
            if(hasOne)
                retailPrices += "\n";
            retailPrices += "P"+values.getRetail_price()+"/"+values.getUnit_name();
            unitNames.add(values.getUnit_name());
            hasOne = true;
        }
        return retailPrices;
    }

    public Double getValuesRetailPrice(int valuePosition) {
        if(valuesList == null || valuesList.size() == 0 || valuesList.size() <= valuePosition)
            return null;
        return valuesList.get(valuePosition).getRetail_price();
    }

    public Double getValuesSubtotal() {
        Double subtotal = 0d;
        for (Values values : valuesList) {
            subtotal += values.getSubtotal();
        }
        return subtotal;
    }

    public String getValuesUnit() {
        for(Values values : valuesList) {
            if(values.getUnit_name() == null)
                return product.getBase_unit_name();
            return values.getUnit_name();
        }
        return product.getBase_unit_name();
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

    private String fixQuantity(String quantity) {
        Double qty = Math.abs(Double.parseDouble(quantity));
        if(isReturns)
            qty *= -1;
        return String.valueOf(qty);
    }
}