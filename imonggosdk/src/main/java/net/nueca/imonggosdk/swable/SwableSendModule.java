package net.nueca.imonggosdk.swable;

import android.util.Log;

import com.android.volley.RequestQueue;

import net.nueca.imonggosdk.R;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.operations.http.HTTPRequests;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.NotificationTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by gama on 10/1/15.
 */
public class SwableSendModule {
    private ImonggoDBHelper dbHelper;
    private ImonggoSwable imonggoSwable;
    private RequestQueue requestQueue;
    private Session session;

    public SwableSendModule(ImonggoSwable imonggoSwable, ImonggoDBHelper helper, Session session, RequestQueue
            requestQueue) {
        this.imonggoSwable = imonggoSwable;
        this.dbHelper = helper;
        this.session = session;
        this.requestQueue = requestQueue;
    }

    public void sendTransaction(Table table, final OfflineData offlineData) {
        try {
            Branch branch = dbHelper.getBranches().queryBuilder().where().eq("id", offlineData.getBranch_id())
                    .queryForFirst();
            if(branch == null || branch.getStatus().equalsIgnoreCase("D")) {
                Log.e("ImonggoSwable", "sending error : Branch '" + (branch == null ? "NULL" : branch.getName()) + "'," +
                        " ID:" + (branch == null ? "NULL" : branch.getId()) + "," + " was deleted or disabled");
                //return;
            }

            if(offlineData.isPagedRequest()) {
                if(offlineData.isNewPagedSend()) {
                    if (offlineData.getObjectFromData() == null) {
                        offlineData.deleteTo(dbHelper);
                        return;
                    }
                    sendFirstPage(table, offlineData);
                } else
                    pagedSend(table, offlineData);
                return;
            }

            JSONObject jsonObject = SwableTools.prepareTransactionJSON(offlineData.getOfflineDataTransactionType(),
                    offlineData.getData());
            //Log.e("JSON", jsonObject.toString());

            requestQueue.cancelAll(offlineData.getId());
            requestQueue.add(
                    HTTPRequests.sendPOSTRequest(imonggoSwable, session, new VolleyRequestListener() {
                        @Override
                        public void onStart(Table table, RequestType requestType) {
                            offlineData.setSyncing(true);

                            Log.e("ImonggoSwable", "sending : started -- Transaction Type: " +
                                    offlineData.getObjectFromData().getClass().getSimpleName() +
                                    " - with RefNo '" + offlineData.getReference_no() + "'");

                            if (imonggoSwable.getSwableStateListener() != null)
                                imonggoSwable.getSwableStateListener().onSyncing(offlineData);
                        }

                        @Override
                        public void onSuccess(Table table, RequestType requestType, Object response) {
                            AccountTools.updateUserActiveStatus(imonggoSwable, true);

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
                                offlineData.updateTo(dbHelper);

                                if (imonggoSwable.getSwableStateListener() != null && offlineData.isSynced())
                                    imonggoSwable.getSwableStateListener().onSynced(offlineData);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }/* catch (SQLException e) {
                            e.printStackTrace();
                        }*/

                            if (offlineData.isSynced()) {
                                imonggoSwable.REQUEST_SUCCESS++;
                                Log.e("--- Request Success +1", "" + imonggoSwable.REQUEST_SUCCESS);
                            }
                            Log.e("REQUEST", imonggoSwable.REQUEST_COUNT + " " + imonggoSwable.REQUEST_SUCCESS);
                            if (offlineData.isSynced() && imonggoSwable.REQUEST_COUNT == imonggoSwable.REQUEST_SUCCESS)
                                NotificationTools.postNotification(imonggoSwable,
                                        ImonggoSwable.NOTIFICATION_ID,
                                        imonggoSwable.getNotificationIcon(),
                                        imonggoSwable.getResources().getString(R.string.app_name),
                                        imonggoSwable.REQUEST_SUCCESS +" transaction" +
                                                (imonggoSwable.REQUEST_SUCCESS != 1 ? "s" : "") + " sent",
                                        null,
                                        imonggoSwable.getPendingIntent());
                        }

                        @Override
                        public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                            Log.e("ImonggoSwable", "sending failed : isConnected? " + hasInternet + " : error [" +
                                    responseCode + "] : " + response);

                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);

                            try {
                                if (responseCode == ImonggoSwable.UNPROCESSABLE_ENTRY) {
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
                                            } else if (errorMsg.contains("document id")) {
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
                                                } else if (errorMsg.contains("document id")) {
                                                    String documentId = errorMsg.substring(
                                                            errorMsg.indexOf("[") + 1, errorMsg.indexOf("]")
                                                    );
                                                    Log.e("STR : SEND_DOCUMENT ID", documentId);
                                                    offlineData.setReturnId(documentId);
                                                }
                                            }
                                        }
                                    }
                                } else if (responseCode == ImonggoSwable.UNAUTHORIZED_ACCESS) {
                                    offlineData.setSynced(true);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            offlineData.updateTo(dbHelper);

                            if (imonggoSwable.getSwableStateListener() != null) {
                                if (responseCode == ImonggoSwable.UNAUTHORIZED_ACCESS) {
                                    AccountTools.updateUserActiveStatus(imonggoSwable, false);
                                    imonggoSwable.getSwableStateListener().onUnauthorizedAccess(response, responseCode);
                                } else
                                    imonggoSwable.getSwableStateListener().onSyncProblem(offlineData, hasInternet, response, responseCode);
                            }

                            if (offlineData.isSynced() && responseCode != ImonggoSwable.UNAUTHORIZED_ACCESS) {
                                imonggoSwable.REQUEST_SUCCESS++;
                                Log.e("--- Request Success +1", "" + imonggoSwable.REQUEST_SUCCESS);
                            }
                        }

