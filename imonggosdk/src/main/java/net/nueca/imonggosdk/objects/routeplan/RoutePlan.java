package net.nueca.imonggosdk.objects.routeplan;

import android.util.Log;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.base.BaseTable;

import java.sql.SQLException;

/**
 * Created by rhymart on 12/18/15.
 * 160.77+(658.11-499) = 319.88
 */
@DatabaseTable
public class RoutePlan extends BaseTable {

    @DatabaseField
    private String route_code;
    @DatabaseField
    private String status;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "user_id")
    private transient User user;
    @ForeignCollectionField(orderColumnName = "sequence")
    private transient ForeignCollection<RoutePlanDetail> routePlanDetails;

    public RoutePlan() {
    }

    public RoutePlan(User user) {
        this.user = user;
    }

    public String getRoute_code() {
        return route_code;
    }

    public void setRoute_code(String route_code) {
        this.route_code = route_code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ForeignCollection<RoutePlanDetail> getRoutePlanDetails() {
        return routePlanDetails;
    }

    public void setRoutePlanDetails(ForeignCollection<RoutePlanDetail> routePlanDetails) {
        this.routePlanDetails = routePlanDetails;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(RoutePlan.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(RoutePlan.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(RoutePlan.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
