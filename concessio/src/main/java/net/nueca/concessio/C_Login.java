package net.nueca.concessio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.imonggosdk.tools.LoggingTools;

import java.sql.SQLException;

public class C_Login extends LoginActivity {

    String TAG = "C_Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void showNextActivity() {
        finish();
        Intent intent = new Intent(this, C_Module.class);
        startActivity(intent);

        try {
            if (getSession().getUser() != null)
                LoggingTools.showToast(C_Login.this, getSession().getUser().getName());
            Log.e(TAG, getHelper().getUnits().queryForAll().get(0).getProduct().getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
