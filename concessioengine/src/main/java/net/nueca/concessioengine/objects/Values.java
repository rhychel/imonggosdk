package net.nueca.concessioengine.objects;

import android.util.Log;

import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.tools.DiscountTools;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.objects.Settings;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.base.DBTable;
import net.nueca.imonggosdk.objects.invoice.InvoicePurpose;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.tools.Configurations;
import net.nueca.imonggosdk.tools.NumberTools;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    private String quantity = "1", expiry_date;
    private ExtendedAttributes extendedAttributes = null;
    private boolean isBadStock = true;
    // ---- FOR INVOICE
    private String discount_text;
    private Double subtotal, no_discount_subtotal;
    private Double retail_price;
    private InvoicePurpose invoicePurpose;
    private String product_discount_text = "",
            company_discount_text = "",
            customer_discount_text = "";
    private List<Double> product_discounts = new ArrayList<>(),
            company_discounts = new ArrayList<>(),
            customer_discounts = new ArrayList<>();
    private Price price = null;

    public Values() { }

    public Values(Unit unit, String quantity) {
        setValue(quantity, unit);
    }

    public Values(String quantity, Price price) {
        setValue(quantity, price);
    }

    public Values(Unit unit, String quantity, Double retail_price) {
        //Log.e("VALUES", "Values(unit="+(unit!=null?unit.getName():"null")+", quantity="+quantity+", retail_price="+retail_price+")");
        setValue(quantity, unit, retail_price);
    }

    public Unit getUnit() {
        return unit;
    }

    public void setValue(String quantity, Unit unit) {
        //Log.e("VALUES", "setValue(quantity="+quantity +", unit="+(unit!=null?unit.getName():"null")+")");
        if(this.price != null && this.price.getUnit() != null && !this.price.getUnit().equals(unit))
            this.price = null;
        if(unit != null)
            this.retail_price = unit.getRetail_price();
        setValue(quantity, unit, null);
    }

    public void setValue(String quantity, Unit unit, double retail_price, String customer_discount_text) {
        if(customer_discount_text != null)
            this.customer_discount_text = customer_discount_text;
        setValue(quantity, unit, retail_price);
    }

    public void setValue(String quantity, Unit unit, double retail_price) {
        Log.e("VALUES", "setValue(quantity="+quantity +", unit="+(unit!=null?unit.getName():"null")+", retail_price="+retail_price+")");
        if(this.price != null && this.price.getUnit() != null && !this.price.getUnit().equals(unit))
            this.price = null;
        this.retail_price = retail_price;
        setValue(quantity, unit, null);
    }

    public void setValue(String quantity, Price price, String customer_discount_text) {
        setValue(quantity, price, customer_discount_text, null);
    }

    public void setValue(String quantity, Price price, String customer_discount_text, ExtendedAttributes extendedAttributes) {
        Log.e("VALUES", "setValue(quantity="+quantity +", price="+(price!=null?price.toJSONString():"null")+")");
        if(customer_discount_text != null)
            this.customer_discount_text = customer_discount_text;
        setValue(quantity, price, extendedAttributes);
    }

    public void setValue(String quantity, Price price) {
        Log.e("VALUES", "setValue(quantity="+quantity +", price="+(price!=null?price.toJSONString():"null")+")");
        setValue(quantity, price, (ExtendedAttributes) null);
    }

    public void setValue(String quantity, Price price, ExtendedAttributes extendedAttributes) {
        Log.e("VALUES", "setValue price isNull? " + (price==null));
        this.price = price;
        if(price != null) {
            if (price.getRetail_price() != null)
                this.retail_price = price.getRetail_price();
            else
                this.retail_price = price.getProduct().getRetail_price();
            this.discount_text = price.getDiscount_text();
        }
        setValue(quantity, price == null? null : price.getUnit(), extendedAttributes);
    }

    public void setValue(String input_quantity, Unit unit, ExtendedAttributes extendedAttributes) {
        if(extendedAttributes != null)
            this.extendedAttributes = extendedAttributes;
        this.unit = unit;
        if(isValidUnit()) {
            Log.e("Quantity-unit", unit.getQuantity()+" * "+input_quantity);
            this.unit_quantity = input_quantity;
            this.unit_content_quantity = unit.getQuantity();
            if(input_quantity.length() > 0)
                this.quantity = String.valueOf((unit.getQuantity() * Double.valueOf(input_quantity)));
            this.unit_name = unit.getName();
            this.unit_retail_price = this.retail_price * Double.valueOf(this.unit_quantity);
        }
        else {
            this.quantity = input_quantity;
            this.unit_quantity = null;
            this.unit_content_quantity = 0d;
            this.unit_retail_price = 0d;
            if(unit != null)
                this.unit_name = unit.getName();
        }

        Log.e("QTY", this.quantity + " ~ " + input_quantity);

        Log.e("DISCOUNT TEXT", (this.discount_text == null? "null" : this.discount_text) );
        if(this.discount_text != null && this.discount_text.length() > 0) {
            if (this.discount_text.contains(";")) {
                String[] discounts = this.discount_text.split(";");
                this.product_discount_text = discounts[0];
                if(discounts.length > 1)
                    this.company_discount_text = discounts[1];
            } else {
                this.product_discount_text = this.discount_text;
            }
        }

        Log.e("PRICE OBJ", "isNull? " + (price == null));
        if(this.price == null) {
            if(isValidUnit()) {
                this.no_discount_subtotal = this.unit_retail_price;
                this.subtotal = this.unit_retail_price;
            } else {
                this.no_discount_subtotal = this.retail_price * Double.valueOf(this.quantity);
                this.subtotal = this.retail_price * Double.valueOf(this.quantity);
            }

            //if (customer_discount_text != null && customer_discount_text.length() > 0) {
            //    this.subtotal = DiscountTools.applyMultipleDiscounts(
            //            new BigDecimal(this.retail_price), new BigDecimal(this.input_quantity),
            //            customer_discounts, customer_discount_text, ","
            //    ).doubleValue();
            //} else {
            //    this.subtotal = this.retail_price * Double.valueOf(this.quantity);
            //}
        }
        else {
            if(isValidUnit()) {
                this.no_discount_subtotal = this.unit_retail_price;
                this.subtotal = DiscountTools.applyMultipleDiscounts(
                        new BigDecimal(this.retail_price), new BigDecimal(this.unit_quantity),
                        product_discounts, company_discounts, discount_text, ";", ","
                ).doubleValue();
            } else {
                this.no_discount_subtotal = this.retail_price * Double.valueOf(this.quantity);
                this.subtotal = DiscountTools.applyMultipleDiscounts(
                        new BigDecimal(this.retail_price), new BigDecimal(this.quantity),
                        product_discounts, company_discounts, discount_text, ";", ","
                ).doubleValue();
            }

            //if (customer_discount_text != null && customer_discount_text.length() > 0) {
            //    this.subtotal = DiscountTools.applyMultipleDiscounts(
            //            new BigDecimal(this.retail_price), new BigDecimal(input_quantity),
            //            product_discounts, company_discounts, discount_text, ";", ",",
            //            customer_discounts, customer_discount_text
            //    ).doubleValue();
            //} else {
            //    this.subtotal = DiscountTools.applyMultipleDiscounts(
            //            new BigDecimal(this.retail_price), new BigDecimal(input_quantity),
            //            product_discounts, discount_text, ","
            //    ).doubleValue();
            //}
        }

        /** DECIMAL FORMATTING **/
        this.subtotal = NumberTools.formatDouble(this.subtotal, ProductsAdapterHelper.getDecimalPlace());


        Log.e("QTY", this.quantity + " ~ " + input_quantity);
        Log.e("Unit", unit != null? unit.getName()+" id:"+unit.getId() : "null");
        Log.e("Values", "setValue : " + this.retail_price + " * " + input_quantity + " = " + this.subtotal);
    }

    public String getQuantity() {
        if(isValidUnit() && unit_quantity != null)
            return unit_quantity;
        return quantity;
    }

    /**
     * This is used when generating the transaction ready for syncing.
     * @return
     */
    public String getActualQuantity() {
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

    public String getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(String expiry_date) {
        this.expiry_date = expiry_date;
    }

    public boolean isBadStock() {
        return isBadStock;
    }

    public void setBadStock(boolean badStock) {
        isBadStock = badStock;
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

    public String getDiscount_text() {
        return discount_text;
    }

    public void setDiscount_text(String discount_text) {
        this.discount_text = discount_text;
    }

    public double getSubtotal() {
        //subtotal = ( (unit != null? unit.getRetail_price() : retail_price ) * NumberTools.toDouble(quantity) );
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getRetail_price() {
        return retail_price;
    }

    public void setRetail_price(double retail_price) {
        this.retail_price = retail_price;
    }

    public String getProduct_discount_text() {
        if(product_discount_text.length() == 0)
            return null;
        return product_discount_text;
    }

    public String getCompany_discount_text() {
        if(company_discount_text.length() == 0)
            return null;
        return company_discount_text;
    }

    public String getCustomer_discount_text() {
        if(customer_discount_text.length() == 0)
            return null;
        return customer_discount_text;
    }

    public List<Double> getProduct_discounts() {
        return product_discounts;
    }

    public List<Double> getCompany_discounts() {
        return company_discounts;
    }

    public List<Double> getCustomer_discounts() {
        return customer_discounts;
    }

    public Price getPrice() {
        return price;
    }

    public Double getNoDiscountSubtotal() {
        return no_discount_subtotal;
    }

    public InvoicePurpose getInvoicePurpose() {
        return invoicePurpose;
    }

    public void setInvoicePurpose(InvoicePurpose invoicePurpose) {
        this.invoicePurpose = invoicePurpose;
    }

    @Override
    public boolean equals(Object o) {
//        return unit.getId() == ((Values)o).getUnit().getId();
        return line_no == ((Values)o).line_no;
    }

    @Override
    public String toString() {
        return "Values{" +
                "line_no=" + line_no +
                ", unit=" + unit +
                ", unit_quantity='" + unit_quantity + '\'' +
                ", unit_name='" + unit_name + '\'' +
                ", unit_retail_price=" + unit_retail_price +
                ", unit_content_quantity=" + unit_content_quantity +
                ", quantity='" + quantity + '\'' +
                ", extendedAttributes=" + (extendedAttributes != null? extendedAttributes.toString():"null") +
                '}';
    }


}
