package net.nueca.imonggosdk.swable;

import android.util.Log;

import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.operations.http.HTTPRequests;
import net.nueca.imonggosdk.tools.AccountTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by gama on 6/22/15.
 */
public class ImonggoSwable extends SwableService {
    private SwableStateListener swableStateListener;

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

                    List<OfflineData> offlineDataList =
                            getHelper().getOfflineData().queryBuilder().where().
                            eq("isSynced", false).and().eq("isPastCutoff", false).query();

                    if(offlineDataList.size() <= 0) {
                        setSyncing(false);
                        return;
                    }

                    for (OfflineData offlineData : offlineDataList) {
                        offlineData.setQueued(true);

                        if(swableStateListener != null)
                            swableStateListener.onQueued(offlineData);

                        switch (offlineData.getType()) {
                            case OfflineData.TYPE_ORDER:
                                send(Table.ORDERS, offlineData);
                                break;
                            case OfflineData.TYPE_INVOICE:
                                send(Table.INVOICES, offlineData);
                                break;
                            case OfflineData.TYPE_DOCUMENT:
                                send(Table.DOCUMENTS, offlineData);
                                break;
                        }
                    }
                    Log.e("ImonggoSwable", "starting sync : " + offlineDataList.size() + " queued");
                    if(swableStateListener != null)
                        swableStateListener.onSwableStarted();
                    getQueue().start();
                }
                else {
                    Log.e("ImonggoSwable", "stopping sync");
                    if(swableStateListener != null)
                        swableStateListener.onSwableStopping();
                    stopSelf();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void setSwableStateListener(SwableStateListener swableStateListener) {
        this.swableStateListener = swableStateListener;
    }

    public interface SwableStateListener {
        void onSwableStarted();
        void onQueued(OfflineData offlineData);
        void onSyncing(OfflineData offlineData);
        void onSynced(OfflineData offlineData);
        void onSyncProblem(OfflineData offlineData);
        void onSwableStopping();
    }

    private void send(Table table, final OfflineData offlineData) {
        try {
            getQueue().add(
                    HTTPRequests.sendPOSTRequest(this, getSession(), new VolleyRequestListener() {
                        @Override
                        public void onStart(Table table, RequestType requestType) {
                            offlineData.setSyncing(true);

                            if (swableStateListener != null)
                                swableStateListener.onSyncing(offlineData);
                        }

                        @Override
                        public void onSuccess(Table table, RequestType requestType, Object response) {
                            offlineData.setSyncing(false);
                            offlineData.setSynced(true);
                            offlineData.setQueued(false);


                            if (swableStateListener != null)
                                swableStateListener.onSynced(offlineData);
                        }

                        @Override
                        public void onError(Table table, boolean hasInternet, Object response, int responseCode) {

                        }

                        @Override
                        public void onRequestError() {

                        }
                    }, Server.IMONGGO, table, new JSONObject(offlineData.getData()), "")
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
