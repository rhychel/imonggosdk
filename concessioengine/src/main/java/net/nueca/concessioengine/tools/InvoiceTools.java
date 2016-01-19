package net.nueca.concessioengine.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import net.nueca.concessioengine.lists.SelectedProductItemList;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.base.Extras;
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
    /*public static List<InvoiceLine> generateInvoiceLines(SelectedProductItemList selectedProductItems) {
        return generateInvoiceLines(selectedProductItems, (String) null);
    }

    public static List<InvoiceLine> generateInvoiceLines(SelectedProductItemList selectedProductItems, Customer customer) {
        if (customer != null)
            return generateInvoiceLines(selectedProductItems, customer.getDiscount_text());
        return generateInvoiceLines(selectedProductItems);
    }*/

    public static List<InvoiceLine> generateInvoiceLines(SelectedProductItemList selectedProductItems) {
        List<InvoiceLine> invoiceLines = new ArrayList<>();

        for (SelectedProductItem selectedProductItem : selectedProductItems) {
            Product product = selectedProductItem.getProduct();

            for (Values itemValue : selectedProductItem.getValues()) {
                InvoiceLine.Builder builder = new InvoiceLine.Builder();
                builder.product_id(product.getId());

                String discount_text = itemValue.getDiscount_text();
                if (itemValue.getCustomer_discount_text() != null && itemValue.getCustomer_discount_text().length() != 0) {
                    if (discount_text != null && discount_text.length() != 0)
                        discount_text += ";" + itemValue.getCustomer_discount_text();
                    else
                        discount_text = itemValue.getCustomer_discount_text();
                }
                builder.discount_text(discount_text);

                Extras extras = new Extras.Builder()
                        .product_discount_text(itemValue.getProduct_discount_text())
                        .product_discount_amount(generateDiscountAmount(itemValue.getProduct_discounts(),','))
                        .company_discount_text(itemValue.getCompany_discount_text())
                        .company_discount_amount(generateDiscountAmount(itemValue.getCompany_discounts(),','))
                        .customer_discount_text(itemValue.getCustomer_discount_text())
                        .customer_discount_amounts(generateDiscountAmount(itemValue.getCustomer_discounts(),','))
                        .build();
                builder.extras(extras);

                builder.quantity(NumberTools.toBigDecimal(itemValue.getQuantity()).doubleValue());

                double retail_price = itemValue.isValidUnit() ?
                        itemValue.getRetail_price() : product.getRetail_price();
                builder.retail_price(retail_price);

                BigDecimal t_subtotal = new BigDecimal(itemValue.getQuantity())
                        .multiply(new BigDecimal(retail_price));
                BigDecimal subtotal = new BigDecimal(itemValue.getSubtotal());

                if (subtotal.doubleValue() == 0d)
                    builder.subtotal("" + t_subtotal.doubleValue());
                else
                    builder.subtotal("" + itemValue.getSubtotal());

                invoiceLines.add(builder.build());
                Log.e("INVOICE", product.getName() + " " + t_subtotal.doubleValue() + " " + subtotal.doubleValue() + " ~ " + retail_price + " * " +
                        itemValue.getQuantity() + " -- discount: " + discount_text + " | discounted price: " +
                        DiscountTools.applyMultipleDiscounts(new BigDecimal(retail_price), NumberTools.toBigDecimal(itemValue.getQuantity()),
                                discount_text, ",").doubleValue());
            }
        }
        return invoiceLines;
    }

    private static String generateDiscountAmount(List<Double> discountAmounts, char delimiter) {
        String str = "";
        for (Double discount : discountAmounts) {
            str += String.valueOf(discount);
            if(discountAmounts.indexOf(discount) < discountAmounts.size())
              str += delimiter;
        }
        return str;
    }

    /*@Nullable
    public static Location getLocation(Activity activity) {
        String networkProvider = LocationManager.NETWORK_PROVIDER;
        String gpsProvider = LocationManager.GPS_PROVIDER;

        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        Location location = locationManager.getLastKnownLocation(gpsProvider);
        if(location == null)
            location = locationManager.getLastKnownLocation(networkProvider);

        return location;
    }*/

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
