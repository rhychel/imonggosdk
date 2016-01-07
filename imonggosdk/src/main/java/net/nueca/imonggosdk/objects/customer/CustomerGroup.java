package net.nueca.imonggosdk.objects.customer;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.associatives.CustomerCustomerGroupAssoc;
import net.nueca.imonggosdk.objects.price.PriceList;
import net.nueca.imonggosdk.objects.base.BaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 11/10/15.
 */
@DatabaseTable
public class CustomerGroup extends BaseTable {

    @Expose
    @DatabaseField
    private String name;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "price_list_id")
    private transient PriceList priceList;
    @Expose
    @DatabaseField
    private String discount_text; // unused yet
    @Expose
    @DatabaseField
    private String status;
    @Expose
    @DatabaseField
    private String code;

    public CustomerGroup() { }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PriceList getPriceList() {
        return priceList;
    }

    public void setPriceList(PriceList priceList) {
        this.priceList = priceList;
    }

    public String getDiscount_text() {
        return discount_text;
    }

    public void setDiscount_text(String discount_text) {
        this.discount_text = discount_text;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Customer> getCustomers(ImonggoDBHelper2 dbHelper) throws SQLException {
        List<CustomerCustomerGroupAssoc> assocs = dbHelper.fetchObjects(CustomerCustomerGroupAssoc.class).queryBuilder().where().eq
                (CustomerCustomerGroupAssoc.CUSTOMER_GROUP_ID_FIELD_NAME, this).query();

        if(assocs == null || assocs.size() == 0)
            return new ArrayList<>();

        List<Customer> customers = new ArrayList<>();
        for(CustomerCustomerGroupAssoc assoc : assocs)
            customers.add(assoc.getCustomer());
        return customers;
    }

    @Override
    public String toString() {
        return "CustomerGroup{" +
                "name='" + name + '\'' +
                ", priceList=" + priceList +
                ", discount_text='" + discount_text + '\'' +
                ", status='" + status + '\'' +
                ", code='" + code + '\'' +
                '}';
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(CustomerGroup.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(CustomerGroup.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(CustomerGroup.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
