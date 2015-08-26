package net.nueca.concessio_test;

import android.content.Intent;

import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.tools.SettingTools;

/**
 * Created by rhymart on 7/24/15.
 * imonggosdk (c)2015
 */
public class C_SampleLogin extends LoginActivity {

    @Override
    protected void initLoginEquipments() {
        super.initLoginEquipments();
        setServer(Server.IRETAILCLOUD_NET);
        setRequireConcessioSettings(true);
        SettingTools.updateSettings(this, SettingsName.AUTO_UPDATE, true, "");
        /*setModules(Table.USERS.ordinal(), Table.BRANCH_USERS.ordinal(),
                Table.PRODUCTS.ordinal(),
                Table.DOCUMENTS.ordinal());*/

    }

    @Override
    protected void showNextActivity() {
        finish();
        Intent intent = new Intent(this, C_Module2.class);
        startActivity(intent);
    }
}
