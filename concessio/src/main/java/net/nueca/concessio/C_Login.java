package net.nueca.concessio;

import android.content.Intent;
import android.os.Bundle;

import net.nueca.concessioengine.activities.login.LoginActivity;

public class C_Login extends LoginActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void syncingModulesSuccessful() {

    }

    @Override
    protected void showNextActivity() {
        Intent intent = new Intent(getApplicationContext(), C_Login2.class);
        startActivity(intent);
        finish();
    }

}
