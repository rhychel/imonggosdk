package net.nueca.imonggosdk.objects.order;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable2;
import net.nueca.imonggosdk.objects.base.BaseTransactionLine;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Created by gama on 7/1/15.
 */
public class OrderLine extends BaseTransactionLine {
    @Expose
    @DatabaseField
    private int line_no = 0;

    @Expose
    @DatabaseField
    private String brand = null;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "order_id")
    protected transient Order order;

    public OrderLine() {}

    public OrderLine(Builder builder) {
        super(builder);
        line_no = builder.line_no;
        brand = builder.brand;
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
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        if(unit_retail_price == null)
            unit_retail_price = retail_price;

        insertExtrasTo(dbHelper);
        try {
            dbHelper.insert(OrderLine.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateExtrasTo(dbHelper);
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(OrderLine.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        deleteExtrasTo(dbHelper);
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(OrderLine.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        updateExtrasTo(dbHelper);
    }

    @Override
    public void insertExtrasTo(ImonggoDBHelper2 dbHelper) {
        if(extras != null) {
            extras.setOrderLine(this);
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

    public static class Builder extends BaseTransactionLine.Builder<Builder> {
        private int line_no;
        private String brand = null;

        public Builder line_no(int line_no) {
            this.line_no = line_no;
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
