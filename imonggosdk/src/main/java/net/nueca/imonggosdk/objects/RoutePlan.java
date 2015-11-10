package net.nueca.imonggosdk.objects;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.objects.customer.Customer;

/**
 * Created by rhymart on 11/10/15.
 */
@DatabaseTable
public class RoutePlan {

    @DatabaseField
    private Branch branch;
    @ForeignCollectionField
    private ForeignCollection<Customer> foreignCustomers;

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public ForeignCollection<Customer> getForeignCustomers() {
        return foreignCustomers;
    }

    public void setForeignCustomers(ForeignCollection<Customer> foreignCustomers) {
        this.foreignCustomers = foreignCustomers;
    }
}
