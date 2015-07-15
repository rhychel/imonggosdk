package net.nueca.imonggosdk.objects.order;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.base.BatchList;
import net.nueca.imonggosdk.tools.ReferenceNumberTool;

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
    public static transient final int MAX_ORDERLINES_PER_PAGE = 1;

    @Expose
    @DatabaseField
    private String target_delivery_date = ""; // current_date+2days

    @Expose
    @DatabaseField
    private String remark = "";

    @Expose
    @DatabaseField
    private String reference = "";

    @Expose
    @DatabaseField
    private String order_type_code = "stock_request";

    @Expose
    @DatabaseField
    private int serving_branch_id = 0;

    @Expose
    private BatchList<OrderLine> order_lines = new BatchList<>(DatabaseOperation.INSERT);

    @ForeignCollectionField
    private transient ForeignCollection<OrderLine> order_lines_fc;

    public Order() {
        order_lines = new BatchList<>(DatabaseOperation.INSERT);
    }

    public String getTarget_delivery_date() {
        return target_delivery_date;
    }

    public void setTarget_delivery_date(String target_delivery_date) {
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

    public void generateNewReference(Context context, int deviceId) {
        this.reference = ReferenceNumberTool.generateRefNo(context,
                deviceId);
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

    public String getOrder_type_code() {
        return order_type_code;
    }

    public void setOrder_type_code(String order_type_code) {
        this.order_type_code = order_type_code;
    }

    public int getServing_branch_id() {
        return serving_branch_id;
    }

    public void setServing_branch_id(int serving_branch_id) {
        this.serving_branch_id = serving_branch_id;
    }

    public void addOrderLine(OrderLine orderLine) {
        order_lines.add(orderLine);
    }

    public static Order fromJSONString(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        return fromJSONObject(jsonObject);
    }

    public static Order fromJSONObject(JSONObject jsonObject) throws JSONException {
        Gson gson = new Gson();
        if(jsonObject.has("order")) {
            jsonObject = jsonObject.getJSONObject("order");
        }
        Order order = gson.fromJson(jsonObject.toString(),Order.class);
        return order;
    }

    public String toJSONString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        refreshListObjects();
        return (gson.toJson(this));
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        refreshListObjects();
        return new JSONObject(gson.toJson(this));
    }

    private transient boolean didFinalize = false;
    // call this to save the OrderLine objects to database
    public Order finalize(ImonggoDBHelper dbHelper) {
        if(didFinalize) {
            Log.e("Order", "finalize : called more than once");
            return this;
        }
        didFinalize = true;

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
        if(id < 0) {
            Log.e("Order", "insertTo : Invalid ID : use returned ID from server after sending as this Order's ID");
            return;
        }

        try {
            dbHelper.dbOperations(this, Table.ORDERS, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finalize(dbHelper);
    }

    @Override
    public void deleteTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.ORDERS, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finalize(dbHelper);
    }

    @Override
    public void updateTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.ORDERS, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finalize(dbHelper);
    }

    public boolean shouldPageRequest() {
        return order_lines.size() > MAX_ORDERLINES_PER_PAGE;
    }
}
