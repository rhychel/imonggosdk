package net.nueca.imonggosdk.objects.base;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;

/**
 * Created by gama on 7/2/15.
 */
public abstract class BaseTable2 extends DBTable {

    @DatabaseField(generatedId = true)
    protected int id = -1;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Deprecated
    public abstract void insertTo(ImonggoDBHelper dbHelper);
    @Deprecated
    public abstract void deleteTo(ImonggoDBHelper dbHelper);
    @Deprecated
    public abstract void updateTo(ImonggoDBHelper dbHelper);
    @Deprecated
    public void dbOperation(ImonggoDBHelper dbHelper, DatabaseOperation databaseOperation) {
        if(databaseOperation == DatabaseOperation.INSERT) {
            Log.e(TAG, "Inserting to database");
            insertTo(dbHelper);
        } else if(databaseOperation == DatabaseOperation.UPDATE) {
            Log.e(TAG, "Updating to database");
            updateTo(dbHelper);
        } else if(databaseOperation == DatabaseOperation.DELETE) {
            Log.e(TAG, "Deleting to database");
            deleteTo(dbHelper);
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {

    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {

    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {

    }
}
