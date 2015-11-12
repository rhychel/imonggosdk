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
        setIsUsingDefaultDialog(true);
        setIsUsingDefaultLoginLayout(true);
        Fabric.with(this, new Crashlytics());
        setRequireConcessioSettings(false);
        setServer(Server.IMONGGO);
        SettingTools.updateSettings(C_Login.this, SettingsName.AUTO_UPDATE, false, "");
        setModulesToSync(Table.USERS.ordinal(), Table.BRANCH_USERS.ordinal(), Table.SETTINGS.ordinal(), Table.CUSTOMERS.ordinal());


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

        setEditTextAccountID("ourlovelybotique");
        setEditTextEmail("owner@ourlovelybotique.com");
        setEditTextPassword("ourlovelybotique");
    }
}