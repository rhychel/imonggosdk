package net.nueca.concessioengine.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleMultiInputAdapter;
import net.nueca.concessioengine.adapters.SimpleReceiveMultiInputAdapter;
import net.nueca.concessioengine.adapters.SimpleReceiveRecyclerViewAdapter;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.dialogs.BaseQuantityDialog;
import net.nueca.concessioengine.dialogs.SimpleQuantityDialog;
import net.nueca.concessioengine.dialogs.SimpleReceiveDialog;
import net.nueca.concessioengine.objects.ExtendedAttributes;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.operations.ImonggoTools;
import net.nueca.imonggosdk.tools.NumberTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by gama on 9/22/15.
 */
public class ReceiveMultiInputFragment extends ImonggoFragment {
    private RecyclerView rvProducts;

    private TextView tvTitle, tvTotalQuantity;
    private Toolbar tbActionBar;

    private boolean isManual = false;

    private SimpleReceiveMultiInputAdapter simpleReceiveMultiInputAdapter;
    private SimpleReceiveDialog simpleReceiveDialog;
    private SelectedProductItem selectedProductItem;

    private OnValuesUpdatedListener valuesUpdatedListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_receive_multiinput_layout, container, false);

        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);
        tbActionBar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        tbActionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        rvProducts = (RecyclerView) view.findViewById(R.id.rvProducts);
        tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        tvTotalQuantity = (TextView) view.findViewById(R.id.tvTotalQuantity);

        tvTitle.setText(selectedProductItem.getProduct().getName());
        tvTotalQuantity.setText(NumberTools.separateInCommas(selectedProductItem.getQuantity()));

        simpleReceiveMultiInputAdapter = new SimpleReceiveMultiInputAdapter();
        simpleReceiveMultiInputAdapter.setSelectedProductItem(selectedProductItem);
        //simpleReceiveMultiInputAdapter.addAll(selectedProductItem.getValues());
        simpleReceiveMultiInputAdapter.setIsManual(isManual);
        simpleReceiveMultiInputAdapter.initializeRecyclerView(getActivity(), rvProducts);
        simpleReceiveMultiInputAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(View view, final int position) {
                if(simpleReceiveDialog == null)
                    simpleReceiveDialog = new SimpleReceiveDialog(getActivity());
                simpleReceiveDialog.setProductName(selectedProductItem.getProduct().getName());
                simpleReceiveDialog.setQuantity(selectedProductItem.getOriginalQuantity(position));
                simpleReceiveDialog.setReceiveText(selectedProductItem.getQuantity(position));
                simpleReceiveDialog.setReturnText(selectedProductItem.getReturn(position));
                simpleReceiveDialog.setDiscrepancy(selectedProductItem.getDiscrepancy(position));
                simpleReceiveDialog.setIsManual(isManual);

                simpleReceiveDialog.setDialogListener(new SimpleReceiveDialog.SimpleReceiveDialogListener() {
                    @Override
                    public boolean onCancel() {
                        return true;
                    }

                    @Override
                    public void onSave(String receivetxt, String returntxt, String discrepancytxt) {
                        Values values = selectedProductItem.getValues().get(position);

                        /**
                         * TODO: replace UNIT
                         **/
//                    values.setValue(receivetxt, null, new ExtendedAttributes(
//                            NumberTools.toNullableDouble(returntxt), NumberTools.toNullableDouble(discrepancytxt)));
                        values.setQuantity(receivetxt);
                        ExtendedAttributes extendedAttributes = values.getExtendedAttributes();
                        if (extendedAttributes == null)
                            extendedAttributes = new ExtendedAttributes();
                        extendedAttributes.setOutright_return(returntxt);
                        extendedAttributes.setDiscrepancy(discrepancytxt);

                        values.setExtendedAttributes(extendedAttributes);

                        selectedProductItem.addValues(values);
                        simpleReceiveMultiInputAdapter.notifyItemChanged(position);

                        if(valuesUpdatedListener != null)
                            valuesUpdatedListener.onUpdate(values);

                        tvTotalQuantity.setText(NumberTools.separateInCommas(selectedProductItem.getQuantity()));
                    }
                });

                simpleReceiveDialog.show();
            }
        });

        rvProducts.setAdapter(simpleReceiveMultiInputAdapter);

        return view;
    }

    public void setSimpleReceiveDialog(SimpleReceiveDialog simpleReceiveDialog) {
        this.simpleReceiveDialog = simpleReceiveDialog;
    }

    public void setIsManual(boolean isManual) {
        this.isManual = isManual;
    }

    public void setSelectedProductItem(SelectedProductItem selectedProductItem) {
        this.selectedProductItem = selectedProductItem;
    }

    public void setOnValuesUpdatedListener(OnValuesUpdatedListener valuesUpdatedListener) {
        this.valuesUpdatedListener = valuesUpdatedListener;
    }

    public interface OnValuesUpdatedListener {
        void onUpdate(Values values);
    }
}
