package net.nueca.concessio;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import net.nueca.concessioengine.activities.ModuleActivity;
import net.nueca.imonggosdk.interfaces.AccountListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;

/**
 * Created by rhymart on 8/20/15.
 * imonggosdk2 (c)2015
 */
public class C_Welcome extends ModuleActivity {

    private TextView tvAgentName, tvLogout;
    private Spinner spBranch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_welcome);

        tvAgentName = (TextView) findViewById(R.id.tvAgentName);
        tvLogout = (TextView) findViewById(R.id.tvLogout);
        spBranch = (Spinner) findViewById(R.id.spBranch);

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
    }
}
