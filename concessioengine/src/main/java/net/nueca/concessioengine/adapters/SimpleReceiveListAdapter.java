package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.ColorRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseReceiveAdapter;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.document.DocumentLine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by gama on 9/3/15.
 */
public class SimpleReceiveListAdapter extends BaseReceiveAdapter {
    //private ColorStateList defaultTextColor;
    //private Integer errorTextColor;
    private boolean isManual = false;

    public SimpleReceiveListAdapter(Context context, ImonggoDBHelper dbHelper, List<DocumentLine> objects) {
        super(context, R.layout.simple_receive_listitem, dbHelper, objects);
        //errorTextColor = context.getResources().getColor(android.R.color.holo_red_light);
    }

    private static class ListViewHolder {
        AutofitTextView tvProductName, tvNum, tvQuantity,
                tvReceive, tvReturn, tvDiscrepancy;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ListViewHolder lvh;

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.simple_receive_listitem, null);
            lvh = new ListViewHolder();

            lvh.tvProductName = (AutofitTextView) convertView.findViewById(R.id.tvProductName);
            lvh.tvNum = (AutofitTextView) convertView.findViewById(R.id.tvNum);
            lvh.tvQuantity = (AutofitTextView) convertView.findViewById(R.id.tvQuantity);

            lvh.tvReceive = (AutofitTextView) convertView.findViewById(R.id.tvReceive);
            lvh.tvReturn = (AutofitTextView) convertView.findViewById(R.id.tvReturn);
            lvh.tvDiscrepancy = (AutofitTextView) convertView.findViewById(R.id.tvDiscrepancy);

            //if(defaultTextColor == null)
            //    defaultTextColor =  lvh.tvDiscrepancy.getTextColors();

            convertView.setTag(lvh);
        }
        else
            lvh = (ListViewHolder) convertView.getTag();

        Product product = getProductItem(position);

        lvh.tvProductName.setText(product.getName() + (!isManual?" (" + product.getUnit() + ")" : "" ));
        lvh.tvNum.setText("" + getItem(position).getLine_no());
        lvh.tvQuantity.setText(product.getOrig_quantity());
        lvh.tvReceive.setText(product.getRcv_quantity());
        lvh.tvReturn.setText(product.getRet_quantity());
        lvh.tvDiscrepancy.setText(product.getDsc_quantity());

        lvh.tvQuantity.setVisibility(isManual? View.GONE : View.VISIBLE);
        lvh.tvDiscrepancy.setVisibility(isManual? View.GONE : View.VISIBLE);

        //BigDecimal orig_qty = new BigDecimal(product.getOrig_quantity().replaceAll("[^0-9.]",""));
        /*BigDecimal rcv_qty = new BigDecimal(product.getRcv_quantity().replaceAll("[^0-9.]", ""));
        BigDecimal ret_qty = new BigDecimal(product.getRet_quantity().replaceAll("[^0-9.]", ""));
        BigDecimal dsc_qty = new BigDecimal(product.getDsc_quantity().replaceAll("[^0-9.]", ""));

        boolean error = dsc_qty.compareTo(BigDecimal.ZERO) < 0;
        error = error || (dsc_qty.compareTo(BigDecimal.ZERO) > 0 &&
                rcv_qty.add(ret_qty).compareTo(BigDecimal.ZERO) > 0);

        if(error)
            lvh.tvDiscrepancy.setTextColor(errorTextColor);
        else
            lvh.tvDiscrepancy.setTextColor(defaultTextColor);*/
        return convertView;
    }

    /*public void setErrorTextColor(@ColorRes Integer errorTextColor) {
        this.errorTextColor = errorTextColor;
    }*/

    public boolean isManual() {
        return isManual;
    }

    public void setIsManual(boolean isManual) {
        this.isManual = isManual;
    }

    public boolean updateList(List<DocumentLine> documentLines) {
        updateProductList(documentLines);
        notifyDataSetChanged();
        return getCount() > 0;
    }
}
