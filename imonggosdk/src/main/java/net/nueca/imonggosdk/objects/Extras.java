package net.nueca.imonggosdk.objects;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable2;

import java.sql.SQLException;

/**
 * Created by rhymart on 8/24/15.
 * imonggosdk2 (c)2015
 */
@DatabaseTable
public class Extras extends BaseTable2 {

    @DatabaseField
    private String batch_maintained = "";

    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "product_id")
    private transient Product product;

    public Extras() {}

    public Extras(String batch_maintained, Product product) {
        this.batch_maintained = batch_maintained;
        this.product = product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public boolean isBatch_maintained() {
        return batch_maintained.equals("true");
    }

    public void setBatch_maintained(String batch_maintained) {
        this.batch_maintained = batch_maintained;
    }

    @Override
    public void insertTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.PRODUCT_EXTRAS, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.PRODUCT_EXTRAS, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.PRODUCT_EXTRAS, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
