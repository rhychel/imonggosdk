package net.nueca.imonggosdk.objects.order;

import com.google.gson.Gson;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.base.BatchList;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.objects.invoice.InvoiceTaxRate;
import net.nueca.imonggosdk.objects.invoice.Payment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by rhymart on 5/6/14.
 * NuecaLibrary (c)2014
 */
public class Order extends BaseTable {

    @DatabaseField
    private String target_delivery_date = ""; // current_date+2days

    @DatabaseField
    private String remark = "";

    @DatabaseField
    private String reference = "";

    @DatabaseField
    private String order_type_code = "stock_request";

    @DatabaseField
    private int serving_branch_id = 0;

    @DatabaseField
    private BatchList<OrderLine> order_lines = new BatchList<>(DatabaseOperation.INSERT);

    @ForeignCollectionField
    private transient ForeignCollection<OrderLine> order_lines_fc;

    public Order() {
        order_lines = new BatchList<>(DatabaseOperation.INSERT);
    }

    public String getTargetDeliveryDate() {
        return target_delivery_date;
    }

    public void setTargetDeliveryDate(String target_delivery_date) {
        this.target_delivery_date = target_delivery_date;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public List<OrderLine> getOrderLines() {
        if(order_lines == null || order_lines.size() <= 0)
            refreshListObjects();
        return order_lines;
    }

    public void setOrderLines(BatchList<OrderLine> order_lines) {
        this.order_lines = order_lines;
    }

    public JSONArray getOrderLineJSONArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        refreshListObjects();
        for (OrderLine orderLine : order_lines) {
            jsonArray.put(orderLine.toJSONObject());
        }
        return jsonArray;
    }

    public String getOrderTypeCode() {
        return order_type_code;
    }

    public void setOrderTypeCode(String order_type_code) {
        this.order_type_code = order_type_code;
    }

    public int getServingBranchId() {
        return serving_branch_id;
    }

    public void setServingBranchId(int serving_branch_id) {
        this.serving_branch_id = serving_branch_id;
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new Gson();
        refreshListObjects();
        return new JSONObject(gson.toJson(this));
    }

    // call this to save the OrderLine objects to database
    public Order refresh(ImonggoDBHelper dbHelper) {
        for(OrderLine orderLine : order_lines)
            orderLine.setOrder(this);
        order_lines.doOperation(dbHelper);
        return this;
    }

    // call this when using getHelper().getOrders() to re-initialize the list that
    // was lost when Order was saved to the database
    public Order refreshListObjects() {
        if( (order_lines == null || order_lines.size() <= 0) &&
                (order_lines_fc != null && order_lines_fc.size() > 0)) {
            for(OrderLine orderLine : order_lines_fc) {
                order_lines.add(orderLine);
            }
        }
        return this;
    }

    @Override
    public void insertTo(ImonggoDBHelper dbHelper) {
        refresh(dbHelper);
        try {
            dbHelper.dbOperations(this, Table.ORDERS, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper dbHelper) {
        refresh(dbHelper);
        try {
            dbHelper.dbOperations(this, Table.ORDERS, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper dbHelper) {
        refresh(dbHelper);
        try {
            dbHelper.dbOperations(this, Table.ORDERS, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
