package net.nueca.concessio;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.LoggingTools;

import java.sql.SQLException;

/**
 * Created by Jn on 7/10/2015.
 * imonggosdk (c)2015
 */
public class C_Login2 extends ImonggoAppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(net.nueca.concessioengine.R.layout.concessioengine_select_branches);

        // TODO: remove this
        Button test_unlink_button = (Button) findViewById(net.nueca.concessioengine.R.id.btnTestUnlink);
        Button test_fetch_button = (Button) findViewById(net.nueca.concessioengine.R.id.btnTestFunction);

        test_unlink_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoggingTools.showToast(getApplicationContext(), "Unlink Account..");

                try {
                    AccountTools.unlinkAccount(C_Login2.this, getHelper(), null);
                    finish();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        test_fetch_button.setVisibility(View.GONE);
    }
}
