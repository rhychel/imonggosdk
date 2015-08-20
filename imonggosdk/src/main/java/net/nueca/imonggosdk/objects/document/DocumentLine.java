package net.nueca.imonggosdk.objects.document;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by gama on 7/20/15.
 */
public class DocumentLine {
    protected int line_no;
    protected int product_id;
    protected double quantity;
    protected ExtendedAttributes extended_attributes;
    protected String discount_text;

    protected Double price;
    protected Double retail_price;
    protected Integer unit_id;
    protected Double unit_content_quantity;
    protected String unit_name;
    protected Double unit_quantity;
    protected Double unit_retail_price;

    public DocumentLine(Builder builder) {
        line_no = builder.line_no;
        product_id = builder.product_id;
        quantity = builder.quantity;
        extended_attributes = builder.extended_attributes;
        discount_text = builder.discount_text;
        price = builder.price;
        retail_price = builder.retail_price;
        unit_id = builder.unit_id;
        unit_content_quantity = builder.unit_content_quantity;
        unit_name = builder.unit_name;
        unit_quantity = builder.unit_quantity;
        unit_retail_price = builder.unit_retail_price;
    }

    public int getLine_no() {
        return line_no;
    }

    public void setLine_no(int line_no) {
        this.line_no = line_no;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public ExtendedAttributes getExtended_attributes() {
        return extended_attributes;
    }

    public void setExtended_attributes(ExtendedAttributes extended_attributes) {
        this.extended_attributes = extended_attributes;
    }

    public String getDiscount_text() {
        return discount_text;
    }

    public void setDiscount_text(String discount_text) {
        this.discount_text = discount_text;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getRetail_price() {
        return retail_price;
    }

    public void setRetail_price(Double retail_price) {
        this.retail_price = retail_price;
    }

    public Integer getUnit_id() {
        return unit_id;
    }

    public void setUnit_id(Integer unit_id) {
        this.unit_id = unit_id;
    }

    public Double getUnit_content_quantity() {
        return unit_content_quantity;
    }

    public void setUnit_content_quantity(Double unit_content_quantity) {
        this.unit_content_quantity = unit_content_quantity;
    }

    public String getUnit_name() {
        return unit_name;
    }

    public void setUnit_name(String unit_name) {
        this.unit_name = unit_name;
    }

    public Double getUnit_quantity() {
        return unit_quantity;
    }

    public void setUnit_quantity(Double unit_quantity) {
        this.unit_quantity = unit_quantity;
    }

    public Double getUnit_retail_price() {
        return unit_retail_price;
    }

    public void setUnit_retail_price(Double unit_retail_price) {
        this.unit_retail_price = unit_retail_price;
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new Gson();
        return new JSONObject(gson.toJson(this));
    }

    public static class Builder {
        protected int line_no = 0;
        protected int product_id = 0;
        protected double quantity;
        protected ExtendedAttributes extended_attributes;
        protected String discount_text;
        protected Double price;
        protected Double retail_price;
        protected Integer unit_id = null;
        protected Double unit_content_quantity = null;
        protected String unit_name = null;
        protected Double unit_quantity = null;
        protected Double unit_retail_price = null;

        public Builder line_no(int line_no) {
            this.line_no = line_no;
            return this;
        }
        public Builder product_id(int product_id) {
            this.product_id = product_id;
            return this;
        }
        public Builder quantity(double quantity) {
            this.quantity = quantity;
            return this;
        }
        public Builder extended_attributes(ExtendedAttributes extended_attributes) {
            this.extended_attributes = extended_attributes;
            return this;
        }
        public Builder discount_text(String discount_text) {
            this.discount_text = discount_text;
            return this;
        }
        public Builder price(double price) {
            this.price = price;
            return this;
        }
        public Builder retail_price(double retail_price) {
            this.retail_price = retail_price;
            return this;
        }
        public Builder unit_id(int unit_id) {
            this.unit_id = unit_id;
            return this;
        }
        public Builder unit_content_quantity(double unit_content_quantity) {
            this.unit_content_quantity = unit_content_quantity;
            return this;
        }
        public Builder unit_name(String unit_name) {
            this.unit_name = unit_name;
            return this;
        }
        public Builder unit_quantity(double unit_quantity) {
            this.unit_quantity = unit_quantity;
            return this;
        }
        public Builder unit_retail_price(double unit_retail_price) {
            this.unit_retail_price = unit_retail_price;
            return this;
        }

        public DocumentLine build() {
            return new DocumentLine(this);
        }
    }
}
