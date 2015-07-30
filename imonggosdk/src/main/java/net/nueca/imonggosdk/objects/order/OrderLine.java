package net.nueca.imonggosdk.objects.order;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rhymart on 5/6/14.
 * NuecaLibrary (c)2014
 */
public class OrderLine {
    private int line_no = 0;

    private int product_id = 0;

    private double retail_price = 0.0;

    private double quantity = 0.0;

    /*@Expose
    @DatabaseField
    private int unit_id = -1;

    @Expose
    @DatabaseField
    private double unit_quantity = 0.0;

    @Expose
    @DatabaseField
    private double unit_content_quantity = 0.0;

    @Expose
    @DatabaseField
    private double unit_retail_price = 0.0;

    @Expose
    @DatabaseField
    private String unit_name = "";

    @Expose
    @DatabaseField
    private String brand = "";*/

    public OrderLine(Builder builder) {
        line_no = builder.line_no;
        product_id = builder.product_id;
        retail_price = builder.retail_price;
        quantity = builder.quantity;
    }

    public OrderLine(int product_id, double quantity) {
        this.product_id = product_id;
        this.quantity = quantity;
    }


    public OrderLine(int product_id, double retail_price, double quantity) {
        this.product_id = product_id;
        this.retail_price = retail_price;
        this.quantity = quantity;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public double getRetail_price() {
        return retail_price;
    }

    public void setRetail_price(double retail_price) {
        this.retail_price = retail_price;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    /*public void setUnitId(int unit_id) {
        this.unit_id = unit_id;
    }

    public int getUnitId() {
        return unit_id;
    }*/

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new Gson();
        return new JSONObject(gson.toJson(this));
    }

    /*public double getUnitQuantity() {
        return unit_quantity;
    }

    public void setUnitQuantity(double unit_quantity) {
        this.unit_quantity = unit_quantity;
    }

    public double getUnitContentQuantity() {
        return unit_content_quantity;
    }

    public void setUnitContentQuantity(double unit_content_quantity) {
        this.unit_content_quantity = unit_content_quantity;
    }

    public double getUnitRetailPrice() {
        return unit_retail_price;
    }

    public void setUnitRetailPrice(double unit_retail_price) {
        this.unit_retail_price = unit_retail_price;
    }

    public String getUnitName() {
        return unit_name;
    }

    public void setUnitName(String unit_name) {
        this.unit_name = unit_name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }*/

    public int getLine_no() {
        return line_no;
    }

    public void setLine_no(int line_no) {
        this.line_no = line_no;
    }

    public static class Builder {
        private int line_no;
        private int product_id;
        private double retail_price;
        private double quantity;

        public Builder line_no(int line_no) {
            this.line_no = line_no;
            return this;
        }
        public Builder product_id(int product_id) {
            this.product_id = product_id;
            return this;
        }
        public Builder retail_price(double retail_price) {
            this.retail_price = retail_price;
            return this;
        }
        public Builder quantity(double quantity) {
            this.quantity = quantity;
            return this;
        }

        public OrderLine build() {
            return new OrderLine(this);
        }
    }
}
