package net.nueca.imonggosdk.objects.invoice;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.customer.Customer;

import java.sql.SQLException;

/**
 * Created by rhymart on 11/10/15.
 */
@DatabaseTable
public class PaymentTerms extends BaseTable {

    @DatabaseField
    private String code, name;
    @ForeignCollectionField
    private ForeignCollection<Customer> customers;

    public PaymentTerms() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ForeignCollection<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(ForeignCollection<Customer> customers) {
        this.customers = customers;
    }

    @Override
    public boolean equals(Object o) {
        return id == ((PaymentTerms)o).getId();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(PaymentTerms.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(PaymentTerms.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(PaymentTerms.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
