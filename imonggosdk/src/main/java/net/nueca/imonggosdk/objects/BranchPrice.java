package net.nueca.imonggosdk.objects;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.DBTable;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
@Deprecated
@DatabaseTable
public class BranchPrice extends DBTable {
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "branch_id", uniqueCombo = true)
    private transient Branch branch;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "product_id", uniqueCombo = true)
    private transient Product product;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "unit_id", uniqueCombo = true)
    private transient Unit unit;
    @DatabaseField
    private double retail_price = 0.0;
    @DatabaseField
    protected String utc_created_at, utc_updated_at;
    @DatabaseField
    private String name, description;
    @DatabaseField
    private double unit_retail_price = 0.0;

    public BranchPrice() { }

    public BranchPrice(Branch branch, Product product) {
        this.branch = branch;
        this.product = product;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getRetail_price() {
        return retail_price;
    }

    public void setRetail_price(double retail_price) {
        this.retail_price = retail_price;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getUtc_created_at() {
        return utc_created_at;
    }

    public void setUtc_created_at(String utc_created_at) {
        this.utc_created_at = utc_created_at;
    }

    public String getUtc_updated_at() {
        return utc_updated_at;
    }

    public void setUtc_updated_at(String utc_updated_at) {
        this.utc_updated_at = utc_updated_at;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getUnit_retail_price() {
        return unit_retail_price;
    }

    public void setUnit_retail_price(double unit_retail_price) {
        this.unit_retail_price = unit_retail_price;
    }

    @Override
    public String toString() {
        return "BranchPrice{" +
                "branch=" + branch +
                ", product=" + product +
                ", retail_price=" + retail_price +
                ", utc_created_at='" + utc_created_at + '\'' +
                ", utc_updated_at='" + utc_updated_at + '\'' +
                '}';
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(BranchPrice.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(BranchPrice.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(BranchPrice.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
