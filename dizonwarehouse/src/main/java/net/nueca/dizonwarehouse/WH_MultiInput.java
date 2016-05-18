package net.nueca.dizonwarehouse;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.fragments.MultiInputSelectedItemFragment;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;

/**
 * Created by rhymart on 8/24/15.
 * imonggosdk2 (c)2015
 */
public class WH_MultiInput extends ModuleActivity implements SetupActionBar {

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
        Log.e("isManualReceive", isManualReceive+"");
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
