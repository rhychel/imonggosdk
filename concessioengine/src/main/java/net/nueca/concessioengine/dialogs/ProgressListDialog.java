package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseAdapter;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.imonggosdk.enums.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymartmanchus on 25/01/2016.
 */

public class ProgressListDialog extends BaseAppCompatDialog {

    public interface ProgressListener {
        void retryDownload();
    }

    private ProgressListener progressListener;
    private ListView lvModules;
    private Button btnCancel;
    private ProgressAdapter progressAdapter;
    private List<Table> tablesToUpdate;
    private int currentDownloading = -1;

    protected ProgressListDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public ProgressListDialog(Context context, int theme) {
        super(context, theme);
    }

    public ProgressListDialog(Context context) {
        super(context);
    }

    public ProgressListDialog(Context context, List<Table> tablesToUpdate) {
        super(context, R.style.AppCompatDialogStyle_Light);
        this.tablesToUpdate = tablesToUpdate;
        setTitle("Updating...");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.progress_list_dialog);

        lvModules = (ListView) super.findViewById(R.id.lvModules);
        btnCancel = (Button) super.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        progressAdapter = new ProgressAdapter(getContext(), generateProgressList());
        lvModules.setAdapter(progressAdapter);
    }

    public List<Progress> generateProgressList() {
        List<Progress> progressList = new ArrayList<>();
        for(Table table : tablesToUpdate)
            progressList.add(new Progress(table));
        return progressList;
    }

    public void setTablesToUpdate(List<Table> tablesToUpdate) {
        this.tablesToUpdate = tablesToUpdate;
    }

    public void initDownload(Table table) {
        currentDownloading = tablesToUpdate.indexOf(table);
        progressAdapter.getItem(currentDownloading).setInProgress(true);
        progressAdapter.notifyItemChanged(lvModules, currentDownloading);
        btnCancel.setEnabled(false);
    }

    public void updateProgress(int progress, int max) {
        btnCancel.setEnabled(false);

        Log.e("ProgressListDialog", progressAdapter.getItem(currentDownloading).getTable() + " progressing");

        progressAdapter.getItem(currentDownloading).setMax(max);
        progressAdapter.getItem(currentDownloading).updateProgress(progress);
        progressAdapter.notifyItemChanged(lvModules, currentDownloading);
    }

    public void finishedDownload() {
        progressAdapter.getItem(currentDownloading).setDone(true);
        progressAdapter.getItem(currentDownloading).setInProgress(false);
        progressAdapter.notifyItemChanged(lvModules, currentDownloading);
    }

    public void errorDownload() {
        btnCancel.setEnabled(true);
        progressAdapter.getItem(currentDownloading).setError(true);
        progressAdapter.notifyItemChanged(lvModules, currentDownloading);
    }

    public ProgressListener getProgressListener() {
        return progressListener;
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public class Progress {
        private int progress = 0, max = 100;
        private boolean isDone = false, isInProgress = false, isError = false;
        private Table table;

        public Progress(Table table) {
            this.table = table;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public boolean isDone() {
            return isDone;
        }

        public void setDone(boolean done) {
            isDone = done;
        }

        public Table getTable() {
            return table;
        }

        public void setTable(Table table) {
            this.table = table;
        }

        public boolean isInProgress() {
            return isInProgress;
        }

        public void setInProgress(boolean inProgress) {
            isInProgress = inProgress;
        }

        public boolean isError() {
            return isError;
        }

        public void setError(boolean error) {
            isError = error;
        }

        public void updateProgress(int page) {
            progress = (int) Math.ceil((((double) page / (double) max) * 100.0));
        }
    }

    public class ProgressAdapter extends BaseAdapter<Progress> {


        public ProgressAdapter(Context context, List<Progress> objects) {
            super(context, R.layout.item_module, objects);
        }

        public class ListViewHolder {
            TextView tvModuleName, tvDownloadProgress;
            ProgressBar pbModuleProgress, pbCircularProgressBar;
            ImageView ivRetrySync;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListViewHolder lvh = null;
            if(convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_module, null);

                lvh = new ListViewHolder();
                lvh.tvModuleName = (TextView) convertView.findViewById(R.id.tvModuleName);
                lvh.tvDownloadProgress = (TextView) convertView.findViewById(R.id.tvDownloadProgress);
                lvh.pbModuleProgress = (ProgressBar) convertView.findViewById(R.id.pbModuleProgress);
                lvh.pbCircularProgressBar = (ProgressBar) convertView.findViewById(R.id.pbCircularProgressBar);
                lvh.ivRetrySync = (ImageView) convertView.findViewById(R.id.ivRetrySync);
                lvh.ivRetrySync.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(progressListener != null) {
                            getItem(currentDownloading).setError(false);
                            notifyItemChanged(lvModules, currentDownloading);
                            btnCancel.setEnabled(false);
                            progressListener.retryDownload();
                        }
                    }
                });

                convertView.setTag(lvh);
            }
            else
                lvh = (ListViewHolder) convertView.getTag();

            Progress progress = getItem(position);

            lvh.tvModuleName.setText(progress.getTable().getStringName());
            lvh.pbCircularProgressBar.setVisibility(View.VISIBLE);
            lvh.tvDownloadProgress.setVisibility(View.INVISIBLE);
            lvh.ivRetrySync.setVisibility(View.INVISIBLE);
            if(progress.isError()) {
                lvh.ivRetrySync.setVisibility(View.VISIBLE);
                lvh.pbCircularProgressBar.setVisibility(View.INVISIBLE);
            }
            else if(progress.isInProgress()) {
                lvh.pbModuleProgress.setProgress(progress.getProgress());
                lvh.pbModuleProgress.setMax(100);
                lvh.tvDownloadProgress.setText(progress.getProgress()+"%");
                lvh.pbCircularProgressBar.setVisibility(View.INVISIBLE);
                lvh.tvDownloadProgress.setVisibility(View.VISIBLE);
            }
            else if(progress.isDone()) {
                lvh.pbCircularProgressBar.setVisibility(View.INVISIBLE);
                lvh.tvDownloadProgress.setVisibility(View.VISIBLE);
                lvh.tvDownloadProgress.setText("100%");
            }

            return convertView;
        }
    }

}
