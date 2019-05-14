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
@DatabaseTable
public class Inventory extends DBTable {

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "branch_id")
    private transient Branch branch;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "product_id")
    private transient Product product;
    @DatabaseField
    private double quantity = 0.0;
    @DatabaseField
    private String utc_updated_at = "", utc_created_at = "";

    public Inventory() { }

    public Inventory(Branch branch, Product product) {
        this.branch = branch;
        this.product = product;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getInventory() {
        Log.e("Inventory", quantity+"");
        String inventoryQty = String.valueOf(quantity);
        if(inventoryQty.contains(".")) {
            String[] values = inventoryQty.split(".");
            if(values.length > 0) {
                if (Integer.valueOf(values[1]) > 0)
                    return inventoryQty;
                return values[0];
            }
            return inventoryQty;
        }
        return inventoryQty;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUtc_updated_at() {
        return utc_updated_at;
    }

    public void setUtc_updated_at(String utc_updated_at) {
        this.utc_updated_at = utc_updated_at;
    }

    public String getUtc_created_at() {
        return utc_created_at;
    }

    public void setUtc_created_at(String utc_created_at) {
        this.utc_created_at = utc_created_at;
    }

    public void operationQuantity(double quantity, boolean shouldAdd) {
        if(shouldAdd)
            addQuantity(quantity);
        else
            subtractQuantity(quantity);
    }

    public void addQuantity(double addQuantity) {
        Log.e("Inventory", "addQuantity = "+addQuantity);
        this.quantity += addQuantity;
        Log.e("Inventory", "addQuantity -> quantity = "+addQuantity);
    }

    public void subtractQuantity(double subtractQuantity) {
        Log.e("Inventory", "subtractQuantity = "+subtractQuantity);
        this.quantity -= subtractQuantity;
        Log.e("Inventory", "subtractQuantity -> quantity = "+quantity);
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(Inventory.class, this);
            product.setInventory(this);
            product.updateTo(dbHelper);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(Inventory.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(Inventory.class, this);
            product.setInventory(this);
            product.updateTo(dbHelper);
            Log.e("Inventory", "update");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
