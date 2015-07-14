package net.nueca.imonggosdk.activities;

import android.support.v7.app.AppCompatActivity;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;

/**
 * Created by Jn on 6/10/2015.
 * imonggosdk (c)2015
 */
public class ImonggoAppCompatActivity extends AppCompatActivity {

    private ImonggoDBHelper dbHelper;

    /**
     * Returns the Database Helper
     *
     * @return databaseHelper
     */
    public ImonggoDBHelper getHelper() {
        if (dbHelper == null)
            dbHelper = OpenHelperManager.getHelper(this, ImonggoDBHelper.class);
        return dbHelper;
    }

    /**
     * Gets the current session
     *
     * @return user session
     * @throws SQLException
     */
    public Session getSession() throws SQLException {
        Session session = null;
        if (AccountTools.isLoggedIn(getHelper()))
            session = getHelper().getSessions().queryForAll().get(0);
        return session;
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            OpenHelperManager.releaseHelper();
            dbHelper = null;
        }
        super.onDestroy();
    }
}
