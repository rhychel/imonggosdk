package net.nueca.imonggosdk.objects;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.base.DBTable;
import net.nueca.imonggosdk.objects.invoice.SalesPromotion;

import java.sql.SQLException;

/**
 * Created by Jn on 01/12/15.
 */

@DatabaseTable
public class SalesPushSettings extends DBTable {

    @DatabaseField(generatedId = true, columnName = "id")
    int id;

    @DatabaseField
    private int enable_threshold, threshold, return_trigger;

    @DatabaseField
    private String offline_message = "", encouragement_message = "", trigger = "", congratulation_message = "";

    @DatabaseField(foreign = true, columnName = "sales_push")
    private SalesPromotion salesPromotion;

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(SalesPushSettings.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(SalesPushSettings.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(SalesPushSettings.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
