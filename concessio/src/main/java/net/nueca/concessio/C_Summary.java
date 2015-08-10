package net.nueca.concessio;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import net.nueca.concessioengine.activities.ModuleActivity;
import net.nueca.concessioengine.fragments.SimpleProductsFragment;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;

/**
 * Created by rhymart on 8/6/15.
 * imonggosdk2 (c)2015
 */
public class C_Summary extends ModuleActivity implements SetupActionBar {

    private SimpleProductsFragment simpleProductsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_fragment);

        simpleProductsFragment = new SimpleProductsFragment();
        simpleProductsFragment.setHelper(getHelper());
        simpleProductsFragment.setSetupActionBar(this);
//        simpleProductsFragment
    }

    @Override
    public void setupActionBar(Toolbar toolbar) {

    }
}
