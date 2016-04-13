package net.nueca.imonggosdk.swable;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.google.gson.Gson;

import net.nueca.imonggosdk.R;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.invoice.Invoice;
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
public class SwableSendModule extends BaseSwableModule {

    public SwableSendModule(ImonggoSwable imonggoSwable, ImonggoDBHelper2 helper, Session session, RequestQueue requestQueue) {
        super(imonggoSwable, helper, session, requestQueue);
    }

    public void sendTransaction(Table table, final OfflineData offlineData) {
        if(queueTracker.containsKey(offlineData.getId()))
            return;
        queueTracker.put(offlineData.getId(), offlineData);
        //QUEUED_TRANSACTIONS++;
        Log.e("SwableSendModule", "sendTransaction " + table.getStringName() + " " + offlineData.getType());
        //Log.e("SwableSendModule", "sendTransaction " + offlineData.getObjectFromData().toString());
        try {
            Branch branch = dbHelper.fetchObjects(Branch.class).queryBuilder().where().eq("id", offlineData.getBranch_id())
                    .queryForFirst();
            if(branch == null || branch.getStatus().equalsIgnoreCase("D")) {
                Log.e("ImonggoSwable", "sending error : Branch '" + (branch == null ? "NULL" : branch.getName()) + "'," +
                        " ID:" + (branch == null ? "NULL" : branch.getId()) + "," + " was deleted or disabled");
                //return;
            }

            if(offlineData.isPagedRequest()) {
                if(offlineData.isNewPagedSend()) {
                    if (offlineData.getObjectFromData() == null) {
                        Log.e("ImonggoSwable", "sending error : object is null ~ deleting offlinedata");
                        offlineData.deleteTo(dbHelper);
                        queueTracker.remove(offlineData.getId());
                        //QUEUED_TRANSACTIONS--;
                        return;
                    }
                    sendFirstPage(table, offlineData);
                } else
                    pagedSend(table, offlineData);
                return;
            }

            Gson gson = new Gson();
            String jsonStr = gson.toJson(offlineData.getObjectFromData());
            Log.e("SwableSendModule", "sendTransaction : non-paging : "+jsonStr);
            if(jsonStr.length() > 1000) {
                int chunkCount = jsonStr.length() / 1000;
                for (int i = 0; i <= chunkCount; i++) {
                    int max = 1000 * (i + 1);
                    if (max >= jsonStr.length()) {
                        Log.e("SwableSendModule", jsonStr.substring(1000 * i));
                    } else {
                        Log.e("SwableSendModule", jsonStr.substring(1000 * i, max));
                    }
                }
            }
            JSONObject jsonObject;

            //final List<Integer> forSendingBatch = new ArrayList<>();

            if(offlineData.getType() == OfflineData.INVOICE) {
                Invoice invoice = offlineData.getObjectFromData(Invoice.class);
                //invoice.createNewPaymentBatch();
                //invoice.updateTo(dbHelper);
                //invoice.joinAllNewToCurrentPaymentBatch();
                Log.e("Customer", invoice.getCustomer() == null? "null" : invoice.getCustomer().getId() + " "
                        + invoice.getCustomer().getReturnId());
                //invoice.setPayments(invoice.getUnsentBatchPayment(offlineData.getSentPaymentBatch()));
                //invoice.setPayments(invoice.getNewBatchPayment());
                invoice.setPayments(invoice.getUnmarkedPayments());

                //for(InvoicePayment payment : invoice.getPayments())
                //    forSendingBatch.add(payment.getPaymentBatchNo());

                jsonObject = SwableTools.prepareTransactionJSON(offlineData.getType(),
                        invoice.toJSONObject());
            }
            else {
                jsonObject = SwableTools.prepareTransactionJSON(offlineData.getType(),
                        offlineData.getData());
            }
            Log.e("SwableSendModule", "sendTransaction : non-paging : "+jsonObject.toString());
            //Log.e("JSON", jsonObject.toString());

            requestQueue.cancelAll(offlineData.getId());
            requestQueue.add(
                    HTTPRequests.sendPOSTRequest(imonggoSwable, session, new VolleyRequestListener() {
                                @Override
                                public void onStart(Table table, RequestType requestType) {
                                    offlineData.setSyncing(true);
                                    offlineData.setStatusLog("syncing started");

                                    Log.e("ImonggoSwable", "sending : started -- Transaction Type: " +
                                            offlineData.getObjectFromData().getClass().getSimpleName() +
                                            " - with RefNo '" + offlineData.getReference_no() + "'");

                                    offlineData.updateTo(dbHelper);
                                    if (imonggoSwable.getSwableStateListener() != null)
                                        imonggoSwable.getSwableStateListener().onSyncing(offlineData);
                                }

                                @Override
                                public void onSuccess(Table table, RequestType requestType, Object response) {
                                    queueTracker.remove(offlineData.getId());
                                    //QUEUED_TRANSACTIONS--;
                                    AccountTools.updateUserActiveStatus(imonggoSwable, true);

                                    Log.e("ImonggoSwable", "sending success : " + response);
                                    try {
                                        offlineData.setSyncing(false);
                                        offlineData.setQueued(false);
                                        offlineData.setStatusLog("sending success");

                                        if (response instanceof JSONObject) {
                                            JSONObject responseJson = ((JSONObject) response);
                                            if (responseJson.has("id")) {
                                                Log.d("ImonggoSwable", "sending success : return ID : " +
                                                        responseJson.getString("id"));
                                                offlineData.setReturnId(responseJson.getString("id"));

                                                if (offlineData.getType() == OfflineData.INVOICE) {
                                                    Invoice invoice = offlineData.getObjectFromData(Invoice.class);
                                                    invoice.markSentPayment(Integer.parseInt(responseJson.getString("id")));
                                                    invoice.updateTo(dbHelper);
                                                }
                                            }
                                            if (offlineData.getType() == OfflineData.INVOICE && responseJson.has("customer_points")) {
                                                Invoice invoice = offlineData.getObjectFromData(Invoice.class);
                                                Customer customer = invoice.getCustomer();
                                                customer.setAvailable_points(responseJson.getString("customer_points"));
                                                customer.updateTo(dbHelper);
                                            }
                                        }

                                        offlineData.setSynced(true);
                                        Log.e("SwableSendModule " + 120, "updating offlineData <<<<<<<<<<<<<<<<<<<<<<");
                                        offlineData.updateTo(dbHelper);

                                        if (imonggoSwable.getSwableStateListener() != null && offlineData.isSynced())
                                            imonggoSwable.getSwableStateListener().onSynced(offlineData);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }/* catch (SQLException e) {
                        e.printStackTrace();
                    }*/

                                    if (offlineData.isSynced()) {
                                        SUCCESS_TRANSACTIONS++;
                                        Log.e("--- Request Success +1", "" + SUCCESS_TRANSACTIONS);
                                        if (offlineData.getOfflineDataTransactionType().isVoiding()) {
                                            offlineData.setSynced(false);
                                            Log.e("SwableSendModule " + 137, "updating offlineData <<<<<<<<<<<<<<<<<<<<<<");
                                            offlineData.updateTo(dbHelper);
                                        }
                                    }
                                    Log.e("REQUEST", getQueueTrackerCount() + " " + SUCCESS_TRANSACTIONS);
                                    if (offlineData.isSynced() && getQueueTrackerCount() == 0)
                                        NotificationTools.postNotification(imonggoSwable,
                                                ImonggoSwable.NOTIFICATION_ID,
                                                APP_ICON_DRAWABLE,
//                                        imonggoSwable.getNotificationIcon(),
                                                imonggoSwable.getResources().getString(R.string.app_name),
                                                SUCCESS_TRANSACTIONS + " transaction" +
                                                        (SUCCESS_TRANSACTIONS != 1 ? "s" : "") + " sent",
                                                null,
                                                imonggoSwable.getPendingIntent());
                                }

                                @Override
                                public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                                    queueTracker.remove(offlineData.getId());
                                    //QUEUED_TRANSACTIONS--;
                                    Log.e("ImonggoSwable", "sending failed : isConnected? " + hasInternet + " : error [" +
                                            responseCode + "] : " + response);

                                    offlineData.setSyncing(false);
                                    offlineData.setQueued(false);
                                    offlineData.setStatusLog("sending failed : isConnected? " + hasInternet + " : error [" +
                                            responseCode + "] : " + response);

                                    try {
                                        if (responseCode == ImonggoSwable.UNPROCESSABLE_ENTRY) {
                                            if (response instanceof String) {
                                                String errorMsg = ((String) response).toLowerCase();
                                                if (errorMsg.contains("reference has already been taken")) {
                                                    offlineData.setSynced(true);
                                                    offlineData.setForConfirmation(true);

                                                    if (errorMsg.contains("order id") || errorMsg.contains("document id")) {
                                                        String id = errorMsg.substring(
                                                                errorMsg.indexOf("[") + 1, errorMsg.indexOf("]")
                                                        );
                                                        offlineData.setReturnId(id);
                                                    }
                                                }
                                                if (errorMsg.contains("not a valid writable field")) {
                                                    offlineData.setSynced(false);
                                                    offlineData.setCancelled(true);
                                                }
                                                if (errorMsg.contains("already posted for this layaway")) {
                                                    offlineData.setSynced(true);
                                                    //offlineData.setForConfirmation(true);

                                                    String layawayId = errorMsg.substring(
                                                            errorMsg.indexOf("[") + 1, errorMsg.indexOf("]")
                                                    );
                                                    offlineData.setReturnId(layawayId);
                                                    Invoice invoice = offlineData.getObjectFromData(Invoice.class);
                                                    invoice.markSentPayment(Integer.parseInt(layawayId));
                                                    invoice.updateTo(dbHelper);
                                                }
                                            } else if (response instanceof JSONObject) {
                                                JSONObject responseJson = (JSONObject) response;
                                                if (responseJson.has("error")) {
                                                    String errorMsg = responseJson.getString("error").toLowerCase();

                                                    if (errorMsg.contains("reference has already been taken")) {
                                                        offlineData.setSynced(true);
                                                        offlineData.setForConfirmation(true);

                                                        if (errorMsg.contains("order id") || errorMsg.contains("document id")) {
                                                            String id = errorMsg.substring(
                                                                    errorMsg.indexOf("[") + 1, errorMsg.indexOf("]")
                                                            );
                                                            offlineData.setReturnId(id);
                                                        }
                                                    }
                                                    if (errorMsg.contains("not a valid writable field")) {
                                                        offlineData.setSynced(false);
                                                        offlineData.setCancelled(true);
                                                    }
                                                    if (errorMsg.contains("already posted for this layaway")) {
                                                        offlineData.setSynced(true);
                                                        //offlineData.setForConfirmation(true);

                                                        String layawayId = errorMsg.substring(
                                                                errorMsg.indexOf("[") + 1, errorMsg.indexOf("]")
                                                        );
                                                        offlineData.setReturnId(layawayId);
                                                        Invoice invoice = offlineData.getObjectFromData(Invoice.class);
                                                        invoice.markSentPayment(Integer.parseInt(layawayId));
                                                        invoice.updateTo(dbHelper);
                                                    }
                                                }
                                            }
                                        } else if (responseCode == ImonggoSwable.UNAUTHORIZED_ACCESS) {
                                            offlineData.setSynced(true);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Log.e("SwableSendModule " + 225, "updating offlineData <<<<<<<<<<<<<<<<<<<<<<");
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
                                        if (offlineData.getOfflineDataTransactionType().isVoiding()) {
                                            offlineData.setSynced(false);
                                            Log.e("SwableSendModule " + 241, "updating offlineData <<<<<<<<<<<<<<<<<<<<<<");
                                            offlineData.updateTo(dbHelper);
                                        }
                                    }
                                }

                                @Override
                                public void onRequestError() {
                                    queueTracker.remove(offlineData.getId());
                                    //QUEUED_TRANSACTIONS--;
                                    Log.e("ImonggoSwable", "sending failed : request error");
                                    offlineData.setSyncing(false);
                                    offlineData.setQueued(false);
                                    offlineData.setSynced(false);
                                    offlineData.setStatusLog("request error");
                                    Log.e("SwableSendModule " + 255, "updating offlineData <<<<<<<<<<<<<<<<<<<<<<");
                                    offlineData.updateTo(dbHelper);
                                }
                            }, session.getServer(), table, jsonObject, (offlineData.getType() != OfflineData.CUSTOMER ?
                                    "?branch_id=" + offlineData.getBranch_id() + offlineData.getParameters() :
                                    offlineData.getParametersAsFirstParameter())
                    ).setTag(offlineData.getId()).setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
            );
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
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
                        sendThisPage(table, i+1, max_page, SwableTools.prepareTransactionJSON(offlineData.getType()
                                , childOrders.get(i).toJSONObject()), offlineData);
                    }
                } else {
                    List<Order> childOrders = order.getChildOrders();
                    for(int i = 0; i < childOrders.size(); i++) {
                        sendThisPage(table, i+1, max_page, SwableTools.prepareTransactionJSON(offlineData.getType
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
                        sendThisPage(table, i+1, max_page, SwableTools.prepareTransactionJSON(offlineData.getType
                                (), childDocuments.get(i).toJSONObject()), offlineData);
                    }
                } else {
                    for(int i = 0; i < childDocuments.size(); i++) {
                        sendThisPage(table, i+1, max_page, SwableTools.prepareTransactionJSON(offlineData.getType
                                (), childDocuments.get(i).toJSONObject()), offlineData);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    private void sendThisPage(Table table, final int page, final int maxpage, final JSONObject jsonObject,
                              final OfflineData parent) throws JSONException {

        requestQueue.cancelAll(parent.getId() + "-" + page);
        requestQueue.add(
                HTTPRequests.sendPOSTRequest(imonggoSwable, session, new VolleyRequestListener() {
                    @Override
                    public void onStart(Table table, RequestType requestType) {
                        parent.setSyncing(true);
                        parent.setStatusLog("syncing started");

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
                                parent.setStatusLog("sending success");
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
                            Log.e("SwableSendModule " + 366, "updating offlineData <<<<<<<<<<<<<<<<<<<<<<");
                            parent.updateTo(dbHelper);

                            if (parent.isSynced() && !parent.getReturnId().contains(ImonggoSwable.NO_RETURN_ID) &&
                                    parent.getReturnIdList().size() == maxpage) {
                                Log.e("ImonggoSwable", "paged sending : returned ID's : " + parent.getReturnId() +
                                        " size : " + parent.getReturnIdList().size());
                                if (imonggoSwable.getSwableStateListener() != null)
                                    imonggoSwable.getSwableStateListener().onSynced(parent);

                                SUCCESS_TRANSACTIONS++;
                                Log.e("--- Request Success +1", "" + SUCCESS_TRANSACTIONS);

                                if (parent.isSynced() && getQueueTrackerCount() == 0)
                                    NotificationTools.postNotification(imonggoSwable,
                                            ImonggoSwable.NOTIFICATION_ID,
                                            APP_ICON_DRAWABLE,
//                                            imonggoSwable.getNotificationIcon(),
                                            imonggoSwable.getResources().getString(R.string.app_name),
                                            SUCCESS_TRANSACTIONS + " " + "transactions" + " sent",
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

                        parent.setStatusLog("sending failed [" + page + "] : isConnected? " + hasInternet + " : error [" +
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
                        Log.e("SwableSendModule " + 462, "updating offlineData <<<<<<<<<<<<<<<<<<<<<<");
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
                            SUCCESS_TRANSACTIONS++;
                            Log.e("--- Request Success +1", "" + SUCCESS_TRANSACTIONS);
                        }
                    }

                    @Override
                    public void onRequestError() {
                        Log.e("ImonggoSwable", "sending failed [" + page + "] : request error");
                        parent.setSyncing(false);
                        parent.setQueued(false);
                        parent.setSynced(false);
                        parent.setStatusLog("request error");
                        Log.e("SwableSendModule " + 488, "updating offlineData <<<<<<<<<<<<<<<<<<<<<<");
                        parent.updateTo(dbHelper);
                    }
                }, session.getServer(), table, jsonObject, "?branch_id="+ parent.getBranch_id() + parent.getParameters()
                ).setTag(parent.getId()+ "-" +page)
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
            firstPage = firstPage.getChildDocumentAt(0);
            //firstPage.setDocument_lines(firstPage.getDocumentLineAt(0));
            jsonObject = firstPage.toJSONObject();
        }
        else return;

        Log.e("SwableSendModule", "sendFirstPage : reference -> " + jsonObject.get("reference"));
        jsonObject = SwableTools.prepareTransactionJSON(offlineData.getType(), jsonObject);
        Log.e("FIRST_PAGE >>>>> ", jsonObject.toString());

        requestQueue.cancelAll(offlineData.getId()+ "-" +page);
        requestQueue.add(
                HTTPRequests.sendPOSTRequest2(imonggoSwable, session, new VolleyRequestListener() {
                    @Override
                    public void onStart(Table table, RequestType requestType) {
                        offlineData.setSyncing(true);
                        offlineData.setStatusLog("syncing started");

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
                        queueTracker.remove(offlineData.getId());
                        //QUEUED_TRANSACTIONS--;
                        AccountTools.updateUserActiveStatus(imonggoSwable, true);

                        Log.e("ImonggoSwable", "sending success [" + page + "] : " + response);
                        offlineData.setStatusLog("sending success [" + page + "] : " + response);
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
                            Log.e("SwableSendModule " + 567, "updating offlineData <<<<<<<<<<<<<<<<<<<<<<");
                            offlineData.updateTo(dbHelper);

                            queueNonLastPage(table, offlineData, offlineData.getReturnIdList().get(page - 1));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                        queueTracker.remove(offlineData.getId());
                        //QUEUED_TRANSACTIONS--;
                        Log.e("ImonggoSwable", "sending failed [" + page + "] : isConnected? " + hasInternet + " : error [" +
                                responseCode + "] : " + response);

                        boolean isNullReturnId = true;
                        offlineData.setStatusLog("sending failed [" + page + "] : isConnected? " + hasInternet + " : error [" +
                                responseCode + "] : " + response);

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
                        Log.e("SwableSendModule " + 646, "updating offlineData <<<<<<<<<<<<<<<<<<<<<<");
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
                        queueTracker.remove(offlineData.getId());
                        //QUEUED_TRANSACTIONS--;
                        Log.e("ImonggoSwable", "sending failed [" + page + "] : request error");
                        offlineData.setSyncing(false);
                        offlineData.setQueued(false);
                        offlineData.setSynced(false);
                        offlineData.setStatusLog("request error");
                        Log.e("SwableSendModule " + 666, "updating offlineData <<<<<<<<<<<<<<<<<<<<<<");
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
        boolean hasPendingNonLastPage = false;
        for(int i = 1; i < offlineData.getPagedRequestCount()-1; i++) {
            if(i >= idList.size() || ( i < idList.size() && idList.get(i).equals("@") )) {
                hasPendingNonLastPage = true;
                Log.e("SwableSendModule", "queueNonLastPage : queueing page:" + (i+1) + " i:"+ i + " size:"+idList
                        .size() + " retId:" + (idList.size() <= i ? "@" : idList.get(i)));
                sendNextPage(table, offlineData, id, i + 1);
            }
        }
        if(!hasPendingNonLastPage) {
            sendNextPage(table, offlineData, id, offlineData.getPagedRequestCount());
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
                    .order_lines((offlineData.getObjectFromData(Order.class)).getOrderLineAt(page - 1));
            if(page == maxpage)
                orderBuilder.reference(offlineData.getReference_no());
            jsonObject = orderBuilder.build().toJSONObject();
        }
        else if(table == Table.DOCUMENTS) {
            Document.Builder documentBuilder = new Document.Builder()
                    .document_lines((offlineData.getObjectFromData(Document.class)).getDocumentLineAt(page - 1));
            if(page == maxpage)
                documentBuilder.reference(offlineData.getReference_no());
            //Document document = (Document) offlineData.getObjectFromData();
            //document = document.getChildDocumentAt(page - 1);
            jsonObject = documentBuilder.build().toJSONObject();
        }
        else return;

        Log.e("SwableSendModule", "sendNextPage : reference -> " + jsonObject.get("reference") + " page:" + page);
        jsonObject.put("reference",null);
        Log.e("SwableSendModule", "sendNextPage : " + jsonObject.toString());

        jsonObject = SwableTools.prepareTransactionJSON(offlineData.getType(), jsonObject);

        requestQueue.cancelAll(offlineData.getId()+ "-" +page);
        requestQueue.add(
                HTTPRequests.sendPUTRequest2(imonggoSwable, session, new VolleyRequestListener() {
                    @Override
                    public void onStart(Table table, RequestType requestType) {
                        offlineData.setSyncing(true);
                        offlineData.setStatusLog("syncing started");

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
                                offlineData.setStatusLog("sending success");
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
                            Log.e("SwableSendModule " + 771, "updating offlineData <<<<<<<<<<<<<<<<<<<<<<");
                            offlineData.updateTo(dbHelper);

                            if(!offlineData.getReturnId().contains(ImonggoSwable.NO_RETURN_ID) &&
                                    offlineData.getReturnIdList().size() >= maxpage-1 && page != maxpage) {
                                sendNextPage(table, offlineData, id, maxpage);
                            }

                            if (offlineData.isSynced() && page == maxpage) {
                                SUCCESS_TRANSACTIONS++;
                                Log.e("--- Request Success +1", "" + SUCCESS_TRANSACTIONS);
                                if(offlineData.getOfflineDataTransactionType().isVoiding()) {
                                    offlineData.setSynced(false);
                                    Log.e("SwableSendModule " + 784, "updating offlineData <<<<<<<<<<<<<<<<<<<<<<");
                                    offlineData.updateTo(dbHelper);
                                }
                            }
                            Log.e("REQUEST", getQueueTrackerCount() + " " + SUCCESS_TRANSACTIONS);
                            if (offlineData.isSynced() && getQueueTrackerCount() == 0)
                                NotificationTools.postNotification(imonggoSwable,
                                        ImonggoSwable.NOTIFICATION_ID,
                                        APP_ICON_DRAWABLE,
//                                        imonggoSwable.getNotificationIcon(),
                                        imonggoSwable.getResources().getString(R.string.app_name),
                                        SUCCESS_TRANSACTIONS +" transaction" +
                                                (SUCCESS_TRANSACTIONS != 1 ? "s" : "") + " sent",
                                        null,
                                        imonggoSwable.getPendingIntent());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                        Log.e("ImonggoSwable", "sending failed [" + page + "] : isConnected? " + hasInternet + " : error [" +
                                responseCode + "] : " + response);

                        offlineData.setStatusLog("sending failed [" + page + "] : isConnected? " + hasInternet + " : error [" +
                                responseCode + "] : " + response);

                        if(!hasInternet) {
                            offlineData.setSynced(false);
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);
                            Log.e("SwableSendModule " + 815, "updating offlineData <<<<<<<<<<<<<<<<<<<<<<");
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

                        Log.e("SwableSendModule " + 907, "updating offlineData <<<<<<<<<<<<<<<<<<<<<<");
                        offlineData.updateTo(dbHelper);

                        if (imonggoSwable.getSwableStateListener() != null) {
                            if (responseCode == ImonggoSwable.UNAUTHORIZED_ACCESS) {
                                AccountTools.updateUserActiveStatus(imonggoSwable, false);
                                imonggoSwable.getSwableStateListener().onUnauthorizedAccess(response, responseCode);
                            } else
                                imonggoSwable.getSwableStateListener().onSyncProblem(offlineData, hasInternet, response, responseCode);
                        }

                        if(offlineData.isSynced()) {
                            if(offlineData.getOfflineDataTransactionType().isVoiding()) {
                                offlineData.setSynced(false);
                                Log.e("SwableSendModule " + 921, "updating offlineData <<<<<<<<<<<<<<<<<<<<<<");
                                offlineData.updateTo(dbHelper);
                            }
                        }
                    }

                    @Override
                    public void onRequestError() {
                        Log.e("ImonggoSwable", "sending failed [" + page + "] : request error");
                        offlineData.setSyncing(false);
                        offlineData.setQueued(false);
                        offlineData.setSynced(false);
                        offlineData.setStatusLog("request error");
                        Log.e("SwableSendModule " + 934, "updating offlineData <<<<<<<<<<<<<<<<<<<<<<");
                        offlineData.updateTo(dbHelper);
                    }
                }, session.getServer(), table, jsonObject, id, offlineData.getBranch_id(), page == maxpage, offlineData
                        .getParameters()).setTag(offlineData.getId()+ "-" +page)
        );
    }
}
