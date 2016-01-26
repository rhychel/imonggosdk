package net.nueca.concessioengine.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.nueca.concessioengine.R;

import java.util.ArrayList;
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
        printerList = new ArrayList<>();
    }

    public void setPrinterList(List<String> printerList) {
        this.printerList = printerList;
    }

    public void addPrinterEntry(String printer) {
        printerList.add(printer);
    }

    @Override
    public PrinterListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simple_printerdata, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new PrinterListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SimplePrinterListAdapter.PrinterListViewHolder holder, int position) {
        holder.mPrinter.setText(printerList.get(position));
    }

    @Override
    public int getItemCount() {
        return printerList.size();
    }

    public static class PrinterListViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mPrinter;

        public PrinterListViewHolder(View v) {
            super(v);
            mPrinter = (TextView) v.findViewById(R.id.printer_name);
        }
    }

}
