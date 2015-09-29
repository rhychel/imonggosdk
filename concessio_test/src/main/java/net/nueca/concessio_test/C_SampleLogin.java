package net.nueca.concessio_test;

import android.content.Intent;

import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;

/**
 * Created by rhymart on 7/24/15.
 * imonggosdk (c)2015
 */
public class C_SampleLogin extends LoginActivity {

    @Override
    protected void initLoginEquipments() {
        super.initLoginEquipments();
        setRequireConcessioSettings(false);
        setServer(Server.IMONGGO);
        setAutoUpdateApp(true);
        setModulesToSync(Table.USERS.ordinal(), Table.BRANCH_USERS.ordinal(), Table.DAILY_SALES.ordinal());

    }

    @Override
    protected void showNextActivityAfterLogin() {
        finish();
        Intent intent = new Intent(this, C_Module.class);
        startActivity(intent);
    }
}
