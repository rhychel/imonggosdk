package net.nueca.imonggosdk.objects.associatives;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.User;

/**
 * Created by rhymart on 5/12/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class BranchUserAssoc {
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
}
