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
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.base.BaseTable2;
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
public class SwableUpdateModule extends BaseSwableModule {

    public SwableUpdateModule(ImonggoSwable imonggoSwable, ImonggoDBHelper2 helper, Session session, RequestQueue requestQueue) {
        super(imonggoSwable, helper, session, requestQueue);
    }

    public void updateTransaction(Table table, final OfflineData offlineData) {
        QUEUED_TRANSACTIONS++;
        Log.e("SwableUpdateModule", "updateTransaction " + table.getStringName() + " " + offlineData.getType());
        Log.e("SwableUpdateModule", "updateTransaction " + offlineData.getObjectFromData().toString());
        try {
            Branch branch = dbHelper.fetchObjects(Branch.class).queryBuilder().where().eq("id", offlineData.getBranch_id())
                    .queryForFirst();
            if(branch == null || branch.getStatus().equalsIgnoreCase("D")) {
                Log.e("ImonggoSwable", "updating error : Branch '" + (branch == null ? "NULL" : branch.getName()) + "'," +
                        " ID:" + (branch == null ? "NULL" : branch.getId()) + "," + " was deleted or disabled");
                //return;
            }

            /*if(offlineData.isPagedRequest()) {
                if(offlineData.isNewPagedUpdate()) {
                    if (offlineData.getObjectFromData() == null) {
                        offlineData.deleteTo(dbHelper);
                        return;
                    }
                    updateFirstPage(table, offlineData);
                } else
                    pagedUpdate(table, offlineData);
                return;
            }*/

            //JSONObject data;
            JSONObject jsonObject;
            if(offlineData.getType() == OfflineData.INVOICE) {
                Invoice invoice = offlineData.getObjectFromData(Invoice.class);
                boolean hasNewPayment = invoice.createNewPaymentBatch();
                invoice.updateTo(dbHelper);

                if(!hasNewPayment) {
                    QUEUED_TRANSACTIONS--;
                    return;
                }

                invoice.setPayments(invoice.getNewBatchPayment());
                jsonObject = SwableTools.prepareTransactionJSON(offlineData.getType(),
                        invoice.toJSONObject());
            }
            else {
                jsonObject = SwableTools.prepareTransactionJSON(offlineData.getType(),
                        offlineData.getData());
            }
            Log.e("SwableUpdateModule", "updateTransaction : "+jsonObject.toString());
            //Log.e("JSON", jsonObject.toString());

            requestQueue.cancelAll(offlineData.getId());
            requestQueue.add(
                    HTTPRequests.sendPUTRequest(imonggoSwable, session, new VolleyRequestListener() {
                        @Override
                        public void onStart(Table table, RequestType requestType) {
                            offlineData.setSyncing(true);

                            Log.e("ImonggoSwable", "updating : started -- Transaction Type: " +
                                    offlineData.getObjectFromData().getClass().getSimpleName() +
                                    " - with RefNo '" + offlineData.getReference_no() + "'");

                            if (imonggoSwable.getSwableStateListener() != null)
                                imonggoSwable.getSwableStateListener().onSyncing(offlineData);
                        }

                        @Override
                        public void onSuccess(Table table, RequestType requestType, Object response) {
                            QUEUED_TRANSACTIONS--;
                            AccountTools.updateUserActiveStatus(imonggoSwable, true);

                            Log.e("ImonggoSwable", "updating success : " + response);
                            try {
                                offlineData.setSyncing(false);
                                offlineData.setQueued(false);

                                if (response instanceof JSONObject) {
                                    JSONObject responseJson = ((JSONObject) response);
                                    if (responseJson.has("id")) {
                                        Log.d("ImonggoSwable", "updating success : return ID : " +
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
                                SUCCESS_TRANSACTIONS++;
                                Log.e("--- Request Success +1", "" + SUCCESS_TRANSACTIONS);
                            }
                            Log.e("REQUEST", QUEUED_TRANSACTIONS + " " + SUCCESS_TRANSACTIONS);
                            if (offlineData.isSynced() && QUEUED_TRANSACTIONS == 0)
                                NotificationTools.postNotification(imonggoSwable,
                                        ImonggoSwable.NOTIFICATION_ID,
                                        imonggoSwable.getNotificationIcon(),
                                        imonggoSwable.getResources().getString(R.string.app_name),
                                        SUCCESS_TRANSACTIONS +" transaction" +
                                                (SUCCESS_TRANSACTIONS != 1 ? "s" : "") + " sent",
                                        null,
                                        imonggoSwable.getPendingIntent());
                        }

                        @Override
                        public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                            QUEUED_TRANSACTIONS--;
                            Log.e("ImonggoSwable", "updating failed : isConnected? " + hasInternet + " : error [" +
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
                                                Log.e("STR : UPDATE_ORDER ID", orderId);
                                                offlineData.setReturnId(orderId);
                                            } else if (errorMsg.contains("document id")) {
                                                String documentId = errorMsg.substring(
                                                        errorMsg.indexOf("[") + 1, errorMsg.indexOf("]")
                                                );
                                                Log.e("STR: UPDATE_DOCUMENT ID", documentId);
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
                                                    Log.e("JSON : UPDATE_ORDER ID", orderId);
                                                    offlineData.setReturnId(orderId);
                                                } else if (errorMsg.contains("document id")) {
                                                    String documentId = errorMsg.substring(
                                                            errorMsg.indexOf("[") + 1, errorMsg.indexOf("]")
                                                    );
                                                    Log.e("STR: UPDATE_DOCUMENT ID", documentId);
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
                                SUCCESS_TRANSACTIONS++;
                                Log.e("--- Request Success +1", "" + SUCCESS_TRANSACTIONS);
                            }
                        }

                        @Override
                        public void onRequestError() {
                            QUEUED_TRANSACTIONS--;
                            Log.e("ImonggoSwable", "updating failed : request error");
                            offlineData.setSyncing(false);
                            offlineData.setQueued(false);
                            offlineData.setSynced(false);
                            offlineData.updateTo(dbHelper);
                        }
                    }, session.getServer(), table, jsonObject, "" + (
                            offlineData.getObjectFromData() instanceof BaseTable  ? ((BaseTable) offlineData.getObjectFromData()).getId() :
                            offlineData.getObjectFromData() instanceof BaseTable2 ? ((BaseTable2) offlineData.getObjectFromData()).getId() :
                            offlineData.getReturnId()),
                            (offlineData.getType() != OfflineData.CUSTOMER?
                            "?branch_id="+ offlineData.getBranch_id() + offlineData.getParameters() :
                            offlineData.getParametersAsFirstParameter() )
                    ).setTag(offlineData.getId())
            );
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
