package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.nueca.concessioengine.R;

import java.util.List;

/**
 * ....Created by Jn on 7/9/2015.
 * imonggosdk (c)2015
 */
public class CustomModuleAdapter extends BaseCustomDialogRecyclerAdapter<CustomModuleAdapter.ViewHolder> {


    public View view;
    private ViewGroup mViewGroupParent;
    private int mItemLayout;

    public CustomModuleAdapter(Context context, int itemLayout, List<String> moduleName) {
        super(context, moduleName);
        this.mItemLayout = itemLayout;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mItemLayout, parent, false);
        this.mViewGroupParent = parent;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolderHelper(VH holder, int position) {
        holder.bind(getModuleAt(position), getProgressAt(position));
        holder.itemView.setTag(getModuleAt(position));
    }


    public class ViewHolder extends BaseCustomDialogRecyclerAdapter.VH {
        private TextView moduleName;
        private TextView progressPercentageDownload;
        private ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);

            moduleName = (TextView) itemView.findViewById(R.id.module_name);

            progressPercentageDownload = (TextView) itemView.findViewById(R.id.progress_download);
            progressPercentageDownload.setText("0%");

            progressBar = (ProgressBar) itemView.findViewById(R.id.module_progress_download);
            progressBar.setMax(100);
            progressBar.setProgress(0);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
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

        @Override
        public void onClick(View v) {
            if(mOnItemClickListener != null) {
                mOnItemClickListener.onItemClicked(v, getLayoutPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(mOnItemClickListener != null) {
                mOnItemLongClickListener.onItemLongClicked(v, getLayoutPosition());
            }

            return true;
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
