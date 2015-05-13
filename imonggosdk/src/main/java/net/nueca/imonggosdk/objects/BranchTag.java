package net.nueca.imonggosdk.objects;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by rhymart on 5/12/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class BranchTag {

    @DatabaseField(generatedId=true)
    private int id;

    @DatabaseField
    private String tag = "";

    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "branch_id")
    private Branch branch;

    public BranchTag() { }

    public BranchTag(String tag) {
        this.tag = tag;
    }

    public BranchTag(String tag, Branch branch) {
        this.tag = tag;
        this.branch = branch;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    @Override
    public boolean equals(Object o) {
        return tag == ((BranchTag)o).getTag();
    }

    @Override
    public String toString() {
        return tag;
    }
}
