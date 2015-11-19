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
import net.nueca.imonggosdk.objects.price.PriceList;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/12/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class Branch extends BaseTable {

    @DatabaseField
    private int subscription_type;
    @DatabaseField
    private String name, city, zipcode, tin, country, street, state, site_type, status = "A";
    @ForeignCollectionField
    private transient ForeignCollection<BranchTag> branchTags;
    @ForeignCollectionField
    private transient ForeignCollection<TaxRate> taxRates;
    @ForeignCollectionField
    private transient ForeignCollection<PriceList> priceLists;
    @ForeignCollectionField
    private transient ForeignCollection<RoutePlan> routePlans;
    @ForeignCollectionField
    private transient ForeignCollection<BranchPrice> branchPrices;

    public Branch() { }

    public int getSubscription_type() {
        return subscription_type;
    }

    public void setSubscription_type(int subscription_type) {
        this.subscription_type = subscription_type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getTin() {
        return tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSite_type() {
        return site_type;
    }

    public void setSite_type(String site_type) {
        this.site_type = site_type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ForeignCollection<BranchTag> getBranchTags() {
        return branchTags;
    }

    public void setBranchTags(ForeignCollection<BranchTag> branchTags) {
        this.branchTags = branchTags;
    }

    public ForeignCollection<TaxRate> getTaxRates() {
        return taxRates;
    }

    public void setTaxRates(ForeignCollection<TaxRate> taxRates) {
        this.taxRates = taxRates;
    }

    public ForeignCollection<PriceList> getPriceLists() {
        return priceLists;
    }

    public void setPriceLists(ForeignCollection<PriceList> priceLists) {
        this.priceLists = priceLists;
    }

    public ForeignCollection<RoutePlan> getRoutePlans() {
        return routePlans;
    }

    public void setRoutePlans(ForeignCollection<RoutePlan> routePlans) {
        this.routePlans = routePlans;
    }

    public ForeignCollection<BranchPrice> getBranchPrices() {
        return branchPrices;
    }

    public void setBranchPrices(ForeignCollection<BranchPrice> branchPrices) {
        this.branchPrices = branchPrices;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Branch) && ((Branch)o).getId() == id;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(Branch.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(Branch.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(Branch.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
