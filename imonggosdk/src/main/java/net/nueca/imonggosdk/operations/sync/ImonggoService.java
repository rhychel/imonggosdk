package net.nueca.imonggosdk.operations.sync;

import android.app.Service;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
public abstract class ImonggoService extends Service {
    private Session session;
    private User mUser;
    private ImonggoDBHelper dbHelper;
    private RequestQueue queue;

    /**
     * Data retriever for Imonggo Database Data Center.
     *
     * @return dbHelper
     */
    protected ImonggoDBHelper getHelper() {
        if(dbHelper == null)
            dbHelper = OpenHelperManager.getHelper(this, ImonggoDBHelper.class);
        return dbHelper;
    }

    @Override
    public void onDestroy() {
        if(dbHelper != null) {
            OpenHelperManager.releaseHelper();
            dbHelper = null;
        }
        super.onDestroy();
    }

    protected RequestQueue getQueue() {
        if(queue == null)
            queue = Volley.newRequestQueue(this);
        return queue;
    }

    protected Session getSession() {
        if(session == null) {
            try {
                List<Session> sessionList = getHelper().getSessions().queryForAll();
                session = sessionList.get(0);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return session;
    }

    protected User getUser() {
        if (mUser == null)
            try {
                mUser = getHelper().getUsers().queryBuilder().where().eq("email ", getSession().getEmail()).queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return mUser;
    }
}
