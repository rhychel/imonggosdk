package net.nueca.imonggosdk.enums;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
public enum DatabaseOperation {
    /**
     * Create table.
     */
    CREATE,
    /**
     * Insert new row on the table.
     */
    INSERT,
    /**
     * Delete a row on the table.
     */
    DELETE,
    /**
     * Delete all rows on the table.
     */
    DELETE_ALL,
    /**
     * Update a row on the table.
     */
    UPDATE,
    /**
     * Update a row on the table with parameter.
     */
    UPDATE_WITH_PARAMETER,
    /**
     * Drop the table.
     */
    DROP
}
