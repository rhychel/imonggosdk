package net.nueca.concessio;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import net.nueca.concessioengine.activities.ModuleActivity;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.enums.ConcessioModule;

/**
 * Created by rhymart on 8/21/15.
 * imonggosdk2 (c)2015
 */
public class C_Dashboard extends ImonggoAppCompatActivity {

    private Button btnOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_dashboard);

        btnOrder = (Button) findViewById(R.id.btnOrder);

        btnOrder.setOnClickListener(onChooseModule);
    }

    private View.OnClickListener onChooseModule = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(C_Dashboard.this, C_Module.class);
            switch(view.getId()) {
                case R.id.btnOrder: {
                    intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.ORDERS.ordinal());
                } break;
            }
            startActivity(intent);
        }
    };
}
