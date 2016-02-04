package net.nueca.concessioengine.adapters;

import android.app.FragmentManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseSplitPaymentAdapter;
import net.nueca.concessioengine.enums.DialogType;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.dialogs.SimplePaymentDialog;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.invoice.PaymentType;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;
import net.nueca.imonggosdk.tools.NumberTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 20/10/2015.
 */
public class SimpleSplitPaymentAdapter extends BaseSplitPaymentAdapter<SimpleSplitPaymentAdapter.ListViewHolder> {
    private ImonggoDBHelper2 dbHelper;
    private FragmentManager fragmentManager;

    public SimpleSplitPaymentAdapter(Context context, ImonggoDBHelper2 dbHelper) {
        this(context, dbHelper, ListingType.BASIC_PAYMENTS);
    }

    public SimpleSplitPaymentAdapter(Context context, InvoiceTools.PaymentsComputation computation, ImonggoDBHelper2 dbHelper) {
        this(context, dbHelper, ListingType.BASIC_PAYMENTS, computation);
    }

    public SimpleSplitPaymentAdapter(Context context, ImonggoDBHelper2 dbHelper, ListingType listingType, InvoiceTools.PaymentsComputation computation) {
        super(context, chooseLayout(listingType), computation);
        this.dbHelper = dbHelper;
        this.listingType = listingType;
    }

    public SimpleSplitPaymentAdapter(Context context, ImonggoDBHelper2 dbHelper, ListingType listingType) {
        super(context, chooseLayout(listingType), null);
        this.dbHelper = dbHelper;
        this.listingType = listingType;
    }

    public SimpleSplitPaymentAdapter(Context context,  ImonggoDBHelper2 dbHelper, ListingType listingType, InvoiceTools.PaymentsComputation
            computation, List<PaymentType> paymentTypes) {
        super(context, chooseLayout(listingType), computation, paymentTypes);
        this.dbHelper = dbHelper;
        this.listingType = listingType;
    }

    private static int chooseLayout(ListingType listingType) {
        switch (listingType) {
            case COLORED_PAYMENTS:
                return R.layout.simple_checkout_payment_item2;
            default:
                return R.layout.simple_checkout_payment_item;
        }
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(getListItemResource(), parent, false);

        ListViewHolder lvh = new ListViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder lvh, final int position) {
        if(listingType == ListingType.BASIC_PAYMENTS && position == getCount()) {
            lvh.tvPaymentType.setText("Tap to add payment...");
            lvh.tvPaymentValue.setText(NumberTools.separateInCommas(""));
            lvh.tvPaymentValue.setVisibility(View.GONE);
            lvh.isAdd = true;
        }
        else {
            InvoicePayment invoicePayment = getItem(position);

            lvh.itemView.setTag(position);
            lvh.tvPaymentType.setText(getPaymentTypeWithId(invoicePayment.getPayment_type_id()).getName());
            lvh.tvPaymentValue.setText("P" + NumberTools.separateInCommas(invoicePayment.getTender()));
            lvh.tvPaymentValue.setVisibility(View.VISIBLE);
            if(listingType == ListingType.COLORED_PAYMENTS)
                lvh.tvPaymentValue.setTextColor(getContext().getResources().getColor(R.color.payment_color));
            lvh.isAdd = false;
            lvh.isLastItem = false;
//            if(listingType == ListingType.BASIC_PAYMENTS) {
//                lvh.tvPaymentType.setText("Tap to add payment...");
//                lvh.tvPaymentValue.setText(NumberTools.separateInCommas(""));
//                lvh.tvPaymentValue.setVisibility(View.GONE);
//                lvh.isAdd = true;
//            }
        }
        lvh.position = position;

        //lvh.showButtons(false);
    }

