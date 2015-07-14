package net.nueca.imonggosdk.swable;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.nueca.imonggosdk.enums.OfflineDataType;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.base.BatchList;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.objects.order.OrderLine;
import net.nueca.imonggosdk.operations.http.HTTPRequests;
import net.nueca.imonggosdk.tools.AccountTools;

import org.apache.commons.lang3.StringUtils;
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

    private static final int NOT_FOUND = 404;
    private static final int UNPROCESSABLE_ENTRY = 422;
    private static final int INTERNAL_SERVER_ERROR = 500;

    private SwableStateListener swableStateListener;

    private User user;
    private User getUser() {
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

    @Override
    public void syncModule() {
        Log.e("ImonggoSwable", "syncModule : called");
        if(!isSyncing()) {
            setSyncing(true);
            try {
                if(AccountTools.isLoggedIn(getHelper())) {
                    Log.e("ImonggoSwable", "syncModule : trying to sync");
                    if(!getSession().isHasLoggedIn()) {
                        setSyncing(false);
                    }

                    /*if(getUser() == null || getUser().getStatus().equalsIgnoreCase("D")) {
                        Log.e("ImonggoSwable", "can't start sync : " +
                                (getUser() == null? "No User" : "User was deleted or disabled"));
                        stopSelf();
                        return;
                    }*/

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
                    Log.e("ImonggoSwable", "starting sync : " + offlineDataList.size() + " queued");
                    if(swableStateListener != null)
                        swableStateListener.onSwableStarted();
                    getQueue().start();
                }
                else {
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

    public interface SwableStateListener {
        void onSwableStarted();
        void onQueued(OfflineData offlineData);
        void onSyncing(OfflineData offlineData);
        void onSynced(OfflineData offlineData);
        void onSyncProblem(OfflineData offlineData, boolean hasInternet, Object response, int responseCode);
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
            /*if(branch == null || branch.getStatus().equalsIgnoreCase("D")) {
                Log.e("ImonggoSwable", "sending error : Branch '" + branch.getName() + "', ID:" + branch.getId() + "," +
                        " was deleted or disabled");
                return;
            }*/
            if(table == Table.ORDERS) {
                Order order = (Order)offlineData.generateObjectFromData();
                if(order.shouldPageRequest()) {
                    pagedSend(table, offlineData);
                    return;
                }
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

                            switch(offlineData.getOfflineDataTransactionType()) {
                                case SEND_ORDER:
                                    Order order = Order.fromJSONString(offlineData.getData());
                                    order.setId(Integer.parseInt(offlineData.getReturnId()));
                                    order.insertTo(getHelper());
                                    break;
                                case SEND_INVOICE:
                                    Invoice invoice = Invoice.fromJSONString(offlineData.getData());
                                    invoice.setId(Integer.parseInt(offlineData.getReturnId()));
                                    invoice.insertTo(getHelper());
                                    break;
                            }

                            if (swableStateListener != null && offlineData.isSynced())
                                swableStateListener.onSynced(offlineData);

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
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        offlineData.updateTo(getHelper());

                        if(swableStateListener != null) {
                            swableStateListener.onSyncProblem(offlineData, hasInternet, response, responseCode);
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
            /*if(branch == null || branch.getStatus().equalsIgnoreCase("D")) {
                Log.e("ImonggoSwable", "deleting error : Branch '" + branch.getName() + "', ID:" + branch.getId() +
                        ", was deleted or disabled");
                return;
            }*/

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
                            Log.e("ImonggoSwable", "deleting success : " + response);
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);

                            offlineData.setSynced(true);
                            offlineData.setCancelled(true);
                            offlineData.updateTo(getHelper());

                            if (swableStateListener != null && offlineData.isSynced())
                                swableStateListener.onSynced(offlineData);
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
                                Log.e("ImonggoSwable", "deleting failed : transaction not found");
                            }

                            if(swableStateListener != null) {
                                swableStateListener.onSyncProblem(offlineData, hasInternet, response, responseCode);
                            }
                        }

                        @Override
                        public void onRequestError() {
                            Log.e("ImonggoSwable", "deleting failed : request error");
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);
                            offlineData.setSynced(false);
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

                                    switch(parent.getOfflineDataTransactionType()) {
                                        case SEND_ORDER:
                                            Order order = Order.fromJSONString(parent.getData());
                                            order.setReference(order.getReference()+"-"+page);
                                            order.setId(Integer.parseInt(responseJson.getString("id")));
                                            order.insertTo(getHelper());
                                            break;
                                        case SEND_INVOICE:
                                            Invoice invoice = Invoice.fromJSONString(parent.getData());
                                            invoice.setReference(invoice.getReference()+"-"+page);
                                            invoice.setId(Integer.parseInt(responseJson.getString("id")));
                                            invoice.insertTo(getHelper());
                                            break;
                                    }
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

                                // TODO Remove
                                SwableTools.voidTransaction(getHelper(),parent.parseReturnID().get(1),
                                        OfflineDataType.CANCEL_ORDER,"test");
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
                                parent.setSynced(false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        parent.updateTo(getHelper());

                        if(swableStateListener != null) {
                            swableStateListener.onSyncProblem(parent, hasInternet, response, responseCode);
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
                                        " - with RefNo '" + getHelper().getOrders().queryBuilder().where().eq("id",
                                        id).queryForFirst().getReference() + "' and returnId '" + id + "'");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            if (swableStateListener != null)
                                swableStateListener.onSyncing(offlineData);
                        }

                        @Override
                        public void onSuccess(Table table, RequestType requestType, Object response) {
                            Log.e("ImonggoSwable", "deleting success : " + response);
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);

                            offlineData.setCancelled(list.indexOf(id) == 0 || offlineData.isCancelled());
                            offlineData.setSynced(offlineData.isCancelled());
                            offlineData.updateTo(getHelper());

                            list.set(list.indexOf(id),NO_RETURN_ID);

                            if(offlineData.isSynced() && Collections.frequency(list, NO_RETURN_ID) == list.size()) {
                                if (swableStateListener != null)
                                    swableStateListener.onSynced(offlineData);
                            }
                        }

                        @Override
                        public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                            Log.e("ImonggoSwable", "deleting failed : isConnected? " + hasInternet + " : error [" +
                                    responseCode + "] : " + response);
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);

                            if(responseCode == UNPROCESSABLE_ENTRY) {
                                offlineData.setCancelled(false);
                            }
                            else if(responseCode == NOT_FOUND) {
                                offlineData.setCancelled(false);
                                Log.e("ImonggoSwable", "deleting failed : transaction not found");
                            }
                            else {
                                offlineData.setCancelled(false);
                            }

                            offlineData.setSynced(offlineData.isCancelled());

                            if(swableStateListener != null) {
                                swableStateListener.onSyncProblem(offlineData, hasInternet, response, responseCode);
                            }
                        }

                        @Override
                        public void onRequestError() {
                            Log.e("ImonggoSwable", "deleting failed : request error");
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);
                            offlineData.setSynced(false);
                            offlineData.setCancelled(false);
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
