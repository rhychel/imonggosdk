package net.nueca.imonggosdk.activities;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/14/15.
 * imonggosdk (c)2015
 */


public class ImonggoAppCompatActivity extends AppCompatActivity {

    private ImonggoDBHelper2 dbHelper2;

    @Override
    protected void onDestroy() {
        if(dbHelper2 != null) {
            OpenHelperManager.releaseHelper();
            dbHelper2 = null;
        }

        super.onDestroy();
    }

    public ImonggoDBHelper2 getHelper() {
        if(dbHelper2 == null)
            dbHelper2 = OpenHelperManager.getHelper(this, ImonggoDBHelper2.class);
        return dbHelper2;
    }

    public Session getSession() throws SQLException {
        Session session = null;
        Log.e("isLoggedIn", AccountTools.isLoggedIn(getHelper()) + "");
        if(AccountTools.isLoggedIn(getHelper()))
            session = getHelper().fetchObjectsList(Session.class).get(0);
        return session;
    }

    public User getUser() throws SQLException {
        if(getSession() == null)
            return null;
        return getSession().getUser();
    }

}
