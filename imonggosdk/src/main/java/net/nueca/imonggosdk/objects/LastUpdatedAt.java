package net.nueca.imonggosdk.objects;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by rhymart on 5/14/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class LastUpdatedAt {
    public static final String BRANCHES_TABLE = "branches";
    public static final String BRANCH_USERS_TABLE = "branch_users";
    public static final String CUSTOMERS_TABLE = "customers";
    public static final String PRODUCTS_TABLE = "products";
    public static final String TAX_SETTINGS_TABLE = "tax_settings";
    public static final String UNITS_TABLE = "units";
    public static final String USERS_TABLE = "users";

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