                        @Override
                        public void onRequestError() {
                            Log.e("ImonggoSwable", "sending failed : request error");
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);
                            offlineData.setSynced(false);
                            offlineData.updateTo(dbHelper);
                        }
                    }, session.getServer(), table, jsonObject, "?branch_id=" + offlineData.getBranch_id() + offlineData
                            .getParameters()).setTag(offlineData.getId())
            );
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                        if(returnIds.get(i).length() <= 0 || !returnIds.get(i).equals(ImonggoSwable.NO_RETURN_ID))
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
                        if(returnIds.get(i).length() <= 0 || !returnIds.get(i).equals(ImonggoSwable.NO_RETURN_ID))
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

        requestQueue.cancelAll(parent.getId() + "-" + page);
        requestQueue.add(
                HTTPRequests.sendPOSTRequest(imonggoSwable, session, new VolleyRequestListener() {
                    @Override
                    public void onStart(Table table, RequestType requestType) {
                        parent.setSyncing(true);

                        Log.e("ImonggoSwable", "sending : started [" + page + "] -- Transaction Type: " +
                                        parent.getObjectFromData().getClass().getSimpleName() +
                                        " - Paged RefNo " + parent.getReference_no() + "-" + (page)
                        );

                        if (imonggoSwable.getSwableStateListener() != null)
                            imonggoSwable.getSwableStateListener().onSyncing(parent);

                        Log.e("Paging", page + " of " + maxpage);
                    }

                    @Override
                    public void onSuccess(Table table, RequestType requestType, Object response) {
                        AccountTools.updateUserActiveStatus(imonggoSwable, true);

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
                            parent.updateTo(dbHelper);

                            if (parent.isSynced() && !parent.getReturnId().contains(ImonggoSwable.NO_RETURN_ID) &&
                                    parent.getReturnIdList().size() == maxpage) {
                                Log.e("ImonggoSwable", "paged sending : returned ID's : " + parent.getReturnId() +
                                        " size : " + parent.getReturnIdList().size());
                                if (imonggoSwable.getSwableStateListener() != null)
                                    imonggoSwable.getSwableStateListener().onSynced(parent);

                                imonggoSwable.REQUEST_SUCCESS++;
                                Log.e("--- Request Success +1", "" + imonggoSwable.REQUEST_SUCCESS);

                                if (parent.isSynced() && imonggoSwable.REQUEST_COUNT == imonggoSwable.REQUEST_SUCCESS)
                                    NotificationTools.postNotification(imonggoSwable,
                                            ImonggoSwable.NOTIFICATION_ID,
                                            imonggoSwable.getNotificationIcon(),
                                            imonggoSwable.getResources().getString(R.string.app_name),
                                            imonggoSwable.REQUEST_SUCCESS + " " + "transactions" + " sent",
                                            null,
                                            imonggoSwable.getPendingIntent());

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                        Log.e("ImonggoSwable", "sending failed [" + page + "] : isConnected? " + hasInternet + " : error [" +
                                responseCode + "] : " + response);

                        if (page == maxpage) {
                            parent.setSyncing(false);
                            parent.setQueued(false);
                        }

                        boolean isNullReturnId = true;

                        try {
                            if (responseCode == ImonggoSwable.UNPROCESSABLE_ENTRY) {
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
                                        } else if (errorMsg.contains("document id")) {
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
                            if (isNullReturnId) {
                                parent.insertReturnIdAt(page - 1, ImonggoSwable.NO_RETURN_ID);
                                parent.setSynced(responseCode == ImonggoSwable.UNAUTHORIZED_ACCESS);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        parent.updateTo(dbHelper);

                        if (imonggoSwable.getSwableStateListener() != null) {
                            if (responseCode == ImonggoSwable.UNAUTHORIZED_ACCESS) {
                                AccountTools.updateUserActiveStatus(imonggoSwable, false);
                                imonggoSwable.getSwableStateListener().onUnauthorizedAccess(response, responseCode);
                            } else
                                imonggoSwable.getSwableStateListener().onSyncProblem(parent, hasInternet, response, responseCode);
                        }

                        if (parent.isSynced() && !parent.getReturnId().contains(ImonggoSwable.NO_RETURN_ID) &&
                                parent.getReturnIdList().size() == maxpage && responseCode != ImonggoSwable
                                .UNAUTHORIZED_ACCESS) {
                            imonggoSwable.REQUEST_SUCCESS++;
                            Log.e("--- Request Success +1", "" + imonggoSwable.REQUEST_SUCCESS);
                        }
                    }

                    @Override
                    public void onRequestError() {
                        Log.e("ImonggoSwable", "sending failed [" + page + "] : request error");
                        parent.setSyncing(false);
                        parent.setQueued(false);
                        parent.setSynced(false);
                        parent.updateTo(dbHelper);
                    }
                }, session.getServer(), table, jsonObject, "?branch_id=" + parent.getBranch_id() + parent
                        .getParameters())
                        .setTag(parent.getId()+ "-" +page)
        );
    }

    private void sendFirstPage(Table table, final OfflineData offlineData) throws JSONException {
        Log.e("SwableSendModule", "sendFirstPage : paged sending start");

        final int page = 1;
        final int maxpage = offlineData.getPagedRequestCount();

        Log.e("SwableSendModule", "sendFirstPage : current returned ID:" + offlineData.getReturnId());

        if(offlineData.getReturnIdList() != null && offlineData.getReturnIdList().size() > 0) {
            if(offlineData.getReturnIdList().get(0).length() > 0 && !offlineData.getReturnIdList().get(0).equals("@")) {
                queueNonLastPage(table, offlineData, offlineData.getReturnIdList().get(page - 1));
                return;
            }
        }

        JSONObject jsonObject;
        if(table == Table.ORDERS) {
            Order firstPage = (Order)offlineData.getObjectFromData();
            firstPage.setOrder_lines(firstPage.getOrderLineAt(0));
            jsonObject = firstPage.toJSONObject();
        }
        else if(table == Table.DOCUMENTS) {
            Document firstPage = (Document)offlineData.getObjectFromData();
            firstPage.setDocument_lines(firstPage.getDocumentLineAt(0));
            jsonObject = firstPage.toJSONObject();
        }
        else return;

        Log.e("SwableSendModule", "sendFirstPage : reference -> " + jsonObject.get("reference"));
        jsonObject = SwableTools.prepareTransactionJSON(offlineData.getOfflineDataTransactionType(), jsonObject);

        requestQueue.cancelAll(offlineData.getId()+ "-" +page);
        requestQueue.add(
                HTTPRequests.sendPOSTRequest2(imonggoSwable, session, new VolleyRequestListener() {
                    @Override
                    public void onStart(Table table, RequestType requestType) {
                        offlineData.setSyncing(true);

                        Log.e("ImonggoSwable", "sending : started [" + page + "] -- Transaction Type: " +
                                        offlineData.getObjectFromData().getClass().getSimpleName() +
                                        " - Paged RefNo " + offlineData.getReference_no() + "-" + (page)
                        );

                        if (imonggoSwable.getSwableStateListener() != null)
                            imonggoSwable.getSwableStateListener().onSyncing(offlineData);

                        Log.e("Paging", page + " of " + maxpage);
                    }

                    @Override
                    public void onSuccess(Table table, RequestType requestType, Object response) {
                        AccountTools.updateUserActiveStatus(imonggoSwable, true);

                        Log.e("ImonggoSwable", "sending success [" + page + "] : " + response);
                        try {

                            if (response instanceof JSONObject) {
                                JSONObject responseJson = ((JSONObject) response);
                                if (responseJson.has("id")) {
                                    Log.d("ImonggoSwable", "sending success : return ID : " +
                                            responseJson.getString("id"));

                                    offlineData.insertReturnIdAt(page - 1, responseJson.getString("id"));
                                }
                            }

                            offlineData.setSynced(offlineData.isAllPageSynced());
                            offlineData.setSyncing(true);
                            offlineData.setQueued(true);
                            offlineData.updateTo(dbHelper);

                            queueNonLastPage(table, offlineData, offlineData.getReturnIdList().get(page - 1));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                        Log.e("ImonggoSwable", "sending failed [" + page + "] : isConnected? " + hasInternet + " : error [" +
                                responseCode + "] : " + response);

                        boolean isNullReturnId = true;

                        try {
                            if (responseCode == ImonggoSwable.UNPROCESSABLE_ENTRY) {
                                if (response instanceof String) {
                                    String errorMsg = ((String) response).toLowerCase();
                                    if (errorMsg.contains("reference has already been taken")) {
                                        offlineData.setSynced(offlineData.isAllPageSynced());
                                        offlineData.setForConfirmation(true);

                                        if (errorMsg.contains("order id")) {
                                            String orderId = errorMsg.substring(
                                                    errorMsg.indexOf("[") + 1, errorMsg.indexOf("]")
                                            );
                                            Log.e("STR : SEND_ORDER ID", orderId);

                                            offlineData.insertReturnIdAt(page - 1, orderId);
                                            isNullReturnId = false;
                                        } else if (errorMsg.contains("document id")) {
                                            String documentId = errorMsg.substring(
                                                    errorMsg.indexOf("[") + 1, errorMsg.indexOf("]")
                                            );
                                            Log.e("STR : SEND_DOCUMENT ID", documentId);

                                            offlineData.insertReturnIdAt(page - 1, documentId);
                                            isNullReturnId = false;
                                        }
                                    }
                                } else if (response instanceof JSONObject) {
                                    JSONObject responseJson = (JSONObject) response;
                                    if (responseJson.has("error")) {
                                        String errorMsg = responseJson.getString("error").toLowerCase();

                                        if (errorMsg.contains("reference has already been taken")) {
                                            offlineData.setSynced(offlineData.isAllPageSynced());
                                            offlineData.setForConfirmation(true);

                                            if (errorMsg.contains("order id")) {
                                                String orderId = errorMsg.substring(
                                                        errorMsg.indexOf("[") + 1, errorMsg.indexOf("]"));
                                                Log.e("JSON : SEND_ORDER ID", orderId);

                                                offlineData.insertReturnIdAt(page - 1, orderId);
                                                isNullReturnId = false;
                                            }
                                        }
                                    }
                                }
                            }
                            if (isNullReturnId) {
                                offlineData.insertReturnIdAt(page - 1, ImonggoSwable.NO_RETURN_ID);
                                offlineData.setSynced(responseCode == ImonggoSwable.UNAUTHORIZED_ACCESS);
                                offlineData.setSyncing(false);
                                offlineData.setQueued(false);
                            }
                            else {
                                if(!offlineData.isAllPageSynced())
                                    queueNonLastPage(table, offlineData, offlineData.getReturnIdList().get(page - 1));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        offlineData.updateTo(dbHelper);

                        if (imonggoSwable.getSwableStateListener() != null) {
                            if (responseCode == ImonggoSwable.UNAUTHORIZED_ACCESS) {
                                AccountTools.updateUserActiveStatus(imonggoSwable, false);
                                imonggoSwable.getSwableStateListener().onUnauthorizedAccess(response, responseCode);
                            } else
                                imonggoSwable.getSwableStateListener().onSyncProblem(offlineData, hasInternet, response, responseCode);
                        }
                    }

                    @Override
                    public void onRequestError() {
                        Log.e("ImonggoSwable", "sending failed [" + page + "] : request error");
                        offlineData.setSyncing(false);
                        offlineData.setQueued(false);
                        offlineData.setSynced(false);
                        offlineData.updateTo(dbHelper);
                    }
                }, session.getServer(), table, jsonObject, offlineData.getBranch_id(), offlineData.getParameters())
                .setTag(offlineData.getId()+ "-" +page)
        );
    }

    private void queueNonLastPage(Table table, final OfflineData offlineData, final String id) throws JSONException {
        Log.e("SwableSendModule", "queueNonLastPage : page count:" + offlineData.getPagedRequestCount());
        if(offlineData.getPagedRequestCount() == 2) {
            sendNextPage(table, offlineData, id, 2);
            return;
        }
        List<String> idList = offlineData.getReturnIdList();
        for(int i = 1; i < offlineData.getPagedRequestCount()-1; i++) {
            if(i >= idList.size() || ( i < idList.size() && idList.get(i).equals("@") )) {
                Log.e("SwableSendModule", "queueNonLastPage : queueing page:" + (i+1) + " i:"+ i + " size:"+idList
                        .size() + " retId:" + (idList.size() <= i ? "@" : idList.get(i)));
                sendNextPage(table, offlineData, id, i + 1);
            }
        }
    }

    /** Page starts from 1 **/
    private void sendNextPage(Table table, final OfflineData offlineData, final String id, final int page) throws
            JSONException {
        Log.e("SwableSendModule", "sendNextPage : page:" + page);
        if(page == 1)
            return;

        Log.e("SwableSendModule", "sendNextPage : started");
        final int maxpage = offlineData.getPagedRequestCount();
        JSONObject jsonObject;
        if(table == Table.ORDERS) {
            Order.Builder orderBuilder = new Order.Builder()
                    .order_lines(((Order) offlineData.getObjectFromData()).getOrderLineAt(page - 1));
            if(page == maxpage)
                orderBuilder.reference(offlineData.getReference_no());
            jsonObject = orderBuilder.build().toJSONObject();
        }
        else if(table == Table.DOCUMENTS) {
            Document.Builder documentBuilder = new Document.Builder()
                    .document_lines(((Document) offlineData.getObjectFromData()).getDocumentLineAt(page - 1));
            if(page == maxpage)
                documentBuilder.reference(offlineData.getReference_no());
            jsonObject = documentBuilder.build().toJSONObject();
        }
        else return;

        Log.e("SwableSendModule", "sendNextPage : reference -> " + jsonObject.get("reference") + " page:" + page);
        jsonObject = SwableTools.prepareTransactionJSON(offlineData.getOfflineDataTransactionType(), jsonObject);

        requestQueue.cancelAll(offlineData.getId()+ "-" +page);
        requestQueue.add(
                HTTPRequests.sendPUTRequest2(imonggoSwable, session, new VolleyRequestListener() {
                    @Override
                    public void onStart(Table table, RequestType requestType) {
                        offlineData.setSyncing(true);

                        Log.e("ImonggoSwable", "sending : started [" + page + "] -- Transaction Type: " +
                                        offlineData.getObjectFromData().getClass().getSimpleName() +
                                        " - Paged RefNo " + offlineData.getReference_no() + "-" + (page)
                        );

                        if (imonggoSwable.getSwableStateListener() != null)
                            imonggoSwable.getSwableStateListener().onSyncing(offlineData);

                        Log.e("Paging", page + " of " + maxpage);
                    }

                    @Override
                    public void onSuccess(Table table, RequestType requestType, Object response) {
                        AccountTools.updateUserActiveStatus(imonggoSwable, true);

                        Log.e("ImonggoSwable", "sending success [" + page + "] : " + response);
                        try {
                            if (page == maxpage) {
                                offlineData.setSyncing(false);
                                offlineData.setQueued(false);
                            }

                            if (response instanceof JSONObject) {
                                JSONObject responseJson = ((JSONObject) response);
                                if (responseJson.has("id")) {
                                    Log.d("ImonggoSwable", "sending success : return ID : " +
                                            responseJson.getString("id"));

                                    offlineData.insertReturnIdAt(page - 1, responseJson.getString("id"));
                                }
                            }

                            offlineData.setSynced(offlineData.isAllPageSynced());
                            offlineData.updateTo(dbHelper);

                            if(!offlineData.getReturnId().contains(ImonggoSwable.NO_RETURN_ID) &&
                                    offlineData.getReturnIdList().size() >= maxpage-1 && page != maxpage) {
                                sendNextPage(table, offlineData, id, maxpage);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                        Log.e("ImonggoSwable", "sending failed [" + page + "] : isConnected? " + hasInternet + " : error [" +
                                responseCode + "] : " + response);

                        if(!hasInternet) {
                            offlineData.setSynced(false);
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);
                            offlineData.updateTo(dbHelper);
                            return;
                        }

                        if (page == maxpage) {
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);
                        }

                        boolean isNullReturnId = true;

                        try {
                            if (responseCode == ImonggoSwable.UNPROCESSABLE_ENTRY) {
                                if (response instanceof String) {
                                    Log.e("SwableSendModule", "sendNextPage : onError : response type String");
                                    String errorMsg = ((String) response).toLowerCase();
                                    if (errorMsg.contains("reference has already been taken")) {
                                        offlineData.setSynced(offlineData.isAllPageSynced());
                                        offlineData.setForConfirmation(true);

                                        if (errorMsg.contains("order id")) {
                                            String orderId = errorMsg.substring(
                                                    errorMsg.indexOf("[") + 1, errorMsg.indexOf("]")
                                            );
                                            Log.e("STR : SEND_ORDER ID", orderId);

                                            offlineData.insertReturnIdAt(page - 1, orderId);
                                            isNullReturnId = false;
                                        } else if (errorMsg.contains("document id")) {
                                            String documentId = errorMsg.substring(
                                                    errorMsg.indexOf("[") + 1, errorMsg.indexOf("]")
                                            );
                                            Log.e("STR : SEND_DOCUMENT ID", documentId);

                                            offlineData.insertReturnIdAt(page - 1, documentId);
                                            isNullReturnId = false;
                                        }
                                    } else if(errorMsg.contains("already posted or voided")) {
                                        offlineData.setSynced(true);
                                        offlineData.setForConfirmation(true);
                                        if(offlineData.getReturnIdListAt(page - 1) == null)
                                            offlineData.insertReturnIdAt(page - 1, ImonggoSwable.NO_RETURN_ID);
                                        isNullReturnId = false;
                                    }
                                } else if (response instanceof JSONObject) {
                                    Log.e("SwableSendModule", "sendNextPage : onError : response type JSONObject");
                                    JSONObject responseJson = (JSONObject) response;
                                    if (responseJson.has("error")) {
                                        String errorMsg = responseJson.getString("error").toLowerCase();

                                        if (errorMsg.contains("reference has already been taken")) {
                                            offlineData.setSynced(offlineData.isAllPageSynced());
                                            offlineData.setForConfirmation(true);

                                            if (errorMsg.contains("order id")) {
                                                String orderId = errorMsg.substring(
                                                        errorMsg.indexOf("[") + 1, errorMsg.indexOf("]"));
                                                Log.e("JSON : SEND_ORDER ID", orderId);

                                                offlineData.insertReturnIdAt(page - 1, orderId);
                                                isNullReturnId = false;
                                            }
                                        } else if (errorMsg.contains("document id")) {
                                            String documentId = errorMsg.substring(
                                                    errorMsg.indexOf("[") + 1, errorMsg.indexOf("]")
                                            );
                                            Log.e("STR : SEND_DOCUMENT ID", documentId);

                                            offlineData.insertReturnIdAt(page - 1, documentId);
                                            isNullReturnId = false;
                                        }
                                    } else if(responseJson.has("base")) {
                                        String errorMsg = responseJson.getString("base").toLowerCase();
                                        if(errorMsg.contains("already posted or voided")) {
                                            offlineData.setSynced(true);
                                            offlineData.setForConfirmation(true);
                                            if(offlineData.getReturnIdListAt(page - 1) == null)
                                                offlineData.insertReturnIdAt(page - 1, ImonggoSwable.NO_RETURN_ID);
                                            isNullReturnId = false;
                                        }
                                    }
                                }
                            }
                            if (isNullReturnId) {
                                offlineData.insertReturnIdAt(page - 1, ImonggoSwable.NO_RETURN_ID);
                                offlineData.setSynced(responseCode == ImonggoSwable.UNAUTHORIZED_ACCESS);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        offlineData.updateTo(dbHelper);

                        if (imonggoSwable.getSwableStateListener() != null) {
                            if (responseCode == ImonggoSwable.UNAUTHORIZED_ACCESS) {
                                AccountTools.updateUserActiveStatus(imonggoSwable, false);
                                imonggoSwable.getSwableStateListener().onUnauthorizedAccess(response, responseCode);
                            } else
                                imonggoSwable.getSwableStateListener().onSyncProblem(offlineData, hasInternet, response, responseCode);
                        }
                    }

                    @Override
                    public void onRequestError() {
                        Log.e("ImonggoSwable", "sending failed [" + page + "] : request error");
                        offlineData.setSyncing(false);
                        offlineData.setQueued(false);
                        offlineData.setSynced(false);
                        offlineData.updateTo(dbHelper);
                    }
                }, session.getServer(), table, jsonObject, id, offlineData.getBranch_id(), page == maxpage, offlineData
                        .getParameters()).setTag(offlineData.getId()+ "-" +page)
        );
    }
}
