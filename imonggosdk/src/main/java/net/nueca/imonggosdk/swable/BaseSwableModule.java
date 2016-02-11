package net.nueca.imonggosdk.swable;

import android.support.annotation.DrawableRes;

import com.android.volley.RequestQueue;

import net.nueca.imonggosdk.R;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Session;

/**
 * Created by gama on 02/02/2016.
 */
public class BaseSwableModule {
    protected ImonggoDBHelper2 dbHelper;
    protected ImonggoSwable imonggoSwable;
    protected RequestQueue requestQueue;
    protected Session session;

    protected int QUEUED_TRANSACTIONS = 0;
    protected int SUCCESS_TRANSACTIONS = 0;

    protected @DrawableRes int APP_ICON_DRAWABLE = R.drawable.ic_check_circle;
    public BaseSwableModule(ImonggoSwable imonggoSwable, ImonggoDBHelper2 helper, Session session, RequestQueue
            requestQueue) {
        this.imonggoSwable = imonggoSwable;
        this.dbHelper = helper;
        this.session = session;
        this.requestQueue = requestQueue;
        this.APP_ICON_DRAWABLE = R.drawable.ic_check_circle;
    }
}
