package net.nueca.imonggosdk.objects.deprecated;

import android.util.Log;

import com.google.gson.Gson;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.base.BaseTable2;

import java.sql.SQLException;

/**
 * Created by rhymart on 8/24/15.
 * imonggosdk2 (c)2015
 */
@Deprecated
@DatabaseTable
public class Extras_2 extends BaseTable2 {

    @DatabaseField
    private String batch_maintained = "";

    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "product_id")
    private transient Product product;

    public Extras_2() {}

    public Extras_2(String batch_maintained, Product product) {
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

//    @Override
//    public void insertTo(ImonggoDBHelper dbHelper) {
//        try {
//            dbHelper.dbOperations(this, Table.PRODUCT_EXTRAS, DatabaseOperation.INSERT);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void deleteTo(ImonggoDBHelper dbHelper) {
//        try {
//            dbHelper.dbOperations(this, Table.PRODUCT_EXTRAS, DatabaseOperation.DELETE);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void updateTo(ImonggoDBHelper dbHelper) {
//        try {
//            dbHelper.dbOperations(this, Table.PRODUCT_EXTRAS, DatabaseOperation.UPDATE);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }


    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        Log.e("Ooops!--insertTo", "Please migrate to the new DBHelper");
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        Log.e("Ooops!--deleteTo", "Please migrate to the new DBHelper");
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        Log.e("Ooops!--updateTo", "Please migrate to the new DBHelper");
    }
}
