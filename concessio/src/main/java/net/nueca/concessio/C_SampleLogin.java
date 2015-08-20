package net.nueca.concessio;

import android.content.Intent;

import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.tools.SettingTools;

/**
 * Created by rhymart on 7/24/15.
 * imonggosdk (c)2015
 */
public class C_SampleLogin extends LoginActivity {

    @Override
    protected void initLoginEquipments() {
        super.initLoginEquipments();
        setModules(new int[]{Table.USERS.ordinal(), Table.PRODUCTS.ordinal(), Table.UNITS.ordinal()});
        setServer(Server.IRETAILCLOUD_NET);
        SettingTools.updateSettings(this, SettingsName.AUTO_UPDATE, false, "");
        int[] modules = {Table.USERS.ordinal(), Table.PRODUCTS.ordinal(), Table.UNITS.ordinal()};
        setModules(modules);
        setRequireConcessioSettings(true);
    }

    @Override
    protected void showNextActivity() {
        finish();
        Intent intent = new Intent(this, C_Module.class);
        startActivity(intent);
    }

}
