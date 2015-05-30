package net.nueca.imonggosdk.objects.base;

import android.util.Log;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.objects.User;

import java.util.ArrayList;

/**
 * Created by rhymart on 5/30/15.
 * imonggosdk (c)2015
 */
public class BatchList<T> extends ArrayList<T> {
    public <T> void batchOperations(ImonggoDBHelper dbHelper, DatabaseOperation databaseOperation) {
        if(size() == 0) {
            Log.e("BatchList Size", "Ooops! There's nothing to save.");
            return;
        }
        if(get(0) instanceof User)
            dbHelper.batchCreateOrUpdateUsers(this, databaseOperation);

    }
}
