package net.nueca.dizonwarehouse;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.crashlytics.android.Crashlytics;

import net.nueca.concessioengine.activities.login.BaseLoginActivity;
import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.DialogTools;
import net.nueca.imonggosdk.tools.SettingTools;

import io.fabric.sdk.android.Fabric;

public class WH_Login extends LoginActivity {

    @Override
    protected void initLoginEquipments() {
        Fabric.with(this, new Crashlytics());
        super.initLoginEquipments();
        //setServer(Server.IRETAILCLOUD_NET);
        //SettingTools.updateSettings(this, SettingsName.SERVERS, "{\"A1029\":\"rebisco\"}");

        setRequireConcessioSettings(true);
        setRequireObjectConcessioSettings(true);
    }

    @Override
    protected void successLogin() {
        super.successLogin();
        int []modulesToDownload = generateModules();
        if(modulesToDownload.length == 0) {
            DialogTools.showDialog(this, "Ooops!", "Kindly contact admin to setup your Concessio.", "Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    unlinkAccount();
                }
            }, R.style.AppCompatDialogStyle_Light);
            return;
        }
        setModulesToSync(modulesToDownload);
        getSyncModules().initializeTablesToSync(modulesToDownload);
    }

    @Override
    protected void updateAppData() {
        super.updateAppData();
        int []modulesToDownload = generateModules();
        setModulesToSync(modulesToDownload);

        getSyncModules().initializeTablesToSync(modulesToDownload);
    }

    @Override
    protected void showNextActivityAfterLogin() {
        finish();
        Intent intent = new Intent(this, WH_Welcome.class/*(SettingTools.defaultBranch(this).equals("") ? WH_Welcome.class : WH_Dashboard.class)*/);
        //C_Dashboard
        startActivity(intent);
    }

    private int[] generateModules() {
        Log.e("generateModules", "here");
        getSyncModules().setSyncAllModules(false);

        ModuleSetting app = ModuleSetting.fetchById(getHelper(), ModuleSetting.class, "app");
        if(app == null) {
            Log.e("App", "nothing is defined");
            return new int[0];
        }
        else
            Log.e("App", app.getSequences().size()+"---");

        return app.modulesToDownload(getHelper(), app.isShow_only_sellable_products());
/*        return new int[]{Table.USERS_ME.ordinal(),
                Table.BRANCH_USERS.ordinal(), Table.SETTINGS.ordinal(),
                Table.UNITS.ordinal(), Table.BRANCH_PRODUCTS.ordinal()};*/

    }

    @Override
    protected void onCreateLoginLayout() {
        super.onCreateLoginLayout();
        setContentView(R.layout.wh_login);

        Log.e("Unlinked", AccountTools.isUnlinked(this)+"---");

        BaseLoginActivity.TEST_ACCOUNT = true;

        setupLayoutEquipments((EditText)findViewById(R.id.etAccountId),
                (EditText)findViewById(R.id.etEmail),
                (EditText)findViewById(R.id.etPassword),
                (Button)findViewById(R.id.btnLogin));


    }
}
