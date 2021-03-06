package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tonicartos.superslim.GridSLM;
import com.tonicartos.superslim.LayoutManager;
import com.tonicartos.superslim.LinearSLM;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.tools.appsettings.AppSettings;

import java.util.List;

/**
 * Created by rhymartmanchus on 12/01/2016.
 */
public class SettingsAdapter extends BaseRecyclerAdapter<SettingsAdapter.ListViewHolder, AppSettings> {

    public SettingsAdapter(Context context, List<AppSettings> list) {
        super(context, list);
        headerDisplay = getContext().getResources().getInteger(R.integer.default_header_display);
        marginsFixed = false;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if(viewType == VIEW_TYPE_HEADER)
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_settings_header_letter, parent, false);
        else
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_settings_listitem, parent, false);
        ListViewHolder lvh = new ListViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        View itemView = holder.itemView;
        AppSettings appSettings = getItem(position);

        final GridSLM.LayoutParams lp = GridSLM.LayoutParams.from(itemView.getLayoutParams());
        if(appSettings.isHeader()) {
            holder.tvHeader.setText(appSettings.getConcessioModule().toString());
//            lp.headerDisplay = headerDisplay;
            if (lp.isHeaderInline() || (marginsFixed && !lp.isHeaderOverlay())) {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            } else {
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            }

            lp.headerEndMarginIsAuto = !marginsFixed;
            lp.headerStartMarginIsAuto = !marginsFixed;
        }
        else {
            holder.text1.setText(appSettings.getAppSettingEntry().getLabel());
        }
        lp.setSlm(LinearSLM.ID);
        lp.setColumnWidth(getContext().getResources().getDimensionPixelSize(R.dimen.grid_column_width));
        lp.setFirstPosition(appSettings.getSectionFirstPosition());
        holder.itemView.setLayoutParams(lp);
        Log.e("onBindViewHolder", "---");
    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isHeader() ? VIEW_TYPE_HEADER : VIEW_TYPE_CONTENT;
    }

    @Override
    public void initializeRecyclerView(Context context, RecyclerView rvProducts) {
        layoutManager = new LayoutManager(context);
        rvProducts.setLayoutManager(layoutManager);
//        rvProducts.setHasFixedSize(true);
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {

        public TextView text1;
        public TextView tvHeader;
        public View itemView;

        public ListViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            if(itemView.findViewById(R.id.tvHeader) != null)
                tvHeader = (TextView) itemView.findViewById(R.id.tvHeader);
            else
                text1 = (TextView) itemView.findViewById(android.R.id.text1);
        }

        @Override
        public void onClick(View v) {

        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }
}
