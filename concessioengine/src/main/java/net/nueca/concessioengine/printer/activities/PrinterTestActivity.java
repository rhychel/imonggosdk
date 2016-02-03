package net.nueca.concessioengine.printer.activities;

import android.os.Bundle;
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
import net.nueca.concessioengine.fragments.interfaces.PrinterDiscoveryListener;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.objects.Session;

/**
 * Created by Jn on 25/01/16.
 * imonggosdk (c)2015
 */
public class PrinterTestActivity extends ImonggoAppCompatActivity implements PrinterDiscoveryListener {

    private static String TAG = "PrinterTestActivity";
    private SimplePrinterListAdapter mPrinterListAdapter;
    private FilterOption mFilterOption;
    private Button mDiscoverPrinter;

    private DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscovery(final DeviceInfo deviceInfo) {
            Log.e(TAG,"EpsonPrinter Discovered: " + deviceInfo.getDeviceName());
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

        Session session = Session.fetchAll(getHelper(), Session.class).get(0);

        if(session != null) {
            Log.e(TAG, ">Session: " + session.isHas_logged_in());
        } else {
            Log.e(TAG, "Session is null");
        }
    }

    private void initComponents() {
        setContentView(R.layout.simple_printer_activity);


        mFilterOption = new FilterOption();
        mFilterOption.setDeviceModel(Discovery.TYPE_PRINTER);
        mFilterOption.setEpsonFilter(Discovery.FILTER_NAME);

        mDiscoverPrinter = (Button) findViewById(R.id.btnDiscoverPrinter);
        mDiscoverPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDiscoverPrinterFragment();
            }
        });

    }

    private void openDiscoverPrinterFragment() {
/*      DiscoverPrinterFL dPrinter = new DiscoverPrinterFL(this);
        dPrinter.setPrinterDiscoveryListener(this);

        CustomDialog customDialog = new CustomDialog(this, R.style.DiscoverPrinter_DialogFrameLayout);
        customDialog.setTitle("Searching for Printer");
        customDialog.setContentView(dPrinter);
        customDialog.setCancelable(true);
        try {
            dPrinter.startTheDisco();
        } catch (Epos2Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            Log.e(TAG, e.getErrorStatus() + "");
        }
        customDialog.show();*/
/*
        TestPrinter printer = TestPrinter.getInstance(this);
        printer.setManufacturer(PrinterManufacturer.EPSON);

        // listener
        printer.execute(this);*/

    }

    public void DiscoverPrinter() {
        Log.e(TAG, "Discovering Printer");
        try {
            Discovery.start(this, mFilterOption, mDiscoveryListener);

        } catch (Epos2Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrinterSelected(DeviceInfo deviceInfo) {

    }

    @Override
    public void onPrinterDiscovered(DeviceInfo deviceInfo) {
        Log.e(TAG, "Printer Discovered! " + deviceInfo.getDeviceName());
    }
}