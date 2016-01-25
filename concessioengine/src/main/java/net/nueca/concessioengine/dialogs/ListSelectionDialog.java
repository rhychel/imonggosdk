package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;

import java.util.List;

/**
 * Created by gama on 9/29/15.
 */
public class ListSelectionDialog extends BaseAppCompatDialog {
    private int layoutRes, listitemRes, recyclerviewId;
    private RecyclerView rvContent;

    private Button btnConfirm, btnCancel;
    private ListSelectionDialogListener listener;
    private ListSelectionDialogAdapter adapter;

    private List<String> objects;

    public ListSelectionDialog(Context context, List<String> objects) {
        super(context);
        this.layoutRes = R.layout.simple_listselection_dialog;
        this.listitemRes = R.layout.simple_listselection_radioitem;
        this.recyclerviewId = R.id.rvContent;
        this.objects = objects;
    }

    /*public ListSelectionDialog(Context mContext, @LayoutRes int layoutRes, @LayoutRes int listitemRes,
                               @IdRes int recyclerviewId) {
        super(mContext);
        this.layoutRes = layoutRes;
        this.listitemRes = listitemRes;
        this.recyclerviewId = recyclerviewId;
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutRes);

        rvContent = (RecyclerView) findViewById(recyclerviewId);
        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        adapter = new ListSelectionDialogAdapter(listitemRes);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                if (listener != null)
                    listener.onItemClick();
            }
        });
        adapter.initializeRecyclerView(getContext(), rvContent);
        rvContent.setAdapter(adapter);
        addElements(objects);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null)
                    listener.onConfirm();
                dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null)
                    listener.onCancel();
                cancel();
            }
        });
    }

    public void setListener(ListSelectionDialogListener listener) {
        this.listener = listener;
    }

    public void addElements(List<String> items) {
        adapter.addAll(items);
        adapter.notifyDataSetChanged();
    }

    public void clearElements() {
        adapter.removeAll();
        adapter.notifyDataSetChanged();
    }

    public interface ListSelectionDialogListener {
        void onItemClick();
        void onConfirm();
        void onCancel();
    }

    public class ListSelectionDialogAdapter extends BaseRecyclerAdapter<ListSelectionDialogAdapter.ListViewHolder,
            String> {
        private int itemRes;
        private int selected = -1;
        public ListSelectionDialogAdapter(int itemRes) {
            this.itemRes = itemRes;
        }

        @Override
        public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(itemRes, parent, false);
            return new ListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ListViewHolder holder, int position) {
            holder.radioButton.setText(getItem(position));
            holder.radioButton.setChecked(position == selected);
        }

        @Override
        public int getItemCount() {
            return objects.size();
        }

        public class ListViewHolder extends net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter.ViewHolder {
            public RadioButton radioButton;

            public ListViewHolder(View itemView) {
                super(itemView);

                radioButton = (RadioButton) itemView.findViewById(R.id.rbItem);

                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if(onItemClickListener != null)
                    onItemClickListener.onItemClicked(v, getLayoutPosition());
                selected = getLayoutPosition();
                notifyItemChanged(getLayoutPosition());
            }

            @Override
            public boolean onLongClick(View v) {
                if(onItemLongClickListener != null)
                    onItemLongClickListener.onItemLongClicked(v, getLayoutPosition());
                return true;
            }
        }
    }
}
