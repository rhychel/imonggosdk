package net.nueca.imonggosdk.objects.base;

import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;

/**
 * Created by gama on 7/21/15.
 */
public abstract class BaseTransactionDB extends BaseTransaction {

    @DatabaseField(id=true)
    protected transient int id = -1;

    @DatabaseField
    protected String utc_created_at;
    @DatabaseField
    protected String utc_updated_at;
    @DatabaseField
    protected String utc_document_date;

    public BaseTransactionDB(Builder builder) {
        super(builder);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUtc_created_at() {
        return utc_created_at;
    }

    public void setUtc_created_at(String utc_created_at) {
        this.utc_created_at = utc_created_at;
    }

    public String getUtc_updated_at() {
        return utc_updated_at;
    }

    public void setUtc_updated_at(String utc_updated_at) {
        this.utc_updated_at = utc_updated_at;
    }

    public String getUtc_document_date() {
        return utc_document_date;
    }

    public void setUtc_document_date(String utc_document_date) {
        this.utc_document_date = utc_document_date;
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
