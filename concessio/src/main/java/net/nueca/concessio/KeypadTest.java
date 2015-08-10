package net.nueca.concessio;

import android.os.Bundle;
import android.widget.EditText;

import net.nueca.imonggosdk.activities.ImonggoActivity;
import net.nueca.imonggosdk.widgets.Numpad;


public class KeypadTest extends ImonggoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogtest);
        Numpad numpad = (Numpad) findViewById(R.id.npInput);
        numpad.addTextHolder((EditText)findViewById(R.id.editText), "editText",false,false,null);
        numpad.getTextHolderWithTag("editText").setEnableDot(false);
    }

}
