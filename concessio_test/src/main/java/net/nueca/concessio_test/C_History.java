package net.nueca.concessio_test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.fragments.BaseTransactionsFragment;
import net.nueca.concessioengine.fragments.SimpleTransactionDetailsFragment;
import net.nueca.concessioengine.fragments.SimpleTransactionsFragment;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.swable.ImonggoSwableServiceConnection;
import net.nueca.imonggosdk.swable.SwableTools;

/**
 * Created by rhymart on 8/3/15.
 * imonggosdk2 (c)2015
 */
public class C_History extends ModuleActivity implements SetupActionBar, BaseTransactionsFragment.TransactionsListener {

    private SimpleTransactionsFragment simpleHistoryFragment;
    private ImonggoSwableServiceConnection swableConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_fragment);

        simpleHistoryFragment = new SimpleTransactionsFragment();
        simpleHistoryFragment.setUseRecyclerView(false);
        simpleHistoryFragment.setHelper(getHelper());
        simpleHistoryFragment.setSetupActionBar(this);
        simpleHistoryFragment.setTransactionsListener(this);

        swableConnection = new ImonggoSwableServiceConnection(simpleHistoryFragment);
        SwableTools.startAndBindSwable(this, swableConnection);

        getSupportFragmentManager().beginTransaction().add(R.id.flContent, simpleHistoryFragment).commit();
    }

    @Override
    public void setupActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        getSupportActionBar().setTitle("History");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(swableConnection);
    }

    @Override
    public void showTransactionDetails(OfflineData offlineData) {
        Intent intent = new Intent(this, C_HistoryDetails.class);
        intent.putExtra(SimpleTransactionDetailsFragment.TRANSACTION_ID, offlineData.getReturnId());
        startActivity(intent);
    }
}
