package nueca.net.salesdashboard.activities;


import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;

import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.imonggosdk.dialogs.DialogTools;
import net.nueca.imonggosdk.enums.DialogType;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.tools.SettingTools;

import java.sql.SQLException;

import io.fabric.sdk.android.Fabric;
import nueca.net.salesdashboard.R;
import nueca.net.salesdashboard.tools.HUDTools;


/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public class DashboardLoginActivity extends LoginActivity {

    public static String TAG = "DashboardLoginActivity";
    private Button loginButton;
    private String user_role_code;

    @Override
    protected void initLoginEquipments() {
        super.initLoginEquipments();
        setIsUsingDefaultDialog(false);
        setIsUsingDefaultLoginLayout(false);
        Fabric.with(this, new Crashlytics());
        setRequireConcessioSettings(false);
        setServer(Server.IMONGGO);
        SettingTools.updateSettings(DashboardLoginActivity.this, SettingsName.AUTO_UPDATE, false, "");
        setModulesToSync(Table.USERS.ordinal(), Table.BRANCH_USERS.ordinal(), Table.SETTINGS.ordinal());
    }

    @Override
    protected void onCreateLoginLayout() {
        super.onCreateLoginLayout();
        setContentView(R.layout.login);
        setupLayoutEquipments((EditText) findViewById(R.id.etAccountId),
                (EditText) findViewById(R.id.etEmail),
                (EditText) findViewById(R.id.etPassword),
                (Button) findViewById(R.id.btnSignIn));
/*
        setEditTextAccountID("ourlovelybotique");                   // ACCOUNT ID
        setEditTextEmail("owner@ourlovelybotique.com");             // EMAIL
        setEditTextPassword("ourlovelybotique");*/
        loginButton = (Button) findViewById(R.id.btnSignIn);
    }

    @Override
    protected void dialogPositiveButtonAction() {
        loginButton.setEnabled(true);
    }

    @Override
    protected void showCustomDownloadDialog() {
        setIsUsingDefaultCustomDialogForSync(false);
        if (isUsingDefaultDialog()) {
            DialogTools.showIndeterminateProgressDialog(DashboardLoginActivity.this, null, getString(R.string.login_progress_text), false);
        } else {
            HUDTools.showIndeterminateProgressHUD(DashboardLoginActivity.this, getString(R.string.login_progress_text), false);
        }
    }

    @Override
    protected void showNextActivityAfterLogin() {
        HUDTools.hideIndeterminateProgressDialog();
        try {
            user_role_code = getSession().getUser().getRole_code();
            Log.e(TAG, user_role_code);

            if (user_role_code.equals("owner") || user_role_code.equals("manager")) {
                finish();
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
            } else if (user_role_code.equals("head_office")) {
                DialogTools.showBasicWithTitle(DashboardLoginActivity.this, "Notice",
                        "Invalid use of head office.",
                        "Ok", null, false, new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                Log.e(TAG, getServer() + "");
                                startLogout();
                                finish();
                                Intent intent = new Intent(DashboardLoginActivity.this, DashboardLoginActivity.class);
                                startActivity(intent);
                            }
                        }
                );
            } else {
                DialogTools.showBasicWithTitle(DashboardLoginActivity.this, "Oops!",
                        "Sorry. Your account credentials is not provisioned to use Imonggo Sales Dashboard. A manager or owner account type is required.",
                        "Ok", null, false, new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                Log.e(TAG, getServer() + "");
                                startLogout();
                                finish();
                                Intent intent = new Intent(DashboardLoginActivity.this, DashboardLoginActivity.class);
                                startActivity(intent);
                            }
                        }
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void showProgressDialog(DialogType type, String message, String positiveText, String negativeText) {
        super.showProgressDialog(type, message, positiveText, negativeText);
        Log.e(TAG, "message: " + message);
        HUDTools.showIndeterminateProgressHUD(DashboardLoginActivity.this, message, false);
    }

    @Override
    protected void beforeLogin() {
        loginButton.setEnabled(false);
    }

    @Override
    protected void successLogin() {
        super.successLogin();
        Log.e(TAG, "Login Successful..");
        HUDTools.hideIndeterminateProgressDialog();
    }

   /* String requires_premium_subscription = getResources().getString(R.string.error_response_requires_premium_subscription);
    if (message.equals(requires_premium_subscription)) {
        DialogTools.showBasicWithTitle(HomeActivity.this,
                getResources().getString(R.string.error_dialog_title_requires_premium_subscription),
                getResources().getString(R.string.error_dialog_message_requires_premium_subscription),
                "Ok", null, false, new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
                    }
                });*/

    @Override
    protected void stopLogin() {
        loginButton.setEnabled(true);
        HUDTools.hideIndeterminateProgressDialog();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        HUDTools.hideIndeterminateProgressDialog();
    }
}
