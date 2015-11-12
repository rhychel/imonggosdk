package net.nueca.imonggosdk.objects.associatives;

import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;

import java.sql.SQLException;

/**
 * Created by rhymart on 11/11/15.
 */
public class CustomerCustomerGroupAssoc {
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

    public void insertTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.CUSTOMER_CUSTOMER_GROUP, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.CUSTOMER_CUSTOMER_GROUP, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.CUSTOMER_CUSTOMER_GROUP, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void dbOperation(ImonggoDBHelper dbHelper, DatabaseOperation databaseOperation) {
        if(databaseOperation == DatabaseOperation.INSERT)
            insertTo(dbHelper);
        else if(databaseOperation == DatabaseOperation.UPDATE)
            updateTo(dbHelper);
        else if(databaseOperation == DatabaseOperation.DELETE)
            deleteTo(dbHelper);
    }

}
