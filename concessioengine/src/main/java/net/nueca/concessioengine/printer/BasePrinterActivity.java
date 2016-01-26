package net.nueca.concessioengine.printer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimplePrinterListAdapter;

/**
 * Created by Jn on 19/01/16.
 */
public abstract class BasePrinterActivity extends AppCompatActivity {

    private static String TAG = "BasePrinterActivity";
    private RecyclerView mRecyclerView;
    private SimplePrinterListAdapter mPrinterListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private FilterOption mFilterOption;
    private Button mDiscoverPrinter;

    private DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscovery(final DeviceInfo deviceInfo) {
            Log.e(TAG,"Epson Discovered: " + deviceInfo.getDeviceName());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "PrinterName: " +  deviceInfo.getDeviceName());
                    Log.e(TAG, "Target" + deviceInfo.getTarget());
                    mPrinterListAdapter.addPrinterEntry(deviceInfo.getDeviceName());
                    mPrinterListAdapter.notifyDataSetChanged();
                }
            });
        }
    };


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initComponents();
    }

    private void initComponents() {
        setContentView(R.layout.simple_printer_activity);

        mDiscoverPrinter = (Button) findViewById(R.id.btnDiscoverPrinter);
        mDiscoverPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiscoverPrinter();
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.rvPrinterList);
        mRecyclerView.setHasFixedSize(true);

        mPrinterListAdapter = new SimplePrinterListAdapter();

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mPrinterListAdapter);

        mFilterOption = new FilterOption();
        mFilterOption.setDeviceModel(Discovery.TYPE_PRINTER);
        mFilterOption.setEpsonFilter(Discovery.FILTER_NAME);


    }

    public void DiscoverPrinter() {
        Log.e(TAG, "Discovering Printer");
        try {
            Discovery.start(this, mFilterOption, mDiscoveryListener);

        } catch (Epos2Exception e) {
            e.printStackTrace();
        }
    }
}
