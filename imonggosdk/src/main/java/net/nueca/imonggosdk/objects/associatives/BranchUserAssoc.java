package net.nueca.imonggosdk.objects.associatives;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.base.DBTable;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/12/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class BranchUserAssoc extends DBTable {
    public static final String BRANCH_ID_FIELD_NAME = "branch_id";
    public static final String USER_ID_FIELD_NAME = "user_id";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = BRANCH_ID_FIELD_NAME)
    private Branch branch;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = USER_ID_FIELD_NAME)
    private User user;

    public BranchUserAssoc() { }

    public BranchUserAssoc(Branch branch, User user) {
        this.branch = branch;
        this.user = user;
    }

    public Branch getBranch() {
        return branch;
    }

    public User getUser() {
        return user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "BranchUserAssoc{" +
                "id=" + id +
                ", branch=" + branch.getName() +
                ", user=" + user.getName() +
                '}';
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(BranchUserAssoc.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(BranchUserAssoc.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(BranchUserAssoc.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
