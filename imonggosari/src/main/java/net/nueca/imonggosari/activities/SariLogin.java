package net.nueca.imonggosari.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.AccountSettings;

import io.fabric.sdk.android.Fabric;

public class SariLogin extends LoginActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
    }

    @Override
    protected void initLoginEquipments() {
        super.initLoginEquipments();
        setServer(Server.IRETAILCLOUD_COM);

        setRequireConcessioSettings(false);
    }

    @Override
    protected void successLogin() {
        super.successLogin();
        int []modulesToDownload = generateModules(true);
        setModulesToSync(generateModules(true));
        getSyncModules().initializeTablesToSync(modulesToDownload);
    }

    @Override
    protected void updateAppData() {
        super.updateAppData();
        int []modulesToDownload = generateModules(true);
        setModulesToSync(generateModules(false));

        getSyncModules().initializeTablesToSync(modulesToDownload);
    }

    @Override
    protected void showNextActivityAfterLogin() {
        finish();
        Intent intent = new Intent(this, SariMain.class);
        Log.e(">>>>", "SariMain");
        startActivity(intent);
    }

    private int[] generateModules(boolean includeUsers) {
        getSyncModules().setSyncAllModules(false);
        int modulesToDownload = includeUsers ? 4 : 2;
        if(AccountSettings.hasPullout(this)) {
            modulesToDownload+=2;
        }
        if(AccountSettings.hasReceive(this))
            modulesToDownload++;
        if(AccountSettings.hasSales(this))
            modulesToDownload++;

        int index = modulesToDownload;
        int[] modules = new int[modulesToDownload];
        if(includeUsers) {
            modules[modulesToDownload - (index--)] = Table.USERS.ordinal();
            modules[modulesToDownload - (index--)] = Table.BRANCH_USERS.ordinal();
        }
        modules[modulesToDownload-(index--)] = Table.PRODUCTS.ordinal();
        modules[modulesToDownload-(index--)] = Table.UNITS.ordinal();
        if(AccountSettings.hasPullout(this)) {
            modules[modulesToDownload-(index--)] = Table.DOCUMENT_TYPES.ordinal();
            modules[modulesToDownload-(index--)] = Table.DOCUMENT_PURPOSES.ordinal();
        }
        if(AccountSettings.hasReceive(this))
            modules[modulesToDownload-(index--)] = Table.DOCUMENTS.ordinal();
        if(AccountSettings.hasSales(this))
            modules[modulesToDownload-(index--)] = Table.CUSTOMERS.ordinal();

        return modules;
    }

    @Override
    protected void onCreateLoginLayout() {
        super.onCreateLoginLayout();

        setEditTextAccountID("imaghinesari");
        setEditTextEmail("imaghinesari@gmail.com");
        setEditTextPassword("imaghinesari");
    }
}
