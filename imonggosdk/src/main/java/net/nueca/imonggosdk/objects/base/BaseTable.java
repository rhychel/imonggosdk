package net.nueca.imonggosdk.objects.base;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
public abstract class BaseTable extends DBTable {

    public static final String TAG = "BASETABLE";

    private Object obj;

    @DatabaseField(id=true)
    protected int id = -1;

    @DatabaseField
    protected String searchKey = "";

    @Expose
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "extras_id")
    protected Extras extras;

    @Expose
    @DatabaseField
    protected String utc_created_at, utc_updated_at;

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
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

    public Extras getExtras() {
        return extras;
    }

    public void setExtras(Extras extras) {
        this.extras = extras;
    }

}
