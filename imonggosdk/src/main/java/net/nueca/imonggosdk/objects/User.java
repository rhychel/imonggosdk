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
import net.nueca.imonggosdk.objects.base.Extras;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/12/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class User extends BaseTable {
    @DatabaseField
    private int home_branch_id = 0;
    @DatabaseField
    private String name, email, role_code, status;
    @DatabaseField
    private transient int sequenceNumber = 1;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "session_id")
    private transient Session session = null;
    @ForeignCollectionField
    private ForeignCollection<Extras> foreignCustomersExtras;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "route_plan_id")
    private RoutePlan routePlan;

    private transient boolean isSelected = false;// For what?

    public User() { }

    public int getHome_branch_id() {
        return home_branch_id;
    }

    public void setHome_branch_id(int home_branch_id) {
        this.home_branch_id = home_branch_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole_code() {
        return role_code;
    }

    public void setRole_code(String role_code) {
        this.role_code = role_code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public ForeignCollection<Extras> getForeignCustomersExtras() {
        return foreignCustomersExtras;
    }

    public void setForeignCustomersExtras(ForeignCollection<Extras> foreignCustomersExtras) {
        this.foreignCustomersExtras = foreignCustomersExtras;
    }

    public RoutePlan getRoutePlan() {
        return routePlan;
    }

    public void setRoutePlan(RoutePlan routePlan) {
        this.routePlan = routePlan;
    }

    @Override
    public String toString() {
        return "User{" +
                "home_branch_id=" + home_branch_id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role_code='" + role_code + '\'' +
                ", status='" + status + '\'' +
                ", sequenceNumber=" + sequenceNumber +
                ", session=" + session +
                ", isSelected=" + isSelected +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof User) && ((User)o).getId() == id;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(User.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(User.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(User.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
