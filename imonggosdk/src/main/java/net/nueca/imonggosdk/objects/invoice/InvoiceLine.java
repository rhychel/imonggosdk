package net.nueca.imonggosdk.objects.invoice;

import com.google.gson.Gson;

import net.nueca.imonggosdk.objects.Product;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gama on 7/1/15.
 */
public class InvoiceLine {
    protected int product_id;

    protected int quantity;

    protected double retail_price;

    protected String discount_text;

    public InvoiceLine () {}

    public InvoiceLine(int product_id, int quantity, int retail_price, String discount_text) {
        this.product_id = product_id;
        this.quantity = quantity;
        this.retail_price = retail_price;
        this.discount_text = discount_text;
    }

    public InvoiceLine(Product product, int quantity, String discount_text) {
        this.product_id = product.getId();
        this.quantity = quantity;
        this.retail_price = product.getRetail_price();
        this.discount_text = discount_text;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getRetail_price() {
        return retail_price;
    }

    public void setRetail_price(double retail_price) {
        this.retail_price = retail_price;
    }

    public String getDiscount_text() {
        return discount_text;
    }

    public void setDiscount_text(String discount_text) {
        this.discount_text = discount_text;
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new Gson();
        return new JSONObject(gson.toJson(this));
    }

    public double computeTotal(boolean applyDiscount) {
        double total = retail_price * quantity;
        if(applyDiscount){

        }
        return total;
    }
}
