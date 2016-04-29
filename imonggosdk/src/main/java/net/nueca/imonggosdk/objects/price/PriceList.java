package net.nueca.imonggosdk.objects.price;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;

import java.sql.SQLException;

/**
 * Created by rhymart on 11/10/15.
 */
@DatabaseTable
public class PriceList extends BaseTable {

    @Expose
    @DatabaseField
    private String code;

    @Expose
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "branch_id")
    private Branch branch;

    @ForeignCollectionField
    private transient ForeignCollection<CustomerGroup> customerGroups; //can be null
    @ForeignCollectionField
    private transient ForeignCollection<Customer> customers; // can be null
    @ForeignCollectionField
    private transient ForeignCollection<Price> prices;

    @Expose
    @DatabaseField
    private String status;

    public PriceList() { }

    public PriceList(Builder builder) {
        this.code = builder.code;
        this.branch = builder.branch;
        this.status = builder.status;
        this.utc_created_at = builder.utc_created_at;
        this.utc_updated_at = builder.utc_updated_at;
        this.searchKey = builder.searchKey;
        this.extras = builder.extras;
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

    public ForeignCollection<CustomerGroup> getCustomerGroups() {
        return customerGroups;
    }

    public void setCustomerGroups(ForeignCollection<CustomerGroup> customerGroups) {
        this.customerGroups = customerGroups;
    }

    public ForeignCollection<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(ForeignCollection<Customer> customers) {
        this.customers = customers;
    }

    public void addCustomer(Customer customer) {
        this.customers.add(customer);
    }

    public ForeignCollection<Price> getPrices() {
        return prices;
    }

    public void setPrices(ForeignCollection<Price> prices) {
        this.prices = prices;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String toJSONString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }
    @Override
    public String toString() {
        return "PriceList(id=" + id + ")\n" + toJSONString();
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
            if(prices != null) {
                if(prices.size() != 0) {
                    Log.e(TAG, "deleting price details size with... " + prices.size());
                    for (Price price : prices)
                        price.deleteTo(dbHelper);
                } else {
                    Log.e(TAG, "price details is nul. skipping deleting ");
                }

            } else {
                Log.e(TAG, "price details is nul. skipping deleting ");
            }



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

    public static class Builder {
        protected String code;
        protected String status;
        protected Branch branch;
        protected String searchKey = "";
        protected Extras extras;
        protected String utc_created_at, utc_updated_at;

        public PriceList build() {
            return new PriceList(this);
        }
        public Builder code(String code) {
            this.code = code;
            return this;
        }
        public Builder status(String status) {
            this.status = status;
            return this;
        }
        public Builder branch(Branch branch) {
            this.branch = branch;
            return this;
        }
        public Builder extras(Extras extras) {
            this.extras = extras;
            return this;
        }
        public Builder searchKey(String searchKey) {
            this.searchKey = searchKey;
            return this;
        }

        public Builder utc_created_at(String utc_created_at) {
            this.utc_created_at = utc_created_at;
            return this;
        }

        public Builder utc_updated_at(String utc_updated_at) {
            this.utc_updated_at = utc_updated_at;
            return this;
        }

    }
}
