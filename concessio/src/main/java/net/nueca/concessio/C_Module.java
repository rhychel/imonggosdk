package net.nueca.concessio;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import net.nueca.concessioengine.activities.ModuleActivity;
import net.nueca.concessioengine.adapters.SimpleProductListAdapter;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.imonggosdk.enums.DocumentTypeCode;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.document.ExtendedAttributes;
import net.nueca.imonggosdk.swable.ImonggoSwableServiceConnection;
import net.nueca.imonggosdk.swable.SwableTools;

import org.json.JSONException;

import java.sql.SQLException;
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

        try {
            SimpleProductListAdapter simpleProductListAdapter = new SimpleProductListAdapter(this, getHelper(),
                    getHelper().getProducts().queryForAll());
            lvSampleProducts.setAdapter(simpleProductListAdapter);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        imonggoSwableServiceConnection = new ImonggoSwableServiceConnection(null);
        SwableTools.startAndBindSwable(this, imonggoSwableServiceConnection);

        try {
            Document document = new Document.Builder()
                    .generateReference(this, getSession().getDevice_id())
                    .document_type_code(DocumentTypeCode.RECEIVE_ADJUSTMENT)
                    .addDocumentLine(
                            new DocumentLine.Builder()
                                    .product_id(179215)
                                    .line_no(1)
                                    .quantity(5)
                                    .extended_attributes(
                                            new ExtendedAttributes.Builder()
                                                    .delivery_date("2015-07-20")
                                                    .brand("Rsrh")
                                                    .build()
                                    )
                                    .build()
                    )
                    .addDocumentLine(
                            new DocumentLine.Builder()
                                    .product_id(179216)
                                    .line_no(2)
                                    .quantity(55.5)
                                    .extended_attributes(
                                            new ExtendedAttributes.Builder()
                                                    .delivery_date("2015-07-23")
                                                    .brand("Dm")
                                                    .build()
                                    )
                                    .build()
                    )
                    .addDocumentLine(
                            new DocumentLine.Builder()
                                    .product_id(179217)
                                    .line_no(3)
                                    .quantity(5)
                                    .extended_attributes(
                                            new ExtendedAttributes.Builder()
                                                    .delivery_date("2015-07-20")
                                                    .brand("")
                                                    .build()
                                    )
                                    .build()
                    )
                    .addDocumentLine(
                            new DocumentLine.Builder()
                                    .product_id(179218)
                                    .line_no(4)
                                    .quantity(66.58)
                                    .extended_attributes(
                                            new ExtendedAttributes.Builder()
                                                    .delivery_date("2015-07-23")
                                                    .brand("Midfield")
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();
            new SwableTools.Transaction(getHelper())
                    .toSend()
                    .object(document)
                    .forBranch(getSession().getUser().getHome_branch_id())
                    .queue();
            /*new SwableTools.Transaction(getHelper())
                    .toCancel()
                    .objectContainingReturnId("46827") // or .object(<OfflineData object>)
                    .withReason("TRY")
                    .queue();*/
            //SwableTools.sendTransaction(getHelper(),getSession(),document, OfflineDataType.SEND_DOCUMENT);
            List<OfflineData> offlineDataList = getHelper().getOfflineData().queryForAll();
            for(OfflineData offlineData : offlineDataList)
                Log.e("offlinedata", offlineData.getReturnId());
            /*SwableTools.voidTransaction(getHelper(), offlineDataList.get(2),
                    OfflineDataType.CANCEL_DOCUMENT, "test");*/
            //Log.e("DOCUMENT", document.toString());
        } catch (SQLException e) {
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
        /*try {
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
        }*/
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProductsAdapterHelper.destroyProductAdapterHelper();
        imonggoSwableServiceConnection.forceStart();
        SwableTools.stopAndUnbindSwable(this,imonggoSwableServiceConnection);
    }
}
