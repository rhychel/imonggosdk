package net.nueca.concessioengine.dialogs;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.nueca.concessioengine.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jn on 7/6/2015.
 * imonggosdk (c)2015
 */
public abstract class BaseRecyclerAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<BaseRecyclerAdapter.VH> {

    private int mItemLayout;
    private List<String> mModuleName;
    private List<Integer> mDownloadProgress;
    private ViewGroup mViewGroupParent;


    public BaseRecyclerAdapter(int itemLayout, List<String> moduleName) {
        this.mItemLayout = itemLayout;
        this.mModuleName = moduleName;
        this.mDownloadProgress = new ArrayList<>();

        for(int i=0; i< mModuleName.size(); i++) {
            this.mDownloadProgress.add(i,0);
        }
    }

    public BaseRecyclerAdapter() {

    }

    public void setDownloadProgress(List<Integer> progress) {
        this.mDownloadProgress = progress;
        notifyDataSetChanged();
    }

    public void setDownloadProgress(int position, int progress) {
        this.mDownloadProgress.set(position, progress);
        notifyItemChanged(position);
    }

    @Override
    public BaseRecyclerAdapter.VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mItemLayout, parent, false);
        this.mViewGroupParent = parent;
        return new BaseRecyclerAdapter.VH(view);
    }

    @Override
    public void onBindViewHolder(BaseRecyclerAdapter.VH holder, int position) {
        holder.bind(getModuleAt(position), getProgressAt(position));
        holder.itemView.setTag(getModuleAt(position));
    }

    @Override
    public int getItemCount() {
        return mModuleName.size();
    }

    public String getModuleAt (int position) {
        if (position < mModuleName.size() && mModuleName != null) {
            return mModuleName.get(position);
        } else {
            return null;
        }
    }

    public Integer getProgressAt (int position) {
        if (position < mDownloadProgress.size() && mDownloadProgress != null) {
            return mDownloadProgress.get(position);
        } else {
            return null;
        }
    }

    // View Holder
    public static class VH extends RecyclerView.ViewHolder {
        private TextView moduleName;
        private TextView progressPercentageDownload;
        private ProgressBar progressBar;

        public VH(View itemView) {
            super(itemView);

            //moduleName = (TextView) itemView.findViewById(R.id.module_name);

            //progressPercentageDownload = (TextView) itemView.findViewById(R.id.progress_download);
            progressPercentageDownload.setText("0%");

            //progressBar = (ProgressBar) itemView.findViewById(R.id.module_progress_download);
            progressBar.setMax(100);
            progressBar.setProgress(0);
        }
        public void bind(String name, int progress) {

            getModuleName().setText(name);
            getProgressPercentageDownload().setText(progress + "%");
            getProgressBar().setProgress(progress);
        }

        public TextView getModuleName() {
            return moduleName;
        }

        public TextView getProgressPercentageDownload() {
            return progressPercentageDownload;
        }

        public ProgressBar getProgressBar() {
            return progressBar;
        }
    }
}