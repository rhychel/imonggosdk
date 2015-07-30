package net.nueca.imonggosdk.swable;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.DrawableRes;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.nueca.imonggosdk.R;
import net.nueca.imonggosdk.enums.OfflineDataType;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.base.BatchList;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.objects.order.OrderLine;
import net.nueca.imonggosdk.operations.http.HTTPRequests;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.NotificationTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by gama on 6/22/15.
 */
public class ImonggoSwable extends SwableService {

    private static final String NO_RETURN_ID = "@";

    private static final int UNAUTHORIZED_ACCESS = 401;
    private static final int NOT_FOUND = 404;
    private static final int UNPROCESSABLE_ENTRY = 422;
    private static final int INTERNAL_SERVER_ERROR = 500;

    public static final String NOTIFICATION_ACTION = "swable_notification_action";
    private static final int NOTIFICATION_ID = 1000;

    private SwableStateListener swableStateListener;

    private int REQUEST_SUCCESS = 0;
    private int REQUEST_COUNT = 0;

    private int APP_ICON_DRAWABLE = R.drawable.ic_check_circle;

    private IntentFilter notificationFilter = new IntentFilter();

    private User user;
    protected User getUser() {
        if(user == null) {
            try {
                user = getHelper().getUsers().queryBuilder().where().eq("email", getSession().getEmail()).queryForFirst();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return user;
    }

    private PendingIntent pendingIntent = null;
    private PendingIntent getPendingIntent() {
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
                        REQUEST_SUCCESS = 0;
                        REQUEST_COUNT = 0;
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
        if(!isReceiverAttached) {
            notificationFilter = new IntentFilter();
            notificationFilter.addAction(NOTIFICATION_ACTION);
            registerReceiver(receiver, notificationFilter);
        }
    }

    @Override
    public void syncModule() {
        Log.e("ImonggoSwable", "syncModule : called");
        if(!isSyncing()) {
            setSyncing(true);
            try {
                if(AccountTools.isLoggedIn(getHelper()) && AccountTools.isUserActive(this)) {
                    Log.e("ImonggoSwable", "syncModule : trying to sync");
                    if(!getSession().isHas_logged_in()) {
                        setSyncing(false);
                    }

                    //REQUEST_SUCCESS = 0;
                    //NOTIFICATION_ID++;

                    List<OfflineData> offlineDataList =
                        getHelper().getOfflineData().queryBuilder().where().
                        eq("isSynced", false).and().eq("isPastCutoff", false).query();

                    if(offlineDataList.size() <= 0) {
                        setSyncing(false);
                        return;
                    }

                    int count = 0;
                    for (OfflineData offlineData : offlineDataList) {
                        count++;
                        Log.e("OfflineDataList", offlineData.getOfflineDataTransactionType().toString() + " " +
                                count + " " + offlineDataList.size());

                        if(offlineData.isCancelled()) {
                            swableStateListener.onAlreadyCancelled(offlineData);
                            continue;
                        }
                        if(offlineData.isQueued()) {
                            continue;
                        }

                        offlineData.setQueued(true);

                        if(swableStateListener != null)
                            swableStateListener.onQueued(offlineData);

                        switch ( offlineData.getOfflineDataTransactionType() ) {
                            case SEND_ORDER:
                                send(Table.ORDERS, offlineData);
                                break;
                            case SEND_INVOICE:
                                send(Table.INVOICES, offlineData);
                                break;
                            case SEND_DOCUMENT:
                                send(Table.DOCUMENTS, offlineData);
                                break;

                            case CANCEL_ORDER:
                                delete(Table.ORDERS, offlineData);
                                break;
                            case CANCEL_INVOICE:
                                delete(Table.INVOICES, offlineData);
                                break;
                            case CANCEL_DOCUMENT:
                                delete(Table.DOCUMENTS, offlineData);
                                break;
                        }
                    }
                    Log.e("ImonggoSwable", "starting sync : " + offlineDataList.size() + " queued transactions");
                    REQUEST_COUNT += offlineDataList.size();
                    if(swableStateListener != null)
                        swableStateListener.onSwableStarted();
                    getQueue().start();
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

    public void setSwableStateListener(SwableStateListener swableStateListener) {
        this.swableStateListener = swableStateListener;
    }

    public void setNotificationIcon(@DrawableRes int iconResource) {
        APP_ICON_DRAWABLE = iconResource;
    }

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

    private JSONObject prepareTransactionJSON(OfflineDataType offlineDataType, String jsonString) throws JSONException {
        switch(offlineDataType) {
            case SEND_ORDER:
                jsonString = "{\"order\":" + jsonString + "}";
                break;
            case SEND_INVOICE:
                jsonString = "{\"invoice\":" + jsonString + "}";
                break;
            case SEND_DOCUMENT:
                jsonString = "{\"document\":" + jsonString + "}";
                break;
        }
        return new JSONObject(jsonString);
    }

    private void send(Table table, final OfflineData offlineData) {
        try {
            Branch branch = getHelper().getBranches().queryBuilder().where().eq("id", offlineData.getBranch_id())
                    .queryForFirst();
            if(branch == null || branch.getStatus().equalsIgnoreCase("D")) {
                Log.e("ImonggoSwable", "sending error : Branch '" + (branch == null? "NULL" : branch.getName()) + "'," +
                        " ID:" + (branch == null? "NULL" : branch.getId()) + "," + " was deleted or disabled");
                //return;
            }

            if(offlineData.generateObjectFromData().shouldPageRequest()) {
                pagedSend(table, offlineData);
                return;
            }

            JSONObject jsonObject = prepareTransactionJSON(offlineData.getOfflineDataTransactionType(),
                    offlineData.getData());
            //Log.e("JSON", jsonObject.toString());

            getQueue().add(
                HTTPRequests.sendPOSTRequest(this, getSession(), new VolleyRequestListener() {
                    @Override
                    public void onStart(Table table, RequestType requestType) {
                        offlineData.setSyncing(true);
                        try {
                            Log.e("ImonggoSwable", "sending : started -- Transaction Type: " +
                                    offlineData.generateObjectFromData().getClass().getSimpleName() +
                                    " - with RefNo '" + offlineData.getReference_no() + "'");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

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

                            if(response instanceof JSONObject) {
                                JSONObject responseJson = ((JSONObject) response);
                                if(responseJson.has("id")) {
                                    Log.d("ImonggoSwable", "sending success : return ID : " +
                                            responseJson.getString("id"));
                                    offlineData.setReturnId(responseJson.getString("id"));
                                }
                            }

                            offlineData.setSynced(true);
                            offlineData.updateTo(getHelper());

                            if(swableStateListener != null && offlineData.isSynced())
                                swableStateListener.onSynced(offlineData);

                            if(offlineData.isSynced() && REQUEST_COUNT == REQUEST_SUCCESS)
                                NotificationTools.postNotification(ImonggoSwable.this, NOTIFICATION_ID, APP_ICON_DRAWABLE,
                                        getResources().getString(R.string.app_name), REQUEST_SUCCESS + " transaction"
                                                + (REQUEST_SUCCESS!=1? "s" : "") + " sent", null, getPendingIntent());

                            // TODO Remove
                            //SwableTools.voidTransaction(getHelper(), Integer.parseInt(offlineData.getReturnId()),
                            // OfflineDataType.CANCEL_INVOICE, "wrong order");
                            //SwableTools.voidTransaction(getHelper(),Integer.parseInt(offlineData.getReturnId())+1,
                            // OfflineDataType.CANCEL_INVOICE, "just because");

                            /*if(offlineData.getType() == OfflineData.ORDER)
                                SwableTools.voidTransaction(getHelper(),
                                    getHelper().getOfflineData().queryBuilder().where().eq("type", OfflineData.ORDER)
                                            .queryForFirst(),
                                    OfflineDataType.CANCEL_ORDER,
                                    "basta");*/
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }/* catch (SQLException e) {
                            e.printStackTrace();
                        }*/

                        if(offlineData.isSynced()) {
                            REQUEST_SUCCESS++;
                            Log.e("--- Request Success +1", ""+REQUEST_SUCCESS);
                        }
                    }

                    @Override
                    public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                        Log.e("ImonggoSwable", "sending failed : isConnected? " + hasInternet + " : error [" +
                                responseCode + "] : " + response);

                        offlineData.setSyncing(false);
                        offlineData.setQueued(false);
                        try {
                            if(responseCode == UNPROCESSABLE_ENTRY) {
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
                                        }
                                    }
                                }
                            }
                            else if(responseCode == UNAUTHORIZED_ACCESS) {
                                offlineData.setSynced(true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                        Log.e("ImonggoSwable", "sending failed : request error");
                        offlineData.setSyncing(false);
                        offlineData.setQueued(false);
                        offlineData.setSynced(false);
                    }
                }, getSession().getServer(), table, jsonObject, "?branch_id="+ offlineData.getBranch_id() + offlineData
                        .getParameters())
            );
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void delete(Table table, final OfflineData offlineData) {
        try {
            Branch branch = getHelper().getBranches().queryBuilder().where().eq("id", offlineData.getBranch_id())
                    .queryForFirst();
            if(branch == null || branch.getStatus().equalsIgnoreCase("D")) {
                Log.e("ImonggoSwable", "sending error : Branch '" + (branch == null? "NULL" : branch.getName()) + "'," +
                        " ID:" + (branch == null? "NULL" : branch.getId()) + "," + " was deleted or disabled");
                //return;
            }

            if(offlineData.parseReturnID().size() > 1) {
                pagedDelete(table,offlineData);
                return;
            }

            getQueue().add(
                HTTPRequests.sendDELETERequest(this, getSession(), new VolleyRequestListener() {
                        @Override
                        public void onStart(Table table, RequestType requestType) {
                            offlineData.setSyncing(true);
                            try {
                                Log.e("ImonggoSwable", "deleting : started -- Transaction Type: " +
                                        offlineData.generateObjectFromData().getClass().getSimpleName() +
                                        " - with RefNo '" + offlineData.getReference_no() +
                                        "' and returnId '" + offlineData.getReturnId() + "'");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

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

                            if(offlineData.isSynced() && REQUEST_COUNT == REQUEST_SUCCESS)
                                NotificationTools.postNotification(ImonggoSwable.this, NOTIFICATION_ID, APP_ICON_DRAWABLE,
                                        getResources().getString(R.string.app_name), REQUEST_SUCCESS + " transaction"
                                                + (REQUEST_SUCCESS!=1? "s" : "") + " sent", null, getPendingIntent());

                            if(offlineData.isSynced()) {
                                REQUEST_SUCCESS++;
                                Log.e("--- Request Success +1", ""+REQUEST_SUCCESS);
                            }
                        }

                        @Override
                        public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                            Log.e("ImonggoSwable", "deleting failed : isConnected? " + hasInternet + " : error [" +
                                    responseCode + "] : " + response);
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);
                            offlineData.setSynced(true);

                            if(responseCode == UNPROCESSABLE_ENTRY) {
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
        /*} catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    public void pagedSend(Table table, final OfflineData offlineData) {
        Gson gson = new Gson();
        try {
            if(table == Table.ORDERS) {
                Order order = (Order)offlineData.generateObjectFromData();
                List<Object> orderLines = new ArrayList<>();
                orderLines.addAll(order.getOrderLines());

                int max_size = Order.MAX_ORDERLINES_PER_PAGE;
                int max_page = SwableTools.computePagedRequestCount(order.getOrderLines().size(), max_size);

                if(offlineData.getReturnId().length() > 0) { // for retry sending
                    List<String> returnIds = offlineData.parseReturnID();
                    for (int i = 0; i < max_page; i++) {
                        if(returnIds.get(i).length() <= 0 || !returnIds.get(i).equals(NO_RETURN_ID))
                            continue;
                        String orderLineN = gson.toJson(partition(i, orderLines, max_size));
                        Order t_order = Order.fromJSONString(order.toJSONString());

                        Type type = new TypeToken<BatchList<OrderLine>>() {}.getType();
                        t_order.setOrderLines((BatchList<OrderLine>) gson.fromJson(orderLineN, type));

                        String paged_ref = t_order.getReference() + "-" + (i + 1);
                        t_order.setReference(paged_ref);

                        //Log.e("PAGEDSEND " + (i+1) + " of " + max_page, t_order.toJSONString());
                        sendThisPage(table, i + 1, max_page, prepareTransactionJSON(offlineData.getOfflineDataTransactionType
                                (), t_order.toJSONString()), offlineData);
                    }
                } else {
                    for (int i = 0; i < max_page; i++) {
                        String orderLineN = gson.toJson(partition(i, orderLines, max_size));
                        Order t_order = Order.fromJSONString(order.toJSONString());

                        Type type = new TypeToken<BatchList<OrderLine>>() {}.getType();
                        t_order.setOrderLines((BatchList<OrderLine>) gson.fromJson(orderLineN, type));

                        String paged_ref = t_order.getReference() + "-" + (i + 1);
                        t_order.setReference(paged_ref);

                        //Log.e("PAGEDSEND " + (i+1) + " of " + max_page, t_order.toJSONString());
                        sendThisPage(table, i + 1, max_page, prepareTransactionJSON(offlineData.getOfflineDataTransactionType
                                (), t_order.toJSONString()), offlineData);
                    }
                }
            }
            else if(table == Table.DOCUMENTS) {
                Document document = (Document)offlineData.generateObjectFromData();
                List<Object> documentLine = new ArrayList<>();
                documentLine.addAll(document.getDocument_lines());

                int max_size = Document.MAX_DOCUMENTLINES_PER_PAGE;
                int max_page = SwableTools.computePagedRequestCount(document.getDocument_lines().size(), max_size);

                if(offlineData.getReturnId().length() > 0) { // for retry sending
                    List<String> returnIds = offlineData.parseReturnID();
                    for (int i = 0; i < max_page; i++) {
                        if(returnIds.get(i).length() <= 0 || !returnIds.get(i).equals(NO_RETURN_ID))
                            continue;
                        String documentLineN = gson.toJson(partition(i, documentLine, max_size));
                        Document t_docu = Document.fromJSONString(document.toJSONString());

                        Type type = new TypeToken<BatchList<DocumentLine>>() {}.getType();
                        t_docu.setDocument_lines((BatchList<DocumentLine>) gson.fromJson(documentLineN, type));

                        String paged_ref = t_docu.getReference() + "-" + (i + 1);
                        t_docu.setReference(paged_ref);

                        //Log.e("PAGEDSEND " + (i+1) + " of " + max_page, t_order.toJSONString());
                        sendThisPage(table, i + 1, max_page, prepareTransactionJSON(offlineData.getOfflineDataTransactionType
                                (), t_docu.toJSONString()), offlineData);
                    }
                } else {
                    for (int i = 0; i < max_page; i++) {
                        String documentLineN = gson.toJson(partition(i, documentLine, max_size));
                        Document t_docu = Document.fromJSONString(document.toJSONString());

                        Type type = new TypeToken<BatchList<DocumentLine>>() {}.getType();
                        t_docu.setDocument_lines((BatchList<DocumentLine>) gson.fromJson(documentLineN, type));

                        String paged_ref = t_docu.getReference() + "-" + (i + 1);
                        t_docu.setReference(paged_ref);

                        //Log.e("PAGEDSEND " + (i+1) + " of " + max_page, t_order.toJSONString());
                        sendThisPage(table, i + 1, max_page, prepareTransactionJSON(offlineData.getOfflineDataTransactionType
                                (), t_docu.toJSONString()), offlineData);
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

                        try {
                            Log.e("ImonggoSwable", "sending : started [" + page + "] -- Transaction Type: " +
                                    parent.generateObjectFromData().getClass().getSimpleName() +
                                    " - Paged RefNo " + parent.getReference_no()+"-"+(page)
                            );
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

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
                                    parent.parseReturnID().size() == maxpage) {
                                Log.e("ImonggoSwable", "paged sending : returned ID's : " + parent.getReturnId() +
                                        " size : " + parent.parseReturnID().size());
                                if(swableStateListener != null)
                                    swableStateListener.onSynced(parent);

                                REQUEST_SUCCESS++;
                                Log.e("--- Request Success +1", ""+REQUEST_SUCCESS);

                                if(parent.isSynced() && REQUEST_COUNT == REQUEST_SUCCESS)
                                    NotificationTools.postNotification(ImonggoSwable.this, NOTIFICATION_ID, APP_ICON_DRAWABLE,
                                            getResources().getString(R.string.app_name), REQUEST_SUCCESS + " transactions" +
                                                    " sent", null, getPendingIntent());

                                // TODO Remove
                                SwableTools.voidTransaction(getHelper(),parent.parseReturnID().get(1),
                                        OfflineDataType.CANCEL_DOCUMENT,"test");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (SQLException e) {
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
                                parent.parseReturnID().size() == maxpage && responseCode != UNAUTHORIZED_ACCESS) {
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
        final List<String> list = offlineData.parseReturnID();
        try {
            for(final String id : list) {

                getQueue().add(
                    HTTPRequests.sendDELETERequest(this, getSession(), new VolleyRequestListener() {
                        @Override
                        public void onStart(Table table, RequestType requestType) {
                            offlineData.setSyncing(true);
                            try {
                                Log.e("ImonggoSwable", "deleting : started -- Transaction Type: " +
                                        offlineData.generateObjectFromData().getClass().getSimpleName() +
                                        " - with RefNo '" + offlineData.getReference_no() + "' and returnId '" + id + "'");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

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

    private static List<Object> partition(int nthPartition, List<Object> list, int size) {
        if(nthPartition < 0)
            throw new IllegalArgumentException("nthPartition can't be negative");
        if(size < 0)
            throw new IllegalArgumentException("size can't be negative");

        if(nthPartition > list.size()/size)
            throw new IndexOutOfBoundsException("can't create partition " + nthPartition + " of " + list.size()/size +
                    " allowed partitions for list with size " + list.size());

        int start = nthPartition * size;
        int end = Math.min(start + size, list.size());
        return list.subList(start, end);
    }
}
