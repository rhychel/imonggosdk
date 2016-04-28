package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseRoutePlanRecyclerAdapter;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.tools.NumberTools;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by rhymart on 12/2/15.
 */
public class SimpleRoutePlanRecyclerViewAdapter extends BaseRoutePlanRecyclerAdapter<SimpleRoutePlanRecyclerViewAdapter.ListViewHolder> {

    public SimpleRoutePlanRecyclerViewAdapter(Context context, List<Customer> list) {
        super(context, list);
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_route_plan_item, parent, false);

        ListViewHolder lvh = new ListViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        Customer customer = getItem(position);

        if(customer != null) {
            Log.e("Customer", "is not null -- adapter");
            holder.tvCustomerName.setText(customer.getName());
            holder.tvCompany.setText(customer.getCompany_name());

            if(customer.getLastPurchase() != null && !customer.getLastPurchase().equals("None") && !customer.getLastPurchase().isEmpty()) {
                SimpleDateFormat fromDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                fromDate.setTimeZone(TimeZone.getTimeZone("UTC"));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy, cccc h:mma");
                simpleDateFormat.setTimeZone(TimeZone.getDefault());
                try {
                    Date date = fromDate.parse(customer.getLastPurchase().split("T")[0] + " " + customer.getLastPurchase().split("T")[1].replace("Z", ""));
                    holder.tvLastTransaction.setText(simpleDateFormat.format(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            holder.tvTransactionBranch.setText(customer.getLastPurchaseBranch());
            holder.tvSubtotal.setText("P 0.00");
            holder.ivStatus.setImageResource(R.drawable.ic_check_round_teal);
            holder.tvSubtotal.setTextColor(ContextCompat.getColor(getContext(), R.color.payment_color));

            List<Invoice> myInvoices = customer.getMyInvoices();
            if(myInvoices.size() > 0) {
                ProductsAdapterHelper.setSelectedCustomer(customer);

                InvoiceTools.PaymentsComputation paymentsComputation = new InvoiceTools.PaymentsComputation();

                /*int device_id = ProductsAdapterHelper.getSession().getDevice_id();
                if(Integer.parseInt(myInvoices.get(0).getReference().substring(0,myInvoices.get(0).getReference().indexOf('-'))) != device_id) {
                    List<InvoiceLine> invoiceLines = null;
                    try {
                        invoiceLines = InvoiceTools.generateInvoiceLines(InvoiceTools.generateSelectedProductItemList
                                (ProductsAdapterHelper.getDbHelper(),myInvoices.get(0),customer,ProductsAdapterHelper.getSelectedBranch(),false,false));
                        invoiceLines.addAll(InvoiceTools.generateInvoiceLines(InvoiceTools.generateSelectedProductItemList
                                (ProductsAdapterHelper.getDbHelper(),myInvoices.get(0),customer,ProductsAdapterHelper.getSelectedBranch(),true,false)));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    myInvoices.get(0).setInvoiceLines(invoiceLines);
                }*/

                paymentsComputation.addAllInvoiceLines(myInvoices.get(0).getInvoiceLines());
                paymentsComputation.addAllPayments(myInvoices.get(0).getPayments());

                BigDecimal remaining = paymentsComputation.getRemaining();
                holder.tvSubtotal.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
                if (remaining.signum() == 1)
                    holder.tvSubtotal.setText("P " + NumberTools.separateInCommas(remaining));
                else {
                    holder.tvSubtotal.setText("P " + NumberTools.separateInCommas(paymentsComputation.getTotalPayable(true)));
                    holder.tvSubtotal.setTextColor(ContextCompat.getColor(getContext(), R.color.payment_color));
                }

                double balance = 0.0;
                for(Invoice invoice : myInvoices) {
                    InvoiceTools.PaymentsComputation temp = new InvoiceTools.PaymentsComputation();
                    /*device_id = ProductsAdapterHelper.getSession().getDevice_id();
                    if(Integer.parseInt(invoice.getReference().substring(0,invoice.getReference().indexOf('-'))) != device_id) {
                        List<InvoiceLine> invoiceLines = null;
                        try {
                            invoiceLines = InvoiceTools.generateInvoiceLines(InvoiceTools.generateSelectedProductItemList
                                    (ProductsAdapterHelper.getDbHelper(),invoice,customer,ProductsAdapterHelper.getSelectedBranch(),false,false));
                            invoiceLines.addAll(InvoiceTools.generateInvoiceLines(InvoiceTools.generateSelectedProductItemList
                                    (ProductsAdapterHelper.getDbHelper(),invoice,customer,ProductsAdapterHelper.getSelectedBranch(),true,false)));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        invoice.setInvoiceLines(invoiceLines);
                    }*/
                    temp.addAllInvoiceLines(invoice.getInvoiceLines());
                    temp.addAllPayments(invoice.getPayments());

                    balance += temp.getRemaining().doubleValue();
                }

                if(balance > 0) {
                    holder.ivStatus.setImageResource(R.drawable.ic_alert_red);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {

        public ImageView ivStatus;
        public TextView tvCustomerName, tvCompany, tvLastTransaction, tvTransactionBranch, tvSubtotal;

        public ListViewHolder(View itemView) {
            super(itemView);

            ivStatus = (ImageView) itemView.findViewById(R.id.ivStatus);
            tvCustomerName = (TextView) itemView.findViewById(R.id.tvCustomerName);
            tvCompany = (TextView) itemView.findViewById(R.id.tvCompany);
            tvLastTransaction = (TextView) itemView.findViewById(R.id.tvLastTransaction);
            tvTransactionBranch = (TextView) itemView.findViewById(R.id.tvTransactionBranch);
            tvSubtotal = (TextView) itemView.findViewById(R.id.tvSubtotal);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(onItemClickListener != null)
                onItemClickListener.onItemClicked(v, getLayoutPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }
}
