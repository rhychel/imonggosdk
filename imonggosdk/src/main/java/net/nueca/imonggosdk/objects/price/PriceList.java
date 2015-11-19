package net.nueca.imonggosdk.objects.price;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;

import java.sql.SQLException;

/**
 * Created by rhymart on 11/10/15.
 */
@DatabaseTable
public class PriceList extends BaseTable {

    @DatabaseField
    private String code;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "branch_id")
    private Branch branch;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "customer_group_id")
    private CustomerGroup customerGroup; //can be null
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "customer_id")
    private Customer customer; // can be null
    @ForeignCollectionField
    private ForeignCollection<Price> foreignPrices;
    @DatabaseField
    private String status;
    @DatabaseField
    private String discount_text; // product_discount,company_discount

    public PriceList() { }

    public String getDiscount_text() {
        return discount_text;
    }

    public void setDiscount_text(String discount_text) {
        this.discount_text = discount_text;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public CustomerGroup getCustomerGroup() {
        return customerGroup;
    }

    public void setCustomerGroup(CustomerGroup customerGroup) {
        this.customerGroup = customerGroup;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public ForeignCollection<Price> getForeignPrices() {
        return foreignPrices;
    }

    public void setForeignPrices(ForeignCollection<Price> foreignPrices) {
        this.foreignPrices = foreignPrices;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(PriceList.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(PriceList.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(PriceList.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
