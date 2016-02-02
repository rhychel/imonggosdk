package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.epson.epos2.discovery.DeviceInfo;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.enums.Table;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Jn on 7/9/2015.
 * imonggosdk (c)2015
 */
public class CustomModuleAdapter extends CustomDialogRecyclerAdapter<CustomModuleAdapter.ViewHolder> {

    private int mItemLayout;

    public CustomModuleAdapter(List<Table> moduleName, Context context, int itemLayout) {
        super(moduleName, context);
        this.mItemLayout = itemLayout;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mItemLayout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolderHelper(VH holder, int position) {

        if (getCircularProgressBar(position)) {
            holder.hideCircularProgressBar();
        } else {
            holder.showCircularProgressBar();
        }

        if(getRetryButtonStatus(position)){
            holder.showRetryButton();
        }

        holder.bindLoginModule(getModuleAt(position), getProgressAt(position));

        holder.itemView.setTag(getModuleAt(position));
    }


    public class ViewHolder extends CustomDialogRecyclerAdapter.VH {
        private TextView tvModuleName;
        private TextView tvPercentageDownload;
        private ImageView imgRetryBtn;
        private ProgressBar pbHorizontal;
        private ProgressBar pbCircular;

        public ViewHolder(View itemView) {
            super(itemView);

            tvModuleName = (TextView) itemView.findViewById(R.id.tvModuleName);
            pbCircular = (ProgressBar) itemView.findViewById(R.id.pbCircularProgressBar);
            imgRetryBtn = (ImageView) itemView.findViewById(R.id.ivRetrySync);

            tvPercentageDownload = (TextView) itemView.findViewById(R.id.tvDownloadProgress);
            tvPercentageDownload.setText("0%");

            pbHorizontal = (ProgressBar) itemView.findViewById(R.id.pbModuleProgress);
            pbHorizontal.setMax(100);
            pbHorizontal.setProgress(0);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void bindPrinter(DeviceInfo deviceInfo, int position) {
            // do nothing this method is not for you. not this time. sorry.
        }

        @Override
        public void bindPrinterName(String printerName, int position) {
            // do nothing this method is not for you. not this time. sorry.
        }

        @Override
        public void bindLoginModule(String name, int progress) {
            getModuleName().setText(name);
            getPercentageDownload().setText(progress + "%");
            getHorizontalProgressBar().setProgress(progress);

        }

        @Override
        public void hideCircularProgressBar() {
            getPercentageDownload().setVisibility(View.VISIBLE);
            getCircularProgressBar().setVisibility(View.GONE);
            getImgRetryBtn().setVisibility(View.GONE);
        }

        @Override
        public void showCircularProgressBar() {
            getPercentageDownload().setVisibility(View.GONE);
            getCircularProgressBar().setVisibility(View.VISIBLE);
            getImgRetryBtn().setVisibility(View.GONE);
        }

        @Override
        public void showRetryButton() {
            getPercentageDownload().setVisibility(View.GONE);
            getCircularProgressBar().setVisibility(View.GONE);
            getImgRetryBtn().setVisibility(View.VISIBLE);
        }

        @Override
        public void hideRetryButton() {
            getImgRetryBtn().setVisibility(View.GONE);
        }

        public TextView getModuleName() {
            return tvModuleName;
        }

        public TextView getPercentageDownload() {
            return tvPercentageDownload;
        }

        public ImageView getImgRetryBtn() {
            return imgRetryBtn;
        }

        public ProgressBar getHorizontalProgressBar() {
            return pbHorizontal;
        }

        public ProgressBar getCircularProgressBar() {
            return pbCircular;
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
    }

    /**
     * Update the RecyclerView Rows by passing the integer progress
     *
     * @param progress List of Progress
     */
    public void updateProgressBar(int position, int progress) {
            setDownloadProgress(position, progress);
    }

    public void hideCircularProgressBar(int position) {
            updateCircularProgressBar(position, true);
    }

    public void showCircularProgressBar(int position) {
        if(!getCircularProgressBar(position)) {
            updateCircularProgressBar(position, false);
        }
    }

    public void hideRetryButton(int position) {
        updateRetryButton(position, false);
    }

    public void showRetryButton(int position) {
        if(!getRetryButtonStatus(position)) {
            updateRetryButton(position, true);
        }
    }
}