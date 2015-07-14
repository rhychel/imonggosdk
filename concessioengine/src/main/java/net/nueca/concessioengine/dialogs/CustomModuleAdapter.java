package net.nueca.concessioengine.dialogs;

import android.support.v7.widget.RecyclerView;
import android.view.View;


import java.util.List;

/**
 * ....Created by Jn on 7/9/2015.
 * imonggosdk (c)2015
 */
public class CustomModuleAdapter extends BaseRecyclerAdapter<CustomModuleAdapter.ViewHolder> {


    public View view;

    public CustomModuleAdapter(int itemLayout, List<String> moduleName) {
        super(itemLayout, moduleName);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }

    /**
     * Update the RecyclerView Rows by passing the list
     * @param progress List of Progress
     */
    public void updateProgressBar(List<Integer> progress){
        setDownloadProgress(progress);
    }

    public void updateProgressBar(int position, int progress){
        setDownloadProgress(position, progress);
    }

}
