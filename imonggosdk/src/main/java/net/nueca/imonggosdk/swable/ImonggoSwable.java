package net.nueca.imonggosdk.swable;

import android.util.Log;

import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;

/**
 * Created by gama on 6/22/15.
 */
public class ImonggoSwable extends SwableService {
    private User user;
    private User getUser() {
        if(user == null) {
            try {
                user = getHelper().getUsers().queryBuilder().where().eq("email", getSession().getEmail()).query().get(0);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return user;
    }

    @Override
    public void syncModule() {
        Log.e("ImonggoSwable", "syncModule : called");
        if(!isSyncing()) {
            setSyncing(true);
            try {
                if(AccountTools.isLoggedIn(getHelper())) {
                    Log.e("ImonggoSwable", "syncModule : syncing");
                    if(!getSession().isHasLoggedIn()) {
                        setSyncing(false);
                    }
                }
                else {
                    Log.e("ImonggoSwable", "syncModule : stopping self");
                    stopSelf();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
