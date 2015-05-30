package net.nueca.imonggosdk.objects;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable;

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

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof User) && ((User)o).getId() == id;
    }

    @Override
    public void insert(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.USERS, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(ImonggoDBHelper dbHelper) {

    }

    @Override
    public void update(ImonggoDBHelper dbHelper) {

    }

}
