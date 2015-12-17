package net.nueca.imonggosdk.objects.branchentities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.base.DBTable;

import java.sql.SQLException;

/**
 * Created by rhymart on 12/16/15.
 */
@DatabaseTable
public class BranchProduct extends DBTable {

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "product_id", uniqueCombo = true)
    private transient Product product;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "branch_id", uniqueCombo = true)
    private transient Branch branch;

    @DatabaseField
    private transient boolean isBaseUnitSellable = false;
    @DatabaseField
    private String name, description;
    @DatabaseField
    private double retail_price = 0.0;
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

    @Override
    public String toString() {
        return "BranchProduct{" +
                "product=" + product +
                ", branch=" + branch +
                ", isBaseUnitSellable=" + isBaseUnitSellable +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", retail_price=" + retail_price +
                ", unit_retail_price=" + unit_retail_price +
                '}';
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(BranchProduct.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(BranchProduct.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(BranchProduct.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
