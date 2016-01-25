package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import net.nueca.concessioengine.adapters.base.BaseSplitPaymentAdapter;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;

import java.util.List;

/**
 * Created by gama on 16/10/2015.
 */
public abstract class BaseCheckoutFragment extends ImonggoFragment implements BaseSplitPaymentAdapter.OnPaymentUpdateListener {
    public static final String INVOICE_ARGUMENT_KEY = "invoice";

    protected SetupActionBar setupActionBar;
    protected Toolbar tbActionBar;

    protected Invoice invoice;
    protected InvoiceTools.PaymentsComputation computation = new InvoiceTools.PaymentsComputation();

    public BaseCheckoutFragment() {
        if(getArguments() != null && getArguments().containsKey(INVOICE_ARGUMENT_KEY)) {
            invoice = (Invoice) getArguments().get(INVOICE_ARGUMENT_KEY);
            computation.clearAll();
            computation.addAllInvoiceLines(invoice.getInvoiceLines());
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(setupActionBar != null && tbActionBar != null)
            setupActionBar.setupActionBar(tbActionBar);
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
        computation.clearAll();
        computation.addAllInvoiceLines(invoice.getInvoiceLines());
    }

    public Invoice getCheckoutInvoice() {
        if(this.invoice == null)
            invoice = new Invoice();
        invoice.setInvoiceLines(getInvoiceLines());
        invoice.setPayments(getPayments());

        Customer customer = ProductsAdapterHelper.getSelectedCustomer();

        invoice.setCustomer(customer);

        /** EXTRAS **/
        Extras extras = invoice.getExtras() == null? new Extras() : invoice.getExtras();
        extras.setCustomer_discount_text_summary(customer.getDiscount_text());
        extras.setCustomer_discount_amounts_summary(
                InvoiceTools.generateDiscountAmount(InvoiceTools.consolidateCustomerDiscount(invoice.getInvoiceLines()),',')
        );
        extras.setTotal_selling_price("" +
                (InvoiceTools.addNoDiscountSubtotals(invoice.getInvoiceLines()) -
                        InvoiceTools.sum(InvoiceTools.getAllProductDiscount(invoice.getInvoiceLines())))
        );
        extras.setTotal_company_discount("" + InvoiceTools.sum(InvoiceTools.getAllCompanyDiscount(invoice.getInvoiceLines())));
        extras.setTotal_customer_discount("" + InvoiceTools.sum(InvoiceTools.consolidateCustomerDiscount(invoice.getInvoiceLines())));

        extras.setTotal_unit_retail_price("" +
                ( Double.valueOf(extras.getTotal_selling_price()) -
                        (Double.valueOf(extras.getTotal_company_discount()) +
                                Double.valueOf(extras.getTotal_customer_discount())) )
        );
        extras.setPayment_term_id(customer.getPayment_terms_id());
        extras.setPayment_term_code(customer.getPaymentTerms() == null? null : customer.getPaymentTerms().getCode());
        invoice.setExtras(extras);

        return invoice;
    }

    public List<InvoiceLine> getInvoiceLines() {
        return computation.getInvoiceLines();
    }

    public List<InvoicePayment> getPayments() {
        return computation.getPayments();
    }
    
    public double getAmountDue() {
        return computation.getTotalPayable().doubleValue();
    }

    public double getRemainingBalance() {
        return computation.getRemaining().doubleValue();
    }

    public InvoiceTools.PaymentsComputation getComputation() {
        return computation;
    }

    public void setSetupActionBar(SetupActionBar setupActionBar) {
        this.setupActionBar = setupActionBar;
    }
}
