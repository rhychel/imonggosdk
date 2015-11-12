package net.nueca.imonggosdk.objects.base;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.tools.ReferenceNumberTool;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gama on 04/11/2015.
 */
public abstract class BaseTransactionTable extends BaseTable {
    @Expose
    @DatabaseField
    protected String reference;

    @Expose
    @DatabaseField
    protected String utc_document_date;

    public BaseTransactionTable() {}

    public BaseTransactionTable(Builder builder) {
        this.reference = builder.reference;
        this.utc_document_date = builder.utc_document_date;
        this.utc_created_at = builder.utc_created_at;
        this.utc_updated_at = builder.utc_updated_at;
        this.searchKey = builder.searchKey;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String generateNewReferenceNumber(Context context, int deviceId) {
        this.reference = ReferenceNumberTool.generateRefNo(context,
                deviceId);
        return this.reference;
    }

    public String getUtc_document_date() {
        return utc_document_date;
    }

    public void setUtc_document_date(String utc_document_date) {
        this.utc_document_date = utc_document_date;
    }

    protected static class Builder <T extends Builder> {
        private String reference = "";
        private String searchKey = "";
        private String utc_created_at, utc_updated_at, utc_document_date;

        public Builder() {}

        public T reference(String reference) {
            this.reference = reference;
            return (T)this;
        }

        public T generateReference(Context context, int deviceId) {
            this.reference = ReferenceNumberTool.generateRefNo(context, deviceId);
            return (T)this;
        }

        public T searchKey(String searchKey) {
            this.searchKey = searchKey;
            return (T)this;
        }

        public T utc_created_at(String utc_created_at) {
            this.utc_created_at = utc_created_at;
            return (T)this;
        }

        public T utc_updated_at(String utc_updated_at) {
            this.utc_updated_at = utc_updated_at;
            return (T)this;
        }

        public T utc_document_date(String utc_document_date) {
            this.utc_document_date = utc_document_date;
            return (T)this;
        }

        public <T extends BaseTransactionTable> T build() throws NoSuchMethodException {
            throw new NoSuchMethodException("Builder method build() was not implemented");
        }
    }

    public abstract boolean shouldPageRequest();
    public abstract int getChildCount();
    public abstract void refresh();

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

    public String toJSONString() {
        refresh();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return (gson.toJson(this));
    }

    @Override
    public String toString() {
        return toJSONString();
    }

    public JSONObject toJSONObject() throws JSONException {
        refresh();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return new JSONObject(gson.toJson(this));
    }
}
