package net.nueca.concessioengine.objects;

import net.nueca.imonggosdk.objects.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 31/05/2016.
 */
public class ReceivedProductItemLine {
    private Double expected_quantity;
    private Double expected_price;

    private Double total_actual_quantity = 0d;
    private Double total_discrepancy = 0d;
    private Double total_outright_return = 0d;
    private Double total_subtotal = 0d;

    private Unit unit;

    private List<ReceivedItemValue> itemValueList = new ArrayList<>();

    public ReceivedProductItemLine(Unit unit, Double expected_quantity, Double expected_price) {
        this.unit = unit;
        this.expected_quantity = expected_quantity;
        this.expected_price = expected_price;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Double getExpected_quantity() {
        return expected_quantity;
    }

    public void setExpected_quantity(Double expected_quantity) {
        this.expected_quantity = expected_quantity;
    }

    public Double getExpected_price() {
        return expected_price;
    }

    public void setExpected_price(Double expected_price) {
        this.expected_price = expected_price;
    }

    public Double getTotal_actual_quantity() {
        return total_actual_quantity;
    }

    public Double getTotal_discrepancy() {
        return total_discrepancy;
    }

    public Double getTotal_outright_return() {
        return total_outright_return;
    }

    public Double getTotal_subtotal() {
        return total_subtotal;
    }

    public int size() {
        return itemValueList.size();
    }

    public Double getMinActualPrice() {
        Double min = null;
        for(ReceivedItemValue itemValue : itemValueList) {
            if(min == null)
                min = itemValue.getPrice();
            else if(min > itemValue.getPrice())
                min = itemValue.getPrice();
        }
        if(min == null)
            min = 0d;
        return min;
    }
    public Double getMaxActualPrice() {
        Double max = null;
        for(ReceivedItemValue itemValue : itemValueList) {
            if(max == null)
                max = itemValue.getPrice();
            else if(max < itemValue.getPrice())
                max = itemValue.getPrice();
        }
        if(max == null)
            max = 0d;
        return max;
    }

    public List<ReceivedItemValue> getItemValueList() {
        return itemValueList;
    }

    public ReceivedItemValue getItemValueAt(int position) {
        return itemValueList.get(position);
    }

    public ReceivedItemValue setItemValueAt(int position, ReceivedItemValue itemValue) {
        ReceivedItemValue forDelete = itemValueList.set(position, itemValue);
        if(forDelete != null) {
            this.total_actual_quantity -= forDelete.getQuantity();
            this.total_outright_return -= forDelete.getOutright_return();
            this.total_subtotal -= forDelete.getSubtotal();
            this.total_discrepancy = this.expected_quantity - this.total_actual_quantity;

            this.total_actual_quantity += itemValue.getQuantity();
            this.total_outright_return += itemValue.getOutright_return();
            this.total_subtotal += itemValue.getSubtotal();
            this.total_discrepancy = this.expected_quantity - this.total_actual_quantity;
        }
        return forDelete;
    }

    public boolean addItemValue(ReceivedItemValue itemValue) {
        if(itemValueList.add(itemValue)) {
            this.total_actual_quantity += itemValue.getQuantity();
            this.total_outright_return += itemValue.getOutright_return();
            this.total_subtotal += itemValue.getSubtotal();
            this.total_discrepancy = this.expected_quantity - this.total_actual_quantity;

            return true;
        }
        else
            return false;
    }

    public void removeItemValueAt(int position) {
        ReceivedItemValue forDelete = itemValueList.remove(position);

        if(forDelete != null) {
            this.total_actual_quantity -= forDelete.getQuantity();
            this.total_outright_return -= forDelete.getOutright_return();
            this.total_subtotal -= forDelete.getSubtotal();
            this.total_discrepancy = this.expected_quantity - this.total_actual_quantity;
        }
    }

    public boolean isValidUnit() {
        return (unit != null && unit.getId() != -1);
    }
}
