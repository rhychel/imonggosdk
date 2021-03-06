package net.nueca.concessio_test;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.imonggosdk.enums.DocumentTypeCode;
import net.nueca.imonggosdk.enums.OfflineDataType;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.interfaces.AccountListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.SettingTools;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by rhymart on 8/20/15.
 * imonggosdk2 (c)2015
 */
public class C_Welcome extends ModuleActivity {

    private TextView tvAgentName, tvLogout;
    private Spinner spBranch;
    private ImageButton btnBegin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_welcome);

        /*Document.Builder builder = new Document.Builder();
        try {
            builder.addDocumentLine(new DocumentLine.Builder()
                    .line_no(1)
                    .unit_content_quantity(200)
                    .unit_id(31167)
                    .unit_name("Bndl(s)")
                    .unit_quantity(60)
                    .unit_retail_price(0)
                    .product_id(192565)
                    .quantity(20)
                    .retail_price(0)
                    .build());

            builder.generateReference(this, getSession().getDevice_id());
            builder.target_branch_id(getSession().getCurrent_branch_id());
            builder.document_type_code(DocumentTypeCode.RELEASE_ADJUSTMENT);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Document document1 = builder.build();
        document1.setReference(document1.getReference()+"a");
        document1.setExtras(new Extras.Builder()
                .customer_id(182556)
                .build());
        Document document2 = builder.build();
        document2.setReference(document2.getReference()+"b");
        document2.setExtras(new Extras.Builder()
                .customer_id(182557)
                .build());

        try {

            for(OfflineData to : getHelper().fetchObjects(OfflineData.class).queryForAll())
                to.deleteTo(getHelper());

            Log.e("DOCUMENT", document1.getReference() + " " + document1.getId());
            Log.e("DOCUMENT", document1.toJSONString());

            OfflineData offlineData = new SwableTools.Transaction(getHelper())
                    .toSend()
                    .forBranch(document1.getTarget_branch_id())
                    .object(document1)
                    .queue();
            new SwableTools.Transaction(getHelper())
                    .toSend()
                    .forBranch(document2.getTarget_branch_id())
                    .object(document2)
                    .queue();
            Log.e("OFFLINEDATA " + offlineData.getId(), ((Document)offlineData.getObjectFromData()).getId() + " ~~~ " + offlineData.getObjectFromData().toString
                    ());
            int id = offlineData.getId();
            offlineData = null;
            offlineData = getHelper().fetchObjects(OfflineData.class).queryBuilder().where().eq("id", id).queryForFirst();
            Log.e("OFFLINEDATA " + offlineData.getId(), offlineData.getObjectFromData() != null ? ((Document)offlineData
                    .getObjectFromData()).getId() + "" : "null");
            Log.e("OFFLINEDATA JSON", offlineData.getObjectFromData().toString());

        }*//* catch (JSONException e) {
            e.printStackTrace();
        }*//* catch (SQLException e) {
            e.printStackTrace();
        }*/

        /*try {
            Log.e("UNITS count", getHelper().fetchObjectsList(Unit.class).size() + "");
            for(Unit unit : getHelper().fetchObjectsList(Unit.class)) {
                Log.e("UNIT " + unit.getId(), unit.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }*/

        /*Customer customer = new Customer.Builder()
                .first_name("Johnny")
                .last_name("Tester")
                .build();
        OfflineData offlineData = new SwableTools.Transaction(getHelper())
                .toSend()
                .forBranch(860)
                .object(customer)
                .queue();*/
        /*try {
            List<OfflineData> offlineDatas = getHelper().fetchObjectsList(OfflineData.class);
            for(OfflineData offlineData : offlineDatas)
                offlineData.deleteTo(getHelper());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        OfflineData offlineData = new OfflineData(
                new Document.Builder()
                        .addDocumentLine(new DocumentLine())
                        .build(),
                OfflineDataType.CANCEL_DOCUMENT);
        offlineData.setBranch_id(876);
        offlineData.setReturnId("60321");
        offlineData.insertTo(getHelper());
        new SwableTools.Transaction(getHelper())
                .toCancel()
                .object(offlineData)
                .withReason("VOID")
                .queue();
        SwableTools.startSwable(this);*/

        tvAgentName = (TextView) findViewById(R.id.tvAgentName);
        tvLogout = (TextView) findViewById(R.id.tvLogout);
        spBranch = (Spinner) findViewById(R.id.spBranch);
        btnBegin = (ImageButton) findViewById(R.id.ibtnBegin);

        tvLogout.setText(Html.fromHtml("<u><i>Not you?<br/>Logout</i></u>"));

        ArrayAdapter<Branch> branchArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, getBranches());
        spBranch.setAdapter(branchArrayAdapter);

        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    AccountTools.unlinkAccount(C_Welcome.this, getHelper(), new AccountListener() {
                        @Override
                        public void onLogoutAccount() { }

                        @Override
                        public void onUnlinkAccount() {
                            finish();
                            Intent intent = new Intent(C_Welcome.this, C_Login.class);
                            startActivity(intent);
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            tvAgentName.setText("Hello, "+getSession().getUser().getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        btnBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Branch branch = (Branch)spBranch.getSelectedItem();
                SettingTools.updateSettings(C_Welcome.this, SettingsName.DEFAULT_BRANCH, String.valueOf(branch.getId()));

                /*try {
                    Log.e("Document Query", "start " + getUser().getHome_branch_id());
                    for(Document document : getHelper().fetchObjects(Document.class).queryBuilder().where()
                        .eq("intransit_status", "Intransit").and()
                        .eq("branch_id", getUser().getHome_branch_id()).or()
                        .eq("target_branch_id", getUser().getHome_branch_id())
                        .query()) {

                        //if(document.getId() == 0)
                        //    document.deleteTo(getHelper());
                        Log.e("Document " + document.getId(), document.getReference() + " " + document.getDocument_type_code
                                ().name());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }*/
                finish();
                Intent intent = new Intent(C_Welcome.this, SampleSales.class);
                startActivity(intent);
            }
        });
    }
}
