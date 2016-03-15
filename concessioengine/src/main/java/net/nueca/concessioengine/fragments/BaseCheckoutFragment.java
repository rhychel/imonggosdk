package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;

import net.nueca.concessioengine.adapters.base.BaseSplitPaymentAdapter;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.tools.DiscountTools;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;
import net.nueca.imonggosdk.tools.NumberTools;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 16/10/2015.
 */
public abstract class BaseCheckoutFragment extends ImonggoFragment implements BaseSplitPaymentAdapter.OnPaymentUpdateListener {
    public static final String INVOICE_ARGUMENT_KEY = "invoice";

    protected SetupActionBar setupActionBar;
    protected Toolbar tbActionBar;

    protected boolean isLayaway = false;

    protected Invoice invoice;
    protected InvoiceTools.PaymentsComputation computation = new InvoiceTools.PaymentsComputation();

    public BaseCheckoutFragment() {
        if(getArguments() != null && getArguments().containsKey(INVOICE_ARGUMENT_KEY)) {
            invoice = (Invoice) getArguments().get(INVOICE_ARGUMENT_KEY);
            computation.clearAll();

            computation.addAllInvoiceLines(invoice.getInvoiceLines());
            if(invoice.getPayments() != null)
                computation.addAllPayments(invoice.getPayments());
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
        Log.e("INVOICE RECEIVED", invoice.toJSONString());
        computation.clearAll();

        computation.addAllInvoiceLines(invoice.getInvoiceLines());
        computation.addAllPayments(invoice.getPayments());
    }

    public Invoice getCheckoutInvoice() {
        if(this.invoice == null)
            invoice = new Invoice();
        invoice.setInvoiceLines(getInvoiceLines());
        invoice.setPayments(getPayments());
        Gson gson = new Gson();
        Log.e(">>>>>>>>>>>>>>>>>>>>", gson.toJson(invoice.getPayments()));
        //invoice.joinAllNewToCurrentPaymentBatch();

        Customer customer = ProductsAdapterHelper.getSelectedCustomer();

        invoice.setCustomer(customer);

        /** EXTRAS **/
        Extras extras = invoice.getExtras() == null? new Extras() : invoice.getExtras();
        if(getModuleSetting(ConcessioModule.INVOICE).isHas_partial()) {
            Log.e("generateInvoice", "has partial");
            int decimal = ProductsAdapterHelper.getDecimalPlace();
            extras.setTotal_selling_price("" +
                            (NumberTools.formatDouble(computation.getTotalPayableNoDiscount().doubleValue(), decimal) -
                                    NumberTools.formatDouble(computation.getTotalProductDiscount().doubleValue(), decimal))
                    //(NumberTools.formatDouble(InvoiceTools.addNoDiscountSubtotals(invoice.getInvoiceLines()),2) -
                    //        NumberTools.formatDouble(InvoiceTools.sum(InvoiceTools.getAllProductDiscount(invoice.getInvoiceLines())),2))
            );
            extras.setTotal_company_discount("" +
                    NumberTools.formatDouble(computation.getTotalCompanyDiscount().doubleValue(), decimal));
            //NumberTools.formatDouble(InvoiceTools.sum(InvoiceTools.getAllCompanyDiscount(invoice.getInvoiceLines())),2));
            //extras.setTotal_customer_discount("" + InvoiceTools.sum(InvoiceTools.consolidateCustomerDiscount(invoice.getInvoiceLines())));

            //List<Double> customerDiscounts = new ArrayList<>();
            //double subtotal = NumberTools.formatDouble(InvoiceTools.addSubtotals(invoice.getInvoiceLines()),2);
            double totalCustomerDiscount = NumberTools.formatDouble(computation.getTotalCustomerDiscount().doubleValue(), decimal);
            //double totalCustomerDiscount = NumberTools.formatDouble(subtotal - DiscountTools.applyMultipleDiscounts(new BigDecimal(subtotal),
            //        BigDecimal.ONE, customerDiscounts, customer != null? customer.getDiscount_text() : "",",").doubleValue(),2);
            extras.setTotal_customer_discount("" + totalCustomerDiscount);
            extras.setCustomer_discount_text_summary(customer != null? customer.getDiscount_text() : "");
            extras.setCustomer_discount_amounts_summary(
                    //InvoiceTools.generateDiscountAmount(customerDiscounts,',')
                    InvoiceTools.generateDiscountAmount(computation.getCustomerDiscount(),',', ProductsAdapterHelper.getDecimalPlace())
            );

            extras.setTotal_unit_retail_price("" +
                    ( Double.valueOf(extras.getTotal_selling_price()) -
                            (Double.valueOf(extras.getTotal_company_discount()) +
                                    Double.valueOf(extras.getTotal_customer_discount())) )
            );
            if(customer != null) {
                extras.setPayment_term_id(customer.getPayment_terms_id());
                extras.setPayment_term_code(customer.getPaymentTerms() == null ? null : customer.getPaymentTerms().getCode());
            }
        }
        else
            extras = null;

        invoice.setExtras(extras);

        return invoice;
    }

    public BigDecimal getPointsInAmountUsed() {
        return computation.getTotalPointsPayment();
    }

    public List<InvoiceLine> getInvoiceLines() {
        return computation.getInvoiceLines();
    }

    public List<InvoicePayment> getPayments() {
        List<InvoicePayment> payments = computation.getPayments();
        payments.addAll(computation.getReturnsPayments());
        return payments;
    }
    
    public double getAmountDue() {
        return computation.getTotalPayable(true).doubleValue();
    }

    public double getRemainingBalance() {
        return getRemainingBalance(false);
    }

    public double getRemainingBalance(boolean abs) {
        if(abs)
            return Math.abs(computation.getRemaining().doubleValue());
        return computation.getRemaining().doubleValue();
    }

    public double getTotalPaymentMade() {
        return computation.getTotalPaymentMade().doubleValue();
    }

    public InvoiceTools.PaymentsComputation getComputation() {
        return computation;
    }

    public void setSetupActionBar(SetupActionBar setupActionBar) {
        this.setupActionBar = setupActionBar;
    }

    public boolean isLayaway() {
        return isLayaway;
    }

    public void setLayaway(boolean layaway) {
        isLayaway = layaway;
    }
}