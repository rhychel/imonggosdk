package net.nueca.concessio;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import net.nueca.concessioengine.activities.ModuleActivity;
import net.nueca.concessioengine.fragments.MultiInputSelectedItemFragment;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;

/**
 * Created by rhymart on 8/11/15.
 * imonggosdk (c)2015
 */
public class TestMultiInput extends ModuleActivity implements SetupActionBar {

    private MultiInputSelectedItemFragment multiInputSelectedItemFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_fragment);

        multiInputSelectedItemFragment = new MultiInputSelectedItemFragment();
        multiInputSelectedItemFragment.setHelper(getHelper());
        multiInputSelectedItemFragment.setSetupActionBar(this);
        multiInputSelectedItemFragment.setProductId(getIntent().getIntExtra(MultiInputSelectedItemFragment.PRODUCT_ID, 0));

        getSupportFragmentManager().beginTransaction().add(R.id.flContent, multiInputSelectedItemFragment).commit();
    }

    @Override
    public void onBackPressed() {
        multiInputSelectedItemFragment.updateSelectedProductItem();
        super.onBackPressed();
    }

    @Override
    public void setupActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }
}
