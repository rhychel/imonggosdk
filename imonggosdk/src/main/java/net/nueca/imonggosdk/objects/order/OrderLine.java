package net.nueca.imonggosdk.objects.order;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable2;
import net.nueca.imonggosdk.objects.invoice.Invoice;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/6/14.
 * NuecaLibrary (c)2014
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
    private String brand = "";

    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "order_id")
    private transient Order order;

    public OrderLine() {}

    public OrderLine(int product_id, double quantity) {
        this.product_id = product_id;
        this.quantity = quantity;
    }


    public OrderLine(int product_id, double retail_price, double quantity) {
        this.product_id = product_id;
        this.retail_price = retail_price;
        this.quantity = quantity;
    }

    public int getProductId() {
        return product_id;
    }

    public void setProductId(int product_id) {
        this.product_id = product_id;
    }

    public double getRetailPrice() {
        return retail_price;
    }

    public void setRetailPrice(double retail_price) {
        this.retail_price = retail_price;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setUnitId(int unit_id) {
        this.unit_id = unit_id;
    }

    public int getUnitId() {
        return unit_id;
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new Gson();
        return new JSONObject(gson.toJson(this));
        /*JSONObject jsonObject = new JSONObject();
        jsonObject.put("line_no", line_no);
        jsonObject.put("product_id", product_id);
        jsonObject.put("retail_price", retail_price);
        jsonObject.put("quantity", quantity);
        jsonObject.put("unit_id", unit_id);
        jsonObject.put("unit_quantity", unit_quantity);
        jsonObject.put("unit_content_quantity", unit_content_quantity);
        jsonObject.put("unit_name", unit_name);
        jsonObject.put("unit_id", unit_id);
        jsonObject.put("brand", brand);
        return jsonObject;*/
    }

    public double getUnitQuantity() {
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
    }

    public int getLineNo() {
        return line_no;
    }

    public void setLineNo(int line_no) {
        this.line_no = line_no;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    @Override
    public void insertTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.ORDER_LINES, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.ORDER_LINES, DatabaseOperation.DELETE);
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
}
