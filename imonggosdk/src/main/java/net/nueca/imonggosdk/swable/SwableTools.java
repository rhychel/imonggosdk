package net.nueca.imonggosdk.swable;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.OfflineDataType;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.operations.http.HTTPRequests;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;

public class SwableTools {
	public static Intent startSwable(Activity activity) {
		Log.e("SwableTools", "startSwable in " + activity.getClass().getSimpleName());
		Intent service = new Intent(activity,ImonggoSwable.class);
		activity.startService(service);
		return service;
	}
    public static boolean startAndBindSwable(Activity activity, ImonggoSwable.SwableStateListener listener) {
        return activity.bindService(startSwable(activity), new ImonggoSwableServiceConnection(listener),
                Context.BIND_AUTO_CREATE);
    }
	
	public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}

    public static OfflineData sendTransaction(ImonggoDBHelper helper, User user, Object o, OfflineDataType type)
            throws SQLException, JSONException {
        return sendTransaction(helper, user, o, type, "");
    }
    public static OfflineData sendTransaction(ImonggoDBHelper helper, User user, Object o, OfflineDataType type, String parameters)
            throws SQLException, JSONException {
        return sendTransaction(helper, user.getHome_branch_id(), o, type, parameters);
    }

    public static OfflineData sendTransaction(ImonggoDBHelper helper, Session session, Object o, OfflineDataType type)
            throws SQLException, JSONException {
        return sendTransaction(helper, session, o, type, "");
    }
    public static OfflineData sendTransaction(ImonggoDBHelper helper, Session session, Object o, OfflineDataType type, String parameters)
            throws SQLException, JSONException {
        if(helper.getUsers().queryForAll().size() <= 0) {
            Log.e("SwableTools", "sendTransaction : no users in table, Users");
            return null;
        }
        User user = helper.getUsers().queryBuilder().where().eq("email", session.getEmail()).query().get(0);
        return sendTransaction(helper, user.getHome_branch_id(), o, type, parameters);
    }

    public static OfflineData sendTransaction(ImonggoDBHelper helper, int branchId, Object o, OfflineDataType type)
            throws SQLException, JSONException {
        return sendTransaction(helper, branchId, o, type, "");
    }
    public static OfflineData sendTransaction(ImonggoDBHelper helper, int branchId, Object o, OfflineDataType type, String parameters)
            throws SQLException, JSONException {
        if (type == OfflineDataType.CANCEL_ORDER || type == OfflineDataType.CANCEL_INVOICE ||
                type == OfflineDataType.CANCEL_DOCUMENT) {
            Log.e("SwableTools", "sendTransaction : can't have a CANCEL transaction type");
            return null;
        }

        OfflineData offlineData;
        if(o instanceof Order)
            offlineData = new OfflineData((Order)o, type);
        else if(o instanceof Invoice)
            offlineData = new OfflineData((Invoice)o, type);
        else {
            Log.e("SwableTools", "sendTransaction : Class '" + o.getClass().getSimpleName() + "' is not supported for " +
                    "OfflineData --- Use Order and Invoice");
            return null;
        }

        offlineData.setBranch_id(branchId);
        offlineData.setParameters(parameters);

        offlineData.insertTo(helper);
        return offlineData;
    }

    public static void voidTransaction(ImonggoDBHelper helper, String returnId, OfflineDataType type, String reason)
            throws SQLException {
        if (type == OfflineDataType.SEND_ORDER || type == OfflineDataType.SEND_INVOICE ||
                type == OfflineDataType.SEND_DOCUMENT) {
            Log.e("SwableTools", "voidTransaction : can't have a SEND transaction type");
            return;
        }

        /*List<OfflineData> transactions = helper.getOfflineData().queryBuilder().where().eq("returnId", returnId)
                .query();*/
        List<OfflineData> transactions = helper.getOfflineData().queryBuilder().where().like("returnId", "%" +
                returnId + "%").query();
        if(transactions == null || transactions.size() <= 0) {
            Log.e("SwableTools", "voidTransaction : offlinedata with return id '" + returnId + "' not found");
            return;
        }
        OfflineData forVoid = null;
        for(OfflineData od : transactions) {
            for(String str: od.parseReturnID()) {
                if(str.equalsIgnoreCase(returnId))
                    forVoid = od;
            }
        }
        if(forVoid == null) {
            Log.e("SwableTools", "voidTransaction : offlinedata with return id '" + returnId + "' not found");
            return;
        }
        forVoid.setOfflineDataTransactionType(type);
        forVoid.setSynced(false);
        forVoid.setDocumentReason(reason);
        forVoid.updateTo(helper);
    }

    public static void voidTransaction(ImonggoDBHelper helper, OfflineData offlineData, OfflineDataType type, String reason)
            throws SQLException {
        if (type == OfflineDataType.SEND_ORDER || type == OfflineDataType.SEND_INVOICE ||
                type == OfflineDataType.SEND_DOCUMENT) {
            Log.e("SwableTools", "voidTransaction : can't have a SEND transaction type");
            return;
        }

        if(offlineData.getReturnId() == null || offlineData.getReturnId().length() <= 0) {
            Log.e("SwableTools", "voidTransaction : offlinedata has no return id");
            return;
        }
        voidTransaction(helper, offlineData.getReturnId(), type, reason);
    }

    public static void sendOrderNow(Context context, Session session, Server server, Order order, int branchId,
                                      String parameters, @Nullable VolleyRequestListener listener) {
        sendTransactionNow(context, session, server, Table.ORDERS, order, branchId, parameters, listener);
    }
    public static void sendInvoiceNow(Context context, Session session, Server server, Invoice invoice, int branchId,
                                      String parameters, @Nullable VolleyRequestListener listener) {
        sendTransactionNow(context, session, server, Table.INVOICES, invoice, branchId, parameters, listener);
    }

    private static void sendTransactionNow(Context context, Session session, Server server, Table table, final Object
            data, int branchId, String parameters, @Nullable VolleyRequestListener listener) {
        if(parameters.length() > 0 && parameters.charAt(0) != '&')
            parameters = "&" + parameters;

        RequestQueue queue = Volley.newRequestQueue(context);
        try {
            String dataStr = "";
            if(data instanceof Invoice)
                dataStr = ((Invoice)data).toJSONString();
            else if(data instanceof Order)
                dataStr = ((Order)data).toJSONString();

            queue.add(
                    HTTPRequests.sendPOSTRequest(context, session,
                            listener != null ? listener : new VolleyRequestListener() {
                                @Override
                                public void onStart(Table table, RequestType requestType) {
                                    Log.e("SwableTools", "sending transaction : " + table.name());
                                }

                                @Override
                                public void onSuccess(Table table, RequestType requestType, Object response) {
                                    try {
                                        Log.e("SwableTools", "sending success : " + response.toString());
                                        if (response instanceof JSONObject) {
                                            JSONObject responseJson = ((JSONObject) response);
                                            if (responseJson.has("id")) {
                                                Log.d("ImonggoSwable", "sending success : return ID : " + responseJson.getString("id"));
                                                if (data instanceof Invoice)
                                                    ((Invoice)data).setId(responseJson.getInt("id"));
                                                else if (data instanceof Order)
                                                    ((Order)data).setId(responseJson.getInt("id"));
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                                    Log.e("SwableTools", "sending error : hasInternet? " + hasInternet + " [" +
                                            responseCode + "] " + response.toString());
                                }

                                @Override
                                public void onRequestError() {
                                    Log.e("SwableTools", "sending request error");
                                }
                            }, server, table, prepareTransactionJSON(table, dataStr), "?branch=" + branchId +
                                    parameters)
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        queue.start();
    }

    private static JSONObject prepareTransactionJSON(Table table, String jsonString) throws JSONException {
        switch(table) {
            case ORDERS:
                jsonString = "{\"order\":" + jsonString + "}";
                break;
            case INVOICES:
                jsonString = "{\"invoice\":" + jsonString + "}";
                break;
            case DOCUMENTS:
                jsonString = "{\"document\":" + jsonString + "}";
                break;
        }
        return new JSONObject(jsonString);
    }

    public static int computePagedRequestCount(int listSize, int maxElementPerPage) {
        return (int)Math.ceil( (double)listSize/(double)maxElementPerPage );
    }
}
