package net.nueca.concessio;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.nueca.concessioengine.activities.LoginActivity;
import net.nueca.imonggosdk.activities.ImonggoActivity;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.widgets.Keypad;

import java.sql.SQLException;
import java.util.List;


public class KeypadTest extends ImonggoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        Keypad keypad = (Keypad) findViewById(R.id.cvKeypad);
        keypad.addTextHolder((TextView) findViewById(R.id.textView), "tv", true, 5, true, new Keypad.TextHolderHelper() {
            @Override
            public void hasFocus(View focused, boolean hasFocus) {

            }

            @Override
            public void onConfirmClick(String text) {

            }

            @Nullable
            @Override
            public void initializeDefaultLayoutButtons(List<Keypad.ExtraButton> extraButtons) {
                extraButtons.add(new Keypad.ExtraButton("Test",null));
            }

            @Nullable
            @Override
            public View initializeCustomLayout(LayoutInflater inflater) {
                return null;
            }
        });
        keypad.addTextHolder((TextView) findViewById(R.id.editText), "et", false, 4, false, new Keypad.TextHolderHelper() {
            @Override
            public void hasFocus(View focused, boolean hasFocus) {

            }

            @Override
            public void onConfirmClick(String text) {

            }

            @Nullable
            @Override
            public void initializeDefaultLayoutButtons(List<Keypad.ExtraButton> extraButtons) {

            }

            @Nullable
            @Override
            public View initializeCustomLayout(LayoutInflater inflater) {
                return inflater.inflate(R.layout.imonggosdk_keypad_drawer_test,null);
            }
        });

        try {
            if(AccountTools.isLoggedIn(getHelper()))
                Log.e("Account", "I'm logged in!");
            else
                Log.e("Account", "I'm not logged in!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
