package net.nueca.concessioengine.adapters.base;

import android.content.Context;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;

import java.util.List;

/**
 * Created by gama on 08/12/2015.
 */
public abstract class BaseSalesProductRecyclerAdapter<T extends BaseProductsRecyclerAdapter.ViewHolder> extends BaseProductsRecyclerAdapter<T> {
    protected Customer customer;
    protected CustomerGroup customerGroup;
    protected Branch branch;

    public BaseSalesProductRecyclerAdapter(Context context) {
        super(context);
    }

    public BaseSalesProductRecyclerAdapter(Context context, List<Product> productsList) {
        super(context, productsList);
    }

    public BaseSalesProductRecyclerAdapter(Context context, ImonggoDBHelper2 dbHelper, List<Product> productsList) {
        super(context, dbHelper, productsList);
    }

    public BaseSalesProductRecyclerAdapter(Context context, ImonggoDBHelper2 dbHelper, List<Product> productsList,
                                           Customer customer, CustomerGroup customerGroup, Branch branch) {
        super(context, dbHelper, productsList);
        this.customer = customer;
        this.customerGroup = customerGroup;
        this.branch = branch;
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

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public ImonggoDBHelper2 getHelper() {
        return ProductsAdapterHelper.getDbHelper();
    }
}
