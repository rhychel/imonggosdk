package net.nueca.imonggosdk.objects;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class TaxSetting {

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private boolean compute_tax = false;
    @DatabaseField
    private boolean tax_inclusive = true;
    @ForeignCollectionField
    private ForeignCollection<TaxRate> taxRates;

    public TaxSetting() { }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isCompute_tax() {
        return compute_tax;
    }

    public void setCompute_tax(boolean compute_tax) {
        this.compute_tax = compute_tax;
    }

    public boolean isTax_inclusive() {
        return tax_inclusive;
    }

    public void setTax_inclusive(boolean tax_inclusive) {
        this.tax_inclusive = tax_inclusive;
    }

    public ForeignCollection<TaxRate> getTaxRates() {
        return taxRates;
    }

    public void setTaxRates(ForeignCollection<TaxRate> taxRates) {
        this.taxRates = taxRates;
    }
}
