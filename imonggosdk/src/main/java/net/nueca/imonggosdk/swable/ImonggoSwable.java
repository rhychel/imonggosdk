package net.nueca.imonggosdk.swable;

import android.util.Log;

import net.nueca.imonggosdk.enums.OfflineDataType;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.operations.http.HTTPRequests;
import net.nueca.imonggosdk.tools.AccountTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by gama on 6/22/15.
 */
public class ImonggoSwable extends SwableService {
    private static final int NOT_FOUND = 404;
    private static final int UNPROCESSABLE_ENTRY = 422;
    private static final int INTERNAL_SERVER_ERROR = 500;

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
                    Log.e("ImonggoSwable", "syncModule : trying to sync");
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

                    int count = 0;
                    for (OfflineData offlineData : offlineDataList) {
                        count++;
                        Log.e("OfflineDataList", OfflineDataType.identify(offlineData.getType()).toString() + " " +
                                count + " " + offlineDataList.size());

                        if(offlineData.isCancelled()) {
                            swableStateListener.onAlreadyCancelled(offlineData);
                            continue;
                        }

                        offlineData.setQueued(true);

                        if(swableStateListener != null)
                            swableStateListener.onQueued(offlineData);

                        switch ( OfflineDataType.identify(offlineData.getType()) ) {
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
                        /*if(OfflineDataType.identify(offlineData.getType()) == OfflineDataType.SEND_ORDER)
                            send(Table.ORDERS, offlineData);
                        else if(OfflineDataType.identify(offlineData.getType()) == OfflineDataType.SEND_INVOICE)
                            send(Table.INVOICES, offlineData);
                        else if(OfflineDataType.identify(offlineData.getType()) == OfflineDataType.SEND_DOCUMENT)
                            send(Table.DOCUMENTS, offlineData);

                        else if(OfflineDataType.identify(offlineData.getType()) == OfflineDataType.CANCEL_ORDER)
                            delete(Table.ORDERS, offlineData);
                        else if(OfflineDataType.identify(offlineData.getType()) == OfflineDataType.CANCEL_INVOICE)
                            delete(Table.INVOICES, offlineData);
                        else if(OfflineDataType.identify(offlineData.getType()) == OfflineDataType.CANCEL_DOCUMENT)
                            delete(Table.DOCUMENTS, offlineData);*/
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

    private void send(Table table, final OfflineData offlineData) {
        try {
            String jsonString = offlineData.getData();
            switch(OfflineDataType.identify(offlineData.getType())) {
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
            JSONObject jsonObject = new JSONObject(jsonString);
            Log.e("JSON", jsonObject.toString());

            getQueue().add(
                HTTPRequests.sendPOSTRequest(this, getSession(), new VolleyRequestListener() {
                    @Override
                    public void onStart(Table table, RequestType requestType) {
                        offlineData.setSyncing(true);
                        Log.e("ImonggoSwable", "sending : started -- with RefNo '" + offlineData.getReference_no() + "'");

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
                                    Log.d("ImonggoSwable", "sending success : return ID : " + responseJson.getString("id"));
                                    offlineData.setReturnId(responseJson.getString("id"));
                                }
                            }

                            offlineData.setSynced(true);
                            offlineData.updateTo(getHelper());

                            switch(OfflineDataType.identify(offlineData.getType())) {
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

                            //SwableTools.voidTransaction(getHelper(), Integer.parseInt(offlineData.getReturnId()), OfflineDataType.CANCEL_INVOICE, "wrong order");

                            //SwableTools.voidTransaction(getHelper(),Integer.parseInt(offlineData.getReturnId())+1,OfflineDataType.CANCEL_INVOICE, "just because");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }/* catch (SQLException e) {
                            e.printStackTrace();
                        }*/



                    }

                    @Override
                    public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                        Log.e("ImonggoSwable", "sending failed : isConnected? " + hasInternet + " : error [" + responseCode + "] : " + response);

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
                    }
                }, getSession().getServer(), table, jsonObject, "?branch_id="+ offlineData.getBranch_id() + offlineData
                        .getParameters())
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void delete(Table table, final OfflineData offlineData) {
        try {
            getQueue().add(
                HTTPRequests.sendDELETERequest(this, getSession(), new VolleyRequestListener() {
                            @Override
                            public void onStart(Table table, RequestType requestType) {
                                offlineData.setSyncing(true);
                                try {
                                    Log.e("JSON", new JSONObject(offlineData.getData()).toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Log.e("ImonggoSwable", "deleting : started -- with RefNo '" + offlineData.getReference_no() +
                                        "' and returnId '" + offlineData.getReturnId() + "'");

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
                                Log.e("ImonggoSwable", "deleting failed : isConnected? " + hasInternet + " : error [" + responseCode + "] : " + response);
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
                            }
                        }, getSession().getServer(), table, offlineData.getReturnId(), "branch_id=" + offlineData.getBranch_id() +
                        "&reason=" + URLEncoder.encode(offlineData.getDocumentReason(), "UTF-8") + offlineData.getParameters())
            );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        /*} catch (JSONException e) {
            e.printStackTrace();
        }*/
    }
}
