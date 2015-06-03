package net.nueca.imonggosdk.tools;

import net.nueca.imonggosdk.enums.Table;

/**
 * Created by rhymart on 5/15/15.
 * imonggosdk (c)2015
 */
public class LastUpdateAtTools {

    public static final String BRANCHES_TABLE = "branches";
    public static final String BRANCH_USERS_TABLE = "branch_users";
    public static final String CUSTOMERS_TABLE = "customers";
    public static final String PRODUCTS_TABLE = "products";
    public static final String TAX_SETTINGS_TABLE = "tax_settings";
    public static final String UNITS_TABLE = "units";
    public static final String USERS_TABLE = "users";

    public static final String DOCUMENTS_TABLE = "documents";

    public static String getTableToSync(Table table) {
        return getTableToSync(table, "");
    }
    public static String getTableToSync(Table table, String id) {
        switch (table) {
            case BRANCHES:
                return BRANCHES_TABLE;
            case BRANCH_USERS:
                return BRANCH_USERS_TABLE;
            case CUSTOMERS:
                return CUSTOMERS_TABLE;
            case PRODUCTS:
                return PRODUCTS_TABLE;
            case TAX_SETTINGS:
                return TAX_SETTINGS_TABLE;
            case UNITS:
                return UNITS_TABLE;
            case USERS:
                return USERS_TABLE;
            case DOCUMENTS:
                return DOCUMENTS_TABLE+id;
        }
        return "";
    }
}
