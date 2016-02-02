package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.objects.UpdateTable;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.tools.DialogTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymartmanchus on 22/01/2016.
 */
public class UpdaterChooserDialog extends BaseAppCompatDialog {

    public interface OnTablesSelected {
        void startUpdate(int[] tables, List<Table> tableList);
    }

    private RecyclerView rvModules;
    private Button btnUpdate, btnCancel;
    private List<Table> tableToUpdate = new ArrayList<>();
    private List<Table> tableList = new ArrayList<>();
    private OnTablesSelected onTablesSelected;
    private ChooserAdapter chooserAdapter;

    public UpdaterChooserDialog(Context context) {
        super(context);
    }

    public UpdaterChooserDialog(Context context, int theme) {
        super(context, theme);
    }

    protected UpdaterChooserDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.updater_chooser_dialog);

        rvModules = (RecyclerView) super.findViewById(R.id.rvModules);
        btnUpdate = (Button) super.findViewById(R.id.btnUpdate);
        btnCancel = (Button) super.findViewById(R.id.btnCancel);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int []modulesToDownload = modulesToDownload();
                if(modulesToDownload.length == 0)
                    Toast.makeText(getContext(), "Please select at least one to update.", Toast.LENGTH_LONG).show();
                else if(onTablesSelected != null) {
                    DialogTools.showConfirmationDialog(getContext(), "Update App", "Are you sure?", "Yes", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onTablesSelected.startUpdate(modulesToDownload, tableList);
                            dismiss();
                        }
                    }, "No", R.style.AppCompatDialogStyle_Light);
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        chooserAdapter = new ChooserAdapter(getContext(), getTablesToUpdate());
        chooserAdapter.initializeRecyclerView(getContext(), rvModules, false);
        rvModules.setAdapter(chooserAdapter);
    }

    private List<UpdateTable> getTablesToUpdate() {
        List<UpdateTable> tableUpdates = new ArrayList<>();
        for(Table table : tableToUpdate)
            tableUpdates.add(new UpdateTable(table));
        return tableUpdates;
    }

    public void setTableToUpdate(Table... tablesToUpdate) {
        this.tableToUpdate = new ArrayList<>();
        for(Table table : tablesToUpdate)
            this.tableToUpdate.add(table);
    }

    public void setTableToUpdate(List<Table> tableToUpdate) {
        this.tableToUpdate = tableToUpdate;
    }

    public void setOnTablesSelected(OnTablesSelected onTablesSelected) {
        this.onTablesSelected = onTablesSelected;
    }

    public class ChooserAdapter extends BaseRecyclerAdapter<ChooserAdapter.ListViewHolder, UpdateTable> {

        public ChooserAdapter(Context context, List<UpdateTable> list) {
            super(context, list);
        }

        public ChooserAdapter(Context context) {
            super(context);
        }

        public ChooserAdapter() {
        }

        @Override
        public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.updater_chooser_item, parent, false);

            ListViewHolder lvh = new ListViewHolder(view);
            return lvh;
        }

        @Override
        public void onBindViewHolder(ListViewHolder holder, int position) {
            holder.tvModule.setText(getItem(position).getTable().getStringName());
            holder.cbSelected.setTag(position);
            holder.cbSelected.setChecked(getItem(position).isSelected());
        }

        @Override
        public int getItemCount() {
            return getCount();
        }

        public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {

            TextView tvModule;
            CheckBox cbSelected;

            public ListViewHolder(View itemView) {
                super(itemView);
                this.tvModule = (TextView) itemView.findViewById(R.id.tvModule);
                this.cbSelected = (CheckBox) itemView.findViewById(R.id.cbSelected);
                this.cbSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        getItem((int)buttonView.getTag()).setSelected(isChecked);
                    }
                });
            }

            @Override
            public void onClick(View v) { }

            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        }

    }

    public int[] modulesToDownload() {
        tableList = new ArrayList<>();
        for(UpdateTable updateTable : chooserAdapter.getList()) {
            if(updateTable.isSelected()) {
                tableList.add(updateTable.getTable());
                switch (updateTable.getTable()) {
                    case ROUTE_PLANS:
                        tableList.add(Table.ROUTE_PLANS_DETAILS);
                        break;
                    case PRICE_LISTS_FROM_CUSTOMERS:
                        tableList.add(Table.PRICE_LISTS_DETAILS);
                        break;
                    case SALES_PROMOTIONS_POINTS:
                        tableList.add(Table.SALES_PROMOTIONS_POINTS_DETAILS);
                        break;
                    case SALES_PROMOTIONS_SALES_DISCOUNT:
                        tableList.add(Table.SALES_PROMOTIONS_SALES_DISCOUNT_DETAILS);
                        break;
                }
            }
        }
        int[] toDownload = new int[tableList.size()];
        int i = 0;
        for(Table table : tableList)
            toDownload[i++] = table.ordinal();
        return toDownload;
    }
}
