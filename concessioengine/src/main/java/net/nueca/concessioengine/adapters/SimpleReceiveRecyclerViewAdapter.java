package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseReceiveRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.document.ExtendedAttributes;
import net.nueca.imonggosdk.tools.NumberTools;

import java.util.ArrayList;
import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by gama on 9/9/15.
 */
public class SimpleReceiveRecyclerViewAdapter extends BaseReceiveRecyclerAdapter<SimpleReceiveRecyclerViewAdapter
        .ListViewHolder> {

    public SimpleReceiveRecyclerViewAdapter(Context context, ImonggoDBHelper dbHelper, List<DocumentLine> objects) {
        super(context, R.layout.simple_receive_listitem, dbHelper, objects);
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(getListItemResource(), parent, false);

        ListViewHolder lvh = new ListViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder lvh, int position) {
        Product product = getProductItem(position);

        lvh.tvProductName.setText(product.getName() + (!isManual && product.getUnit() != null?
                " (" + product.getUnit() + ")" : "" ));
        lvh.tvNum.setText("" + getItem(position).getLine_no());

        lvh.tvQuantity.setText(NumberTools.separateInCommas(product.getOrig_quantity()));
        lvh.tvReceive.setText(NumberTools.separateInCommas(product.getRcv_quantity()));
        lvh.tvReturn.setText(NumberTools.separateInCommas(product.getRet_quantity()));
        lvh.tvDiscrepancy.setText(NumberTools.separateInCommas(product.getDsc_quantity()));

        lvh.tvQuantity.setVisibility(isManual? View.GONE : View.VISIBLE);
        lvh.tvDiscrepancy.setVisibility(isManual? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {
        public AutofitTextView tvProductName, tvNum, tvQuantity,
                tvReceive, tvReturn, tvDiscrepancy;

        public ListViewHolder(View itemView) {
            super(itemView);
            tvProductName = (AutofitTextView) itemView.findViewById(R.id.tvProductName);
            tvNum = (AutofitTextView) itemView.findViewById(R.id.tvNum);
            tvQuantity = (AutofitTextView) itemView.findViewById(R.id.tvQuantity);
            tvReceive = (AutofitTextView) itemView.findViewById(R.id.tvReceive);
            tvReturn = (AutofitTextView) itemView.findViewById(R.id.tvReturn);
            tvDiscrepancy = (AutofitTextView) itemView.findViewById(R.id.tvDiscrepancy);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(onItemClickListener != null)
                onItemClickListener.onItemClicked(v, getLayoutPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            if(onItemLongClickListener != null)
                onItemLongClickListener.onItemLongClicked(v, getLayoutPosition());
            return true;
        }
    }

    public boolean updateList(List<DocumentLine> documentLines) {
        updateProductList(documentLines);
        notifyDataSetChanged();
        return getCount() > 0;
    }

    @Override
    public List<DocumentLine> generateDocumentLines() {
        List<DocumentLine> documentLines = new ArrayList<>();
        for(Product product : productList) {
            Double outright_return = NumberTools.toDouble(product.getRet_quantity());
            Double discrepancy = isManual? 0d : NumberTools.toDouble(product.getDsc_quantity());
            Double receive_qty = NumberTools.toDouble(product.getRcv_quantity());

            if(isManual) {
                if(outright_return == 0d && receive_qty == 0d)
                    continue;
            }

            documentLines.add(
                    new DocumentLine.Builder()
                            .line_no(documentLines.size() + 1)
                            .product_id(product.getId())
                            .quantity(receive_qty)
                            .extended_attributes(
                                    new ExtendedAttributes.Builder()
                                            .outright_return(outright_return != 0d ? "" + outright_return : null)
                                            .discrepancy(discrepancy != 0d ? "" + discrepancy : null)
                                            .buildIfNotEmpty()
                            )
                            .price(1)
                            .discount_text("0.0%")
                            .build()
            );
        }
        return documentLines;
    }
}
