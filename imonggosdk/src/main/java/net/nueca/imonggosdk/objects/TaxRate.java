package net.nueca.imonggosdk.objects;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class TaxRate extends BaseTable {

    @DatabaseField(id=true, columnName = "tax_rate_id")
    protected int id = -1;
    @DatabaseField
    private String status, name;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "branch_id")
    private Branch branch;
    @DatabaseField
    private double value;
    @DatabaseField
    private int tax_rate_type = 0;

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
}
