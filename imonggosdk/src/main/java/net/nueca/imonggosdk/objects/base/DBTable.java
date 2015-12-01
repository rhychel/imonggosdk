package net.nueca.imonggosdk.objects.base;

import android.util.Log;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;

/**
 * Created by rhymart on 11/16/15.
 */
public abstract class DBTable {
    public static final String TAG = "DBTable";

    // NEW --------
    public abstract void insertTo(ImonggoDBHelper2 dbHelper);
    public abstract void deleteTo(ImonggoDBHelper2 dbHelper);
    public abstract void updateTo(ImonggoDBHelper2 dbHelper);
    public void dbOperation(ImonggoDBHelper2 dbHelper, DatabaseOperation databaseOperation) {
        if(databaseOperation == DatabaseOperation.INSERT) {
            //Log.e(TAG, "Inserting to database");
            insertTo(dbHelper);
        } else if(databaseOperation == DatabaseOperation.UPDATE) {
            //Log.e(TAG, "Updating to database");
            updateTo(dbHelper);
        } else if(databaseOperation == DatabaseOperation.DELETE) {
            //Log.e(TAG, "Deleting to database");
            deleteTo(dbHelper);
        }
    }
}
