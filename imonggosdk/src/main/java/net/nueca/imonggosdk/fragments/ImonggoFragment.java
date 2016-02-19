package net.nueca.imonggosdk.fragments;

import android.support.v4.app.Fragment;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.ModuleSettingTools;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
public class ImonggoFragment extends Fragment {

    private ImonggoDBHelper2 dbHelper2;

    public void setHelper(ImonggoDBHelper2 dbHelper) {
        this.dbHelper2 = dbHelper;
    }

    public ImonggoDBHelper2 getHelper() {
        return dbHelper2;
    }

    public Session getSession() throws SQLException {
        Session session = null;
        if(AccountTools.isLoggedIn(getHelper()))
            session = getHelper().fetchObjectsList(Session.class).get(0);
        return session;
    }

    protected ModuleSetting getAppSetting() {
        return getModuleSetting(ConcessioModule.APP);
    }

    protected ModuleSetting getModuleSetting(ConcessioModule concessioModule) {
        try {
            return getHelper().fetchObjects(ModuleSetting.class).queryBuilder().where().eq("module_type", ModuleSettingTools.getModuleToString(concessioModule)).queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
