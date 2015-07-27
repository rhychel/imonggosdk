package net.nueca.concessio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.imonggosdk.enums.OfflineDataType;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.swable.ImonggoSwable;
import net.nueca.imonggosdk.swable.ImonggoSwableServiceConnection;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.concessioengine.activities.login.BaseLoginActivity;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.NotificationTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class C_Login extends LoginActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("C_Login", "onCreate");
        super.onCreate(savedInstanceState);

        try {
            if (AccountTools.isLoggedIn(getHelper()) && !AccountTools.isUnlinked(this)) {
                Log.e("Account", "I'm logged in!");
                Log.i("session pos id", getSession().getDevice_id() + "");
                Log.i("session server", getSession().getServer() + "");
            } else
                Log.e("Account", "I'm not logged in!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void syncingModulesSuccessful() {

    }

@Override
    protected void showNextActivity() {
        finish();
        Intent intent = new Intent(this, C_Module.class);
        startActivity(intent);
    }

    ImonggoSwableServiceConnection swableServiceConnection;
    @Override
    protected void updateAppData() {
        Log.e("C_Login", "updateAppData");
        // update the app code here
    }

    @Override
    protected void updateModules() {
        Log.e("C_Login", "updateModules");
        // set the list of modules here
        int[] modules = {Table.BRANCHES.ordinal(), Table.PRODUCTS.ordinal(), Table.CUSTOMERS.ordinal() };
        setModules(modules);

        swableServiceConnection = new ImonggoSwableServiceConnection(getApplicationInfo().icon,new ImonggoSwable
                .SwableStateListener() {
            @Override
            public void onSwableStarted() {
                Log.e("onSwableStarted", "called");
            }

            @Override
            public void onQueued(OfflineData offlineData) {
                Log.e("onQueued", "called");
            }

            @Override
            public void onSyncing(OfflineData offlineData) {
                Log.e("onSyncing", "called");
            }

            @Override
            public void onSynced(OfflineData offlineData) {
                Log.e("onSynced", "called");
            }

            @Override
            public void onSyncProblem(OfflineData offlineData, boolean hasInternet, Object response, int responseCode) {
                Log.e("onSyncProblem", "called");
            }

            @Override
            public void onUnauthorizedAccess(Object response, int responseCode) {
                Log.e("onUnauthorizedAccess", "called");
            }

            @Override
            public void onAlreadyCancelled(OfflineData offlineData) {
                Log.e("onAlreadyCancelled", "called");
            }

            @Override
            public void onSwableStopping() {
                Log.e("onSwableStopping", "called");
            }
        });
        SwableTools.startAndBindSwable(this, swableServiceConnection);

        try {
            //Log.e("Device ID",getSession().getDevice_id() + "");

            try {
                JSONObject invoicejson = new JSONObject("{\"invoice\":{\"invoice_lines\":[{\"product_id\":115534," +
                        "\"quantity\":2,\"retail_price\":261,\"discount_text\":\"40%\"},{\"product_id\":115514," +
                        "\"quantity\":1,\"retail_price\":261,\"discount_text\":\"5%\"},{\"product_id\":115510," +
                        "\"quantity\":2,\"retail_price\":261,\"discount_text\":\"5%\"}],\"invoice_date\":" +
                        "\"2015-06-30T03:41:30.25200Z\",\"status\":\"5\",\"email\":\"\",\"user_id\":523," +
                        "\"tax_inclusive\":true,\"remark\":\"\",\"reference\":\"475-10\",\"payments\":[{" +
                        "\"payment_type_id\":2,\"amount\":1007.05},{\"amount\":50,\"payment_type_id\":1," +
                        "\"tender\":50}],\"invoice_tax_rates\":[{\"tax_rate_id\":\"\",\"amount\":113.2553571428571," +
                        "\"rate\":0.12}]}}");

                Invoice invoice = Invoice.fromJSONObject(invoicejson);
                invoice.generateNewReference(this, getSession().getDevice_id());

                JSONObject orderjson = new JSONObject("{\"order\":{\"target_delivery_date\":\"2015-06-26\",\"order_type_code\":" +
                        "\"stock_request\",\"serving_branch_id\":357,\"reference\":\"488-20\",\"" +
                        "order_lines\":[{\"product_id\":115552,\"retail_price\":261,\"quantity\":5," +
                        "\"line_no\":1},{\"product_id\":115553,\"retail_price\":261,\"quantity\":25,\"" +
                        "line_no\":2},{\"product_id\":115556,\"retail_price\":261,\"quantity\":9," +
                        "\"line_no\":3}]}}");

                Order order = Order.fromJSONObject(orderjson);
                order = new Order.Builder()
                        .order_type_code("stock_request")
                        .serving_branch_id(357)
                        .order_lines(order.getOrderLines())
                        .generateReference(this, getSession().getDevice_id())
                        .target_delivery_date("2015-06-26")
                        .build();
                //order.generateNewReference(this, getSession().getDevice_id());

                JSONObject docjson = new JSONObject("{\"document\":{\"reference\":\"2759-1\",\"document_type_code\":" +
                        "\"physical_count\",\"document_lines\":[{\"product_id\":115552,\"line_no\": 1,\"quantity\":5," +
                        "\"extended_attributes\":{\"brand\":\"Rsrh\",\"delivery_date\":\"2015-07-20\"}},{\"product_id\":" +
                        "115553,\"line_no\": 2,\"quantity\":55.5,\"extended_attributes\":{\"brand\":\"\",\"" +
                        "delivery_date\":\"2015-07-23\"}},{\"product_id\":115554,\"line_no\": 3,\"quantity\":5," +
                        "\"extended_attributes\":{\"brand\":\"\",\"delivery_date\":\"2015-07-20\"}},{\"product_id\":" +
                        "115554,\"line_no\": 4,\"quantity\":66.58,\"extended_attributes\":{\"brand\":\"RR\"," +
                        "\"delivery_date\":\"2015-07-23\"}}]}}");
                Document document = Document.fromJSONObject(docjson);
                document = new Document.Builder()
                            .document_lines(document.getDocument_lines())
                            .document_type_code("physical_count")
                            .generateReference(this, getSession().getDevice_id())
                            .build();

                //SwableTools.sendTransaction(getHelper(), 429, invoice, OfflineDataType.SEND_INVOICE);
                //SwableTools.sendTransaction(getHelper(), 429, order, OfflineDataType.SEND_ORDER);
                SwableTools.sendTransaction(getHelper(), 429, document, OfflineDataType.SEND_DOCUMENT);
                Log.e("-----------OfflineData", "size : " + getHelper().getOfflineData().queryForAll().size());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreateSelectBranchLayout() {
        Log.e("C_Login", "onCreateSelectBranchLayout");
        updateModules();
    }

    @Override
    protected void beforeLogin() {
        Log.e("C_Login", "beforeLogin");

    }

    @Override
    protected void stopLogin() {
        Log.e("C_Login", "stopLogin");

    }

    @Override
    protected void loginSuccess() {
        Log.e("C_Login", "loginSuccess");

        /*List<Table> list = new ArrayList<>();

        list.add(Table.BRANCH_USERS);
        list.add(Table.USERS);

        DialogTools.showCustomDialog(this, list, "Updating", false);*/
    }

    /**
     * Using Custom Layout
     *
     * 1. call setUsingCustomLayout(...);
     * 2. call setContentView(...);
     * 3. call the function setupLayoutEquipments(...); and it will automatically set the logic
     */
    @Override
    protected void onCreateLoginLayout() {
        Log.e("C_Login", "onCreateLoginLayout");

    }

    @Override
    public void onLogoutAccount() {
        Log.e("C_Login", "onLogoutAccount");

    }

    @Override
    public void onUnlinkAccount() {
        Log.e("C_Login", "onUnlinkAccount");

    }
}
