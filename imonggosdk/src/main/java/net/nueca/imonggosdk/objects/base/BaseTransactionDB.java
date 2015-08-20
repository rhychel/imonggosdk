package net.nueca.imonggosdk.objects.base;

import android.content.Context;

import com.google.gson.Gson;
import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.tools.ReferenceNumberTool;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gama on 7/21/15.
 */
public abstract class BaseTransactionDB extends BaseTransaction {

    @DatabaseField(id=true)
    protected int id = -1;

    @DatabaseField
    protected String reference;

    public BaseTransactionDB(Builder builder) {
        super(builder);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public void setReference(String reference) {
        this.reference = reference;
    }

    protected static class Builder extends BaseTransaction.Builder<Builder> {
        protected int id;

        public Builder id(int id) {
            this.id = id;
            return this;
        }
    }

    public abstract void insertTo(ImonggoDBHelper dbHelper);
    public abstract void deleteTo(ImonggoDBHelper dbHelper);
    public abstract void updateTo(ImonggoDBHelper dbHelper);
    public void dbOperation(ImonggoDBHelper dbHelper, DatabaseOperation databaseOperation) {
        if(databaseOperation == DatabaseOperation.INSERT)
            insertTo(dbHelper);
        else if(databaseOperation == DatabaseOperation.UPDATE)
            updateTo(dbHelper);
        else if(databaseOperation == DatabaseOperation.DELETE)
            deleteTo(dbHelper);
    }
}
