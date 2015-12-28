package net.nueca.concessio_test;

import android.content.Intent;
import android.os.Bundle;

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
        //setIsUsingDefaultDialog(true);
        //setIsUsingDefaultLoginLayout(true);
        Fabric.with(this, new Crashlytics());
        setRequireConcessioSettings(false);
        setRequireObjectConcessioSettings(false);
        setServer(Server.IRETAILCLOUD_NET);

        SettingTools.updateSettings(C_Login.this,
                SettingsName.AUTO_UPDATE, false, "");

        setModulesToSync(
                Table.USERS_ME.ordinal(),
                Table.BRANCH_USERS.ordinal(),
                Table.SETTINGS.ordinal(),
                Table.PRODUCTS.ordinal(),
                Table.UNITS.ordinal(),
                Table.ROUTE_PLANS.ordinal(),
               /* Table.CUSTOMER_CATEGORIES.ordinal(),
                Table.CUSTOMER_BY_SALESMAN.ordinal(),
                Table.CUSTOMER_GROUPS.ordinal(),
                Table.BRANCH_PRODUCTS.ordinal(),
                Table.BRANCH_PRICE_LISTS.ordinal(),
                Table.PRICE_LISTS_DETAILS.ordinal(),
                Table.PAYMENT_TYPES.ordinal(),
                Table.PAYMENT_TERMS.ordinal(),
                Table.INVOICES.ordinal(),
                Table.INVOICE_PURPOSES.ordinal(),*/
                Table.SALES_PUSH.ordinal(),
                Table.SALES_PROMOTIONS_DISCOUNT.ordinal());
    }

    @Override
    protected void showNextActivityAfterLogin() {
        finish();
        Intent intent = new Intent(this, ( C_Customers.class));
        startActivity(intent);
    }

    @Override
    protected void onCreateLoginLayout() {
        super.onCreateLoginLayout();
        setEditTextAccountID("A1001");
        setEditTextEmail("A1001@imonggo.com");
        setEditTextPassword("A1001");
    }
}