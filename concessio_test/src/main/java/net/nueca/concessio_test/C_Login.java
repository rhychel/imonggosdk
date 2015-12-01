package net.nueca.concessio_test;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;

import com.crashlytics.android.Crashlytics;

import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.tools.SettingTools;

import io.fabric.sdk.android.Fabric;


/**
 * Created by rhymart on 8/20/15.
 * imonggosdk2 (c)2015
 */
public class C_Login extends LoginActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
    }

    @Override
    protected void initLoginEquipments() {
        super.initLoginEquipments();
        setIsUsingDefaultDialog(true);
        setIsUsingDefaultLoginLayout(true);
        Fabric.with(this, new Crashlytics());
        setRequireConcessioSettings(false);
        setServer(Server.IRETAILCLOUD_NET);
        SettingTools.updateSettings(C_Login.this, SettingsName.AUTO_UPDATE, false, "");
        setModulesToSync(
                Table.USERS.ordinal(),
                Table.BRANCH_USERS.ordinal(),
                Table.PRODUCTS.ordinal(),
                Table.CUSTOMER_CATEGORIES.ordinal(),
                Table.CUSTOMER_GROUPS.ordinal(),
                Table.CUSTOMERS.ordinal(),
                Table.BRANCH_PRODUCTS.ordinal(),
                Table.UNITS_BRANCH.ordinal(),
                Table.PRICE_LISTS.ordinal(),
                Table.PAYMENT_TYPES.ordinal(),
                Table.PAYMENT_TERMS.ordinal(),
                Table.SALES_PUSH.ordinal(),
                Table.ROUTE_PLANS.ordinal());

    }

    @Override
    protected void showNextActivityAfterLogin() {
        finish();
        Intent intent = new Intent(this, (/*SettingTools.defaultBranch(this).equals("") ?*/ C_Welcome.class
                /*:C_Dashboard.class*/));
        startActivity(intent);
    }

    @Override
    protected void onCreateLoginLayout() {
        super.onCreateLoginLayout();

        setEditTextAccountID("retailpos");
        setEditTextEmail("retailpos@test.com");
        setEditTextPassword("retailpos");
    }
}