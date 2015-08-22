package net.nueca.imonggosdk.objects;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class Unit extends BaseTable {

    @DatabaseField
    private String product_stock_no, status, name, barcode;
    @DatabaseField
    private double cost, quantity, retail_price;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "product_id")
    private transient Product product;

    public Unit() { }

    public Unit(Product product) {
        this.product = product;
    }

    public String getProduct_stock_no() {
        return product_stock_no;
    }

    public void setProduct_stock_no(String product_stock_no) {
        this.product_stock_no = product_stock_no;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getRetail_price() {
        return retail_price;
    }

    public void setRetail_price(double retail_price) {
        this.retail_price = retail_price;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public boolean equals(Object o) {
        return id == ((Unit)o).getId();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void insertTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.UNITS, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.UNITS, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.UNITS, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
