package net.nueca.imonggosdk.objects.branchentities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.base.DBTable;

import java.sql.SQLException;

/**
 * Created by rhymart on 12/16/15.
 */
@Deprecated
@DatabaseTable
public class BranchUnit extends DBTable {

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "unit_id", uniqueCombo = true)
    private transient Unit unit;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "branch_id", uniqueCombo = true)
    private transient Branch branch;
    @DatabaseField
    private double retail_price = 0.0;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "bp_id", foreignColumnName = "branch_product_id")
    private BranchProduct branchProduct;

    public BranchUnit() { }

    public BranchUnit(Unit unit, Branch branch) {
        this.unit = unit;
        this.branch = branch;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public double getRetail_price() {
        return retail_price;
    }

    public void setRetail_price(double retail_price) {
        this.retail_price = retail_price;
    }

    public BranchProduct getBranchProduct() {
        return branchProduct;
    }

    public void setBranchProduct(BranchProduct branchProduct) {
        this.branchProduct = branchProduct;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(BranchUnit.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(BranchUnit.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(BranchUnit.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
