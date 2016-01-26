package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import net.nueca.concessioengine.adapters.base.BaseSplitPaymentAdapter;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
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

    public List<InvoiceLine> getInvoiceLines() {
        return computation.getInvoiceLines();
    }

    public List<InvoicePayment> getPayments() {
        return computation.getPayments();
    }
    
    public String getAmountDue() {
        return computation.getTotalPayable().toPlainString();
    }

    public void setSetupActionBar(SetupActionBar setupActionBar) {
        this.setupActionBar = setupActionBar;
    }
}
