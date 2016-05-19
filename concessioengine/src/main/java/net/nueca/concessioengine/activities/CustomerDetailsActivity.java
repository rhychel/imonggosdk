package net.nueca.concessioengine.activities;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;

/**
 * Created by rhymart on 12/2/15.
 */
@Deprecated
public class CustomerDetailsActivity extends ImonggoAppCompatActivity {

    private RecyclerView rvCustomerDetails;
    private Toolbar tbActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_customer_details);

        tbActionBar = (Toolbar) findViewById(R.id.tbActionBar);
        rvCustomerDetails = (RecyclerView) findViewById(R.id.rvCustomerDetails);

    }

}
