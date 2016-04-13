package net.nueca.imonggosdk.swable;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.base.BaseTable3;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.NetworkTools;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by gama on 6/22/15.
 */
public class ImonggoSwable extends SwableService {

    public static final String NO_RETURN_ID = "@";

    public static final int UNAUTHORIZED_ACCESS = 401;
    public static final int NOT_FOUND = 404;
    public static final int UNPROCESSABLE_ENTRY = 422;
    public static final int INTERNAL_SERVER_ERROR = 500;

    public static final String NOTIFICATION_ACTION = "swable_notification_action";
    public static final int NOTIFICATION_ID = 1000;

    private SwableStateListener swableStateListener;

    private IntentFilter notificationFilter = new IntentFilter();

    private SwableSendModule swableSendModule;
    private SwableVoidModule swableVoidModule;
    private SwableUpdateModule swableUpdateModule;

    private User user;
    @Override
    protected User getUser() {
        if(user == null) {
            try {
                user = getHelper().fetchObjects(User.class).queryBuilder().where().eq("email", getSession().getEmail()).queryForFirst();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return user;
    }

    private PendingIntent pendingIntent = null;
    protected PendingIntent getPendingIntent() {
        if(pendingIntent == null) {
            Intent notificationIntent = new Intent();
            notificationIntent.setAction(NOTIFICATION_ACTION);
            pendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_ID, notificationIntent, PendingIntent
                    .FLAG_CANCEL_CURRENT);
        }
        return pendingIntent;
    }

    private boolean isReceiverAttached = false;
    private final BroadcastReceiver receiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.e("--- RECEIVER", "called");
                    String action = intent.getAction();
                    if(action.equals(NOTIFICATION_ACTION)) {
                        swableSendModule.SUCCESS_TRANSACTIONS = 0;
                        swableVoidModule.SUCCESS_TRANSACTIONS = 0;
                        swableUpdateModule.SUCCESS_TRANSACTIONS = 0;
                    }
                }
            };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        isReceiverAttached = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("ImonggoSwable", "onCreate");
        if(!isReceiverAttached) {
            notificationFilter = new IntentFilter();
            notificationFilter.addAction(NOTIFICATION_ACTION);
            registerReceiver(receiver, notificationFilter);
        }

        swableSendModule = new SwableSendModule(this,getHelper(),getSession(),getQueue());
        swableVoidModule = new SwableVoidModule(this,getHelper(),getSession(),getQueue());
        swableUpdateModule = new SwableUpdateModule(this,getHelper(),getSession(),getQueue());
    }

    @Override
    public void syncModule() {
        Log.e("ImonggoSwable", "syncModule : called");
        if(!isSyncing()) {
            setSyncing(true);
            try {
                if(AccountTools.isLoggedIn(getHelper()) && AccountTools.isUserActive(this)) {
                    Log.e("ImonggoSwable", "syncModule : trying to sync");
                    if(getSession() == null /*|| !getSession().isHas_logged_in()*/) {
                        if(getSession() == null)
                            Log.e("ImonggoSwable", "syncModule : session is null");
                        /*if(!getSession().isHas_logged_in())
                            Log.e("ImonggoSwable", "syncModule : session not logged in");*/
                        setSyncing(false);
                        return;
                    }
                    if(!NetworkTools.isInternetAvailable(this)) {
                        setSyncing(false);
                        Log.e("ImonggoSwable", "syncModule : not connected to network");
                        return;
                    }

                    //REQUEST_SUCCESS = 0;
                    //NOTIFICATION_ID++;
                    List<OfflineData> offlineDataList =
                            getHelper().fetchObjects(OfflineData.class).queryBuilder().orderBy("id", true).where()
                                    .eq("isSynced", false).and()
                                    .eq("isSyncing", false).and()
                                    .eq("isQueued", false).and()
                                    .eq("isCancelled", false).and()
                                    .eq("isBeingModified", false).and()
                                    .eq("isPastCutoff", false).and()
                                    .eq("type", OfflineData.CUSTOMER).query();

                    if((offlineDataList == null || offlineDataList.size() == 0) && swableSendModule.getQueueTrackerCount() == 0) {
                        offlineDataList =
                            getHelper().fetchObjects(OfflineData.class).queryBuilder().orderBy("id", true).where()
                                    .eq("isSynced", false).and()
                                    .eq("isSyncing", false).and()
                                    .eq("isQueued", false).and()
                                    .eq("isCancelled", false).and()
                                    .eq("isBeingModified", false).and()
                                    .eq("isPastCutoff", false).and()
                                    .ne("type", OfflineData.CUSTOMER).query();
                    }

                    if(offlineDataList.size() <= 0) {
                        Log.e("ImonggoSwable", "syncModule : nothing to sync");
                        setSyncing(false);
                        return;
                    }

                    int count = 0;
                    for (OfflineData offlineData : offlineDataList) {
                        count++;
                        Log.e("OfflineDataList", offlineData.getOfflineDataTransactionType().toString() + " " +
                                count + " " + offlineDataList.size());

                        if(offlineData.isCancelled()) {
                            Log.e("ImonggoSwable", "syncModule : already cancelled " + offlineData.getReference_no());
                            if(swableStateListener != null)
                                swableStateListener.onAlreadyCancelled(offlineData);
                            continue;
                        }
                        if(offlineData.isQueued() || offlineData.isSyncing()) {
                            continue;
                        }
                        /*
                        try {
                            if(offlineData.getObjectFromData() == null) {
                                offlineData.deleteTo(getHelper());
                                continue;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }*/
                        Log.e("ImonggoSwable", "OFFLINEDATA: " + offlineData.getReturnId());

                        offlineData.setQueued(true);
                        Log.e("ImonggoSwable " + 200, "updating offlineData <<<<<<<<<<<<<<<<<<<<<<");
                        offlineData.updateTo(getHelper());

                        if(swableStateListener != null)
                            swableStateListener.onQueued(offlineData);

                        switch ( offlineData.getOfflineDataTransactionType() ) {
                            case SEND_ORDER:
                                swableSendModule.sendTransaction(Table.ORDERS, offlineData);
                                break;
                            case SEND_INVOICE:
                            //case SEND_LAYAWAY_INVOICE:
                                swableSendModule.sendTransaction(Table.INVOICES, offlineData);
                                break;
                            case SEND_DOCUMENT:
                                swableSendModule.sendTransaction(Table.DOCUMENTS, offlineData);
                                break;
                            case ADD_CUSTOMER:
                                swableSendModule.sendTransaction(Table.CUSTOMERS, offlineData);
                                break;

                            case UPDATE_INVOICE:
                                swableUpdateModule.updateTransaction(Table.INVOICES, offlineData);
                                break;
                            case UPDATE_CUSTOMER:
                                swableUpdateModule.updateTransaction(Table.CUSTOMERS, offlineData);
                                break;

                            case CANCEL_ORDER:
                                if(offlineData.isAllPageSynced())
                                    swableVoidModule.voidTransaction(Table.ORDERS, offlineData);
                                else
                                    swableSendModule.sendTransaction(Table.ORDERS, offlineData);
                                break;
                            case CANCEL_INVOICE:
                                if(offlineData.isAllPageSynced())
                                    swableVoidModule.voidTransaction(Table.INVOICES, offlineData);
                                else
                                    swableSendModule.sendTransaction(Table.INVOICES, offlineData);
                                break;
                            case CANCEL_DOCUMENT:
                                if(offlineData.isAllPageSynced())
                                    swableVoidModule.voidTransaction(Table.DOCUMENTS, offlineData);
                                else
                                    swableSendModule.sendTransaction(Table.DOCUMENTS, offlineData);
                                break;
                            case DELETE_CUSTOMER:
                                swableVoidModule.voidTransaction(Table.CUSTOMERS, offlineData);
                                break;
                        }

                        //offlineData.updateTo(getHelper());
                    }
                    Log.e("ImonggoSwable", "starting sync : " + offlineDataList.size() + " queued transactions");
                    if(swableStateListener != null)
                        swableStateListener.onSwableStarted();
                    //getQueue().start();
                    //setSyncing(false);
                    Log.e("ImonggoSwable", "isSyncing? " + isSyncing());
                }
                else {
                    if(!AccountTools.isUserActive(this))
                        Log.e("ImonggoSwable", "stopping sync : user might have been deleted or disabled");
                    else
                        Log.e("ImonggoSwable", "stopping sync : not logged in");

                    if(swableStateListener != null)
                        swableStateListener.onSwableStopping();
                    stopSelf();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateSyncingStatus() {
        Log.d("ImonggoSwable", "updateSyncingStatus : sending ~ " + swableSendModule.getQueueTrackerCount());
        Log.d("ImonggoSwable", "updateSyncingStatus : updating ~ " + swableUpdateModule.getQueueTrackerCount());
        Log.d("ImonggoSwable", "updateSyncingStatus : voiding ~ " + swableVoidModule.getQueueTrackerCount());

        setSyncing(swableSendModule.isSyncing() ||
                swableUpdateModule.isSyncing() ||
                swableVoidModule.isSyncing());
        Log.e("ImonggoSwable", "update ~ isSyncing : " + isSyncing());
    }

    @Override
    public void restartSyncingAndQueued() {
        Log.e("ImonggoSwable", "update ~ restartSyncingAndQueued");
        try {
            List<OfflineData> offlineDataList =
                    getHelper().fetchObjects(OfflineData.class).queryBuilder().orderBy("id", true).where()
                            //.eq("isSynced", false).and()
                            //.eq("isCancelled", false).and()
                            //.eq("isBeingModified", false).and()
                            //.eq("isPastCutoff", false).and()
                            .eq("isSyncing", true).or()
                            .eq("isQueued", true).query();
            for(OfflineData offlineData : offlineDataList) {
                swableSendModule.requestQueue.cancelAll(offlineData.getObjectFromData(BaseTable3.class).getId());
                offlineData.setSyncing(false);
                offlineData.setQueued(false);
                offlineData.updateTo(getHelper());
            }

            swableSendModule.clearQueueTracker();
            swableUpdateModule.clearQueueTracker();
            swableVoidModule.clearQueueTracker();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setSwableStateListener(SwableStateListener swableStateListener) {
        this.swableStateListener = swableStateListener;
    }

    public SwableStateListener getSwableStateListener() {
        return swableStateListener;
    }

//    public void setNotificationIcon(@DrawableRes int iconResource) {
//        APP_ICON_DRAWABLE = iconResource;
//    }
//
//    @DrawableRes
//    public int getNotificationIcon() {
//        return APP_ICON_DRAWABLE;
//    }

    public interface SwableStateListener {
        void onSwableStarted();
        void onQueued(OfflineData offlineData);
        void onSyncing(OfflineData offlineData);
        void onSynced(OfflineData offlineData);
        void onSyncProblem(OfflineData offlineData, boolean hasInternet, Object response, int responseCode);
        void onUnauthorizedAccess(Object response, int responseCode);
        void onAlreadyCancelled(OfflineData offlineData);
        void onSwableStopping();
    }
    /** ------------------------------------------------------------------
     *  Following methods moved to SwableSendModule and SwableVoidModule
     *             for better readability and debugging
     **/
    /*
    public void send(Table table, final OfflineData offlineData) {
        try {
            Branch branch = getHelper().getBranches().queryBuilder().where().eq("id", offlineData.getBranch_id())
                    .queryForFirst();
            if(branch == null || branch.getStatus().equalsIgnoreCase("D")) {
                Log.e("ImonggoSwable", "sending error : Branch '" + (branch == null? "NULL" : branch.getName()) + "'," +
                        " ID:" + (branch == null? "NULL" : branch.getId()) + "," + " was deleted or disabled");
                //return;
            }

            if(offlineData.isPagedRequest()) {
                pagedSend(table, offlineData);
                return;
            }

            JSONObject jsonObject = SwableTools.prepareTransactionJSON(offlineData.getOfflineDataTransactionType(),
                    offlineData.getData());
            //Log.e("JSON", jsonObject.toString());

            getQueue().add(
                HTTPRequests.sendPOSTRequest(this, getSession(), new VolleyRequestListener() {
                    @Override
                    public void onStart(Table table, RequestType requestType) {
                        offlineData.setSyncing(true);

                        Log.e("ImonggoSwable", "sending : started -- Transaction Type: " +
                                offlineData.getObjectFromData().getClass().getSimpleName() +
                                " - with RefNo '" + offlineData.getReference_no() + "'");

                        if (swableStateListener != null)
                            swableStateListener.onSyncing(offlineData);
                    }

                    @Override
                    public void onSuccess(Table table, RequestType requestType, Object response) {
                        AccountTools.updateUserActiveStatus(ImonggoSwable.this, true);

                        Log.e("ImonggoSwable", "sending success : " + response);
                        try {
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);

                            if (response instanceof JSONObject) {
                                JSONObject responseJson = ((JSONObject) response);
                                if (responseJson.has("id")) {
                                    Log.d("ImonggoSwable", "sending success : return ID : " +
                                            responseJson.getString("id"));
                                    offlineData.setReturnId(responseJson.getString("id"));
                                }
                            }

                            offlineData.setSynced(true);
                            offlineData.updateTo(getHelper());

                            if (swableStateListener != null && offlineData.isSynced())
                                swableStateListener.onSynced(offlineData);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }*//* catch (SQLException e) {
                            e.printStackTrace();
                        }*//*

                        if (offlineData.isSynced()) {
                            REQUEST_SUCCESS++;
                            Log.e("--- Request Success +1", "" + REQUEST_SUCCESS);
                        }
                        Log.e("REQUEST", REQUEST_COUNT + " " + REQUEST_SUCCESS);
                        if (offlineData.isSynced() && REQUEST_COUNT == REQUEST_SUCCESS)
                            NotificationTools.postNotification(ImonggoSwable.this, NOTIFICATION_ID, APP_ICON_DRAWABLE,
                                    getResources().getString(R.string.app_name), REQUEST_SUCCESS + " transaction"
                                            + (REQUEST_SUCCESS != 1 ? "s" : "") + " sent", null, getPendingIntent());
                    }

                    @Override
                    public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                        Log.e("ImonggoSwable", "sending failed : isConnected? " + hasInternet + " : error [" +
                                responseCode + "] : " + response);

                        offlineData.setSyncing(false);
                        offlineData.setQueued(false);

                        try {
                            if (responseCode == UNPROCESSABLE_ENTRY) {
                                if (response instanceof String) {
                                    String errorMsg = ((String) response).toLowerCase();
                                    if (errorMsg.contains("reference has already been taken")) {
                                        offlineData.setSynced(true);
                                        offlineData.setForConfirmation(true);

                                        if (errorMsg.contains("order id")) {
                                            String orderId = errorMsg.substring(
                                                    errorMsg.indexOf("[") + 1, errorMsg.indexOf("]")
                                            );
                                            Log.e("STR : SEND_ORDER ID", orderId);
                                            offlineData.setReturnId(orderId);
                                        }
                                        else if (errorMsg.contains("document id")) {
                                            String documentId = errorMsg.substring(
                                                    errorMsg.indexOf("[") + 1, errorMsg.indexOf("]")
                                            );
                                            Log.e("STR : SEND_DOCUMENT ID", documentId);
                                            offlineData.setReturnId(documentId);
                                        }
                                    }
                                } else if (response instanceof JSONObject) {
                                    JSONObject responseJson = (JSONObject) response;
                                    if (responseJson.has("error")) {
                                        String errorMsg = responseJson.getString("error").toLowerCase();

                                        if (errorMsg.contains("reference has already been taken")) {
                                            offlineData.setSynced(true);
                                            offlineData.setForConfirmation(true);

                                            if (errorMsg.contains("order id")) {
                                                String orderId = errorMsg.substring(
                                                        errorMsg.indexOf("[") + 1, errorMsg.indexOf("]")
                                                );
                                                Log.e("JSON : SEND_ORDER ID", orderId);
                                                offlineData.setReturnId(orderId);
                                            }
                                            else if (errorMsg.contains("document id")) {
                                                String documentId = errorMsg.substring(
                                                        errorMsg.indexOf("[") + 1, errorMsg.indexOf("]")
                                                );
                                                Log.e("STR : SEND_DOCUMENT ID", documentId);
                                                offlineData.setReturnId(documentId);
                                            }
                                        }
                                    }
                                }
                            } else if (responseCode == UNAUTHORIZED_ACCESS) {
                                offlineData.setSynced(true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        offlineData.updateTo(getHelper());

                        if (swableStateListener != null) {
                            if (responseCode == UNAUTHORIZED_ACCESS) {
                                AccountTools.updateUserActiveStatus(ImonggoSwable.this, false);
                                swableStateListener.onUnauthorizedAccess(response, responseCode);
                            } else
                                swableStateListener.onSyncProblem(offlineData, hasInternet, response, responseCode);
                        }

                        if (offlineData.isSynced() && responseCode != UNAUTHORIZED_ACCESS) {
                            REQUEST_SUCCESS++;
                            Log.e("--- Request Success +1", "" + REQUEST_SUCCESS);
                        }
                    }

                    @Override
                    public void onRequestError() {
                        Log.e("ImonggoSwable", "sending failed : request error");
                        offlineData.setSyncing(false);
                        offlineData.setQueued(false);
                        offlineData.setSynced(false);
                        offlineData.updateTo(getHelper());
                    }
                }, getSession().getServer(), table, jsonObject, "?branch_id=" + offlineData.getBranch_id() + offlineData
                        .getParameters())
            );
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Table table, final OfflineData offlineData) {
        try {
            Branch branch = getHelper().getBranches().queryBuilder().where().eq("id", offlineData.getBranch_id())
                    .queryForFirst();
            if(branch == null || branch.getStatus().equalsIgnoreCase("D")) {
                Log.e("ImonggoSwable", "sending error : Branch '" + (branch == null? "NULL" : branch.getName()) + "'," +
                        " ID:" + (branch == null? "NULL" : branch.getId()) + "," + " was deleted or disabled");
                //return;
            }

            if(offlineData.getReturnIdList().size() > 1) {
                pagedDelete(table,offlineData);
                return;
            }

            getQueue().add(
                HTTPRequests.sendDELETERequest(this, getSession(), new VolleyRequestListener() {
                        @Override
                        public void onStart(Table table, RequestType requestType) {
                            offlineData.setSyncing(true);
                            Log.e("ImonggoSwable", "deleting : started -- Transaction Type: " +
                                    offlineData.getObjectFromData().getClass().getSimpleName() +
                                    " - with RefNo '" + offlineData.getReference_no() +
                                    "' and returnId '" + offlineData.getReturnId() + "'");

                            if (swableStateListener != null)
                                swableStateListener.onSyncing(offlineData);
                        }

                        @Override
                        public void onSuccess(Table table, RequestType requestType, Object response) {
                            AccountTools.updateUserActiveStatus(ImonggoSwable.this, true);

                            Log.e("ImonggoSwable", "deleting success : " + response);
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);

                            offlineData.setSynced(true);
                            offlineData.setCancelled(true);
                            offlineData.updateTo(getHelper());

                            if (swableStateListener != null && offlineData.isSynced())
                                swableStateListener.onSynced(offlineData);

                            if(offlineData.isSynced()) {
                                REQUEST_SUCCESS++;
                                Log.e("--- Request Success +1", ""+REQUEST_SUCCESS);
                            }

                            if(offlineData.isSynced() && REQUEST_COUNT == REQUEST_SUCCESS)
                                NotificationTools.postNotification(ImonggoSwable.this, NOTIFICATION_ID, APP_ICON_DRAWABLE,
                                        getResources().getString(R.string.app_name), REQUEST_SUCCESS + " transaction"
                                                + (REQUEST_SUCCESS!=1? "s" : "") + " sent", null, getPendingIntent());
                        }

                        @Override
                        public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                            Log.e("ImonggoSwable", "deleting failed : isConnected? " + hasInternet + " : error [" +
                                    responseCode + "] : " + response);
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);
                            offlineData.setSynced(true);

                            if(responseCode == UNPROCESSABLE_ENTRY) {
                                Log.e("ImonggoSwable", "deleting failed : transaction already cancelled");
                                offlineData.setCancelled(true);
                            }
                            else if(responseCode == NOT_FOUND) {
                                offlineData.setCancelled(false);
                                Log.e("ImonggoSwable", "deleting failed : transaction not found");
                            }
                            else {
                                offlineData.setCancelled(false);
                            }

                            offlineData.setSynced(offlineData.isCancelled() || responseCode == NOT_FOUND);
                            offlineData.updateTo(getHelper());

                            if(swableStateListener != null) {
                                if(responseCode == UNAUTHORIZED_ACCESS) {
                                    AccountTools.updateUserActiveStatus(ImonggoSwable.this, false);
                                    swableStateListener.onUnauthorizedAccess(response, responseCode);
                                }
                                else
                                    swableStateListener.onSyncProblem(offlineData, hasInternet, response, responseCode);
                            }

                            if(offlineData.isSynced() && responseCode != UNAUTHORIZED_ACCESS) {
                                REQUEST_SUCCESS++;
                                Log.e("--- Request Success +1", ""+REQUEST_SUCCESS);
                            }
                        }

                        @Override
                        public void onRequestError() {
                            Log.e("ImonggoSwable", "deleting failed : request error");
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);
                            offlineData.setSynced(false);
                            offlineData.updateTo(getHelper());
                        }
                    }, getSession().getServer(), table, offlineData.getReturnId(), "branch_id=" +
                    offlineData.getBranch_id() + "&reason=" + URLEncoder.encode(offlineData.getDocumentReason(),
                    "UTF-8") + offlineData.getParameters())
            );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        *//*} catch (JSONException e) {
            e.printStackTrace();
        }*//*
    }

    public void pagedSend(Table table, final OfflineData offlineData) {
        try {
            if(table == Table.ORDERS) {
                Order order = (Order)offlineData.getObjectFromData();
                Log.e("ORDER", order.toString());
                int max_page = order.getChildCount();

                if(offlineData.getReturnId().length() > 0) { // for retry sending
                    List<String> returnIds = offlineData.getReturnIdList();

                    List<Order> childOrders = order.getChildOrders();
                    for(int i = 0; i < childOrders.size(); i++) {
                        if(returnIds.get(i).length() <= 0 || !returnIds.get(i).equals(NO_RETURN_ID))
                            continue;
                        sendThisPage(table, i+1, max_page, SwableTools.prepareTransactionJSON(offlineData.getOfflineDataTransactionType
                                (), childOrders.get(i).toJSONObject()), offlineData);
                    }
                } else {
                    List<Order> childOrders = order.getChildOrders();
                    for(int i = 0; i < childOrders.size(); i++) {
                        sendThisPage(table, i+1, max_page, SwableTools.prepareTransactionJSON(offlineData.getOfflineDataTransactionType
                                (), childOrders.get(i).toJSONObject()), offlineData);
                    }
                }
            }
            else if(table == Table.DOCUMENTS) {
                //Document document = (Document)offlineData.getObjectFromData();
                //Log.e("ImonggoSwable", "pagedSend : " + document.getChildCount());

                List<Document> childDocuments = offlineData.getChildDocuments();

                int max_page = childDocuments.size();

                if(offlineData.getReturnId().length() > 0) { // for retry sending
                    List<String> returnIds = offlineData.getReturnIdList();

                    for(int i = 0; i < childDocuments.size(); i++) {
                        if(returnIds.get(i).length() <= 0 || !returnIds.get(i).equals(NO_RETURN_ID))
                            continue;
                        sendThisPage(table, i+1, max_page, SwableTools.prepareTransactionJSON(offlineData.getOfflineDataTransactionType
                                (), childDocuments.get(i).toJSONObject()), offlineData);
                    }
                } else {
                    for(int i = 0; i < childDocuments.size(); i++) {
                        sendThisPage(table, i+1, max_page, SwableTools.prepareTransactionJSON(offlineData.getOfflineDataTransactionType
                                (), childDocuments.get(i).toJSONObject()), offlineData);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendThisPage(Table table, final int page, final int maxpage, final JSONObject jsonObject,
                              final OfflineData parent) throws JSONException {

        getQueue().add(
                HTTPRequests.sendPOSTRequest(this, getSession(), new VolleyRequestListener() {
                    @Override
                    public void onStart(Table table, RequestType requestType) {
                        parent.setSyncing(true);

                        Log.e("ImonggoSwable", "sending : started [" + page + "] -- Transaction Type: " +
                                parent.getObjectFromData().getClass().getSimpleName() +
                                " - Paged RefNo " + parent.getReference_no()+"-"+(page)
                        );

                        if (swableStateListener != null)
                            swableStateListener.onSyncing(parent);

                        Log.e("Paging", page + " of " + maxpage);
                    }

                    @Override
                    public void onSuccess(Table table, RequestType requestType, Object response) {
                        AccountTools.updateUserActiveStatus(ImonggoSwable.this, true);

                        Log.e("ImonggoSwable", "sending success [" + page + "] : " + response);
                        try {
                            if (page == maxpage) {
                                parent.setSyncing(false);
                                parent.setQueued(false);
                            }

                            if (response instanceof JSONObject) {
                                JSONObject responseJson = ((JSONObject) response);
                                if (responseJson.has("id")) {
                                    Log.d("ImonggoSwable", "sending success : return ID : " +
                                            responseJson.getString("id"));

                                    parent.insertReturnIdAt(page - 1, responseJson.getString("id"));
                                }
                            }

                            parent.setSynced(page == 1 || parent.isSynced());
                            parent.updateTo(getHelper());

                            if (parent.isSynced() && !parent.getReturnId().contains(NO_RETURN_ID) &&
                                    parent.getReturnIdList().size() == maxpage) {
                                Log.e("ImonggoSwable", "paged sending : returned ID's : " + parent.getReturnId() +
                                        " size : " + parent.getReturnIdList().size());
                                if(swableStateListener != null)
                                    swableStateListener.onSynced(parent);

                                REQUEST_SUCCESS++;
                                Log.e("--- Request Success +1", ""+REQUEST_SUCCESS);

                                if(parent.isSynced() && REQUEST_COUNT == REQUEST_SUCCESS)
                                    NotificationTools.postNotification(ImonggoSwable.this, NOTIFICATION_ID, APP_ICON_DRAWABLE,
                                            getResources().getString(R.string.app_name), REQUEST_SUCCESS + " transactions" +
                                                    " sent", null, getPendingIntent());

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                        Log.e("ImonggoSwable", "sending failed [" + page + "] : isConnected? " + hasInternet + " : error [" +
                                responseCode + "] : " + response);

                        if(page == maxpage) {
                            parent.setSyncing(false);
                            parent.setQueued(false);
                        }

                        boolean isNullReturnId = true;

                        try {
                            if(responseCode == UNPROCESSABLE_ENTRY) {
                                if (response instanceof String) {
                                    String errorMsg = ((String) response).toLowerCase();
                                    if (errorMsg.contains("reference has already been taken")) {
                                        parent.setSynced(page == 1 || parent.isSynced());
                                        parent.setForConfirmation(true);

                                        if (errorMsg.contains("order id")) {
                                            String orderId = errorMsg.substring(
                                                    errorMsg.indexOf("[") + 1, errorMsg.indexOf("]")
                                            );
                                            Log.e("STR : SEND_ORDER ID", orderId);

                                            parent.insertReturnIdAt(page - 1, orderId);
                                            isNullReturnId = false;
                                        }
                                        else if (errorMsg.contains("document id")) {
                                            String documentId = errorMsg.substring(
                                                    errorMsg.indexOf("[") + 1, errorMsg.indexOf("]")
                                            );
                                            Log.e("STR : SEND_DOCUMENT ID", documentId);

                                            parent.insertReturnIdAt(page - 1, documentId);
                                            isNullReturnId = false;
                                        }
                                    }
                                } else if (response instanceof JSONObject) {
                                    JSONObject responseJson = (JSONObject) response;
                                    if (responseJson.has("error")) {
                                        String errorMsg = responseJson.getString("error").toLowerCase();

                                        if (errorMsg.contains("reference has already been taken")) {
                                            parent.setSynced(page == 1 || parent.isSynced());
                                            parent.setForConfirmation(true);

                                            if (errorMsg.contains("order id")) {
                                                String orderId = errorMsg.substring(
                                                        errorMsg.indexOf("[") + 1, errorMsg.indexOf("]"));
                                                Log.e("JSON : SEND_ORDER ID", orderId);

                                                parent.insertReturnIdAt(page - 1, orderId);
                                                isNullReturnId = false;
                                            }
                                        }
                                    }
                                }
                            }
                            if(isNullReturnId) {
                                parent.insertReturnIdAt(page - 1, NO_RETURN_ID);
                                parent.setSynced(responseCode == UNAUTHORIZED_ACCESS);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        parent.updateTo(getHelper());

                        if(swableStateListener != null) {
                            if(responseCode == UNAUTHORIZED_ACCESS) {
                                AccountTools.updateUserActiveStatus(ImonggoSwable.this, false);
                                swableStateListener.onUnauthorizedAccess(response, responseCode);
                            }
                            else
                                swableStateListener.onSyncProblem(parent, hasInternet, response, responseCode);
                        }

                        if(parent.isSynced() && !parent.getReturnId().contains(NO_RETURN_ID) &&
                                parent.getReturnIdList().size() == maxpage && responseCode != UNAUTHORIZED_ACCESS) {
                            REQUEST_SUCCESS++;
                            Log.e("--- Request Success +1", ""+REQUEST_SUCCESS);
                        }
                    }

                    @Override
                    public void onRequestError() {
                        Log.e("ImonggoSwable", "sending failed [" + page + "] : request error");
                        parent.setSyncing(false);
                        parent.setQueued(false);
                        parent.setSynced(false);
                    }
                }, getSession().getServer(), table, jsonObject, "?branch_id="+ parent.getBranch_id() + parent
                        .getParameters())
        );
    }

    public void pagedDelete(Table table, final OfflineData offlineData) {
        final List<String> list = offlineData.getReturnIdList();
        try {
            for(final String id : list) {

                getQueue().add(
                    HTTPRequests.sendDELETERequest(this, getSession(), new VolleyRequestListener() {
                        @Override
                        public void onStart(Table table, RequestType requestType) {
                            offlineData.setSyncing(true);
                            Log.e("ImonggoSwable", "deleting : started -- Transaction Type: " +
                                    offlineData.getObjectFromData().getClass().getSimpleName() +
                                    " - with RefNo '" + offlineData.getReference_no() + "' and returnId '" + id + "'");

                            if (swableStateListener != null)
                                swableStateListener.onSyncing(offlineData);
                        }

                        @Override
                        public void onSuccess(Table table, RequestType requestType, Object response) {
                            AccountTools.updateUserActiveStatus(ImonggoSwable.this, true);

                            Log.e("ImonggoSwable", "deleting success : " + response);
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);

                            offlineData.setCancelled(list.indexOf(id) == 0 || offlineData.isCancelled());
                            offlineData.setSynced(offlineData.isCancelled());
                            offlineData.updateTo(getHelper());

                            list.set(list.indexOf(id),NO_RETURN_ID); // indicator that this has been cancelled

                            if(offlineData.isSynced() && Collections.frequency(list, NO_RETURN_ID) == list.size()) {
                                REQUEST_SUCCESS++;
                                Log.e("--- Request Success +1", ""+REQUEST_SUCCESS);
                                if (swableStateListener != null)
                                    swableStateListener.onSynced(offlineData);
                            }

                            if(offlineData.isSynced() && REQUEST_COUNT == REQUEST_SUCCESS)
                                NotificationTools.postNotification(ImonggoSwable.this, NOTIFICATION_ID, APP_ICON_DRAWABLE,
                                        getResources().getString(R.string.app_name), REQUEST_SUCCESS + " transaction"
                                                + (REQUEST_SUCCESS!=1? "s" : "") + " sent", null, getPendingIntent());
                        }

                        @Override
                        public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                            Log.e("ImonggoSwable", "deleting failed : isConnected? " + hasInternet + " : error [" +
                                    responseCode + "] : " + response);
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);

                            if(responseCode == UNPROCESSABLE_ENTRY) { // Already cancelled
                                offlineData.setCancelled(true);
                                list.set(list.indexOf(id),NO_RETURN_ID); // indicator that this has been cancelled
                            }
                            else if(responseCode == NOT_FOUND) {
                                offlineData.setCancelled(false);
                                list.set(list.indexOf(id),NO_RETURN_ID); // indicator that this has been processed
                                Log.e("ImonggoSwable", "deleting failed : transaction not found");
                            }
                            else {
                                offlineData.setCancelled(false);
                            }

                            offlineData.setSynced(offlineData.isCancelled() || responseCode == NOT_FOUND ||
                                    responseCode == UNAUTHORIZED_ACCESS);
                            offlineData.updateTo(getHelper());

                            if(swableStateListener != null) {
                                if(responseCode == UNAUTHORIZED_ACCESS) {
                                    AccountTools.updateUserActiveStatus(ImonggoSwable.this, false);
                                    swableStateListener.onUnauthorizedAccess(response, responseCode);
                                }
                                else
                                    swableStateListener.onSyncProblem(offlineData, hasInternet, response, responseCode);
                            }

                            if(offlineData.isSynced() && Collections.frequency(list, NO_RETURN_ID) == list.size()) {
                                REQUEST_SUCCESS++;
                                Log.e("--- Request Success +1", ""+REQUEST_SUCCESS);
                                if (swableStateListener != null)
                                    swableStateListener.onSynced(offlineData);
                            }

                            if(offlineData.isSynced() && REQUEST_COUNT == REQUEST_SUCCESS)
                                NotificationTools.postNotification(ImonggoSwable.this, NOTIFICATION_ID, APP_ICON_DRAWABLE,
                                        getResources().getString(R.string.app_name), REQUEST_SUCCESS + " transaction"
                                                + (REQUEST_SUCCESS!=1? "s" : "") + " sent", null, getPendingIntent());
                        }

                        @Override
                        public void onRequestError() {
                            Log.e("ImonggoSwable", "deleting failed : request error");
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);
                            offlineData.setSynced(false);
                            offlineData.setCancelled(false);
                            offlineData.updateTo(getHelper());
                        }
                    }, getSession().getServer(), table, id, "branch_id=" + offlineData.getBranch_id() + "&reason="
                        + URLEncoder.encode(offlineData.getDocumentReason(), "UTF-8") + offlineData.getParameters())
                );
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    */
}
