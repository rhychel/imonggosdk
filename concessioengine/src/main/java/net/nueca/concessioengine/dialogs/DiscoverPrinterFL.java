package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimplePrinterListAdapter;
import net.nueca.concessioengine.fragments.interfaces.PrinterDiscoveryListener;

import java.util.ArrayList;
import java.util.List;

public class DiscoverPrinterFL extends FrameLayout implements CustomDialogRecyclerAdapter.OnItemClickListener {

    public static String TAG = "DiscoverPrinterLayout";
    private Context mContext;
    private View mView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private PrinterDiscoveryListener mPrinterDiscoveryListener;
    private List<String> mListOfNames = new ArrayList<>();
    private SimplePrinterListAdapter mSimplePrinterListAdapter;

    public DiscoverPrinterFL(Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            mView = inflater.inflate(R.layout.simple_discover_printer_fragment, null, false);
        }

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.rvPrinterList);
        mLinearLayoutManager = new org.solovyev.android.views.llm.LinearLayoutManager(context);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setHasFixedSize(false);

        mListOfNames.add("Test Data");
        mSimplePrinterListAdapter = new SimplePrinterListAdapter(mListOfNames);
        mSimplePrinterListAdapter.setOnItemClickListener(this);


        mRecyclerView.setAdapter(mSimplePrinterListAdapter);
        addView(mView);
    }

    public void setSimplePrinterListAdapter(SimplePrinterListAdapter mSimplePrinterListAdapter) {
        this.mSimplePrinterListAdapter = mSimplePrinterListAdapter;
    }

    public void setPrinterDiscoveryListener(PrinterDiscoveryListener printerDiscoveryListener) {
        this.mPrinterDiscoveryListener = printerDiscoveryListener;
    }

    @Override
    public void onItemClicked(View view, int position) {
        if (mPrinterDiscoveryListener != null) {
            if (mSimplePrinterListAdapter.getDeviceInfos() != null) {
                mPrinterDiscoveryListener.onPrinterSelected(mSimplePrinterListAdapter.getDeviceInfo(position));
            }
        }
    }


    private DiscoveryListener mDiscoveryListener =  new DiscoveryListener() {
        @Override
        public void onDiscovery(DeviceInfo deviceInfo) {
            if(mPrinterDiscoveryListener != null) {
                mPrinterDiscoveryListener.onPrinterDiscovered(deviceInfo);
            } else {
                Log.e(TAG, "Printer Discover Listener is null");
            }

            Log.e(TAG, "Printer Discovered!");

            if (mSimplePrinterListAdapter != null) {
                mSimplePrinterListAdapter.addDeviceInfo(deviceInfo);
                mSimplePrinterListAdapter.addPrinterEntry(deviceInfo.getDeviceName());
                mSimplePrinterListAdapter.notifyDataSetChanged();
            }
        }
    };

    public void startTheDisco() throws Epos2Exception {
        // TODO FILTER OPTIONS DIALOG!
        FilterOption mFilterOption;

        mFilterOption = new FilterOption();
        mFilterOption.setDeviceModel(Discovery.TYPE_PRINTER);
        mFilterOption.setEpsonFilter(Discovery.FILTER_NAME);
        Log.e(TAG, "Discovering Started");
        Discovery.start(getContext(), mFilterOption, mDiscoveryListener);
    }

}