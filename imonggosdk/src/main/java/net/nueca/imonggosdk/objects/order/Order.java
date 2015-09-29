package net.nueca.imonggosdk.objects.order;

import com.google.gson.Gson;

import net.nueca.imonggosdk.objects.base.BaseTransaction;
import net.nueca.imonggosdk.swable.SwableTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 7/1/15.
 */
public class Order extends BaseTransaction {
    public static transient final int MAX_ORDERLINES_PER_PAGE = 50;

    private String target_delivery_date; // current_date + 2 days
    private String remark;
    private String order_type_code;
    private int serving_branch_id;
    private List<OrderLine> order_lines;

    public Order(Builder builder) {
        super(builder);

        order_lines = builder.order_lines;
        target_delivery_date = builder.target_delivery_date;
        //remark = builder.remark;
        remark = "page=1/1";
        order_type_code = builder.order_type_code;
        serving_branch_id = builder.serving_branch_id;
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
        return order_lines;
    }

    public void setOrder_lines(List<OrderLine> order_lines) {
        this.order_lines = order_lines;
    }

    public JSONArray getOrderLineJSONArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
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

    @Override
    public boolean shouldPageRequest() {
        return order_lines.size() > MAX_ORDERLINES_PER_PAGE;
    }

    @Override
    public int getChildCount() {
        return SwableTools.computePagedRequestCount(order_lines.size(), MAX_ORDERLINES_PER_PAGE);
    }

    public static class Builder extends BaseTransaction.Builder<Builder> {
        private String target_delivery_date = ""; // current_date+2days
        private String remark = "";
        private String order_type_code = "stock_request";
        private int serving_branch_id = 0;
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
        List<OrderLine> list = new ArrayList<>();
        list.addAll(SwableTools.partition(position,order_lines,MAX_ORDERLINES_PER_PAGE));
        return list;
    }

    public Order getChildOrderAt(int position) throws JSONException {
        Order order = Order.fromJSONString(toJSONString());
        order.setOrder_lines(getOrderLineAt(position));
        order.setReference(reference + "-" + (position + 1));
        order.setRemark("page=" + (position+1) + "/" + getChildCount());
        return order;
    }

    public List<Order> getChildOrders() throws JSONException {
        List<Order> orderList = new ArrayList<>();
        for(int i = 0; i < getChildCount(); i++) {
            orderList.add(getChildOrderAt(i));
        }
        return orderList;
    }
}
