package net.nueca.imonggosdk.activities;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.ModuleSettingTools;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/14/15.
 * imonggosdk (c)2015
 */


public abstract class ImonggoAppCompatActivity extends AppCompatActivity {

    protected static final String CUSTOMER_ID = "customer_id";

    protected static final int SUCCESS = 10;
    protected static final int ERROR = 20;
    protected static final int REFRESH = 30;

    protected static final int ADD_CUSTOMER = 100;
    protected static final int EDIT_CUSTOMER = 101;
    protected static final int ALL_CUSTOMERS = 104;
    protected static final int REVIEW_SALES = 102;
    protected static final int RETURN_ITEMS_SALES = 103;

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

    protected void setupNavigationListener(Toolbar toolbar) {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    protected ModuleSetting getAppSetting() {
        try {
            return getHelper().fetchObjects(ModuleSetting.class).queryBuilder().where().eq("module_type", ModuleSettingTools.getModuleToString(ConcessioModule.APP)).queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}
