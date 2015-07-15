package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jn on 7/6/2015.
 * imonggosdk (c)2015
 */
public abstract class BaseCustomDialogRecyclerAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<BaseCustomDialogRecyclerAdapter.VH> {

    private Context mContext;
    private List<String> mModuleName;
    private List<Integer> mDownloadProgress;
    protected OnItemClickListener mOnItemClickListener;
    protected OnItemLongClickListener mOnItemLongClickListener;


    public interface OnItemClickListener {
        void onItemClicked(View view, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClicked(View view, int position);
    }


    public abstract void onBindViewHolderHelper(BaseCustomDialogRecyclerAdapter.VH holder, int position);



    public BaseCustomDialogRecyclerAdapter(Context context, List<String> moduleName) {
        this.mContext = context;
        this.mModuleName = moduleName;
        this.mDownloadProgress = new ArrayList<>();

        // TODO: change this
        for(int i=0; i< mModuleName.size(); i++) {
            this.mDownloadProgress.add(i,0);
        }
    }

    public BaseCustomDialogRecyclerAdapter() {

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
    public void onBindViewHolder(BaseCustomDialogRecyclerAdapter.VH holder, int position) {
        onBindViewHolderHelper(holder, position);
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

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener mOnItemLongClickListener) {
        this.mOnItemLongClickListener = mOnItemLongClickListener;
    }

    // View Holder
    public abstract static class VH extends RecyclerView.ViewHolder  implements View.OnClickListener, View.OnLongClickListener {

        public VH(View itemView) {
            super(itemView);
        }

        public abstract void bind(String name, int progress);
    }

}