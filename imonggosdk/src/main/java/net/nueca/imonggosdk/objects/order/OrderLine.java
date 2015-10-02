package net.nueca.imonggosdk.objects.order;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable2;
import net.nueca.imonggosdk.objects.base.BaseTransactionDB;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Created by gama on 7/1/15.
 */
public class OrderLine extends BaseTable2 {
    @Expose
    @DatabaseField
    private int line_no = 0;
    @Expose
    @DatabaseField
    private int product_id = 0;
    @Expose
    @DatabaseField
    private double retail_price = 0.0;
    @Expose
    @DatabaseField
    private double quantity = 0.0;

    @Expose
    @DatabaseField
    private Integer unit_id = null;
    @Expose
    @DatabaseField
    private Double unit_quantity = null;
    @Expose
    @DatabaseField
    private Double unit_content_quantity = null;
    @Expose
    @DatabaseField
    private Double unit_retail_price = null;
    @Expose
    @DatabaseField
    private String unit_name = null;
    @Expose
    @DatabaseField
    private String brand = null;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "order_id")
    protected transient Order order;

    public OrderLine() {}

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

    public void setUnit_id(Integer unit_id) {
        this.unit_id = unit_id;
    }

    public Integer getUnit_id() {
        return unit_id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new Gson();
        return new JSONObject(gson.toJson(this));
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

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getLine_no() {
        return line_no;
    }

    public void setLine_no(int line_no) {
        this.line_no = line_no;
    }

    @Override
    public void insertTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.ORDER_LINES, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.ORDER_LINES, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.ORDER_LINES, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class Builder {
        private int line_no;
        private int product_id;
        private double retail_price;
        private double quantity;

        private Integer unit_id = null;
        private Double unit_quantity = null;
        private Double unit_content_quantity = null;
        private Double unit_retail_price = null;
        private String unit_name = null;
        private String brand = null;

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
        public Builder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public OrderLine build() {
            return new OrderLine(this);
        }
    }
}
