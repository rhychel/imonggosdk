package net.nueca.imonggosdk.objects.branchentities;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.base.DBTable;

import java.sql.SQLException;

/**
 * Created by rhymart on 12/16/15.
 */
@Deprecated
@DatabaseTable
public class BranchProduct extends DBTable {

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "product_id", uniqueCombo = true)
    private transient Product product;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "branch_id", uniqueCombo = true)
    private transient Branch branch;
    @ForeignCollectionField
    private ForeignCollection<BranchUnit> branchUnits;

    @Expose
    @DatabaseField
    private int branch_product_id;

    @DatabaseField
    private transient boolean isBaseUnitSellable = false;

    @Expose
    @DatabaseField
    private String name, description;
    @Expose
    @DatabaseField
    private double retail_price = 0.0;
    @Expose
    @DatabaseField
    private double unit_retail_price = 0.0;

    public BranchProduct() { }

    public BranchProduct(Product product, Branch branch) {
        this.product = product;
        this.branch = branch;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public boolean isBaseUnitSellable() {
        return isBaseUnitSellable;
    }

    public void setIsBaseUnitSellable(boolean isBaseUnitSellable) {
        this.isBaseUnitSellable = isBaseUnitSellable;
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

    public double getRetail_price() {
        return retail_price;
    }

    public void setRetail_price(double retail_price) {
        this.retail_price = retail_price;
    }

    public double getUnit_retail_price() {
        return unit_retail_price;
    }

    public void setUnit_retail_price(double unit_retail_price) {
        this.unit_retail_price = unit_retail_price;
    }

    public ForeignCollection<BranchUnit> getBranchUnits() {
        return branchUnits;
    }

    public void setBranchUnits(ForeignCollection<BranchUnit> branchUnits) {
        this.branchUnits = branchUnits;
    }

    public int getBranch_product_id() {
        return branch_product_id;
    }

    public void setBranch_product_id(int branch_product_id) {
        this.branch_product_id = branch_product_id;
    }

    public String toJSONString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(BranchProduct.class, this);
            Log.e(TAG, "inserted to database");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(BranchProduct.class, this);
            Log.e(TAG, "deleted to database");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(BranchProduct.class, this);
            Log.e(TAG, "updated to database");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
