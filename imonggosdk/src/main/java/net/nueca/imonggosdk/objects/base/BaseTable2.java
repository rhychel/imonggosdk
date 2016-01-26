package net.nueca.imonggosdk.objects.base;

import android.util.Log;

import com.google.gson.annotations.Expose;
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

    @Expose
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "extras_id")
    protected Extras extras;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Extras getExtras() {
        return extras;
    }

    public void setExtras(Extras extras) {
        this.extras = extras;
    }
}
