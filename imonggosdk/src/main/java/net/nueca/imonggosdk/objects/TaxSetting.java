package net.nueca.imonggosdk.objects;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
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
public class TaxSetting extends DBTable {

    @DatabaseField(generatedId = true)
    private transient int id;
    @DatabaseField
    private boolean compute_tax = false;
    @DatabaseField
    private boolean tax_inclusive = true;
    @ForeignCollectionField
    private transient ForeignCollection<TaxRate> tax_rates;

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

    public ForeignCollection<TaxRate> getTax_rates() {
        return tax_rates;
    }

    public void setTax_rates(ForeignCollection<TaxRate> tax_rates) {
        this.tax_rates = tax_rates;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(TaxSetting.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(TaxSetting.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(TaxSetting.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}