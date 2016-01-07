package net.nueca.concessioengine.tools;

import android.util.Log;

import net.nueca.concessioengine.lists.SelectedProductItemList;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;
import net.nueca.imonggosdk.tools.NumberTools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 16/10/2015.
 */
public class InvoiceTools {
    public static List<InvoiceLine> generateInvoiceLines(SelectedProductItemList selectedProductItems) {
        return generateInvoiceLines(selectedProductItems, null);
    }

    public static List<InvoiceLine> generateInvoiceLines(SelectedProductItemList selectedProductItems, Customer customer) {
        List<InvoiceLine> invoiceLines = new ArrayList<>();

        for(SelectedProductItem selectedProductItem : selectedProductItems) {
            Product product = selectedProductItem.getProduct();

            for(Values itemValue : selectedProductItem.getValues()) {
                InvoiceLine.Builder builder = new InvoiceLine.Builder();
                builder.product_id(product.getId());

                String discount_text = itemValue.getDiscount_text();
                if(discount_text != null && discount_text.length() != 0 && customer != null &&
                        customer.getDiscount_text() != null && customer.getDiscount_text().length() != 0)
                    discount_text += "," + customer.getDiscount_text();
                builder.discount_text(discount_text);

                builder.quantity(NumberTools.toBigDecimal(itemValue.getQuantity()).intValue());

                double retail_price = itemValue.isValidUnit() ?
                        itemValue.getRetail_price() : product.getRetail_price();
                builder.retail_price(retail_price);

                BigDecimal t_subtotal = new BigDecimal(itemValue.getQuantity())
                        .multiply(new BigDecimal(retail_price));
                BigDecimal subtotal = new BigDecimal(itemValue.getSubtotal());

                if(subtotal.doubleValue() == 0d)
                    builder.subtotal(""+t_subtotal.doubleValue());
                else
                    builder.subtotal(""+itemValue.getSubtotal());

                invoiceLines.add(builder.build());
                Log.e("INVOICE", product.getName() + " " + t_subtotal.doubleValue() + " " + subtotal.doubleValue() + " ~ " + retail_price + " * " +
                        itemValue.getQuantity() + " -- discount " + itemValue.getDiscount_text());
            }
        }
        return invoiceLines;
    }

    public static class PaymentsComputation {
        private List<InvoiceLine> invoiceLines = new ArrayList<>();
        private List<InvoicePayment> payments = new ArrayList<>();

        private BigDecimal total_payable = BigDecimal.ZERO;
        private BigDecimal total_payment_made = BigDecimal.ZERO;

        public void addAllInvoiceLines(List<InvoiceLine> invoiceLines) {
            for(InvoiceLine invoiceLine : invoiceLines)
                addInvoiceLine(invoiceLine);
        }

        public void addInvoiceLine(InvoiceLine invoiceLine) {
            invoiceLines.add(invoiceLine);
            total_payable = total_payable.add(new BigDecimal(invoiceLine.getSubtotal()));
        }

        public void removeInvoiceLine(int location) {
            if(location >= invoiceLines.size())
                return;

            InvoiceLine forDelete = invoiceLines.remove(location);
            total_payable = total_payable.subtract(new BigDecimal(forDelete.getSubtotal()));
        }

        public void addAllPayments(List<InvoicePayment> payments) {
            for(InvoicePayment payment : payments)
                addPayment(payment);
        }

        public void addPayment(InvoicePayment payment) {
            double balance = getRemaining().doubleValue();
            double tender = payment.getTender();

            if(tender <= balance)
                payment.setAmount(tender);
            else    // tender > balance
                payment.setAmount(balance);

            total_payment_made = total_payment_made.add(new BigDecimal(payment.getTender()));

            payments.add(payment);
        }

        public void removePayment(int location) {
            if(location >= payments.size())
                return;

            InvoicePayment forDelete = payments.remove(location);
            total_payment_made = total_payment_made.subtract(new BigDecimal(forDelete.getTender()));

            refresh();
        }

        public void removeIfNotIn(List<InvoiceLine> list) {
            for(int i = 0; i < invoiceLines.size(); i++) {
                InvoiceLine invoiceLine = invoiceLines.get(i);
                if (!list.contains(invoiceLine))
                    removeInvoiceLine(i);
            }
        }

        public void clearPayments() {
            payments = new ArrayList<>();
            total_payment_made = BigDecimal.ZERO;
        }

        public void clearAll() {
            invoiceLines = new ArrayList<>();
            payments = new ArrayList<>();
            total_payable = BigDecimal.ZERO;
            total_payment_made = BigDecimal.ZERO;
        }

        private void refresh() {
            List<InvoicePayment> t_payments = payments;
            clearPayments();
            addAllPayments(t_payments);
        }

        public BigDecimal getTotalPaymentMade() {
            return total_payment_made;
        }

        public BigDecimal getTotalPayable() {
            return total_payable;
        }

        public BigDecimal getRemaining() {
            return total_payable.subtract(total_payment_made);
        }

        public List<InvoicePayment> getPayments() {
            return payments;
        }

        public List<InvoiceLine> getInvoiceLines() {
            return invoiceLines;
        }
    }


}
