package net.nueca.concessio;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Spinner;
import android.widget.TextView;

import net.nueca.concessioengine.activities.module.HistoryActivity;

/**
 * Created by rhymart on 12/28/15.
 */
public class C_History extends HistoryActivity {

    private Toolbar tbActionBar;
    private RecyclerView rvHistory;
    private TextView tvNoTransaction;
    private Spinner spTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_layout_history);

        tbActionBar = (Toolbar) findViewById(net.nueca.concessioengine.R.id.tbActionBar);
        rvHistory = (RecyclerView) findViewById(net.nueca.concessioengine.R.id.rvHistory);
        tvNoTransaction = (TextView) findViewById(net.nueca.concessioengine.R.id.tvNoTransactions);
        spTypes = (Spinner) findViewById(net.nueca.concessioengine.R.id.spTypes);

        setSupportActionBar(tbActionBar);

    }
}
