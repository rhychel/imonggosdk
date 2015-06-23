package net.nueca.concessio;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;

import net.nueca.concessioengine.activities.LoginActivity;
import net.nueca.concessioengine.tools.DialogMaterial;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;


public class C_Login extends LoginActivity {

    @Override
    protected void initActivity() {

    }

    @Override
    protected void updateAppData() {

    }

    @Override
    protected void showSelectBranches() {

    }

    @Override
    protected void showDashBoard() {

    }

    @Override
    protected void beforeLogin() {
        Log.i("Jn-C_Login", "beforeLogin");
    }

    @Override
    protected void stopLogin() {
        btnSignIn.setVisibility(View.VISIBLE);
        dialog.hide();
    }

    @Override
    protected void afterLogin() {
        try {
            if (AccountTools.isLoggedIn(getHelper())) {
                btnSignIn.setVisibility(View.GONE);
                btnLogout.setVisibility(View.VISIBLE);

                dialog.hide();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("Jn-C_Login", "onCreate");

        try {
            if (AccountTools.isLoggedIn(getHelper()) && !AccountTools.isUnlinked(this))
                Log.e("Account", "I'm logged in!");
            else
                Log.e("Account", "I'm not logged in!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    Button btnSignIn;
    Button btnLogout;
    MaterialDialog dialog;

    @Override
    protected void createLoginLayout() {
        super.createLoginLayout();
        Log.i("Jn-C_Login", "createLoginLayout");

        btnSignIn = (Button) findViewById(R.id.btn_signin);
        btnLogout = (Button) findViewById(R.id.btn_unlink);
        dialog = DialogMaterial.createProgressDialog(this, "Log In", "Please wait...", false);
        dialog.hide();

        try {
            if (AccountTools.isLoggedIn(getHelper())) {
                btnLogout.setVisibility(View.VISIBLE);
                btnSignIn.setVisibility(View.GONE);
            } else {
                btnLogout.setVisibility(View.GONE);
                btnSignIn.setVisibility(View.VISIBLE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLogin(getApplicationContext(), "carolflower", "carol@me.com", "carolflower", Server.IMONGGO);
                btnSignIn.setVisibility(View.GONE);

                dialog.show();

            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnLogout.setVisibility(View.GONE);
                startLogout();
                unlinkAccount();
            }
        });
    }


    @Override
    public void onLogoutAccount() {
        Log.i("Jn-C_Login", "onLogoutAccount");
        btnLogout.setVisibility(View.GONE);
        btnSignIn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUnlinkAccount() {
        Log.i("Jn-C_Login", "onUnlinkAccount");
        btnLogout.setVisibility(View.GONE);
        btnSignIn.setVisibility(View.VISIBLE);

    }
}
