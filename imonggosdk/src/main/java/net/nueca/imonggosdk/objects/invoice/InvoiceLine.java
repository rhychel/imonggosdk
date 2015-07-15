package net.nueca.imonggosdk.objects.invoice;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.base.BaseTable2;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Created by gama on 7/1/15.
 */
public class InvoiceLine extends BaseTable2 {
    @Expose
    @DatabaseField
    protected int product_id;

    @Expose
    @DatabaseField
    protected int quantity;

    @Expose
    @DatabaseField
    protected double retail_price;

    @Expose
    @DatabaseField
    protected String discount_text;

    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "invoice_id")
    private transient Invoice invoice;

    public InvoiceLine () {}

    public InvoiceLine(Invoice invoice, int product_id, int quantity, int retail_price, String discount_text) {
        this.invoice = invoice;
        this.product_id = product_id;
        this.quantity = quantity;
        this.retail_price = retail_price;
        this.discount_text = discount_text;
    }

    public InvoiceLine(Invoice invoice, Product product, int quantity, String discount_text) {
        this.invoice = invoice;
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

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    @Override
    public void insertTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.INVOICE_LINES, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.INVOICE_LINES, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.INVOICE_LINES, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return new JSONObject(gson.toJson(this));
        /*JSONObject jsonObject = new JSONObject();
        jsonObject.put("product_id", product_id);
        jsonObject.put("quantity", quantity);
        jsonObject.put("retail_price", retail_price);
        jsonObject.put("discount_text", discount_text);
        return jsonObject;*/
    }

    public double computeTotal(boolean applyDiscount) {
        double total = retail_price * quantity;
        if(applyDiscount){

        }
        return total;
    }
}
