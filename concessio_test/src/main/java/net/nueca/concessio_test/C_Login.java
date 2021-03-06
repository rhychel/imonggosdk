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
                Table.CUSTOMER_CATEGORIES.ordinal(),
                Table.CUSTOMER_BY_SALESMAN.ordinal(),
                Table.ROUTE_PLANS.ordinal(),
                Table.ROUTE_PLANS_DETAILS.ordinal(),
                Table.BRANCH_PRODUCTS.ordinal(),
                Table.PRICE_LISTS_FROM_CUSTOMERS.ordinal(),
                Table.PRICE_LISTS_DETAILS.ordinal()/*,
                Table.PAYMENT_TERMS.ordinal(),
                Table.PAYMENT_TYPES.ordinal(),
                Table.INVOICES.ordinal(),
                Table.INVOICE_PURPOSES.ordinal(),
                Table.SALES_PROMOTIONS_SALES_DISCOUNT.ordinal(),
                Table.SALES_PROMOTIONS_SALES_DISCOUNT_DETAILS.ordinal(),
                Table.SALES_PROMOTIONS_POINTS.ordinal(),
                Table.SALES_PROMOTIONS_POINTS_DETAILS.ordinal()*/);
    }

    @Override
    protected void showNextActivityAfterLogin() {
        finish();
        Intent intent = new Intent(this, ( C_Welcome.class));
        startActivity(intent);
    }

    @Override
    protected void onCreateLoginLayout() {
        super.onCreateLoginLayout();
        setEditTextAccountID("C5111");
        setEditTextEmail("C5111A_OSS-1@imonggo.com");
        setEditTextPassword("123rebisco456");
    }
}