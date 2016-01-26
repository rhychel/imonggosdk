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

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.OfflineDataType;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.operations.http.HTTPRequests;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.ListTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

public class SwableTools {
    public static Intent startSwable(Activity activity) {
        Log.e("SwableTools", "startSwable in " + activity.getClass().getSimpleName());
        Intent service = new Intent(activity,ImonggoSwable.class);
        activity.startService(service);
        return service;
    }
    public static Intent stopSwable(Activity activity) {
        Log.e("SwableTools", "stopSwable in " + activity.getClass().getSimpleName());
        Intent service = new Intent(activity,ImonggoSwable.class);
        activity.stopService(service);
        return service;
    }
    public static boolean startAndBindSwable(Activity activity, ImonggoSwableServiceConnection swableServiceConnection) {
        return activity.bindService(startSwable(activity), swableServiceConnection, Context.BIND_AUTO_CREATE);
    }
    public static Intent stopAndUnbindSwable(Activity activity, ImonggoSwableServiceConnection swableServiceConnection) {
        activity.unbindService(swableServiceConnection);
        return stopSwable(activity);
    }
    public static boolean bindSwable(Activity activity, ImonggoSwableServiceConnection swableServiceConnection) {
        Intent service = new Intent(activity,ImonggoSwable.class);
        return activity.bindService(service, swableServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public static void unbindSwable(Activity activity, ImonggoSwableServiceConnection swableServiceConnection) {
        try {
            activity.unbindService(swableServiceConnection);
        } catch (Exception e) {
            Log.e("Oops!", e.getMessage());
        }
    }

    public static boolean isImonggoSwableRunning(Context context) {
        return isMyServiceRunning(context, ImonggoSwable.class);
    }
	private static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}

    @Deprecated
    public static OfflineData sendTransaction(ImonggoDBHelper2 helper, User user, Object o, OfflineDataType type)
            throws SQLException, JSONException {
        return sendTransaction(helper, user, o, type, "");
    }
    @Deprecated
    public static OfflineData sendTransaction(ImonggoDBHelper2 helper, User user, Object o, OfflineDataType type,
                                              String parameters)
            throws SQLException, JSONException {
        return sendTransaction(helper, user.getHome_branch_id(), o, type, parameters);
    }

    @Deprecated
    public static OfflineData sendTransaction(ImonggoDBHelper2 helper, Session session, Object o, OfflineDataType type)
            throws SQLException, JSONException {
        return sendTransaction(helper, session, o, type, "");
    }
    @Deprecated
    public static OfflineData sendTransaction(ImonggoDBHelper2 helper, Session session, Object o, OfflineDataType type,
                                              String parameters)
            throws SQLException, JSONException {
        if(helper.fetchObjectsList(User.class).size() <= 0) {
            Log.e("SwableTools", "sendTransaction : no users in table, Users");
            return null;
        }
        User user = helper.fetchObjects(User.class).queryBuilder().where().eq("email", session.getEmail()).query().get(0);
        return sendTransaction(helper, user.getHome_branch_id(), o, type, parameters);
    }

    @Deprecated
    public static OfflineData sendTransaction(ImonggoDBHelper2 helper, int branchId, Object o, OfflineDataType type)
            throws SQLException, JSONException {
        return sendTransaction(helper, branchId, o, type, "");
    }
    @Deprecated
    public static OfflineData sendTransaction(ImonggoDBHelper2 helper, int branchId, Object o, OfflineDataType type,
                                              String parameters)
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
        else if(o instanceof Document)
            offlineData = new OfflineData((Document)o, type);
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

    @Deprecated
    public static void voidTransaction(ImonggoDBHelper2 helper, String returnId, OfflineDataType type, String reason)
            throws SQLException {
        if (type == OfflineDataType.SEND_ORDER || type == OfflineDataType.SEND_INVOICE ||
                type == OfflineDataType.SEND_DOCUMENT) {
            Log.e("SwableTools", "voidTransaction : can't have a SEND transaction type");
            return;
        }

        /*List<OfflineData> transactions = helper.getOfflineData().queryBuilder().where().eq("returnId", returnId)
                .query();*/
        List<OfflineData> transactions = helper.fetchObjects(OfflineData.class).queryBuilder().where().like("returnId", "%" +
                returnId + "%").query();
        if(transactions == null || transactions.size() <= 0) {
            Log.e("SwableTools", "voidTransaction : offlinedata with return id '" + returnId + "' not found");
            return;
        }
        OfflineData forVoid = null;
        for(OfflineData transaction : transactions) {
            if(forVoid != null)
                break;
            for(String str: transaction.getReturnIdList()) {
                if(str.equalsIgnoreCase(returnId)) {
                    forVoid = transaction;
                    break;
                }
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

    @Deprecated
    public static void voidTransaction(ImonggoDBHelper2 helper, OfflineData offlineData, OfflineDataType type, String reason)
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
        if(offlineData.isPagedRequest())
            voidTransaction(helper, offlineData.getReturnIdList().get(0), type, reason);
        else
            voidTransaction(helper, offlineData.getReturnId(), type, reason);
    }

    @Deprecated
    public static void sendOrderNow(Context context, Session session, Server server, Order order, int branchId,
                                      String parameters, @Nullable VolleyRequestListener listener) {
        sendTransactionNow(context, session, server, Table.ORDERS, order, branchId, parameters, listener);
    }
    @Deprecated
    public static void sendInvoiceNow(Context context, Session session, Server server, Invoice invoice, int branchId,
                                      String parameters, @Nullable VolleyRequestListener listener) {
        sendTransactionNow(context, session, server, Table.INVOICES, invoice, branchId, parameters, listener);
    }
    @Deprecated
    public static void sendDocumentNow(Context context, Session session, Server server, Document document, int branchId,
                                      String parameters, @Nullable VolleyRequestListener listener) {
        sendTransactionNow(context, session, server, Table.INVOICES, document, branchId, parameters, listener);
    }

    @Deprecated
    private static void sendTransactionNow(Context context, Session session, Server server, Table table, final Object
            data, int branchId, String parameters, @Nullable VolleyRequestListener listener) {
        if(parameters.length() > 0 && parameters.charAt(0) != '&')
            parameters = "&" + parameters;

        RequestQueue queue = Volley.newRequestQueue(context);
        try {
            JSONObject jsonObject;
            if(data instanceof Invoice)
                jsonObject = ((Invoice)data).toJSONObject();
            else if(data instanceof Order)
                jsonObject = ((Order)data).toJSONObject();
            else if(data instanceof Document)
                jsonObject = ((Document)data).toJSONObject();
            else {
                Log.e("SwableTools", "sendTransactionNow : Class '" + data.getClass().getSimpleName() + "' is not " +
                        "supported for OfflineData --- Use Order, Document and Invoice");
                return;
            }

            queue.add(
                HTTPRequests.sendPOSTRequest(context, session,
                    listener != null ? listener : new VolleyRequestListener() {
                        @Override
                        public void onStart(Table table, RequestType requestType) {
                            Log.e("SwableTools", "[default listener] sending transaction : " + table.name());
                        }

                        @Override
                        public void onSuccess(Table table, RequestType requestType, Object response) {
                            try {
                                Log.e("SwableTools", "[default listener] sending success : " +
                                        response.toString());
                                if (response instanceof JSONObject) {
                                    JSONObject responseJson = ((JSONObject) response);
                                    if (responseJson.has("id")) {
                                        Log.d("SwableTools", "[default listener] sending success : return ID : "
                                            + responseJson.getString("id"));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                            Log.e("SwableTools", "[default listener] sending error : hasInternet? " + hasInternet + " [" +
                                    responseCode + "] " + response.toString());
                        }

                        @Override
                        public void onRequestError() {
                            Log.e("SwableTools", "[default listener] sending request error");
                        }
                    }, server, table, prepareTransactionJSON(table, jsonObject), "?branch=" + branchId +
                        parameters)
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        queue.start();
    }

    private static JSONObject prepareTransactionJSON(Table table, JSONObject jsonObject) throws JSONException {
        JSONObject transaction = new JSONObject();
        switch(table) {
            case ORDERS:
                transaction.put("order", jsonObject);
                break;
            case INVOICES:
                transaction.put("invoice", jsonObject);
                break;
            case DOCUMENTS:
                transaction.put("document", jsonObject);
                break;
            case CUSTOMERS:
                transaction.put("customer", jsonObject);
                break;
        }
        return transaction;
    }

    public static JSONObject prepareTransactionJSON(OfflineDataType offlineDataType, JSONObject jsonObject) throws
            JSONException {
        JSONObject transaction = new JSONObject();
        switch(offlineDataType) {
            case SEND_ORDER:
                transaction.put("order", jsonObject);
                break;
            case SEND_INVOICE:
                transaction.put("invoice", jsonObject);
                break;
            case SEND_DOCUMENT:
                transaction.put("document", jsonObject);
                break;
            case ADD_CUSTOMER:
                transaction.put("customer", jsonObject);
                break;
        }
        return transaction;
    }

    public static int computePagedRequestCount(int listSize, int maxElementPerPage) {
        return (int)Math.ceil( (double)listSize/(double)maxElementPerPage );
    }

    public static List partition(int nthPartition, List list, int size) {
        return ListTools.partition(nthPartition, list, size);
    }

    /**
     * Adding OfflineData and Direct Sending/Voiding using the Builder structure
     *
     * eg.
     * Sending:
     *  new SwableTools.Transaction(getHelper())
     *      .toSend()
     *      .object(document)
     *      .forBranch(getSession().getUser().getHome_branch_id())
     *      .queue();
     *
     * Voiding:
     *  new SwableTools.Transaction(getHelper())
     *      .toCancel()
     *      .objectContainingReturnId(<return_id string>) OR .object(<OfflineData object>)
     *      .withReason("REASON")
     *      .queue();
     */
    public static class Transaction {

        private ImonggoDBHelper2 helper;
        public Transaction(ImonggoDBHelper2 helper) {
            this.helper = helper;
        }

        public SendTransaction toSend() {
            if(helper == null)
                throw new NullPointerException("SwableTools : Transaction : Helper is NULL");
            return new SendTransaction(helper);
        }
        public CancelTransaction toCancel() {
            if(helper == null)
                throw new NullPointerException("SwableTools : Transaction : Helper is NULL");
            return new CancelTransaction(helper);
        }
        public UpdateTransaction toUpdate() {
            if(helper == null)
                throw new NullPointerException("SwableTools : Transaction : Helper is NULL");
            return new UpdateTransaction(helper);
        }

        public static class CancelTransaction {
            private OfflineData offlineData;
            private ImonggoDBHelper2 helper;
            private String reason = "";

            CancelTransaction(ImonggoDBHelper2 helper) {
                this.helper = helper;
            }

            public CancelTransaction object(OfflineData offlineData) {
                this.offlineData = offlineData;
                return this;
            }

            public CancelTransaction objectContainingReturnId(String returnId) throws SQLException {
                List<OfflineData> transactions = helper.fetchObjects(OfflineData.class).queryBuilder().where().like("returnId", "%" +
                        returnId + "%").query();

                if(transactions == null || transactions.size() == 0)
                    throw new NoSuchElementException("SwableTools : CancelTransaction : OfflineData with return id '"
                            + returnId + "' not found");

                OfflineData forVoid = null;
                for(OfflineData transaction : transactions) {
                    if(forVoid != null)
                        break;
                    for(String str: transaction.getReturnIdList()) {
                        if(str.equalsIgnoreCase(returnId)) {
                            forVoid = transaction;
                            break;
                        }
                    }
                }
                if(forVoid == null)
                    throw new NoSuchElementException("SwableTools : CancelTransaction : OfflineData with return id '"
                        + returnId + "' not found");

                offlineData = forVoid;
                return this;
            }

            public CancelTransaction withReason(String reason) {
                this.reason = reason;
                return this;
            }

            public OfflineData queue() {
                if(offlineData == null)
                    throw new NullPointerException("SwableTools : SendTransaction : Transaction Object is null");
                if(reason == null || reason.isEmpty())
                    throw new NullPointerException("SwableTools : SendTransaction : Reason is required");

                offlineData.setDocumentReason(reason);
                offlineData.setSynced(false);

                switch (offlineData.getType()) {
                    case OfflineData.ORDER:
                        offlineData.setOfflineDataTransactionType(OfflineDataType.CANCEL_ORDER); break;
                    case OfflineData.INVOICE:
                        offlineData.setOfflineDataTransactionType(OfflineDataType.CANCEL_INVOICE); break;
                    case OfflineData.DOCUMENT:
                        offlineData.setOfflineDataTransactionType(OfflineDataType.CANCEL_DOCUMENT); break;
                    case OfflineData.CUSTOMER:
                        offlineData.setOfflineDataTransactionType(OfflineDataType.DELETE_CUSTOMER); break;
                }

                offlineData.updateTo(helper);
                return offlineData;
            }

            public DirectTransaction directCancel() {
                if(offlineData == null)
                    throw new NullPointerException("SwableTools : SendTransaction : Transaction Object is null");
                if(reason == null || reason.isEmpty())
                    throw new NullPointerException("SwableTools : SendTransaction : Reason is required");

                offlineData.setDocumentReason(reason);

                switch (offlineData.getType()) {
                    case OfflineData.ORDER:
                        offlineData.setOfflineDataTransactionType(OfflineDataType.CANCEL_ORDER); break;
                    case OfflineData.INVOICE:
                        offlineData.setOfflineDataTransactionType(OfflineDataType.CANCEL_INVOICE); break;
                    case OfflineData.DOCUMENT:
                        offlineData.setOfflineDataTransactionType(OfflineDataType.CANCEL_DOCUMENT); break;
                    case OfflineData.CUSTOMER:
                        offlineData.setOfflineDataTransactionType(OfflineDataType.DELETE_CUSTOMER); break;
                }
                return new DirectTransaction(offlineData, helper, DirectTransaction.TYPE_CANCEL);
            }
        }

        public static class SendTransaction {
            private OfflineData offlineData;
            private ImonggoDBHelper2 helper;
            private Integer branchId;
            private String branchName;
            private String parameter = "";
            private ConcessioModule concessioModule = ConcessioModule.NONE;

            SendTransaction(ImonggoDBHelper2 helper) {
                this.helper = helper;
            }

            public SendTransaction withParameters(String parameter) {
                if(parameter.length() > 0 && parameter.charAt(0) != '&')
                    parameter = "&" + parameter;
                this.parameter = parameter;
                return this;
            }

            public SendTransaction forBranch(int branchId) {
                this.branchId = branchId;
                return this;
            }

            public SendTransaction forBranch(Branch branch) {
                this.branchName = branch.getName();
                this.branchId = branch.getId();
                return this;
            }

            public SendTransaction fromModule(ConcessioModule concessioModule) {
                this.concessioModule = concessioModule;
                return this;
            }

            public SendTransaction object(Document document) {
                offlineData = new OfflineData(document, OfflineDataType.UNKNOWN);
                return this;
            }
            public SendTransaction object(Order order) {
                offlineData = new OfflineData(order, OfflineDataType.UNKNOWN);
                return this;
            }
            public SendTransaction object(Invoice invoice) {
                offlineData = new OfflineData(invoice, OfflineDataType.UNKNOWN);
                return this;
            }
            public SendTransaction object(Customer customer) {
                offlineData = new OfflineData(customer, OfflineDataType.UNKNOWN);
                return this;
            }
            public OfflineData queue() {
                if(offlineData == null)
                    throw new NullPointerException("SwableTools : SendTransaction : Transaction Object is null");
                if(branchId == null && offlineData.getType() != OfflineData.CUSTOMER)
                    throw new NullPointerException("SwableTools : SendTransaction : Branch ID is null");

                offlineData.setParameters(parameter);
                if(branchId != null)
                    offlineData.setBranch_id(branchId);
                if(branchName != null)
                    offlineData.setBranchName(branchName);
                if(concessioModule != ConcessioModule.NONE)
                    offlineData.setConcessioModule(concessioModule);

                switch (offlineData.getType()) {
                    case OfflineData.ORDER:
                        offlineData.setOfflineDataTransactionType(OfflineDataType.SEND_ORDER); break;
                    case OfflineData.INVOICE:
                        offlineData.setOfflineDataTransactionType(OfflineDataType.SEND_INVOICE); break;
                    case OfflineData.DOCUMENT:
                        offlineData.setOfflineDataTransactionType(OfflineDataType.SEND_DOCUMENT); break;
                    case OfflineData.CUSTOMER:
                        offlineData.setOfflineDataTransactionType(OfflineDataType.ADD_CUSTOMER); break;
                }

                offlineData.insertTo(helper);
                return offlineData;
            }
            public DirectTransaction directSend() {
                if(offlineData == null)
                    throw new NullPointerException("SwableTools : SendTransaction : Transaction Object is null");
                if(branchId == null && offlineData.getType() != OfflineData.CUSTOMER)
                    throw new NullPointerException("SwableTools : SendTransaction : Branch ID is null");

                offlineData.setParameters(parameter);
                if(branchId != null)
                    offlineData.setBranch_id(branchId);
                if(branchName != null)
                    offlineData.setBranchName(branchName);

                switch (offlineData.getType()) {
                    case OfflineData.ORDER:
                        offlineData.setOfflineDataTransactionType(OfflineDataType.SEND_ORDER); break;
                    case OfflineData.INVOICE:
                        offlineData.setOfflineDataTransactionType(OfflineDataType.SEND_INVOICE); break;
                    case OfflineData.DOCUMENT:
                        offlineData.setOfflineDataTransactionType(OfflineDataType.SEND_DOCUMENT); break;
                    case OfflineData.CUSTOMER:
                        offlineData.setOfflineDataTransactionType(OfflineDataType.ADD_CUSTOMER); break;
                }
                return new DirectTransaction(offlineData, helper, DirectTransaction.TYPE_SEND);
            }
        }

        public static class UpdateTransaction {
            private OfflineData offlineData;
            private ImonggoDBHelper2 helper;
            private Integer branchId;
            private String branchName;
            private String parameter = "";
            private ConcessioModule concessioModule = ConcessioModule.NONE;

            UpdateTransaction(ImonggoDBHelper2 helper) {
                this.helper = helper;
            }

            public UpdateTransaction withParameters(String parameter) {
                if(parameter.length() > 0 && parameter.charAt(0) != '&')
                    parameter = "&" + parameter;
                this.parameter = parameter;
                return this;
            }

            public UpdateTransaction forBranch(int branchId) {
                this.branchId = branchId;
                return this;
            }

            public UpdateTransaction forBranch(Branch branch) {
                this.branchName = branch.getName();
                this.branchId = branch.getId();
                return this;
            }

            public UpdateTransaction fromModule(ConcessioModule concessioModule) {
                this.concessioModule = concessioModule;
                return this;
            }

            /*public UpdateTransaction object(Document document) {
                offlineData = new OfflineData(document, OfflineDataType.UNKNOWN);
                return this;
            }
            public UpdateTransaction object(Order order) {
                offlineData = new OfflineData(order, OfflineDataType.UNKNOWN);
                return this;
            }
            public UpdateTransaction object(Invoice invoice) {
                offlineData = new OfflineData(invoice, OfflineDataType.UNKNOWN);
                return this;
            }*/
            public UpdateTransaction object(Customer customer) {
                offlineData = new OfflineData(customer, OfflineDataType.UNKNOWN);
                return this;
            }
            public OfflineData queue() {
                if(offlineData == null)
                    throw new NullPointerException("SwableTools : UpdateTransaction : Transaction Object is null");
                if(branchId == null && offlineData.getType() != OfflineData.CUSTOMER)
                    throw new NullPointerException("SwableTools : UpdateTransaction : Branch ID is null");

                offlineData.setParameters(parameter);
                if(branchId != null)
                    offlineData.setBranch_id(branchId);
                if(branchName != null)
                    offlineData.setBranchName(branchName);
                if(concessioModule != ConcessioModule.NONE)
                    offlineData.setConcessioModule(concessioModule);

                switch (offlineData.getType()) {
                    //case OfflineData.ORDER:
                    //    offlineData.setOfflineDataTransactionType(OfflineDataType.UPDATE_ORDER); break;
                    //case OfflineData.INVOICE:
                    //    offlineData.setOfflineDataTransactionType(OfflineDataType.UPDATE_INVOICE); break;
                    //case OfflineData.DOCUMENT:
                    //    offlineData.setOfflineDataTransactionType(OfflineDataType.UPDATE_DOCUMENT); break;
                    case OfflineData.CUSTOMER:
                        offlineData.setOfflineDataTransactionType(OfflineDataType.UPDATE_CUSTOMER); break;
                }

                offlineData.insertTo(helper);
                return offlineData;
            }
            public DirectTransaction directUpdate() {
                if(offlineData == null)
                    throw new NullPointerException("SwableTools : UpdateTransaction : Transaction Object is null");
                if(branchId == null && offlineData.getType() != OfflineData.CUSTOMER)
                    throw new NullPointerException("SwableTools : UpdateTransaction : Branch ID is null");

                offlineData.setParameters(parameter);
                if(branchId != null)
                    offlineData.setBranch_id(branchId);
                if(branchName != null)
                    offlineData.setBranchName(branchName);

                switch (offlineData.getType()) {
                    //case OfflineData.ORDER:
                    //    offlineData.setOfflineDataTransactionType(OfflineDataType.UPDATE_ORDER); break;
                    //case OfflineData.INVOICE:
                    //    offlineData.setOfflineDataTransactionType(OfflineDataType.UPDATE_INVOICE); break;
                    //case OfflineData.DOCUMENT:
                    //    offlineData.setOfflineDataTransactionType(OfflineDataType.UPDATE_DOCUMENT); break;
                    case OfflineData.CUSTOMER:
                        offlineData.setOfflineDataTransactionType(OfflineDataType.UPDATE_CUSTOMER); break;
                }
                return new DirectTransaction(offlineData, helper, DirectTransaction.TYPE_UPDATE);
            }
        }

        public static class DirectTransaction {
            public static final int TYPE_CANCEL = 0, TYPE_SEND = 1, TYPE_UPDATE = 2;
            private OfflineData offlineData;
            private ImonggoDBHelper2 helper;
            private int type = -1; // 0 - cancel | 1 - send | 2 - update

            private Server server;
            private Session session;
            private VolleyRequestListener listener;
            private RequestQueue queue;
            private Context context;

            DirectTransaction(OfflineData offlineData, ImonggoDBHelper2 helper, int type) {
                this.offlineData = offlineData;
                this.helper = helper;
                this.type = type;
            }

            public DirectTransaction withServer(Server server) {
                this.server = server;
                return this;
            }
            public DirectTransaction withSession(Session session) {
                this.session = session;
                return this;
            }
            public DirectTransaction withListener(VolleyRequestListener listener) {
                this.listener = listener;
                return this;
            }

            private RequestQueue getQueue() {
                return queue;
            }

            public void begin(Activity activity) throws JSONException {
                this.context = activity.getApplicationContext();
                queue = Volley.newRequestQueue(context);
                Table table;

                switch (offlineData.getType()) {
                    case OfflineData.ORDER: table = Table.ORDERS; break;
                    case OfflineData.INVOICE: table = Table.INVOICES; break;
                    case OfflineData.DOCUMENT: table = Table.DOCUMENTS; break;
                    case OfflineData.CUSTOMER: table = Table.CUSTOMERS; break;
                    default: table = Table.ORDERS; break;
                }

                offlineData.setQueued(true);
                offlineData.setSynced(false);
                offlineData.setSyncing(true);
                offlineData.updateTo(helper);

                if(type == TYPE_SEND) {
                    if(offlineData.isPagedRequest())
                        pagedSend(table,offlineData);
                    else
                        getQueue().add(
                                HTTPRequests.sendPOSTRequest(context, session,
                                        new VolleyRequestListener() {
                                            @Override
                                            public void onStart(Table table, RequestType requestType) {
                                                if (listener != null)
                                                    listener.onStart(table, requestType);
                                            }

                                            @Override
                                            public void onSuccess(Table table, RequestType requestType, Object response) {
                                                if (listener != null)
                                                    listener.onSuccess(table, requestType, response);
                                                offlineData.setSynced(true);
                                                offlineData.setSyncing(false);
                                                offlineData.setQueued(false);
                                                offlineData.updateTo(helper);
                                            }

                                            @Override
                                            public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                                                if (listener != null)
                                                    listener.onError(table, hasInternet, response, responseCode);
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
                                                    } else if (responseCode == ImonggoSwable.UNAUTHORIZED_ACCESS) {
                                                        offlineData.setSynced(true);
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                offlineData.updateTo(helper);
                                            }

                                            @Override
                                            public void onRequestError() {
                                                if (listener != null)
                                                    listener.onRequestError();
                                                offlineData.setSynced(false);
                                                offlineData.setSyncing(false);
                                                offlineData.setQueued(false);
                                                offlineData.updateTo(helper);
                                            }
                                        }, server, table, SwableTools.prepareTransactionJSON(table, offlineData.getData()),
                                        (offlineData.getType() != OfflineData.CUSTOMER?
                                                "?branch_id="+ offlineData.getBranch_id() + offlineData.getParameters() : offlineData
                                                .getParametersAsFirstParameter()))
                        );
                }
                else if(type == TYPE_UPDATE) {
                    getQueue().add(
                            HTTPRequests.sendPUTRequest(context, session,
                                    new VolleyRequestListener() {
                                        @Override
                                        public void onStart(Table table, RequestType requestType) {
                                            if (listener != null)
                                                listener.onStart(table, requestType);
                                        }

                                        @Override
                                        public void onSuccess(Table table, RequestType requestType, Object response) {
                                            if (listener != null)
                                                listener.onSuccess(table, requestType, response);
                                            offlineData.setSynced(true);
                                            offlineData.setSyncing(false);
                                            offlineData.setQueued(false);
                                            offlineData.updateTo(helper);
                                        }

                                        @Override
                                        public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                                            if (listener != null)
                                                listener.onError(table, hasInternet, response, responseCode);
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
                                            offlineData.updateTo(helper);
                                        }

                                        @Override
                                        public void onRequestError() {
                                            if (listener != null)
                                                listener.onRequestError();
                                            offlineData.setSynced(false);
                                            offlineData.setSyncing(false);
                                            offlineData.setQueued(false);
                                            offlineData.updateTo(helper);
                                        }
                                    }, server, table, SwableTools.prepareTransactionJSON(table, offlineData.getData()), offlineData.getReturnId(),
                                    (offlineData.getType() != OfflineData.CUSTOMER?
                                            "?branch_id="+ offlineData.getBranch_id() + offlineData.getParameters() : offlineData
                                            .getParametersAsFirstParameter()))
                    );
                }
                else if(type == TYPE_CANCEL) {
                    if(offlineData.isPagedRequest())
                        pagedDelete(table,offlineData);
                    else
                        try {
                            getQueue().add(
                                HTTPRequests.sendDELETERequest(context, session,
                                    new VolleyRequestListener() {
                                        @Override
                                        public void onStart(Table table, RequestType requestType) {
                                            if (listener != null)
                                                listener.onStart(table, requestType);
                                        }

                                        @Override
                                        public void onSuccess(Table table, RequestType requestType, Object response) {
                                            AccountTools.updateUserActiveStatus(context, true);
                                            if (listener != null)
                                                listener.onSuccess(table, requestType, response);
                                            offlineData.setSyncing(false);
                                            offlineData.setQueued(false);

                                            offlineData.setSynced(true);
                                            offlineData.setCancelled(true);
                                            offlineData.updateTo(helper);
                                        }

                                        @Override
                                        public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                                            if (listener != null)
                                                listener.onError(table, hasInternet, response, responseCode);
                                            offlineData.setSyncing(false);
                                            offlineData.setQueued(false);
                                            offlineData.setSynced(true);

                                            if (responseCode == ImonggoSwable.UNPROCESSABLE_ENTRY) {
                                                Log.e("SwableTools", "deleting failed : transaction already " +
                                                        "cancelled");
                                                offlineData.setCancelled(true);
                                            } else if (responseCode == ImonggoSwable.NOT_FOUND) {
                                                offlineData.setCancelled(false);
                                                Log.e("SwableTools", "deleting failed : transaction not found");
                                            } else {
                                                offlineData.setCancelled(false);
                                            }

                                            offlineData.setSynced(offlineData.isCancelled() || responseCode == ImonggoSwable.NOT_FOUND);
                                            offlineData.updateTo(helper);
                                        }

                                        @Override
                                        public void onRequestError() {
                                            if (listener != null)
                                                listener.onRequestError();
                                            offlineData.setSynced(false);
                                            offlineData.setSyncing(false);
                                            offlineData.setQueued(false);
                                            offlineData.updateTo(helper);
                                        }
                                    }, server, table, offlineData.getReturnId(),
                                        (offlineData.getType() != OfflineData.CUSTOMER?
                                                "?branch_id="+ offlineData.getBranch_id() + offlineData.getParameters() : offlineData
                                                .getParametersAsFirstParameter())
                                                + "&reason=" + URLEncoder.encode(offlineData.getDocumentReason(), "UTF-8") )
                            );
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                }
                queue.start();
            }

            public void pagedSend(Table table, final OfflineData offlineData) {
                try {
                    if(table == Table.ORDERS) {
                        Order order = (Order)offlineData.getObjectFromData();

                        int max_page = order.getChildCount();

                        if(offlineData.getReturnId().length() > 0) { // for retry sending
                            List<String> returnIds = offlineData.getReturnIdList();

                            List<Order> childOrders = order.getChildOrders();
                            for(int i = 0; i < childOrders.size(); i++) {
                                if(returnIds.get(i).length() <= 0 ||
                                        !returnIds.get(i).equals(ImonggoSwable.NO_RETURN_ID))
                                    continue;
                                sendThisPage(table, i+1, max_page, childOrders.get(i).toJSONObject(), offlineData);
                            }
                        } else {
                            List<Order> childOrders = order.getChildOrders();
                            for(int i = 0; i < childOrders.size(); i++) {
                                sendThisPage(table, i+1, max_page, childOrders.get(i).toJSONObject(), offlineData);
                            }
                        }
                    }
                    else if(table == Table.DOCUMENTS) {
                        Document document = (Document)offlineData.getObjectFromData();

                        int max_page = document.getChildCount();

                        if(offlineData.getReturnId().length() > 0) { // for retry sending
                            List<String> returnIds = offlineData.getReturnIdList();

                            List<Document> childDocuments = document.getChildDocuments();
                            for(int i = 0; i < childDocuments.size(); i++) {
                                if(returnIds.get(i).length() <= 0 ||
                                        !returnIds.get(i).equals(ImonggoSwable.NO_RETURN_ID))
                                    continue;
                                sendThisPage(table, i+1, max_page, childDocuments.get(i).toJSONObject(), offlineData);
                            }
                        } else {
                            List<Document> childDocuments = document.getChildDocuments();
                            for(int i = 0; i < childDocuments.size(); i++) {
                                sendThisPage(table, i+1, max_page, childDocuments.get(i).toJSONObject(), offlineData);
                            }
                        }
                    }
                    else if(table == Table.INVOICES) {
                        Invoice invoice = (Invoice)offlineData.getObjectFromData();

                        sendThisPage(table, 1, 1, invoice.toJSONObject(), offlineData);
                    }
                    else if(table == Table.CUSTOMERS) {
                        Customer customer = (Customer)offlineData.getObjectFromData();

                        sendThisPage(table, 1, 1, customer.toJSONObject(), offlineData);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            private void sendThisPage(Table table, final int page, final int maxpage, final JSONObject jsonObject,
                                      final OfflineData parent) throws JSONException {
                getQueue().add(
                        HTTPRequests.sendPOSTRequest(context, session, new VolleyRequestListener() {
                            @Override
                            public void onStart(Table table, RequestType requestType) {
                                if(listener != null)
                                    listener.onStart(table, requestType);
                            }

                            @Override
                            public void onSuccess(Table table, RequestType requestType, Object response) {
                                if(listener != null)
                                    listener.onSuccess(table, requestType, response);

                                AccountTools.updateUserActiveStatus(context, true);
                                try {
                                    if (page == maxpage) {
                                        parent.setSyncing(false);
                                        parent.setQueued(false);
                                    }

                                    if (response instanceof JSONObject) {
                                        JSONObject responseJson = ((JSONObject) response);
                                        if (responseJson.has("id")) {
                                            Log.d("SwableTools", "sending success : return ID : " +
                                                    responseJson.getString("id"));

                                            parent.insertReturnIdAt(page - 1, responseJson.getString("id"));
                                        }
                                    }

                                    parent.setSynced(page == 1 || parent.isSynced());
                                    parent.updateTo(helper);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                                if(listener != null)
                                    listener.onError(table, hasInternet, response, responseCode);

                                if(page == maxpage) {
                                    parent.setSyncing(false);
                                    parent.setQueued(false);
                                }

                                boolean isNullReturnId = true;

                                try {
                                    if(responseCode == ImonggoSwable.UNPROCESSABLE_ENTRY) {
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
                                        parent.insertReturnIdAt(page - 1, ImonggoSwable.NO_RETURN_ID);
                                        parent.setSynced(responseCode == ImonggoSwable.UNAUTHORIZED_ACCESS);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                parent.updateTo(helper);
                            }

                            @Override
                            public void onRequestError() {
                                if(listener != null)
                                    listener.onRequestError();
                                parent.setSyncing(false);
                                parent.setQueued(false);
                                parent.setSynced(false);
                            }
                        }, server, table, jsonObject, (offlineData.getType() != OfflineData.CUSTOMER?
                                "?branch_id="+ parent.getBranch_id() + parent.getParameters() : parent.getParametersAsFirstParameter()) )
                );

                getQueue().start();
            }

            public void pagedDelete(Table table, final OfflineData offlineData) {
                final List<String> list = offlineData.getReturnIdList();
                try {
                    for(final String id : list) {

                        getQueue().add(
                                HTTPRequests.sendDELETERequest(context, session, new VolleyRequestListener() {
                                    @Override
                                    public void onStart(Table table, RequestType requestType) {
                                        if(listener != null)
                                            listener.onStart(table, requestType);
                                    }

                                    @Override
                                    public void onSuccess(Table table, RequestType requestType, Object response) {
                                        AccountTools.updateUserActiveStatus(context, true);
                                        if(listener != null)
                                            listener.onSuccess(table, requestType, response);

                                        Log.e("SwableTools", "deleting success : " + response);
                                        offlineData.setSyncing(false);
                                        offlineData.setQueued(false);

                                        offlineData.setCancelled(list.indexOf(id) == 0 || offlineData.isCancelled());
                                        offlineData.setSynced(offlineData.isCancelled());
                                        offlineData.updateTo(helper);

                                        list.set(list.indexOf(id), ImonggoSwable.NO_RETURN_ID); // indicator that this has been cancelled
                                    }

                                    @Override
                                    public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                                        if(listener != null)
                                            listener.onError(table, hasInternet, response, responseCode);
                                        offlineData.setSyncing(false);
                                        offlineData.setQueued(false);

                                        if(responseCode == ImonggoSwable.UNPROCESSABLE_ENTRY) { // Already cancelled
                                            offlineData.setCancelled(true);
                                            list.set(list.indexOf(id),ImonggoSwable.NO_RETURN_ID); // indicator that this has been cancelled
                                        }
                                        else if(responseCode == ImonggoSwable.NOT_FOUND) {
                                            offlineData.setCancelled(false);
                                            list.set(list.indexOf(id),ImonggoSwable.NO_RETURN_ID); // indicator that this has been processed
                                            Log.e("SwableTools", "deleting failed : transaction not found");
                                        }
                                        else {
                                            offlineData.setCancelled(false);
                                        }

                                        offlineData.setSynced(offlineData.isCancelled() || responseCode == ImonggoSwable.NOT_FOUND ||
                                                responseCode == ImonggoSwable.UNAUTHORIZED_ACCESS);
                                        offlineData.updateTo(helper);
                                    }

                                    @Override
                                    public void onRequestError() {
                                        if(listener != null)
                                            listener.onRequestError();
                                        offlineData.setSyncing(false);
                                        offlineData.setQueued(false);
                                        offlineData.setSynced(false);
                                        offlineData.setCancelled(false);
                                        offlineData.updateTo(helper);
                                    }
                                }, server, table, id, (offlineData.getType() != OfflineData.CUSTOMER?
                                        "?branch_id="+ offlineData.getBranch_id() + offlineData.getParameters() :
                                        offlineData.getParametersAsFirstParameter()) +
                                        "&reason=" + URLEncoder.encode(offlineData.getDocumentReason(), "UTF-8"))
                        );
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
