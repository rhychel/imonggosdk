package net.nueca.imonggosdk.objects;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.enums.Table;

/**
 * Created by rhymart on 5/14/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class LastUpdatedAt {
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

}
