package net.nueca.imonggosdk.objects.base;

import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;

/**
 * Created by gama on 7/2/15.
 */
public abstract class BaseTable2 {

    @DatabaseField(generatedId = true)
    protected transient int id = -1;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
