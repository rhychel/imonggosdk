package net.nueca.concessioengine.printer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimplePrinterListAdapter;

/**
 * Created by Jn on 19/01/16.
 */
public abstract class BasePrinterActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mPrinterListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;



    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initComponents();

    }

    private void initComponents() {
        setContentView(R.layout.simple_printer_activity);
        mRecyclerView = (RecyclerView) findViewById(R.id.rvPrinterList);
        mRecyclerView.setHasFixedSize(true);

        mPrinterListAdapter = new SimplePrinterListAdapter();


        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mPrinterListAdapter);



    }


}
