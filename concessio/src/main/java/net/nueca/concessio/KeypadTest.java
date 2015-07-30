package net.nueca.concessio;

import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.nueca.imonggosdk.activities.ImonggoActivity;
import net.nueca.imonggosdk.dialogs.KeypadDialog;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.widgets.Keypad;
import net.nueca.imonggosdk.widgets.Numpad;

import java.sql.SQLException;
import java.util.List;


public class KeypadTest extends ImonggoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.dialogtest);

        Numpad numpad = (Numpad) findViewById(R.id.npInput);
        numpad.addTextHolder((TextView) findViewById(R.id.etQuantity), "tv", true, 6, 2, true, new Numpad
                .TextHolderHelper
                () {
            @Override
            public void hasFocus(View focused, boolean hasFocus) {

            }

            @Override
            public void onConfirmClick(String text) {
                Toast.makeText(KeypadTest.this,text,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMoreButtonClicked() {
                KeypadDialog keypadDialog = new KeypadDialog(KeypadTest.this,false,null,true);
                keypadDialog.show();
                //AppCompatDialog dialog = new AppCompatDialog(KeypadTest.this);
                //dialog.show();
            }
        });
        numpad.addTextHolder((TextView) findViewById(R.id.editText), "et", false, 4, 2, true, new Numpad
                .TextHolderHelper
                () {
            @Override
            public void hasFocus(View focused, boolean hasFocus) {

            }

            @Override
            public void onConfirmClick(String text) {
                Toast.makeText(KeypadTest.this,text,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMoreButtonClicked() {
            }
        });

        /*Keypad keypad = (Keypad) findViewById(R.id.cvKeypad);
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
        });*/

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
