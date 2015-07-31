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
    protected int line_no = 0;
    protected int product_id = 0;
    protected double quantity;
    protected ExtendedAttributes extended_attributes;
    protected String discount_text;
    protected double price;
    protected Double unit_content_quantity;
    protected String unit_name;
    protected Double unit_quantity;

    public DocumentLine(Builder builder) {
        line_no = builder.line_no;
        product_id = builder.product_id;
        quantity = builder.quantity;
        extended_attributes = builder.extended_attributes;
        discount_text = builder.discount_text;
        price = builder.price;
        unit_content_quantity = builder.unit_content_quantity;
        unit_name = builder.unit_name;
        unit_quantity = builder.unit_quantity;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getUnit_content_quantity() {
        return unit_content_quantity;
    }

    public void setUnit_content_quantity(double unit_content_quantity) {
        this.unit_content_quantity = unit_content_quantity;
    }

    public String getUnit_name() {
        return unit_name;
    }

    public void setUnit_name(String unit_name) {
        this.unit_name = unit_name;
    }

    public double getUnit_quantity() {
        return unit_quantity;
    }

    public void setUnit_quantity(double unit_quantity) {
        this.unit_quantity = unit_quantity;
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
        protected double price;
        protected Double unit_content_quantity = null;
        protected String unit_name = null;
        protected Double unit_quantity = null;

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

        public DocumentLine build() {
            return new DocumentLine(this);
        }
    }
}
