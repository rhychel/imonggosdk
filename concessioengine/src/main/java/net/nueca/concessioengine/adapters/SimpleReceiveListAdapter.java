package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseReceiveAdapter;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.document.ExtendedAttributes;
import net.nueca.imonggosdk.tools.NumberTools;

import java.util.ArrayList;
import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by gama on 9/3/15.
 */
public class SimpleReceiveListAdapter extends BaseReceiveAdapter {

    public SimpleReceiveListAdapter(Context context, ImonggoDBHelper dbHelper) {
        super(context, R.layout.simple_receive_listitem, dbHelper);
    }

    private static class ListViewHolder {
        AutofitTextView tvProductName, tvNum, tvQuantity,
                tvReceive, tvReturn, tvDiscrepancy;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ListViewHolder lvh;

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(getListItemResource(), null);
            lvh = new ListViewHolder();

            lvh.tvProductName = (AutofitTextView) convertView.findViewById(R.id.tvProductName);
            lvh.tvNum = (AutofitTextView) convertView.findViewById(R.id.tvNum);
            lvh.tvQuantity = (AutofitTextView) convertView.findViewById(R.id.tvQuantity);

            lvh.tvReceive = (AutofitTextView) convertView.findViewById(R.id.tvReceive);
            lvh.tvReturn = (AutofitTextView) convertView.findViewById(R.id.tvReturn);
            lvh.tvDiscrepancy = (AutofitTextView) convertView.findViewById(R.id.tvDiscrepancy);

            convertView.setTag(lvh);
        }
        else
            lvh = (ListViewHolder) convertView.getTag();

        SelectedProductItem selectedProductItem = getDisplayProductListItem().get(position);
        Product product = selectedProductItem.getProduct();

        lvh.tvProductName.setText(product.getName() + (!isManual && product.getUnit() != null?
                " (" + product.getUnit() + ")" : "" ));
        lvh.tvNum.setText("" + getItem(position).getLine_no());

        lvh.tvQuantity.setText(NumberTools.separateInCommas(selectedProductItem.getOriginalQuantity()));
        lvh.tvReceive.setText(NumberTools.separateInCommas(selectedProductItem.getQuantity()));
        lvh.tvReturn.setText(NumberTools.separateInCommas(selectedProductItem.getReturn()));
        lvh.tvDiscrepancy.setText(NumberTools.separateInCommas(selectedProductItem.getDiscrepancy()));

        lvh.tvQuantity.setVisibility(isManual? View.GONE : View.VISIBLE);
        lvh.tvDiscrepancy.setVisibility(isManual? View.GONE : View.VISIBLE);
        return convertView;
    }

    /*@Override
    public List<DocumentLine> generateDocumentLines() {
        List<DocumentLine> documentLines = new ArrayList<>();
        for(Product product : receivedProducts) {
            Double orig_qty = NumberTools.toDouble(product.getOrig_quantity());
            Double outright_return = NumberTools.toDouble(product.getRet_quantity());
            //Double discrepancy = isManual? 0d : NumberTools.toDouble(product.getDsc_quantity());
            Double receive_qty = NumberTools.toDouble(product.getRcv_quantity());

            if(isManual) {
                if(outright_return == 0d && receive_qty == 0d)
                    continue;
            }

            documentLines.add(
                    new DocumentLine.Builder()
                            .line_no(documentLines.size() + 1)
                            .product_id(product.getId())
                            .useProductDetails(product)
                            .quantity(orig_qty)
                            *//*.extended_attributes(
                                    new ExtendedAttributes.Builder()
                                            .outright_return(outright_return != 0d ? "" + outright_return : null)
                                            .discrepancy(discrepancy != 0d ? "" + discrepancy : null)
                                            .buildIfNotEmpty()
                            )*//*
                            .price(1)
                            .discount_text("0.0%")
                            .build()
            );
        }
        return documentLines;
    }*/

    public boolean updateList(List<DocumentLine> documentLines) {
        updateSelectedProduct(documentLines);
        notifyDataSetChanged();
        return getCount() > 0;
    }
}
