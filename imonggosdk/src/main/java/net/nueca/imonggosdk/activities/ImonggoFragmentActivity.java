package net.nueca.imonggosdk.activities;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
@Deprecated
public class ImonggoFragmentActivity extends FragmentActivity {

    private ImonggoDBHelper2 dbHelper;

    @Override
    protected void onDestroy() {
        if(dbHelper != null) {
            OpenHelperManager.releaseHelper();
            dbHelper = null;
        }
        super.onDestroy();
    }

    public ImonggoDBHelper2 getHelper() {
        if(dbHelper == null)
            dbHelper = OpenHelperManager.getHelper(this, ImonggoDBHelper2.class);
        return dbHelper;
    }

    public Session getSession() throws SQLException {
        Session session = null;
        if(AccountTools.isLoggedIn(getHelper()))
            session = getHelper().fetchObjectsList(Session.class).get(0);
        return session;
    }

}
