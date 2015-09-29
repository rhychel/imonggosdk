package net.nueca.imonggosdk.objects;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/14/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class LastUpdatedAt {
    public static String TAG = "LastUpdatedAt";

    @DatabaseField(id = true)
    private transient String tableName;
    @DatabaseField
    private String last_updated_at;

    public LastUpdatedAt() { }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getLast_updated_at() {
        return last_updated_at;
    }

    public void setLast_updated_at(String last_updated_at) {
        this.last_updated_at = last_updated_at;
    }

    public void insertTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.LAST_UPDATED_AT, DatabaseOperation.INSERT);
            Log.e(TAG, "INSERTING to LastUpdatedAt tables");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void deleteTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.LAST_UPDATED_AT, DatabaseOperation.DELETE);
            Log.e(TAG, "Deleting to LastUpdatedAt tables");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void updateTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.LAST_UPDATED_AT, DatabaseOperation.UPDATE);
            Log.e(TAG, "UPDATING to LastUpdatedAt tables");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "LastUpdatedAt{" +
                "tableName='" + tableName + '\'' +
                ", last_updated_at='" + last_updated_at + '\'' +
                '}';
    }
}