    @Override
    public int getItemCount() {
        if(listingType == ListingType.COLORED_PAYMENTS)
            return getCount();
        return isFullyPaid ? getCount() : getCount()+1;
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {
        boolean isAdd = false;
        TextView tvPaymentType, tvPaymentValue;
        //ImageButton ibtnEdit, ibtnDelete;
        //View llButtons;
        int position;
        boolean isLastItem = false;

        public ListViewHolder(View itemView) {
            super(itemView);
            tvPaymentType = (TextView) itemView.findViewById(R.id.tvPaymentType);
            tvPaymentValue = (TextView) itemView.findViewById(R.id.tvPaymentValue);

            //ibtnEdit = (ImageButton) itemView.findViewById(R.id.ibtnEdit);
            //ibtnDelete = (ImageButton) itemView.findViewById(R.id.ibtnDelete);
            //llButtons = itemView.findViewById(R.id.llButtons);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            //ibtnDelete.setOnClickListener(delete);
        }

        /*public void showButtons(boolean shouldShow) {
            llButtons.setVisibility(shouldShow? View.VISIBLE : View.GONE);
        }*/

        @Override
        public void onClick(final View v) {
            if(listingType == ListingType.BASIC_PAYMENTS) {
                SimplePaymentDialog dialog = new SimplePaymentDialog(getContext(),
                        new ArrayList<>(getPaymentTypes().values()));
                dialog.setFragmentManager(fragmentManager);

                dialog.setListener(new SimplePaymentDialog.PaymentDialogListener() {
                    @Override
                    public void onAddPayment(PaymentType paymentType, String paymentValue, Extras extras) {
                        InvoicePayment.Builder builder = new InvoicePayment.Builder();
                        builder.amount(NumberTools.toDouble(paymentValue));

                        // TODO: must set tender to be able to compute
                        builder.tender(NumberTools.toDouble(paymentValue));

                        if(paymentType != null)
                            builder.payment_type_id(paymentType.getId());

                        InvoicePayment invoicePayment = builder.build();
                        invoicePayment.setExtras(extras);
                        /*add(invoicePayment);
                        notifyItemInserted(getItemCount());

                        computation.addPayment(invoicePayment);
                        if(paymentUpdateListener != null)
                            paymentUpdateListener.onAddPayment(invoicePayment);*/
                        if(isAdd)
                            addPayment(invoicePayment);
                        else {
                            updatePayment(position, invoicePayment);
                        }
                    }
                });
                if(isAdd || !isLayaway)
                    dialog.show();
            }
            else {
                final int position = (int)v.getTag();

                SimplePaymentDialog dialog = new SimplePaymentDialog(getContext(), getPaymentTypeList(),
                        R.style.AppCompatDialogStyle_Light_NoTitle);
                dialog.setDialogType(DialogType.ADVANCED_PAY);
                dialog.setBalanceText(balance); // should be updated
                dialog.setTotalAmountText(totalAmount); // should be updated
                dialog.setInvoicePayment(getItem(position));
                dialog.setListener(new SimplePaymentDialog.PaymentDialogListener() {
                    @Override
                    public void onAddPayment(PaymentType paymentType, String paymentValue, Extras extras) {

                        InvoicePayment.Builder builder = new InvoicePayment.Builder();
                        builder.amount(NumberTools.toDouble(paymentValue));

                        // TODO: must set tender to be able to compute
                        builder.tender(NumberTools.toDouble(paymentValue));

                        if(paymentType != null)
                            builder.payment_type_id(paymentType.getId());

                        InvoicePayment invoicePayment = builder.build();
                        invoicePayment.setExtras(extras);

                        updatePayment(position, invoicePayment);

                        if(onItemClickListener != null)
                            onItemClickListener.onItemClicked(v, position);
                        //simpleSplitPaymentAdapter.add(invoicePayment);
                        //simpleSplitPaymentAdapter.notifyItemInserted(simpleSplitPaymentAdapter.getItemCount());
                    }
                });
                if(!isLayaway)
                    dialog.show();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }

        public boolean isAdd() {
            return isAdd;
        }

        public boolean isLastItem() {
            return isLastItem;
        }

        public int getItemIndex() {
            return position;
        }

        /*private View.OnClickListener delete = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogTools.showConfirmationDialog(getContext(), "Delete", "Are you sure?", "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletePayment(position);
                    }
                }, "No", null);
            }
        };*/
    }
}
