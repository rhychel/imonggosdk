package net.nueca.imonggosdk.fragments;

import android.support.v4.app.Fragment;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
public class ImonggoFragment extends Fragment {

    private ImonggoDBHelper dbHelper;

    public void setHelper(ImonggoDBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public ImonggoDBHelper getHelper() {
        return dbHelper;
    }

    public Session getSession() throws SQLException {
        Session session = null;
        if(AccountTools.isLoggedIn(getHelper()))
            session = getHelper().getSessions().queryForAll().get(0);
        return session;
    }
}
