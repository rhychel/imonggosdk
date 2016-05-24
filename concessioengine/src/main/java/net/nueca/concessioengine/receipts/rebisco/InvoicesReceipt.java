package net.nueca.concessioengine.receipts.rebisco;

import android.content.Context;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.printer.ReceiptTools;
import net.nueca.concessioengine.printer.starmicronics.enums.StarIOPaperSize;
import net.nueca.concessioengine.printer.starmicronics.tools.StarIOPrinterTools;
import net.nueca.concessioengine.receipts.BaseBuilder;
import net.nueca.concessioengine.receipts.BaseReceipt;
import net.nueca.concessioengine.tools.BluetoothTools;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;
import net.nueca.imonggosdk.objects.invoice.PaymentType;
import net.nueca.imonggosdk.tools.Configurations;
import net.nueca.imonggosdk.tools.DateTimeTools;
import net.nueca.imonggosdk.tools.NumberTools;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by rhymartmanchus on 17/05/2016.
 */
public class InvoicesReceipt extends BaseReceipt {

    private Invoice invoice;
    private InvoiceTools.PaymentsComputation paymentsComputation;

    public InvoicesReceipt() { }

    public InvoicesReceipt(Builder builder) {
        this.isReprint = builder.isReprint;
        this.offlineData = builder.offlineData;
        this.moduleSetting = builder.moduleSetting;

        if(builder.context == null)
            throw new NullPointerException("InvoicesReceipt.context cannot be null");
        this.context = builder.context;

        if(builder.invoice == null)
            throw new NullPointerException("InvoicesReceipt.invoice cannot be null");
        this.invoice = builder.invoice;

        if(builder.paymentsComputation == null)
            throw new NullPointerException("InvoicesReceipt.paymentsComputation cannot be null");
        this.paymentsComputation = builder.paymentsComputation;

        if(builder.agentName == null)
            throw new NullPointerException("InvoicesReceipt.agentName cannot be null");
        this.agentName = builder.agentName;

        if(builder.branch == null)
            throw new NullPointerException("InvoicesReceipt.branch cannot be null");
        this.branch = builder.branch;
    }

    public static class Builder extends BaseBuilder<InvoicesReceipt> {

        private Invoice invoice;
        private InvoiceTools.PaymentsComputation paymentsComputation;

        public Builder(Context context) {
            super(context);
        }

        public Builder invoice(Invoice invoice) {
            this.invoice = invoice;
            return this;
        }

        public Builder payments_computation(InvoiceTools.PaymentsComputation paymentsComputation) {
            this.paymentsComputation = paymentsComputation;
            return this;
        }

        @Override
        public InvoicesReceipt build() {
            return new InvoicesReceipt(this);
        }

        @Override
        public InvoicesReceipt print(String... labels) {
            InvoicesReceipt invoicesReceipt = new InvoicesReceipt(this);
            invoicesReceipt.printViaStarPrinter(labels);
            return invoicesReceipt;
        }
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public InvoiceTools.PaymentsComputation getPaymentsComputation() {
        return paymentsComputation;
    }

    public void setPaymentsComputation(InvoiceTools.PaymentsComputation paymentsComputation) {
        this.paymentsComputation = paymentsComputation;
    }

