package net.nueca.concessio;

import android.content.Intent;

import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.imonggosdk.enums.Server;

/**
 * Created by rhymart on 7/24/15.
 * imonggosdk (c)2015
 */
public class C_SampleLogin extends LoginActivity {

    @Override
    protected void initLoginEquipments() {
        super.initLoginEquipments();
        setServer(Server.IRETAILCLOUD_COM);
    }

    @Override
    protected void syncingModulesSuccessful() {
        super.syncingModulesSuccessful();
        finish();
        Intent intent = new Intent(this, C_Module.class);
        startActivity(intent);
    }

    @Override
    protected void showNextActivity() {
        finish();
        Intent intent = new Intent(this, C_Module.class);
        startActivity(intent);
    }

}
