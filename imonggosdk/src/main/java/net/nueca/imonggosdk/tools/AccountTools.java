package net.nueca.imonggosdk.tools;

import net.nueca.imonggosdk.database.ImonggoDBHelper;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
public class AccountTools {

    /**
     *
     * Check if the user is logged in on their Imonggo/Iretailcloud account.
     *
     * @param dbHelper
     * @return
     * @throws SQLException
     */
    public static boolean isLoggedIn(ImonggoDBHelper dbHelper) throws SQLException {
        return (dbHelper.getSessions().countOf() > 0l);
    }
}
