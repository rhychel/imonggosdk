package net.nueca.imonggosdk.objects;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.DBTable;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class TaxRate extends DBTable {

    @DatabaseField(id=true, columnName = "tax_rate_id")
    protected int id = -1;
    @DatabaseField
    private String status, name;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "branch_id")
    private transient Branch branch;
    @DatabaseField
    private double value;
    @DatabaseField
    private int tax_rate_type = 0;
    @DatabaseField
    protected String utc_created_at, utc_updated_at;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "tax_setting_id")
    private transient TaxSetting taxSetting;

    public TaxRate() { }

    public TaxRate(Branch branch) {
        this.branch = branch;
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

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getTax_rate_type() {
        return tax_rate_type;
    }

    public void setTax_rate_type(int tax_rate_type) {
        this.tax_rate_type = tax_rate_type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaxSetting getTaxSetting() {
        return taxSetting;
    }

    public void setTaxSetting(TaxSetting taxSetting) {
        this.taxSetting = taxSetting;
    }

    public String getUtc_created_at() {
        return utc_created_at;
    }

    public void setUtc_created_at(String utc_created_at) {
        this.utc_created_at = utc_created_at;
    }

    public String getUtc_updated_at() {
        return utc_updated_at;
    }

    public void setUtc_updated_at(String utc_updated_at) {
        this.utc_updated_at = utc_updated_at;
    }

    @Override
    public String toString() {
        return "TaxRate{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", name='" + name + '\'' +
                ", branch=" + branch +
                ", value=" + value +
                ", tax_rate_type=" + tax_rate_type +
                ", utc_created_at='" + utc_created_at + '\'' +
                ", utc_updated_at='" + utc_updated_at + '\'' +
                ", taxSetting=" + taxSetting +
                '}';
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(TaxRate.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(TaxRate.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(TaxRate.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
