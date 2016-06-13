package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.tonicartos.superslim.GridSLM;
import com.tonicartos.superslim.LayoutManager;
import com.tonicartos.superslim.LinearSLM;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseOrderReceiveRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.objects.ReceivedMultiItem;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.operations.ImonggoTools;
import net.nueca.imonggosdk.tools.NumberTools;

import me.grantland.widget.AutofitTextView;

/**
 * Created by gama on 01/06/2016.
 */
public class SimpleOrderReceiveRecyclerAdapter extends BaseOrderReceiveRecyclerAdapter
        <SimpleOrderReceiveRecyclerAdapter.ListViewHolder> {

    public SimpleOrderReceiveRecyclerAdapter(Context context, ImonggoDBHelper2 dbHelper) {
        super(context, dbHelper);
        marginsFixed = true;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType == VIEW_TYPE_HEADER)
            view = LayoutInflater.from(getContext()).inflate(R.layout.order_receive_listitem_header, parent, false);
        else
            view = LayoutInflater.from(getContext()).inflate(R.layout.order_receive_listitem_sublist2, parent, false);
        return new ListViewHolder(view, viewType == VIEW_TYPE_HEADER);
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        ReceivedMultiItem item = getItem(position);

        final LayoutManager.LayoutParams params = LayoutManager.LayoutParams.from(holder.itemView.getLayoutParams());

        if(item.isHeader()) {
            holder.tvProductName.setText(item.getProduct().getName());
            holder.tvRetailPrice.setText("P" + NumberTools.separateInCommas(item.getProduct().getRetail_price()));
            holder.tvQuantity.setText(NumberTools.separateInSpaceHideZeroDecimals(item.getReceivedProductItem().getActualQuantity()) +
                item.getProduct().getUnit());
            holder.tvSubtotal.setText("P" + NumberTools.separateInCommas(item.getReceivedProductItem().getSubtotal()));

            if(item.getReceivedProductItem().getActualQuantity() <= 0d) {
                holder.tvQuantity.setVisibility(View.GONE);
                holder.tvSubtotal.setVisibility(View.GONE);
            }
            else {
                holder.tvQuantity.setVisibility(View.VISIBLE);
                holder.tvSubtotal.setVisibility(View.VISIBLE);
            }

            String imageUrl = ImonggoTools.buildProductImageUrl(getContext(), ProductsAdapterHelper.getSession().getApiToken(),
                    ProductsAdapterHelper.getSession().getAcctUrlWithoutProtocol(), item.getProduct().getId() + "", false, false);
            holder.nivProductImage.setImageUrl(imageUrl, ProductsAdapterHelper.getImageLoaderInstance(getContext(), true));

            /*if (lp.isHeaderInline() || (marginsFixed && !lp.isHeaderOverlay())) {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            } else {
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            }

            lp.headerEndMarginIsAuto = !marginsFixed;
            lp.headerStartMarginIsAuto = !marginsFixed;*/
        }
        else {
            if(holder.tvLineNo != null)
                holder.tvLineNo.setVisibility(View.INVISIBLE);
            holder.tvExpectedPrice.setText("P" + NumberTools.separateInCommas(item.getReceivedProductItemLine().getExpected_price()));
            holder.tvExpectedQuantity.setText(NumberTools.separateInCommasHideZeroDecimals(item.getReceivedProductItemLine().getExpected_quantity()) +
                    " " +item.getReceivedProductItemLine().getUnit().getName());

            if(item.getReceivedItemValue() == null) {
                if (item.getReceivedProductItemLine().getMinActualPrice() < item.getReceivedProductItemLine().getMaxActualPrice()) {
                    holder.tvActualPrice.setText(
                            "P" + NumberTools.separateInCommas(item.getReceivedProductItemLine().getMinActualPrice()) + " ~ " +
                                    "P" + NumberTools.separateInCommas(item.getReceivedProductItemLine().getMaxActualPrice())
                    );
                } else
                    holder.tvActualPrice.setText(
                            "P" + NumberTools.separateInCommas(item.getReceivedProductItemLine().getMinActualPrice())
                    );
                holder.tvActualQuantity.setText(NumberTools.separateInCommasHideZeroDecimals(item.getReceivedProductItemLine().getTotal_actual_quantity()) +
                        " " +item.getReceivedProductItemLine().getUnit().getName());

                holder.tvSubtotal.setText("P" + NumberTools.separateInCommas(item.getReceivedProductItemLine().getTotal_subtotal()));

                if(item.getReceivedProductItemLine().size() == 0) {
                    holder.tvActualPrice.setVisibility(View.INVISIBLE);
                    holder.tvActualQuantity.setVisibility(View.INVISIBLE);

                    holder.llActual.setVisibility(View.GONE);
                    holder.tvLabelActual.setVisibility(View.GONE);

                    holder.tvSubtotal.setVisibility(View.INVISIBLE);
                }
                else {
                    holder.tvActualPrice.setVisibility(View.VISIBLE);
                    holder.tvActualQuantity.setVisibility(View.VISIBLE);

                    holder.llActual.setVisibility(View.VISIBLE);
                    holder.tvLabelActual.setVisibility(View.VISIBLE);

                    holder.tvSubtotal.setVisibility(View.VISIBLE);
                }

                holder.llExpected.setVisibility(View.VISIBLE);
                holder.tvLabelExpected.setVisibility(View.VISIBLE);
            }
            else {
                holder.tvActualPrice.setVisibility(View.VISIBLE);
                holder.tvActualQuantity.setVisibility(View.VISIBLE);
                holder.tvSubtotal.setVisibility(View.VISIBLE);

                holder.tvActualPrice.setText("P"+NumberTools.separateInCommas(item.getReceivedItemValue().getPrice()));
                holder.tvActualQuantity.setText(NumberTools.separateInCommasHideZeroDecimals(item.getReceivedItemValue().getQuantity()) +
                        " " +item.getReceivedProductItemLine().getUnit().getName());

                holder.tvSubtotal.setText("P" + NumberTools.separateInCommas(item.getReceivedItemValue().getSubtotal()));

                holder.llExpected.setVisibility(View.GONE);
                holder.tvLabelExpected.setVisibility(View.GONE);
            }
        }
        params.setSlm(LinearSLM.ID);
        params.setFirstPosition(item.getProductItemIndex());
        holder.itemView.setLayoutParams(params);
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {

        public View itemView;

        public NetworkImageView nivProductImage;
        public AutofitTextView tvProductName;
        public TextView tvQuantity, tvRetailPrice;

        public TextView tvLineNo, tvExpectedPrice, tvExpectedQuantity, tvActualPrice, tvActualQuantity;
        public ViewGroup llExpected, llActual;
        public TextView tvLabelActual, tvLabelExpected;

        public AutofitTextView tvSubtotal;

        public ListViewHolder(View itemView, boolean isHeader) {
            super(itemView);

            this.itemView = itemView;

            if(isHeader) {
                nivProductImage = (NetworkImageView) itemView.findViewById(R.id.ivProductImage);
                tvProductName = (AutofitTextView) itemView.findViewById(R.id.tvProductName);
                tvRetailPrice = (TextView) itemView.findViewById(R.id.tvRetailPrice);
                tvQuantity = (TextView) itemView.findViewById(R.id.tvQuantity);
            }
            else {
                tvLineNo = (TextView) itemView.findViewById(R.id.tvLineNo);
                tvExpectedPrice = (TextView) itemView.findViewById(R.id.tvExpectedPrice);
                tvExpectedQuantity = (TextView) itemView.findViewById(R.id.tvExpectedQuantity);
                tvActualPrice = (TextView) itemView.findViewById(R.id.tvActualPrice);
                tvActualQuantity = (TextView) itemView.findViewById(R.id.tvActualQuantity);

                tvLabelActual = (TextView) itemView.findViewById(R.id.tvLabelActual);
                tvLabelExpected = (TextView) itemView.findViewById(R.id.tvLabelExpected);
                llActual = (ViewGroup) itemView.findViewById(R.id.llActual);
                llExpected = (ViewGroup) itemView.findViewById(R.id.llExpected);

                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            tvSubtotal = (AutofitTextView) itemView.findViewById(R.id.tvSubtotal);
        }

        @Override
        public void onClick(View view) {
            if(onItemClickListener != null)
                onItemClickListener.onItemClicked(view, getLayoutPosition());
        }


        @Override
        public boolean onLongClick(View view) {
            if(onItemLongClickListener != null)
                onItemLongClickListener.onItemLongClicked(view, getLayoutPosition());
            return true;
        }
    }
}
