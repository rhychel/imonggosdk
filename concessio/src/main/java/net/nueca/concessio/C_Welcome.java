package net.nueca.concessio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import net.nueca.concessioengine.activities.welcome.WelcomeActivity;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.interfaces.AccountListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.SettingTools;

import java.sql.SQLException;
import java.util.Calendar;

/**
 * Created by rhymart on 8/20/15.
 * imonggosdk2 (c)2015
 */
public class C_Welcome extends WelcomeActivity {

    private TextView tvAgentName;
    private Spinner spBranch;
    private Button btnBegin, btnNotYou;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_welcome);

        tvAgentName = (TextView) findViewById(R.id.tvAgentName);
        btnNotYou = (Button) findViewById(R.id.btnNotYou);
        spBranch = (Spinner) findViewById(R.id.spBranch);
        btnBegin = (Button) findViewById(R.id.btnBegin);

        ArrayAdapter<Branch> branchArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, getBranches());
        branchArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
        spBranch.setAdapter(branchArrayAdapter);

        btnNotYou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    AccountTools.logoutUser(C_Welcome.this, getHelper(), new AccountListener() {
                        @Override
                        public void onLogoutAccount() {
                            finish();
                            Intent intent = new Intent(C_Welcome.this, C_Login.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onUnlinkAccount() { }
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

                finish();
                Intent intent = new Intent(C_Welcome.this, C_Dashboard.class);
                startActivity(intent);
            }
        });
    }
}
