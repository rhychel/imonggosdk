package net.nueca.concessio;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.enums.DialogType;
import net.nueca.concessioengine.fragments.MultiInputSelectedItemFragment;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.imonggosdk.enums.ConcessioModule;

/**
 * Created by rhymart on 8/24/15.
 * imonggosdk2 (c)2015
 */
public class C_MultiInput extends ModuleActivity implements SetupActionBar {

    private MultiInputSelectedItemFragment multiInputSelectedItemFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_framelayout);

        multiInputSelectedItemFragment = new MultiInputSelectedItemFragment();
        multiInputSelectedItemFragment.setHelper(getHelper());
        multiInputSelectedItemFragment.setSetupActionBar(this);
        multiInputSelectedItemFragment.setProductId(getIntent().getIntExtra(MultiInputSelectedItemFragment.PRODUCT_ID, 0));

        boolean isManualReceive = getIntent().getBooleanExtra(MultiInputSelectedItemFragment.IS_MANUAL_RECEIVE, false);
        multiInputSelectedItemFragment.setConcessioModule(concessioModule);
        multiInputSelectedItemFragment.setManualReceive(isManualReceive);
        multiInputSelectedItemFragment.setHasBrand(!isManualReceive);
        multiInputSelectedItemFragment.setHasDeliveryDate(!isManualReceive);
        multiInputSelectedItemFragment.setHasBatchNo(!isManualReceive);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.flContent, multiInputSelectedItemFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        multiInputSelectedItemFragment.updateSelectedProductItem();
        setResult(SUCCESS);
        super.onBackPressed();
    }

    @Override
    public void setupActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}
