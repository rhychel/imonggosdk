package net.nueca.concessioengine.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.epson.epos2.discovery.DeviceInfo;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.dialogs.CustomDialogRecyclerAdapter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jn on 19/01/16.
 */
public class SimplePrinterListAdapter extends CustomDialogRecyclerAdapter<SimplePrinterListAdapter.PrinterListViewHolder> {

    private String TAG = "SimplePrinterListAdapter";
    private List<String> printerList;
    private List<DeviceInfo> deviceInfos;

    public SimplePrinterListAdapter(List<String> printerList) {
        this.printerList = printerList;
    }

    public void addDiscoveredPrinter(String name) {
        printerList.add(name);
        notifyDataSetChanged();
    }

    public void addDeviceInfo(DeviceInfo deviceInfo) {
        Log.e(TAG, "addDeviceInfo");
        deviceInfos.add(deviceInfo);
        notifyDataSetChanged();
    }

    public List<String> getPrinterList() {
        return printerList;
    }

    public List<DeviceInfo> getDeviceInfos() {
        return deviceInfos;
    }

    public DeviceInfo getDeviceInfo(int position) {
        return deviceInfos.get(position);
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_printerdata, parent, false);

        return new PrinterListViewHolder(v);
    }


    @Override
    public void onBindViewHolderHelper(VH holder, int position) {
        if(deviceInfos != null) {
            holder.bindPrinter(deviceInfos.get(position), position);
        }

        if(printerList != null) {
            holder.bindPrinterName(printerList.get(position), position);
            holder.itemView.setTag(printerList.get(position));
        }

    }

    public SimplePrinterListAdapter() {
        printerList = new ArrayList<>();
    }

    public void setPrinterList(List<String> printerList) {
        this.printerList = printerList;
    }

    public void addPrinterEntry(String printer) {
        Log.e(TAG, "add Printer Name");
        printerList.add(printer);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return printerList.size();
    }

    public class PrinterListViewHolder extends CustomDialogRecyclerAdapter.VH {
        // each data item is just a string in this case
        public TextView mPrinter;

        public PrinterListViewHolder(View v) {
            super(v);
            mPrinter = (TextView) v.findViewById(R.id.printer_name);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void bindPrinter(DeviceInfo deviceInfo, int position) {

        }

        @Override
        public void bindPrinterName(String printerName, int position) {
            mPrinter.setText(printerName);
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                try {
                    mOnItemClickListener.onItemClicked(v, getLayoutPosition());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemLongClickListener.onItemLongClicked(v, getLayoutPosition());
            }
            return true;
        }

        @Override
        public void bindLoginModule(String name, int progress) {

        }

        @Override
        public void hideCircularProgressBar() {

        }

        @Override
        public void showCircularProgressBar() {

        }

        @Override
        public void showRetryButton() {

        }

        @Override
        public void hideRetryButton() {

        }
    }

}
