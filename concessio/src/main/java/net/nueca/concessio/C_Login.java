package net.nueca.concessio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;

import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;

public class C_Login extends LoginActivity {


    @Override
    protected void updateAppData() {

    }

    @Override
    protected void beforeLogin() {

    }

    @Override
    protected void stopLogin() {

    }


    @Override
    protected void syncingModulesSuccessful() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (AccountTools.isLoggedIn(getHelper()) && !AccountTools.isUnlinked(this)) {
                Log.e("Account", "I'm logged in!");
                Log.i("session pos id", getSession().getDevice_id() + "");
                Log.i("session server", getSession().getServer() + "");
            } else
                Log.e("Account", "I'm not logged in!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLogoutAccount() {

    }

    @Override
    public void onUnlinkAccount() {

    }

    @Override
    protected void showNextActivity() {
        finish();
        Intent intent = new Intent(this, C_Module.class);
        startActivity(intent);
    }

}
