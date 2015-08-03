package net.nueca.concessio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.tools.LoggingTools;
import net.nueca.imonggosdk.tools.SettingTools;

import java.sql.SQLException;

public class C_Login extends LoginActivity {

    String TAG = "C_Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void initLoginEquipments() {
        super.initLoginEquipments();
        setServer(Server.IRETAILCLOUD_COM);

        // set the Modules to download
        int[] modules = {Table.USERS.ordinal(), Table.PRODUCTS.ordinal(), Table.UNITS.ordinal()};
        setModules(modules);

        SettingTools.updateSettings(this, SettingsName.AUTO_UPDATE, true, "");
    }


    @Override
    protected void showNextActivity() {

        finish();
        Intent intent = new Intent(this, C_Module.class);
        startActivity(intent);

        try {
            if (getSession().getUser() != null)
                LoggingTools.showToast(C_Login.this, "Welcome " + getSession().getUser().getName() + "!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
