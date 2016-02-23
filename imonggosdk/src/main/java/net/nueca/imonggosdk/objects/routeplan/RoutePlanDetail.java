package net.nueca.imonggosdk.objects.routeplan;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.base.BaseTable2;
import net.nueca.imonggosdk.objects.base.DBTable;
import net.nueca.imonggosdk.objects.customer.Customer;

import java.sql.SQLException;

/**
 * Created by rhymart on 12/18/15.
 */
@DatabaseTable
public class RoutePlanDetail extends BaseTable2 {
    @DatabaseField
    private String frequency;
    @DatabaseField
    private String route_day;
    @DatabaseField
    private int sequence;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "route_plan_id", uniqueCombo = true)
    private transient RoutePlan routePlan;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "customer_id", uniqueCombo = true)
    private Customer customer;

    public RoutePlanDetail() { }

    public RoutePlanDetail(RoutePlan routePlan, Customer customer) {
        this.routePlan = routePlan;
        this.customer = customer;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getRoute_day() {
        return route_day;
    }

    public void setRoute_day(String route_day) {
        this.route_day = route_day;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public RoutePlan getRoutePlan() {
        return routePlan;
    }

    public void setRoutePlan(RoutePlan routePlan) {
        this.routePlan = routePlan;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @Override
    public String toString() {
        return "RoutePlanDetail{" +
                "frequency='" + frequency + '\'' +
                ", route_day='" + route_day + '\'' +
                ", sequence=" + sequence +
                '}';
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(RoutePlanDetail.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(RoutePlanDetail.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(RoutePlanDetail.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
