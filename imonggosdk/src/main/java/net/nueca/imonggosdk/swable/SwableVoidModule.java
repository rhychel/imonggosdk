package net.nueca.imonggosdk.swable;

import android.util.Log;

import com.android.volley.RequestQueue;

import net.nueca.imonggosdk.R;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.operations.http.HTTPRequests;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.NotificationTools;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Created by gama on 10/1/15.
 */
public class SwableVoidModule extends BaseSwableModule {

    public SwableVoidModule(ImonggoSwable imonggoSwable, ImonggoDBHelper2 helper, Session session, RequestQueue requestQueue) {
        super(imonggoSwable, helper, session, requestQueue);
    }

    public void voidTransaction(Table table, final OfflineData offlineData) {
        QUEUED_TRANSACTIONS++;
        try {
            Branch branch = dbHelper.fetchObjects(Branch.class).queryBuilder().where().eq("id", offlineData.getBranch_id())
                    .queryForFirst();
            if(branch == null || branch.getStatus().equalsIgnoreCase("D")) {
                Log.e("ImonggoSwable", "sending error : Branch '" + (branch == null ? "NULL" : branch.getName()) + "'," +
                        " ID:" + (branch == null ? "NULL" : branch.getId()) + "," + " was deleted or disabled");
                //return;
            }

            if(offlineData.getReturnIdList().size() > 1 && !offlineData.isNewPagedSend()) {
                pagedDelete(table,offlineData);
                return;
            }

            requestQueue.add(
                    HTTPRequests.sendDELETERequest(imonggoSwable, session, new VolleyRequestListener() {
                        @Override
                        public void onStart(Table table, RequestType requestType) {
                            offlineData.setSyncing(true);
                            Log.e("ImonggoSwable", "deleting : started -- Transaction Type: " +
                                    offlineData.getObjectFromData().getClass().getSimpleName() +
                                    " - with RefNo '" + offlineData.getReference_no() +
                                    "' and returnId '" + offlineData.getReturnId() + "'");

                            if (imonggoSwable.getSwableStateListener() != null)
                                imonggoSwable.getSwableStateListener().onSyncing(offlineData);
                        }

                        @Override
                        public void onSuccess(Table table, RequestType requestType, Object response) {
                            QUEUED_TRANSACTIONS--;
                            AccountTools.updateUserActiveStatus(imonggoSwable, true);

                            Log.e("ImonggoSwable", "deleting success : " + response);
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);

                            offlineData.setSynced(true);
                            offlineData.setCancelled(true);
                            offlineData.updateTo(dbHelper);

                            if (imonggoSwable.getSwableStateListener() != null && offlineData.isSynced())
                                imonggoSwable.getSwableStateListener().onSynced(offlineData);

                            if (offlineData.isSynced()) {
                                SUCCESS_TRANSACTIONS++;
                                Log.e("--- Request Success +1", "" + SUCCESS_TRANSACTIONS);
                            }

                            if (offlineData.isSynced() && QUEUED_TRANSACTIONS == 0)
                                NotificationTools.postNotification(imonggoSwable, ImonggoSwable.NOTIFICATION_ID,
                                        APP_ICON_DRAWABLE,
//                                        imonggoSwable.getNotificationIcon(),
                                        imonggoSwable.getResources().getString(R.string.app_name), SUCCESS_TRANSACTIONS + " transaction"
                                                + (SUCCESS_TRANSACTIONS != 1 ? "s" : "") + " sent", null, imonggoSwable.getPendingIntent());
                        }

                        @Override
                        public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                            QUEUED_TRANSACTIONS--;
                            Log.e("ImonggoSwable", "deleting failed : isConnected? " + hasInternet + " : error [" +
                                    responseCode + "] : " + response);
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);
                            offlineData.setSynced(true);

                            if (responseCode == ImonggoSwable.UNPROCESSABLE_ENTRY) {
                                Log.e("ImonggoSwable", "deleting failed : transaction already cancelled");
                                offlineData.setCancelled(true);
                            } else if (responseCode == ImonggoSwable.NOT_FOUND) {
                                offlineData.setCancelled(false);
                                Log.e("ImonggoSwable", "deleting failed : transaction not found");
                            } else {
                                offlineData.setCancelled(false);
                            }

                            offlineData.setSynced(offlineData.isCancelled() || responseCode == ImonggoSwable.NOT_FOUND);
                            offlineData.updateTo(dbHelper);

                            if (imonggoSwable.getSwableStateListener() != null) {
                                if (responseCode == ImonggoSwable.UNAUTHORIZED_ACCESS) {
                                    AccountTools.updateUserActiveStatus(imonggoSwable, false);
                                    imonggoSwable.getSwableStateListener().onUnauthorizedAccess(response, responseCode);
                                } else
                                    imonggoSwable.getSwableStateListener().onSyncProblem(offlineData, hasInternet, response, responseCode);
                            }

                            if (offlineData.isSynced() && responseCode != ImonggoSwable.UNAUTHORIZED_ACCESS) {
                                SUCCESS_TRANSACTIONS++;
                                Log.e("--- Request Success +1", "" + SUCCESS_TRANSACTIONS);
                            }
                        }

                        @Override
                        public void onRequestError() {
                            QUEUED_TRANSACTIONS--;
                            Log.e("ImonggoSwable", "deleting failed : request error");
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);
                            offlineData.setSynced(false);
                            offlineData.updateTo(dbHelper);
                        }
                    }, session.getServer(), table, offlineData.getReturnIdListAt(0), "branch_id=" +
                            offlineData.getBranch_id() + "&reason=" + URLEncoder.encode(offlineData.getDocumentReason(),
                            "UTF-8") + offlineData.getParameters())
            );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        /*} catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    @Deprecated
    public void pagedDelete(Table table, final OfflineData offlineData) {
        final List<String> list = offlineData.getReturnIdList();
        try {
            for(final String id : list) {

                requestQueue.add(
                        HTTPRequests.sendDELETERequest(imonggoSwable, session, new VolleyRequestListener() {
                            @Override
                            public void onStart(Table table, RequestType requestType) {
                                offlineData.setSyncing(true);
                                Log.e("ImonggoSwable", "deleting : started -- Transaction Type: " +
                                        offlineData.getObjectFromData().getClass().getSimpleName() +
                                        " - with RefNo '" + offlineData.getReference_no() + "' and returnId '" + id + "'");

                                if (imonggoSwable.getSwableStateListener() != null)
                                    imonggoSwable.getSwableStateListener().onSyncing(offlineData);
                            }

                            @Override
                            public void onSuccess(Table table, RequestType requestType, Object response) {
                                AccountTools.updateUserActiveStatus(imonggoSwable, true);

                                Log.e("ImonggoSwable", "deleting success : " + response);
                                offlineData.setSyncing(false);
                                offlineData.setQueued(false);

                                offlineData.setCancelled(list.indexOf(id) == 0 || offlineData.isCancelled());
                                offlineData.setSynced(offlineData.isCancelled());
                                offlineData.updateTo(dbHelper);

                                list.set(list.indexOf(id), ImonggoSwable.NO_RETURN_ID); // indicator that this has been cancelled

                                if (offlineData.isSynced() && Collections.frequency(list, ImonggoSwable.NO_RETURN_ID) == list.size()) {
                                    SUCCESS_TRANSACTIONS++;
                                    Log.e("--- Request Success +1", "" + SUCCESS_TRANSACTIONS);
                                    if (imonggoSwable.getSwableStateListener() != null)
                                        imonggoSwable.getSwableStateListener().onSynced(offlineData);
                                }

                                if (offlineData.isSynced() && QUEUED_TRANSACTIONS == SUCCESS_TRANSACTIONS)
                                    NotificationTools.postNotification(imonggoSwable, ImonggoSwable.NOTIFICATION_ID,
                                            APP_ICON_DRAWABLE,
//                                            imonggoSwable.getNotificationIcon(),
                                            imonggoSwable.getResources().getString(R.string.app_name), SUCCESS_TRANSACTIONS + " transaction"
                                                    + (SUCCESS_TRANSACTIONS != 1 ? "s" : "") + " sent", null, imonggoSwable.getPendingIntent());
                            }

                            @Override
                            public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                                Log.e("ImonggoSwable", "deleting failed : isConnected? " + hasInternet + " : error [" +
                                        responseCode + "] : " + response);
                                offlineData.setSyncing(false);
                                offlineData.setQueued(false);

                                if (responseCode == ImonggoSwable.UNPROCESSABLE_ENTRY) { // Already cancelled
                                    offlineData.setCancelled(true);
                                    list.set(list.indexOf(id), ImonggoSwable.NO_RETURN_ID); // indicator that this has been cancelled
                                } else if (responseCode == ImonggoSwable.NOT_FOUND) {
                                    offlineData.setCancelled(false);
                                    list.set(list.indexOf(id), ImonggoSwable.NO_RETURN_ID); // indicator that this has been processed
                                    Log.e("ImonggoSwable", "deleting failed : transaction not found");
                                } else {
                                    offlineData.setCancelled(false);
                                }

                                offlineData.setSynced(offlineData.isCancelled() || responseCode == ImonggoSwable.NOT_FOUND ||
                                        responseCode == ImonggoSwable.UNAUTHORIZED_ACCESS);
                                offlineData.updateTo(dbHelper);

                                if (imonggoSwable.getSwableStateListener() != null) {
                                    if (responseCode == ImonggoSwable.UNAUTHORIZED_ACCESS) {
                                        AccountTools.updateUserActiveStatus(imonggoSwable, false);
                                        imonggoSwable.getSwableStateListener().onUnauthorizedAccess(response, responseCode);
                                    } else
                                        imonggoSwable.getSwableStateListener().onSyncProblem(offlineData, hasInternet, response, responseCode);
                                }

                                if (offlineData.isSynced() && Collections.frequency(list, ImonggoSwable.NO_RETURN_ID) == list.size()) {
                                    SUCCESS_TRANSACTIONS++;
                                    Log.e("--- Request Success +1", "" + SUCCESS_TRANSACTIONS);
                                    if (imonggoSwable.getSwableStateListener() != null)
                                        imonggoSwable.getSwableStateListener().onSynced(offlineData);
                                }

                                if (offlineData.isSynced() && QUEUED_TRANSACTIONS == SUCCESS_TRANSACTIONS)
                                    NotificationTools.postNotification(imonggoSwable, ImonggoSwable.NOTIFICATION_ID,
                                            APP_ICON_DRAWABLE,
//                                            imonggoSwable.getNotificationIcon(),
                                            imonggoSwable.getResources().getString(R.string.app_name), SUCCESS_TRANSACTIONS + " transaction"
                                                    + (SUCCESS_TRANSACTIONS != 1 ? "s" : "") + " sent", null, imonggoSwable.getPendingIntent());
                            }

                            @Override
                            public void onRequestError() {
                                Log.e("ImonggoSwable", "deleting failed : request error");
                                offlineData.setSyncing(false);
                                offlineData.setQueued(false);
                                offlineData.setSynced(false);
                                offlineData.setCancelled(false);
                                offlineData.updateTo(dbHelper);
                            }
                        }, session.getServer(), table, id, "branch_id=" + offlineData.getBranch_id() + "&reason="
                                + URLEncoder.encode(offlineData.getDocumentReason(), "UTF-8") + offlineData.getParameters())
                );
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
