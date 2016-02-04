package net.nueca.imonggosdk.objects;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.base.BaseTable;

import java.sql.SQLException;

/**
 *
 * BranchProduct.id = branch_product.branch_price_id
 * Created by rhymartmanchus on 03/02/2016.
 */
@DatabaseTable
public class BranchProduct extends BaseTable {

    @DatabaseField
    private double unit_retail_price;
    @DatabaseField
    private String name, description;
    @DatabaseField
    private double retail_price;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "product_id")
    private transient Product product;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "unit_id")
    private transient Unit unit;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "branch_id")
    private transient Branch branch;

    public BranchProduct() {
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public double getUnit_retail_price() {
        return unit_retail_price;
    }

    public void setUnit_retail_price(double unit_retail_price) {
        this.unit_retail_price = unit_retail_price;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
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
