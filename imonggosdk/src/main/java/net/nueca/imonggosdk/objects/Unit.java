package net.nueca.imonggosdk.objects;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.base.Extras;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class Unit extends BaseTable implements Extras.DoOperationsForExtras {

    @DatabaseField
    private String product_stock_no, status, name, barcode;
    @DatabaseField
    private double cost, quantity, retail_price;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "product_id")
    private transient Product product;

    @ForeignCollectionField
    private transient ForeignCollection<BranchPrice> branchPrices;

    @ForeignCollectionField // need?
    private transient ForeignCollection<Price> prices;

    @DatabaseField
    private boolean is_default_ordering_unit = false;
    @DatabaseField
    private String discount_text;

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

    public boolean is_default_ordering_unit() {
        return is_default_ordering_unit;
    }

    public void setIs_default_ordering_unit(boolean is_default_ordering_unit) {
        this.is_default_ordering_unit = is_default_ordering_unit;
    }

    public String getDiscount_text() {
        return discount_text;
    }

    public void setDiscount_text(String discount_text) {
        this.discount_text = discount_text;
    }

    public ForeignCollection<Price> getPrices() {
        return prices;
    }

    public void setPrices(ForeignCollection<Price> prices) {
        this.prices = prices;
    }

    public ForeignCollection<BranchPrice> getBranchPrices() {
        return branchPrices;
    }

    public void setBranchPrices(ForeignCollection<BranchPrice> branchPrices) {
        this.branchPrices = branchPrices;
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
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            insertExtrasTo(dbHelper);
            dbHelper.insert(Unit.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            deleteExtrasTo(dbHelper);
            dbHelper.delete(Unit.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            updateExtrasTo(dbHelper);
            dbHelper.update(Unit.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insertExtrasTo(ImonggoDBHelper2 dbHelper) {
        extras.setUnit(this);
        extras.setId(Unit.class.getName().toUpperCase(), id);
        extras.insertTo(dbHelper);
    }

    @Override
    public void deleteExtrasTo(ImonggoDBHelper2 dbHelper) {
        extras.deleteTo(dbHelper);
    }

    @Override
    public void updateExtrasTo(ImonggoDBHelper2 dbHelper) {
        extras.updateTo(dbHelper);
    }
}
