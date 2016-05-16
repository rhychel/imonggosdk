package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.tonicartos.superslim.GridSLM;
import com.tonicartos.superslim.LayoutManager;
import com.tonicartos.superslim.LinearSLM;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseMultipleProductRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.objects.MultiItem;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.operations.ImonggoTools;

import java.sql.SQLException;
import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by rhymartmanchus on 12/05/2016.
 */
public class SimpleMultipleProductRecyclerAdapter extends BaseMultipleProductRecyclerAdapter<SimpleMultipleProductRecyclerAdapter.ListViewHolder> {

    public SimpleMultipleProductRecyclerAdapter(Context context) {
        super(context);
    }

    public SimpleMultipleProductRecyclerAdapter(Context context, List<MultiItem> list) {
        super(context, list);
        initializeHeader();
    }

    public SimpleMultipleProductRecyclerAdapter(Context context, ImonggoDBHelper2 dbHelper, List<MultiItem> list) {
        super(context, dbHelper, list);
        initializeHeader();
    }

    private void initializeHeader() {
        marginsFixed = false;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if(viewType == VIEW_TYPE_HEADER) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.simple_multiitem_header, parent, false);
            Log.e("MultiItem", "isCalled as header");
        }
        else {
            v = LayoutInflater.from(getContext()).inflate(R.layout.simple_multiitem_item, parent, false);
            Log.e("MultiItem", "isCalled as item");
        }
        ListViewHolder lvh = new ListViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        View itemView = holder.itemView;
        MultiItem multiItem = getItem(position);

        final GridSLM.LayoutParams lp = GridSLM.LayoutParams.from(itemView.getLayoutParams());
        if(multiItem.isHeader()) {
            holder.tvSubtotal.setVisibility(View.GONE);
            holder.tvProductName.setText(multiItem.getProduct().getName());
            SelectedProductItem selectedProductItem = getSelectedProductItems().getSelectedProductItem(multiItem.getProduct());
            Unit unit = null;
            if(selectedProductItem != null && selectedProductItem.getValues() != null && selectedProductItem.getValues().size() > 0) {
                Values values = selectedProductItem.getValues().get(0);
                unit = values.getUnit();
            }
            if(unit == null) {
                try {
                    unit = getHelper().fetchObjects(Unit.class).queryBuilder().where().eq("name", multiItem.getProduct().getUnit()).and()
                            .eq("product_id", multiItem.getProduct()).queryForFirst();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if(selectedProductItem == null && multiItem.getProduct().getExtras() != null && multiItem.getProduct().getExtras().getDefault_selling_unit() != null && multiItem.getProduct().getExtras()
                    .getDefault_selling_unit().length() > 0 && unit == null) {
                try {
                    Unit t_unit = getHelper().fetchObjects(Unit.class).queryBuilder().where().eq("id", Integer.parseInt(multiItem.getProduct().getExtras()
                            .getDefault_selling_unit())).queryForFirst();
                    if(t_unit != null)
                        unit = t_unit;
                } catch (SQLException e) { e.printStackTrace(); }
            }
            holder.tvInStock.setVisibility(View.GONE);

            // Quantity
            if(getSelectedProductItems().hasSelectedProductItem(multiItem.getProduct())) {
                holder.llQuantity.setVisibility(View.VISIBLE);
                holder.tvQuantity.setText(String.format("%1$s %2$s", getSelectedProductItems().getQuantity(multiItem.getProduct()), getSelectedProductItems()
                        .getUnitName(multiItem.getProduct(), false)));
            }

            holder.tvRetailPrice.setText(String.format("%s", selectedProductItem.getValuesRetailPrices()));

            if(multiItem.getProduct().getThumbnail_url() != null && !multiItem.getProduct().getThumbnail_url().trim().equals("")) {
                String imageUrl = ImonggoTools.buildProductImageUrl(getContext(), ProductsAdapterHelper.getSession().getApiToken(),
                        ProductsAdapterHelper.getSession().getAcctUrlWithoutProtocol(), multiItem.getProduct().getId() + "", false, false);
                holder.ivProductImage.setImageUrl(imageUrl, ProductsAdapterHelper.getImageLoaderInstance(getContext(), true));
            }

            if (lp.isHeaderInline() || (marginsFixed && !lp.isHeaderOverlay())) {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            } else {
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            }

            lp.headerEndMarginIsAuto = !marginsFixed;
            lp.headerStartMarginIsAuto = !marginsFixed;
        }
        else {
            // TODO Support more
            holder.tvQuantity.setText(getItem(position).getValues().getQuantity()+" "+getItem(position).getValues().getUnit_name());
        }

        lp.setSlm(LinearSLM.ID);
        lp.setColumnWidth(getContext().getResources().getDimensionPixelSize(R.dimen.grid_column_width));
        lp.setFirstPosition(multiItem.getSectionFirstPosition());
        holder.itemView.setLayoutParams(lp);
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
        Log.e("initializeRecyclerView", "SimpleMultipleProductRecyclerAdapter");
        layoutManager = new LayoutManager(context){
            @Override
            public Parcelable onSaveInstanceState() { return null; }

            @Override
            public void onRestoreInstanceState(Parcelable state) { }
        };
        rvProducts.setLayoutManager(layoutManager);
        Log.e("initializeRecyclerView", "SimpleMultipleProductRecyclerAdapter--has been set");
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {

        public View itemView;
        // HEADER
        public NetworkImageView ivProductImage;
        public AutofitTextView tvInStock, tvProductName;
        public TextView tvSubtotal, tvRetailPrice;
        public LinearLayout llQuantity;

        // ITEMS
        public AutofitTextView tvQuantity;
        public TextView tvBrand, tvDeliveryDate, tvBatchNo;

        public ListViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;

            tvQuantity = (AutofitTextView) itemView.findViewById(R.id.tvQuantity);
            // HEADER
            if(itemView.findViewById(R.id.tvBrand) == null) {
                ivProductImage = (NetworkImageView) itemView.findViewById(R.id.ivProductImage);
                tvInStock = (AutofitTextView) itemView.findViewById(R.id.tvInStock);
//            tvRetailPrice = (TextView) itemView.findViewById(R.id.tvRetailPrice);
//            ivProductImage = (NetworkImageView) itemView.findViewById(R.id.ivProductImage);
                tvProductName = (AutofitTextView) itemView.findViewById(R.id.tvProductName);
                tvSubtotal = (TextView) itemView.findViewById(R.id.tvSubtotal);
                llQuantity = (LinearLayout) itemView.findViewById(R.id.llQuantity);
                tvRetailPrice = (TextView) itemView.findViewById(R.id.tvRetailPrice);
            }
            else {
                tvBrand = (TextView) itemView.findViewById(R.id.tvBrand);
                tvDeliveryDate = (TextView) itemView.findViewById(R.id.tvDeliveryDate);
                tvBatchNo = (TextView) itemView.findViewById(R.id.tvBatchNo);

                tvDeliveryDate.setVisibility(View.GONE);
                tvBatchNo.setVisibility(View.GONE);
                tvBatchNo.setVisibility(View.GONE);
            }
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
