package net.nueca.concessioengine.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Jn on 19/01/16.
 */
public class SimplePrinterListAdapter extends RecyclerView.Adapter<SimplePrinterListAdapter.PrinterListViewHolder> {

    private List<String> printerList;

    public SimplePrinterListAdapter(List<String> printerList) {
        this.printerList = printerList;
    }

    public SimplePrinterListAdapter() {

    }

    @Override
    public PrinterListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(SimplePrinterListAdapter.PrinterListViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return printerList.size();
    }

    public static class PrinterListViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;

        public PrinterListViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

}