    private void printViaStarPrinter(final String... labels) {
        if(!BluetoothTools.isEnabled())
            return;

        if(!StarIOPrinterTools.isPrinterOnline(context, StarIOPrinterTools.getTargetPrinter(context), "portable"))
            return;

        ArrayList<byte[]> data = new ArrayList<>();

        for(int i = 0;i < labels.length;i++) {
            // ---------- HEADER
            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center Justification <ESC> a n (0 Left, 1 Center, 2 Right)0,
            data.add((branch.getName()+"\r\n").getBytes());
            data.add((branch.generateAddress()+"\r\n\r\n").getBytes());

            data.add(("ORDER SLIP\r\n\r\n").getBytes());
            data.add(("Salesman: "+agentName+"\r\n").getBytes());
            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
            if(offlineData != null) {
                data.add(("Ref #: "+offlineData.getReference_no()+"\r\n").getBytes());
                data.add(("Date: " + simpleDateFormat.format(offlineData.getDateCreated())+"\r\n").getBytes());
            }
            else {
                data.add(("Ref #: "+invoice.getReference()+"\r\n").getBytes());
                data.add(("Date: " + simpleDateFormat.format(Calendar.getInstance().getTime()) + "\r\n").getBytes());
            }
            // ---------- HEADER

            double totalQuantity = 0.0;
            data.add("ORDERS\r\n".getBytes());
            data.add("================================".getBytes());
            data.add("Quantity                  Amount".getBytes());
            data.add("================================".getBytes());

            int totalInvoiceLines = invoice.getSalesInvoiceLines().size()+invoice.getRgsInvoiceLines().size()+invoice.getBoInvoiceLines().size()+invoice.getPayments().size();

            double numberOfPages = Math.ceil((double)totalInvoiceLines/Configurations.MAX_ITEMS_FOR_PRINTING), items = 0;
            int page = 1;

            for (InvoiceLine invoiceLine : invoice.getSalesInvoiceLines()) {
                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                Product product = Product.fetchById(ProductsAdapterHelper.getDbHelper(), Product.class, invoiceLine.getProduct_id());
                data.add((product.getName() + "\r\n").getBytes());
                if(invoiceLine.getUnit_id() != null) {
                    totalQuantity += invoiceLine.getUnit_quantity();
                    data.add(("  " + invoiceLine.getUnit_quantity() + "   " + invoiceLine.getUnit_name() + " x " + NumberTools.separateInCommas(invoiceLine.getRetail_price())+"\r\n").getBytes());
                    data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                    data.add((NumberTools.separateInCommas(invoiceLine.getSubtotal())+"\r\n").getBytes());
                }
                else {
                    totalQuantity += invoiceLine.getQuantity();
                    data.add(("  " + invoiceLine.getQuantity() + "   " + product.getBase_unit_name() + " x " + NumberTools.separateInCommas(invoiceLine.getRetail_price())+"\r\n").getBytes());
                    data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                    data.add((NumberTools.separateInCommas(invoiceLine.getSubtotal())+"\r\n").getBytes());
                }
                items++;

                if(numberOfPages > 1.0 && page < (int)numberOfPages && items == Configurations.MAX_ITEMS_FOR_PRINTING) {
                    data.add(("\r\n\r\n\r\n").getBytes());
                    data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center
                    data.add(("*Page "+page+"*\r\n\r\n").getBytes());
                    data.add(("- - - - - - CUT HERE - - - - - -\r\n\r\n").getBytes());
                    page++;
                    items = 0;

                    if(!StarIOPrinterTools.print(context, StarIOPrinterTools.getTargetPrinter(context), "portable", StarIOPaperSize.p2INCH, data))
                        break;
                    data.clear();
                }
            }
            data.add("--------------------------------".getBytes());
            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left

            data.add((ReceiptTools.spacer("Total Quantity: ", NumberTools.separateInCommas(totalQuantity), 32)+"\r\n").getBytes());
            data.add((ReceiptTools.spacer("Gross Amount: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalPayableNoReturns(false).doubleValue(), 2)), 32)+"\r\n").getBytes());

            if(paymentsComputation.getCustomerDiscount().size() > 0) {
                data.add((ReceiptTools.spacer("LESS Customer Discount: ", invoice.getExtras().getCustomer_discount_text_summary(), 32) + "\r\n").getBytes());
                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                for (Double cusDisc : paymentsComputation.getCustomerDiscount())
                    data.add(("(" + NumberTools.separateInCommas(cusDisc) + ")\r\n").getBytes());
            }

            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
            data.add((ReceiptTools.spacer("Net Order Amount: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalPayableNoReturns(true).doubleValue(), 2)), 32)+"\r\n\r\n").getBytes());

            invoice.getReturnInvoiceLines();
            if(invoice.getBoInvoiceLines().size() > 0) {
                totalQuantity = 0.0;
                data.add("BAD ORDERS\r\n".getBytes());
                data.add("================================".getBytes());
                data.add("Quantity                  Amount".getBytes());
                data.add("================================".getBytes());
                for (InvoiceLine invoiceLine : invoice.getBoInvoiceLines()) {
                    data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                    Product product = Product.fetchById(ProductsAdapterHelper.getDbHelper(), Product.class, invoiceLine.getProduct_id());
                    data.add((product.getName() + "\r\n").getBytes());
                    if (invoiceLine.getUnit_id() != null) {
                        totalQuantity += invoiceLine.getUnit_quantity();
                        data.add(("  " + Math.abs(invoiceLine.getUnit_quantity()) + "   " + invoiceLine.getUnit_name() + " x " + NumberTools.separateInCommas(Math.abs(invoiceLine.getRetail_price())) + "\r\n").getBytes());
                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                        data.add((NumberTools.separateInCommas(Math.abs(Double.valueOf(invoiceLine.getSubtotal()))) + "\r\n").getBytes());
                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                        data.add(("Reason: " + invoiceLine.getExtras().getInvoice_purpose_name() + "\r\n").getBytes());
                    }
                    else {
                        totalQuantity += invoiceLine.getQuantity();
                        data.add(("  " + Math.abs(invoiceLine.getQuantity()) + "   " + product.getBase_unit_name() + " x " + NumberTools.separateInCommas(Math.abs(invoiceLine.getRetail_price())) + "\r\n").getBytes());
                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                        data.add((NumberTools.separateInCommas(Math.abs(Double.valueOf(invoiceLine.getSubtotal()))) + "\r\n").getBytes());
                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                        data.add(("Reason: " + invoiceLine.getExtras().getInvoice_purpose_name() + "\r\n").getBytes());
                    }
                    items++;

                    if(numberOfPages > 1.0 && page < (int)numberOfPages && items == Configurations.MAX_ITEMS_FOR_PRINTING) {
                        data.add(("\r\n\r\n\r\n").getBytes());
                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center
                        data.add(("*Page "+page+"*\r\n\r\n").getBytes());
                        data.add(("- - - - - - CUT HERE - - - - - -\r\n\r\n").getBytes());
                        page++;
                        items = 0;

                        if(!StarIOPrinterTools.print(context, StarIOPrinterTools.getTargetPrinter(context), "portable", StarIOPaperSize.p2INCH, data))
                            break;
                        data.clear();
                    }
                }
                data.add("--------------------------------".getBytes());
                data.add((ReceiptTools.spacer("Total Quantity: ", NumberTools.separateInCommas(NumberTools.formatDouble(Math.abs(totalQuantity), 2)), 32)+"\r\n").getBytes());
                data.add((ReceiptTools.spacer("LESS Net BO Amount: ", "("+NumberTools.separateInCommas(NumberTools.formatDouble(Math.abs(paymentsComputation.getReturnsPayments().get(0).getAmount()),2))+")", 32)+"\r\n\r\n").getBytes());
            }
            if(invoice.getRgsInvoiceLines().size() > 0) {
                totalQuantity = 0.0;
                data.add("RGS\r\n".getBytes());
                data.add("================================".getBytes());
                data.add("Quantity                  Amount".getBytes());
                data.add("================================".getBytes());
                for (InvoiceLine invoiceLine : invoice.getRgsInvoiceLines()) {
                    data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                    Product product = Product.fetchById(ProductsAdapterHelper.getDbHelper(), Product.class, invoiceLine.getProduct_id());
                    data.add((product.getName() + "\r\n").getBytes());
                    if (invoiceLine.getUnit_id() != null) {
                        totalQuantity += invoiceLine.getUnit_quantity();
                        data.add(("  " + Math.abs(invoiceLine.getUnit_quantity()) + "   " + invoiceLine.getUnit_name() + " x " + NumberTools.separateInCommas(Math.abs(invoiceLine.getRetail_price())) + "\r\n").getBytes());
                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                        data.add((NumberTools.separateInCommas(Math.abs(Double.valueOf(invoiceLine.getSubtotal()))) + "\r\n").getBytes());
                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                        data.add(("Reason: " + invoiceLine.getExtras().getInvoice_purpose_name() + "\r\n").getBytes());
                    }
                    else {
                        totalQuantity += invoiceLine.getQuantity();
                        data.add(("  " + Math.abs(invoiceLine.getQuantity()) + "   " + product.getBase_unit_name() + " x " + NumberTools.separateInCommas(Math.abs(invoiceLine.getRetail_price())) + "\r\n").getBytes());
                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                        data.add((NumberTools.separateInCommas(Math.abs(Double.valueOf(invoiceLine.getSubtotal()))) + "\r\n").getBytes());
                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                        data.add(("Reason: " + invoiceLine.getExtras().getInvoice_purpose_name() + "\r\n").getBytes());
                    }
                    items++;

                    if(numberOfPages > 1.0 && page < (int)numberOfPages && items == Configurations.MAX_ITEMS_FOR_PRINTING) {
                        data.add(("\r\n\r\n\r\n").getBytes());
                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center
                        data.add(("*Page "+page+"*\r\n\r\n").getBytes());
                        data.add(("- - - - - - CUT HERE - - - - - -\r\n\r\n").getBytes());
                        page++;
                        items = 0;

                        if(!StarIOPrinterTools.print(context, StarIOPrinterTools.getTargetPrinter(context), "portable", StarIOPaperSize.p2INCH, data))
                            break;
                        data.clear();
                    }
                }
                data.add("--------------------------------".getBytes());
                data.add((ReceiptTools.spacer("Total Quantity: ", NumberTools.separateInCommas(Math.abs(totalQuantity)), 32)+"\r\n").getBytes());
                if(paymentsComputation.getReturnsPayments().size() > 1)
                    data.add((ReceiptTools.spacer("LESS Net RGS Amount: ", "("+NumberTools.separateInCommas(Math.abs(paymentsComputation.getReturnsPayments().get(2).getAmount()))+")", 32)+"\r\n\r\n").getBytes());
                else
                    data.add((ReceiptTools.spacer("LESS Net RGS Amount: ", "("+NumberTools.separateInCommas(Math.abs(paymentsComputation.getReturnsPayments().get(0).getAmount()))+")", 32)+"\r\n\r\n").getBytes());
            }

            data.add((ReceiptTools.spacer("Amount Due: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalPayable(true).doubleValue(), 2)), 32)+"\r\n\r\n").getBytes());

            data.add("PAYMENTS\r\n".getBytes());
            data.add("================================".getBytes());
            data.add("Payments                  Amount".getBytes());
            for(InvoicePayment invoicePayment : invoice.getPayments()) {
                PaymentType paymentType = PaymentType.fetchById(ProductsAdapterHelper.getDbHelper(), PaymentType.class, invoicePayment.getPayment_type_id());
                if(!paymentType.getName().trim().equals("Credit Memo") && !paymentType.getName().trim().equals("RS Slip")) {
                    data.add((ReceiptTools.spacer(paymentType.getName(), DateTimeTools.convertToDate(invoicePayment.getExtras().getPayment_date(), "yyyy-MM-dd", "MMM dd, yyyy")+"       ", 32) + "\r\n").getBytes());
                    data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                    data.add((NumberTools.separateInCommas(invoicePayment.getTender()) + "\r\n").getBytes());

                    items++;

                    if (numberOfPages > 1.0 && page < (int) numberOfPages && items == Configurations.MAX_ITEMS_FOR_PRINTING) {
                        data.add(("\r\n\r\n\r\n").getBytes());
                        data.add(new byte[]{0x1b, 0x1d, 0x61, 0x01}); // Center
                        data.add(("*Page " + page + "*\r\n\r\n").getBytes());
                        data.add(("- - - - - - CUT HERE - - - - - -\r\n\r\n").getBytes());
                        page++;
                        items = 0;

                        if (!StarIOPrinterTools.print(context, StarIOPrinterTools.getTargetPrinter(context), "portable", StarIOPaperSize.p2INCH, data))
                            break;
                        data.clear();
                    }
                }
            }
            data.add((ReceiptTools.spacer("Paid Amount: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalPaymentMade().doubleValue(), 2)), 32)+"\r\n").getBytes());
            data.add("--------------------------------".getBytes());
            if(paymentsComputation.getRemaining().doubleValue() < 0) {
                data.add((ReceiptTools.spacer("Balance: ", "0.00", 32) + "\r\n\r\n").getBytes());
                data.add((ReceiptTools.spacer("Change: ", NumberTools.separateInCommas(Math.abs(NumberTools.formatDouble(paymentsComputation.getRemaining().doubleValue(), 2))), 32) + "\r\n\r\n").getBytes());
            }
            else
                data.add((ReceiptTools.spacer("Balance: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getRemaining().doubleValue(), 2)), 32) + "\r\n\r\n").getBytes());
            SimpleDateFormat nowFormat = new SimpleDateFormat("yyyy-MM-dd");
            data.add(("Available Points("+nowFormat.format(Calendar.getInstance().getTime())+"):\r\n").getBytes());
            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
            data.add((NumberTools.separateInCommas(ProductsAdapterHelper.getSelectedCustomer().getAvailable_points())+"\r\n").getBytes());

            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
            data.add(("\r\n\r\nCustomer Name: "+ProductsAdapterHelper.getSelectedCustomer().generateFullName()+"\r\n").getBytes());
            data.add(("Customer Code: "+ProductsAdapterHelper.getSelectedCustomer().getCode()+"\r\n").getBytes());
            data.add(("Address: "+ProductsAdapterHelper.getSelectedCustomer().generateAddress()+"\r\n").getBytes());
            data.add("Signature:______________________\r\n".getBytes());
            if(ProductsAdapterHelper.getSelectedCustomer().getPaymentTerms() != null)
                data.add(("Terms: "+(ProductsAdapterHelper.getSelectedCustomer().getPaymentTerms().getName() == null
                        ? "None"
                        : ProductsAdapterHelper.getSelectedCustomer().getPaymentTerms().getName())+"\r\n\r\n").getBytes());
            else
                data.add("\r\n".getBytes());

            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center
            data.add(labels[i].getBytes());
            if(isReprint) {
                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center
                data.add("\r\n** This is a reprint **\r\n".getBytes());
            }
            if(i < labels.length-1) {
                data.add("\r\n\r\n\r\n".getBytes());
                data.add("- - - - - - CUT HERE - - - - - -\r\n\r\n".getBytes());
            }
            else
                data.add("\r\n\r\n\r\n".getBytes());

            if(!StarIOPrinterTools.print(context, StarIOPrinterTools.getTargetPrinter(context), "portable", StarIOPaperSize.p2INCH, data))
                break;

            data.clear();
        }
    }
}
