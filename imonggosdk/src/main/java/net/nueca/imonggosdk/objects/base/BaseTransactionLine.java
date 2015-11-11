package net.nueca.imonggosdk.objects.base;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;


/**
 * Created by gama on 11/11/2015.
 */
public abstract class BaseTransactionLine extends BaseTable2 {
    @Expose
    @DatabaseField
    protected int product_id = 0;
    @Expose
    @DatabaseField
    protected double retail_price = 0.0;
    @Expose
    @DatabaseField
    protected double quantity = 0.0;

    @Expose
    @DatabaseField
    protected Integer unit_id = null;
    @Expose
    @DatabaseField
    protected Double unit_quantity = null;
    @Expose
    @DatabaseField
    protected Double unit_content_quantity = null;
    @Expose
    @DatabaseField
    protected Double unit_retail_price = null;
    @Expose
    @DatabaseField
    protected String unit_name = null;

    public BaseTransactionLine() {}

    public BaseTransactionLine(Builder builder) {
        product_id = builder.product_id;
        retail_price = builder.retail_price;
        quantity = builder.quantity;

        unit_id = builder.unit_id;
        unit_quantity = builder.unit_quantity;
        unit_content_quantity = builder.unit_content_quantity;
        unit_retail_price = builder.unit_retail_price;
        unit_name = builder.unit_name;
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

    public Integer getUnit_id() {
        return unit_id;
    }

    public void setUnit_id(Integer unit_id) {
        this.unit_id = unit_id;
    }

    public Double getUnit_quantity() {
        return unit_quantity;
    }

    public void setUnit_quantity(Double unit_quantity) {
        this.unit_quantity = unit_quantity;
    }

    public Double getUnit_content_quantity() {
        return unit_content_quantity;
    }

    public void setUnit_content_quantity(Double unit_content_quantity) {
        this.unit_content_quantity = unit_content_quantity;
    }

    public Double getUnit_retail_price() {
        return unit_retail_price;
    }

    public void setUnit_retail_price(Double unit_retail_price) {
        this.unit_retail_price = unit_retail_price;
    }

    public String getUnit_name() {
        return unit_name;
    }

    public void setUnit_name(String unit_name) {
        this.unit_name = unit_name;
    }

    public static abstract class Builder {
        protected int product_id = -1;
        protected double retail_price = 0.0;
        protected double quantity = 0.0;

        protected Integer unit_id = null;
        protected Double unit_quantity = null;
        protected Double unit_content_quantity = null;
        protected Double unit_retail_price = null;
        protected String unit_name = null;

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
        public Builder unit_id(int unit_id) {
            this.unit_id = unit_id;
            return this;
        }
        public Builder unit_quantity(double unit_quantity) {
            this.unit_quantity = unit_quantity;
            return this;
        }
        public Builder unit_content_quantity(double unit_content_quantity) {
            this.unit_content_quantity = unit_content_quantity;
            return this;
        }
        public Builder unit_retail_price(double unit_retail_price) {
            this.unit_retail_price = unit_retail_price;
            return this;
        }
        public Builder unit_name(String unit_name) {
            this.unit_name = unit_name;
            return this;
        }

        public abstract BaseTransactionLine build();
    }
}
