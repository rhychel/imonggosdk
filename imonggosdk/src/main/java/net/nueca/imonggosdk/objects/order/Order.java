package net.nueca.imonggosdk.objects.order;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Parameter;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.base.BaseTransactionTable3;
import net.nueca.imonggosdk.objects.base.BatchList;
import net.nueca.imonggosdk.operations.ImonggoTools;
import net.nueca.imonggosdk.operations.http.HTTPRequests;
import net.nueca.imonggosdk.swable.SwableTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 7/1/15.
 */
public class Order extends BaseTransactionTable3 {
    public static transient final int MAX_ORDERLINES_PER_PAGE = 50;
    public static String ORDERTYPE_PURCHASE_ORDER = "PURCHASE_ORDER";
    public static String ORDERTYPE_STOCK_REQUEST = "STOCK_REQUEST";

    @Expose
    @DatabaseField
    private String target_delivery_date; // current_date + 2 days
    @Expose
    @DatabaseField
    private String remark;
    @Expose
    @DatabaseField
    private String order_type_code;
    @Expose
    @DatabaseField
    private int serving_branch_id;
    @DatabaseField
    private Integer branch_id;
    @Expose
    private List<OrderLine> order_lines;

    @ForeignCollectionField(orderColumnName = "line_no")
    private transient ForeignCollection<OrderLine> order_lines_fc;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "offlinedata_id")
    protected transient OfflineData offlineData;

    public Order() {}

    public Order(Builder builder) {
        super(builder);

        order_lines = builder.order_lines;
        target_delivery_date = builder.target_delivery_date;
        //remark = builder.remark;
        remark = "page=1/1";
        order_type_code = builder.order_type_code;
        serving_branch_id = builder.serving_branch_id;
        branch_id = builder.branch_id;
    }

    public Integer getBranch_id() {
        return branch_id;
    }

    public void setBranch_id(Integer branch_id) {
        this.branch_id = branch_id;
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

    public List<OrderLine> getOrder_lines() {
        refresh();
        return order_lines;
    }

    public void setOrder_lines(List<OrderLine> order_lines) {
        this.order_lines = order_lines;
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
        if(order_lines == null)
            order_lines = new ArrayList<>();
        order_lines.add(orderLine);
    }

    public void setOfflineData(OfflineData offlineData) {
        this.offlineData = offlineData;
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

    @Override
    public String toJSONString() {
        refresh();
        return super.toJSONString();
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        refresh();
        return super.toJSONObject();
    }

    @Override
    public boolean shouldPageRequest() {
        refresh();
        return order_lines.size() > MAX_ORDERLINES_PER_PAGE;
    }

    @Override
    public int getChildCount() {
        refresh();
        return SwableTools.computePagedRequestCount(order_lines.size(), MAX_ORDERLINES_PER_PAGE);
    }

    @Override
    public void refresh() {
        if(order_lines_fc != null && order_lines == null) {
            order_lines = new ArrayList<>(order_lines_fc);
        }
    }

    public static class Builder extends BaseTransactionTable3.Builder<Builder> {
        private String target_delivery_date; // current_date+2days
        private String remark;
        private String order_type_code; // purchase order
        private int serving_branch_id = 0;
        private Integer branch_id;
        private List<OrderLine> order_lines = new ArrayList<>();

        public Builder target_delivery_date(String date) {
            target_delivery_date = date;
            return this;
        }
        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }
        public Builder order_type_code(String order_type_code) {
            this.order_type_code = order_type_code;
            return this;
        }
        public Builder serving_branch_id(int serving_branch_id) {
            this.serving_branch_id = serving_branch_id;
            return this;
        }
        public Builder branch_id(Integer branch_id) {
            this.branch_id = branch_id;
            return this;
        }
        public Builder order_lines(List<OrderLine> order_lines) {
            this.order_lines = order_lines;
            return this;
        }

        public Builder addOrderLine(OrderLine orderLine) {
            if(order_lines == null)
                order_lines = new ArrayList<>();
            order_lines.add(orderLine);
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }

    public List<OrderLine> getOrderLineAt(int position) {
        refresh();

        List<OrderLine> list = new ArrayList<>();
        list.addAll(SwableTools.partition(position, order_lines, MAX_ORDERLINES_PER_PAGE));
        return list;
    }

    public Order getChildOrderAt(int position) throws JSONException {
        Order order = Order.fromJSONString(toJSONString());
        order.setOrder_lines(getOrderLineAt(position));
        //order.setReference(reference + "-" + (position + 1));
        order.setRemark("page=" + (position + 1) + "/" + getChildCount());
        return order;
    }

    public List<Order> getChildOrders() throws JSONException {
        List<Order> orderList = new ArrayList<>();
        for(int i = 0; i < getChildCount(); i++) {
            orderList.add(getChildOrderAt(i));
        }
        return orderList;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        /*if(shouldPageRequest()) {
            try {
                List<Order> orders = getChildOrders();
                for (Order child : orders)
                    child.insertTo(dbHelper);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }*/

        insertExtrasTo(dbHelper);
        try {
            dbHelper.insert(Order.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        refresh();
        if(order_lines != null) {
            BatchList<OrderLine> batchListIns = new BatchList<>(DatabaseOperation.INSERT, dbHelper);
            BatchList<OrderLine> batchListUpd = new BatchList<>(DatabaseOperation.UPDATE, dbHelper);
            for (OrderLine orderLine : order_lines) {
                orderLine.setOrder(this);
                if(orderLine.getId() == -1)
                    batchListIns.add(orderLine);
                else
                    batchListUpd.add(orderLine);
            }
            batchListIns.doOperation(OrderLine.class);
            batchListUpd.doOperation(OrderLine.class);
        }

        updateExtrasTo(dbHelper);
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        /*refresh();
        if(shouldPageRequest()) {
            try {
                List<Order> orders = getChildOrders();
                for (Order child : orders)
                    child.deleteTo(dbHelper);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }*/

        try {
            dbHelper.delete(Order.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        refresh();
        if(order_lines != null) {
            BatchList<OrderLine> batchList = new BatchList<>(DatabaseOperation.DELETE, dbHelper);
            batchList.addAll(order_lines);
            batchList.doOperation(OrderLine.class);
        }

        deleteExtrasTo(dbHelper);
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        /*refresh();
        if(shouldPageRequest()) {
            try {
                List<Order> orders = getChildOrders();
                for (Order child : orders)
                    child.updateTo(dbHelper);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }*/

        try {
            dbHelper.update(Order.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(order_lines != null) {
            BatchList<OrderLine> batchList = new BatchList<>(DatabaseOperation.UPDATE, dbHelper);
            for (OrderLine orderLine : order_lines) {
                orderLine.setOrder(this);
                batchList.add(orderLine);
            }
            batchList.doOperation(OrderLine.class);
        }

        updateExtrasTo(dbHelper);
    }

    @Override
    public void insertExtrasTo(ImonggoDBHelper2 dbHelper) {
        if(extras != null) {
            extras.setOrder(this);
            extras.setId(getClass().getName().toUpperCase(), id);
            extras.insertTo(dbHelper);
        }
    }

    @Override
    public void deleteExtrasTo(ImonggoDBHelper2 dbHelper) {
        if(extras != null)
            extras.deleteTo(dbHelper);
    }

    @Override
    public void updateExtrasTo(ImonggoDBHelper2 dbHelper) {
        if(extras != null) {
            String idstr = getClass().getName().toUpperCase() + "_" + id;
            if (idstr.equals(extras.getId()))
                extras.updateTo(dbHelper);
            else {
                extras.deleteTo(dbHelper);
                extras.setId(getClass().getName().toUpperCase(), id);
                extras.insertTo(dbHelper);
            }
        }
    }

    public static void fetchByReference(Context context, String branch_id, String reference, String order_type_code, Session session,
                                        VolleyRequestListener volleyRequestListener) {
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(HTTPRequests.sendGETJsonArrayRequest(context, session, volleyRequestListener, session.getServer(), Table.ORDERS,
                RequestType.API_CONTENT, String.format(ImonggoTools.generateParameter(Parameter.ORDER_TYPE, Parameter.BRANCH_ID, Parameter
                        .REFERENCE), order_type_code, branch_id, reference)));
    }
}