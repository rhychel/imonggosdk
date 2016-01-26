package net.nueca.imonggosdk.objects.associatives;

import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.DBTable;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;

import java.sql.SQLException;

/**
 * Created by rhymart on 11/11/15.
 */
public class CustomerCustomerGroupAssoc extends DBTable {
    public static final String CUSTOMER_ID_FIELD_NAME = "customer_id";
    public static final String CUSTOMER_GROUP_ID_FIELD_NAME = "customer_group_id";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = CUSTOMER_ID_FIELD_NAME)
    private Customer customer;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = CUSTOMER_GROUP_ID_FIELD_NAME)
    private CustomerGroup customerGroup;

    public CustomerCustomerGroupAssoc() {
    }

    public CustomerCustomerGroupAssoc(Customer customer, CustomerGroup customerGroup) {
        this.customer = customer;
        this.customerGroup = customerGroup;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public CustomerGroup getCustomerGroup() {
        return customerGroup;
    }

    public void setCustomerGroup(CustomerGroup customerGroup) {
        this.customerGroup = customerGroup;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(CustomerCustomerGroupAssoc.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(CustomerCustomerGroupAssoc.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(CustomerCustomerGroupAssoc.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "CustomerCustomerGroupAssoc(id="+id+") = {" +
                "\"customer\" : " + (customer == null? "null" : customer.toJSONString()) + ", " +
                "\"customer_group\" : " + (customerGroup == null? "null" : customerGroup.toJSONString()) +
                "}";
    }
}
