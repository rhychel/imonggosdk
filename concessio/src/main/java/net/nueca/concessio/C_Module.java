package net.nueca.concessio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.gson.Gson;

import net.nueca.concessioengine.activities.ModuleActivity;
import net.nueca.concessioengine.adapters.SimpleProductListAdapter;
import net.nueca.concessioengine.adapters.customer.SimpleCustomerAdapter;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.imonggosdk.enums.OfflineDataType;
import net.nueca.imonggosdk.objects.Customer;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.document.ExtendedAttributes;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.objects.order.OrderLine;
import net.nueca.imonggosdk.swable.ImonggoSwable;
import net.nueca.imonggosdk.swable.ImonggoSwableServiceConnection;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.AccountTools;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by rhymart on 6/4/15.
 * imonggosdk (c)2015
 */
public class C_Module extends ModuleActivity {

    private ListView lvSampleProducts;
    private ImonggoSwableServiceConnection imonggoSwableServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_list);
        lvSampleProducts = (ListView) findViewById(R.id.lvSampleProducts);

        /*try {
            SimpleProductListAdapter simpleProductListAdapter = new SimpleProductListAdapter(this, getHelper(), getHelper().getProducts().queryForAll());
            lvSampleProducts.setAdapter(simpleProductListAdapter);
        } catch (SQLException e) {
            e.printStackTrace();
        }*/

        try {
            List<Customer> customerList = new ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                Customer customer = new Customer();
                customer.setId(i);
                customer.setAlternate_code(String.format("#%011d",(int)(Math.random()*10000000)));
                customer.setStreet("Unit 403B DECA Corporate Center, Panganiban Drive");
                customer.setCity("Naga City");
                customer.setZipcode("4400");
                customer.setCountry("Philippines");
                customer.setFirst_name("Pepe");
                customer.setLast_name("Smith " + i);
                customer.setName("Pepe Smith " + i);
                customer.setGender("M");
                customerList.add(customer);
            }
            Log.e("Customers", getHelper().getCustomers().countOf() + "");
            SimpleCustomerAdapter simpleCustomerAdapter = new SimpleCustomerAdapter(this, customerList);
            lvSampleProducts.setAdapter(simpleCustomerAdapter);
            lvSampleProducts.setOnItemClickListener(simpleCustomerAdapter);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        imonggoSwableServiceConnection = new ImonggoSwableServiceConnection(R.drawable.ic_check_circle,
                new ImonggoSwable.SwableStateListener() {
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
                        Log.e("onSynced", "called -- " + offlineData.getData());
                    }

                    @Override
                    public void onSyncProblem(OfflineData offlineData, boolean hasInternet, Object response,
                                              int responseCode) {
                        Log.e("onSyncProblem", "called -- " + offlineData.getData());
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
        SwableTools.startAndBindSwable(this, imonggoSwableServiceConnection);

        try {
            /*Document document = new Document.Builder()
                    .document_type_code("release_branch")
                    .remark("delivery_reference_no=32-100000200,page=1/1")
                    .addDocumentLine(
                            new DocumentLine.Builder()
                                    .product_id(98032)
                                    .line_no(1)
                                    .quantity(30)
                                    .discount_text("0.0%")
                                    .price(1)
                                    .retail_price(553.2)
                                    .unit_id(81)
                                    .unit_content_quantity(5)
                                    .unit_name("pack")
                                    .unit_quantity(6)
                                    .unit_retail_price(2766)
                                    .build()
                    )
                    .document_purpose_name("Transfer to Warehouse")
                    .target_branch_id(418)
                    .generateReference(this, getSession().getDevice_id())
                    .build();
            Log.e("DOCUMENT", document.toJSONString());
            SwableTools.sendTransaction(getHelper(),277,document, OfflineDataType.SEND_DOCUMENT);*/
            Order order = new Order.Builder()
                    .target_delivery_date("2015-06-26")
                    .order_type_code("stock_request")
                    .serving_branch_id(357)
                    .generateReference(this, getSession().getDevice_id())
                    .addOrderLine(
                            new OrderLine.Builder()
                                    .product_id(115552)
                                    .retail_price(261)
                                    .quantity(5)
                                    .line_no(1)
                                    .build()
                    )
                    .addOrderLine(
                            new OrderLine.Builder()
                                    .product_id(115553)
                                    .retail_price(261)
                                    .quantity(25)
                                    .line_no(2)
                                    .build()
                    )
                    .addOrderLine(
                            new OrderLine.Builder()
                                    .product_id(115556)
                                    .retail_price(261)
                                    .quantity(9)
                                    .line_no(3)
                                    .build()
                    )
                    .build();
            Gson gson = new Gson();
            Log.e("ORDERS", gson.toJson(order.getChildOrders()));
            Document document = new Document.Builder()
                    .document_type_code("release_branch")
                    .remark("delivery_reference_no=32-100000200,page=1/1")
                    .addDocumentLine(
                            new DocumentLine.Builder()
                                    .product_id(36151)
                                    .line_no(1)
                                    .quantity(5)
                                    .extended_attributes(
                                            new ExtendedAttributes.Builder()
                                                    .brand("Rsrh")
                                                    .delivery_date("2015-07-20")
                                                    .build()
                                    )
                                    .build()
                    )
                    .addDocumentLine(
                            new DocumentLine.Builder()
                                    .product_id(36151)
                                    .line_no(2)
                                    .quantity(55.5)
                                    .extended_attributes(
                                            new ExtendedAttributes.Builder()
                                                    .brand("Dm")
                                                    .delivery_date("2015-07-23")
                                                    .build()
                                    )
                                    .build()
                    )
                    .document_purpose_name("Transfer to Warehouse")
                    .target_branch_id(418)
                    .generateReference(this, getSession().getDevice_id())
                    .build();
            Log.e("DOCUMENT", gson.toJson(document.getChildDocuments()));

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.c_module, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.sample:

                    AccountTools.unlinkAccount(C_Module.this, getHelper(), null);
                    Intent intent = new Intent(C_Module.this, C_Login.class);

                    startActivity(intent);
                    finish();

                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProductsAdapterHelper.destroyProductAdapterHelper();
        SwableTools.stopAndUnbindSwable(this, imonggoSwableServiceConnection);
    }
}
