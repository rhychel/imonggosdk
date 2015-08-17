package net.nueca.concessioengine.objects;

import net.nueca.imonggosdk.objects.Unit;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 [
     {
         "product_id": 115509,
         "retail_price": 8769.6,
         "quantity": 313.59999999999997, INPUTTED QUANTITY * quantity from the UNIT OBJECT
         "unit_id": 9088,
         "unit_quantity": 28, --- INPUTTED QUANTITY
         "unit_content_quantity": 11.2, -- quantity from the UNIT OBJECT
         "unit_retail_price": 98219.52, -- RETAIL PRICE from UNIT OBJECT * UNIT QUANTITY
         "unit_name": "CUPS",
         "line_no": 1
     },
     {
         "product_id": 115510,
         "retail_price": 430.5,
         "quantity": 50,
         "unit_id": 80,
         "unit_quantity": 5,
         "unit_content_quantity": 10,
         "unit_retail_price": 4305,
         "unit_name": "box",
         "line_no": 2
     },
     {
         "product_id": 115511,
         "retail_price": 261,
         "quantity": 2,
         "line_no": 3
     }
 ]
 */
public class Values {

    private int line_no = -1;
    private Unit unit;
    private String unit_quantity = null, unit_name = null;
    private double unit_retail_price = 0.0, unit_content_quantity = 0.0;
    private String quantity = "1";
    private ExtendedAttributes extendedAttributes = null;

    public Values() { }

    public Values(Unit unit, String quantity) {
        setValue(quantity, unit);
    }

    public Unit getUnit() {
        return unit;
    }

    public void setValue(String quantity, Unit unit) {
        setValue(quantity, unit, null);
    }

    public void setValue(String quantity, Unit unit, ExtendedAttributes extendedAttributes) {
        if(extendedAttributes != null)
            this.extendedAttributes = extendedAttributes;
        if(unit != null && unit.getId() != -1) {
            this.quantity = String.valueOf((unit.getQuantity() * Double.valueOf(quantity)));
            this.unit_quantity = quantity;
            this.unit_content_quantity = unit.getQuantity();
            this.unit_name = unit.getName();
            this.unit_retail_price = unit.getRetail_price()*Double.valueOf(this.unit_quantity);
            this.unit = unit;
        }
        else{
            this.quantity = quantity;
            this.unit = unit;
        }
    }

    public String getQuantity() {
        if(unit != null && unit.getId() != -1)
            return unit_quantity;
        return quantity;
    }

    public int getLine_no() {
        return line_no;
    }

    public void setLine_no(int line_no) {
        this.line_no = line_no;
    }

    public String getUnit_quantity() {
        return unit_quantity;
    }

    public void setUnit_quantity(String unit_quantity) {
        this.unit_quantity = unit_quantity;
    }

    public String getUnit_name() {
        return unit_name;
    }

    public void setUnit_name(String unit_name) {
        this.unit_name = unit_name;
    }

    public double getUnit_retail_price() {
        return unit_retail_price;
    }

    public void setUnit_retail_price(double unit_retail_price) {
        this.unit_retail_price = unit_retail_price;
    }

    public double getUnit_content_quantity() {
        return unit_content_quantity;
    }

    public void setUnit_content_quantity(double unit_content_quantity) {
        this.unit_content_quantity = unit_content_quantity;
    }

    public ExtendedAttributes getExtendedAttributes() {
        return extendedAttributes;
    }

    public void setExtendedAttributes(ExtendedAttributes extendedAttributes) {
        this.extendedAttributes = extendedAttributes;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public boolean isValidUnit() {
        return (unit != null && unit.getId() != -1);
    }

    @Override
    public boolean equals(Object o) {
//        return unit.getId() == ((Values)o).getUnit().getId();
        return line_no == ((Values)o).line_no;
    }

    @Override
    public String toString() {
        return quantity;
    }
}
