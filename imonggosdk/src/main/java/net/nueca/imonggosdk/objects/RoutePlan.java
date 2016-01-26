package net.nueca.imonggosdk.objects;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.customer.Customer;

import java.sql.SQLException;

/**
 * Created by rhymart on 11/10/15.
 */
@Deprecated
@DatabaseTable
public class RoutePlan extends BaseTable {

    @DatabaseField
    private String code;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "user_id")
    private User user;
    @ForeignCollectionField
    private ForeignCollection<Customer> foreignCustomers; // sequence (per customer_id)

    public RoutePlan() { }

    public ForeignCollection<Customer> getForeignCustomers() {
        return foreignCustomers;
    }

    public void setForeignCustomers(ForeignCollection<Customer> foreignCustomers) {
        this.foreignCustomers = foreignCustomers;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "RoutePlan{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", user=" + user +
                ", foreignCustomers=" + foreignCustomers +
                '}';
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
