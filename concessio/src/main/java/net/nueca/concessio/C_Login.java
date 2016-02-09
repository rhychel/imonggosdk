package net.nueca.concessio;

import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.DefaultRetryPolicy;
import com.crashlytics.android.Crashlytics;
import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.activities.login.BaseLoginActivity;
import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.objects.base.DBTable;
import net.nueca.imonggosdk.tools.DialogTools;
import net.nueca.imonggosdk.tools.ModuleSettingTools;
import net.nueca.imonggosdk.tools.SettingTools;

import java.sql.SQLException;
import java.util.List;

import io.fabric.sdk.android.Fabric;

/**
 * Created by rhymart on 8/20/15.
 * imonggosdk2 (c)2015
 */
public class C_Login extends LoginActivity {

    @Override
    protected void initLoginEquipments() {
        Fabric.with(this, new Crashlytics());
        super.initLoginEquipments();
        setServer(Server.IRETAILCLOUD_NET);

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
        Intent intent = new Intent(this, (SettingTools.defaultBranch(this).equals("") ? C_Welcome.class : C_Dashboard.class));//C_Dashboard
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

        return app.modulesToDownload(getHelper());

    }

    @Override
    protected void onCreateLoginLayout() {
        super.onCreateLoginLayout();
        setContentView(R.layout.c_login);

        BaseLoginActivity.TEST_ACCOUNT = true;

        setupLayoutEquipments((EditText)findViewById(R.id.etAccountId),
                (EditText)findViewById(R.id.etEmail),
                (EditText)findViewById(R.id.etPassword),
                (Button)findViewById(R.id.btnLogin));

        setEditTextAccountID("A1029");
            setEditTextEmail("A1072A_OSS-1@imonggo.com");
        setEditTextPassword("123rebisco456");

    }
}