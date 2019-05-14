package net.nueca.imonggosdk.tools;

import net.nueca.imonggosdk.enums.Table;

/**
 * Created by rhymart on 5/15/15.
 * imonggosdk (c)2015
 */
public class LastUpdateAtTools {

    public static final String DOCUMENTS = "documents";


    public static String getTableToSync(Table table) {
        return getTableToSync(table, "");
    }

    public static String getTableToSync(Table table, String id) {
        switch (table) {
            case DOCUMENTS:
            case DOCUMENT_TRANSFER_OUT:
            case DOCUMENT_ADJUSTMENT_OUT:
                return DOCUMENTS + id;
            default:
                return table.getStringName();
        }
    }
}
