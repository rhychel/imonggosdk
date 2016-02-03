package net.nueca.concessioengine.tools;

import android.util.Log;

import net.nueca.concessioengine.lists.SelectedProductItemList;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;
import net.nueca.imonggosdk.objects.invoice.InvoicePurpose;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.tools.NumberTools;

import java.math.BigDecimal;
import java.sql.SQLException;
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
        return generateInvoiceLines(selectedProductItems, 0);
    }

    public static List<InvoiceLine> generateInvoiceLines(SelectedProductItemList selectedProductItems, int line_no_start) {
        List<InvoiceLine> invoiceLines = new ArrayList<>();

        for (SelectedProductItem selectedProductItem : selectedProductItems) {
            Product product = selectedProductItem.getProduct();

            for (Values itemValue : selectedProductItem.getValues()) {
                InvoiceLine.Builder builder = new InvoiceLine.Builder();
                builder.line_no(invoiceLines.size() + line_no_start + 1);
                /** Product ID **/
                builder.product_id(product.getId());

                /** Units **/
                if(itemValue.isValidUnit()) {
                    builder.unit_id(itemValue.getUnit().getId());
                    builder.unit_name(itemValue.getUnit_name());
                    builder.unit_quantity(Double.valueOf(itemValue.getUnit_quantity()));
                    builder.unit_content_quantity(itemValue.getUnit_content_quantity());
                    builder.unit_retail_price(itemValue.getUnit_retail_price());
                } else
                    builder.unit_name(product.getBase_unit_name());

                /** Extras - Discounts **/
                String discount_text = itemValue.getDiscount_text();
                if (itemValue.getCustomer_discount_text() != null && itemValue.getCustomer_discount_text().length() != 0) {
                    if (discount_text != null && discount_text.length() != 0)
                        discount_text += ";" + itemValue.getCustomer_discount_text();
                    else
                        discount_text = itemValue.getCustomer_discount_text();
                }
                if(itemValue.getSubtotal() >= 0d) {
                    builder.discount_text(discount_text);

                    Extras.Builder extrasBuilder = new Extras.Builder();

                    extrasBuilder.product_discount_text(itemValue.getProduct_discount_text())
                            .product_discount_amount(generateDiscountAmount(itemValue.getProduct_discounts(), ','))
                            .company_discount_text(itemValue.getCompany_discount_text())
                            .company_discount_amount(generateDiscountAmount(itemValue.getCompany_discounts(), ','))
                            /*.customer_discount_text(itemValue.getCustomer_discount_text())
                            .customer_discount_amounts(generateDiscountAmount(itemValue.getCustomer_discounts(), ','))*/;

                    Extras extras = extrasBuilder.build();
                    if (extras.getProduct_discount_text() != null ||
                            extras.getCompany_discount_text() != null ||
                            extras.getCustomer_discount_text() != null)
                        builder.extras(extrasBuilder.build());

                /** Extras - BO and RGS **/
                } else {
                    Extras.Builder extrasBuilder = new Extras.Builder();

                    extrasBuilder.is_bad_stock(itemValue.isBadStock());
                    InvoicePurpose invoicePurpose = itemValue.getInvoicePurpose();
                    extrasBuilder.invoice_purpose_id(invoicePurpose.getId());
                    extrasBuilder.invoice_purpose_code(invoicePurpose.getCode());
                    extrasBuilder.invoice_purpose_name(invoicePurpose.getName());
                    extrasBuilder.expiry_date(itemValue.getExpiry_date());

                    Extras extras = extrasBuilder.build();
                    builder.extras(extras);
                }

                /** Quantity **/
                builder.quantity(NumberTools.toBigDecimal(itemValue.getQuantity()).doubleValue());

                /** Retail Price **/
                double retail_price = itemValue.isValidUnit() ?
                        itemValue.getRetail_price() : itemValue.getPrice() != null?
                        itemValue.getRetail_price() : product.getRetail_price();
                builder.retail_price(retail_price);

                /** Subtotal **/
                BigDecimal t_subtotal = new BigDecimal(itemValue.getQuantity())
                        .multiply(new BigDecimal(retail_price));
                BigDecimal subtotal = new BigDecimal(itemValue.getSubtotal());

                if (subtotal.doubleValue() == 0d)
                    builder.subtotal("" + t_subtotal.doubleValue());
                else
                    builder.subtotal("" + itemValue.getSubtotal());

                /** Subtotal No Discount **/
                builder.no_discount_subtotal("" + itemValue.getNoDiscountSubtotal());

                /** Add InvoiceLine **/
                invoiceLines.add(builder.build());
                Log.e("INVOICE", product.getName() + " " + t_subtotal.doubleValue() + " " + subtotal.doubleValue() + " ~ " + retail_price + " * " +
                        itemValue.getQuantity() + " -- discount: " + discount_text + " | discounted price: " +
                        DiscountTools.applyMultipleDiscounts(new BigDecimal(retail_price), NumberTools.toBigDecimal(itemValue.getQuantity()),
                                discount_text, ",").doubleValue());
            }
        }
        return invoiceLines;
    }

    public static SelectedProductItemList generateSelectedProductItemList(ImonggoDBHelper2 helper, OfflineData offlineData,
                                                                          boolean isReturns, boolean isMultiline)
            throws SQLException {
        if(offlineData.getType() != OfflineData.INVOICE)
            throw new ClassCastException("OfflineData object is not of type INVOICE");

        /** Invoice **/
        Invoice invoice = offlineData.getObjectFromData(Invoice.class);
        /** Branch **/
        Branch salesBranch = helper.fetchObjects(Branch.class).queryBuilder().where()
                .eq("id", offlineData.getBranch_id()).queryForFirst();
        /** Customer **/
        Customer salesCustomer = invoice.getCustomer();

        return generateSelectedProductItemList(helper, invoice, salesCustomer, salesBranch, isReturns, isMultiline);
    }

    public static SelectedProductItemList generateSelectedProductItemList(ImonggoDBHelper2 helper, Invoice invoice,
                                          Customer newSalesCustomer, Branch newSalesBranch, boolean isReturns, boolean isMultiline)
            throws SQLException {
        Log.e("InvoiceTools", "generateSelectedProductItemList : starting");

        SelectedProductItemList selectedProductItemList = new SelectedProductItemList();
        selectedProductItemList.setReturns(isReturns);

        /** CustomerGroup **/
        List<CustomerGroup> customerGroups = newSalesCustomer.getCustomerGroups(helper);
        CustomerGroup salesCustomerGroup = null;
        if(customerGroups != null)
            salesCustomerGroup = customerGroups.get(0);

        for(InvoiceLine invoiceLine : invoice.getInvoiceLines()) {
            Log.e("InvoiceLine >> ", invoiceLine.getQuantity() +"");
            if(isReturns && invoiceLine.getQuantity() >= 0d)
                continue;
            else if(!isReturns && invoiceLine.getQuantity() < 0d)
                continue;

            /** Product **/
            Product product = helper.fetchObjects(Product.class).queryBuilder().where()
                    .eq("id", invoiceLine.getProduct_id()).queryForFirst();

            /** Unit **/
            Unit unit = null;
            if(invoiceLine.getUnit_id() != null)
                unit = helper.fetchObjects(Unit.class).queryBuilder().where()
                        .eq("id", invoiceLine.getUnit_id()).queryForFirst();

            SelectedProductItem selectedProductItem = selectedProductItemList.getSelectedProductItem(product);
            if(selectedProductItem == null)
                selectedProductItem = selectedProductItemList.initializeItem(product);

            /** Multiline **/
            selectedProductItem.setIsMultiline(isMultiline);

            Values values = new Values();

            /** Price **/
            Price price = PriceTools.identifyPrice(helper, product, newSalesBranch, salesCustomerGroup, newSalesCustomer, unit);
            Double branchPrice = PriceTools.getBranchPrice(helper,product,newSalesBranch,unit);
            if(price != null)
                values.setValue("" + invoiceLine.getQuantity(), price);
            else
                values.setValue("" + invoiceLine.getQuantity(), unit, branchPrice);

            selectedProductItem.addValues(values);
            selectedProductItemList.add(selectedProductItem);
            Log.e("InvoiceTools", " >> " + product.getId());
        }

        Log.e("InvoiceTools", "generateSelectedProductItemList : end");
        return selectedProductItemList;
    }

    public static String generateDiscountAmount(List<Double> discountAmounts, char delimiter) {
        if(discountAmounts == null)
            return null;
        String str = "";
        for (Double discount : discountAmounts) {
            str += String.valueOf(discount);
            if(discountAmounts.indexOf(discount) < discountAmounts.size())
                str += delimiter;
        }
        if(str.length() == 0)
            return null;
        return str;
    }
    private static List<Double> parseDiscountAmount(String discountAmounts, char delimiter) {
        String[] discountStr = discountAmounts.split(""+delimiter);
        List<Double> discounts = new ArrayList<>();
        for(String str : discountStr) {
            discounts.add(Double.valueOf(str));
        }
        return discounts;
    }

    private static final int PRODUCT_DISCOUNT = 0,
            COMPANY_DISCOUNT = 1,
            CUSTOMER_DISCOUNT = 2,
            NO_DISCOUNT_SUBTOTAL = 3,
            SUBTOTAL = 4;
    private static List<Double> addAllField(List<InvoiceLine> invoiceLines, int field) {
        if(invoiceLines == null || invoiceLines.size() == 0)
            return null;
        List<Double> discounts = null;
        switch (field) {
            case PRODUCT_DISCOUNT:
                for (InvoiceLine invoiceLine : invoiceLines) {
                    if(invoiceLine.getExtras() == null || invoiceLine.getExtras().getProduct_discount_amount() == null)
                        continue;
                    if(discounts == null)
                        discounts = new ArrayList<>();
                    double itemDiscount = sum(parseDiscountAmount(invoiceLine.getExtras().getProduct_discount_amount(), ','));
                    discounts.add(itemDiscount);
                }
                break;
            case COMPANY_DISCOUNT:
                for (InvoiceLine invoiceLine : invoiceLines) {
                    if(invoiceLine.getExtras() == null || invoiceLine.getExtras().getCompany_discount_amount() == null)
                        continue;
                    if(discounts == null)
                        discounts = new ArrayList<>();
                    double itemDiscount = sum(parseDiscountAmount(invoiceLine.getExtras().getCompany_discount_amount(), ','));
                    discounts.add(itemDiscount);
                }
                break;
            case CUSTOMER_DISCOUNT:
                for (InvoiceLine invoiceLine : invoiceLines) {
                    if(invoiceLine.getExtras() == null || invoiceLine.getExtras().getCustomer_discount_amounts() == null)
                        continue;
                    if(discounts == null)
                        discounts = parseDiscountAmount(invoiceLine.getExtras().getCustomer_discount_amounts(), ',');
                    else {
                        List<Double> forAdding = parseDiscountAmount(invoiceLine.getExtras().getCustomer_discount_amounts(), ',');
                        for(int i=0; i < forAdding.size(); i++) {
                            if(discounts.size() <= i)
                                discounts.add(forAdding.get(i));
                            else
                                discounts.set(i, discounts.get(i)+forAdding.get(i));
                        }
                    }
                }
                break;
            case NO_DISCOUNT_SUBTOTAL:
                for (InvoiceLine invoiceLine : invoiceLines) {
                    if(invoiceLine.getNo_discount_subtotal() == null)
                        continue;
                    if(discounts == null)
                        discounts = new ArrayList<>();
                    discounts.add(Double.valueOf(invoiceLine.getNo_discount_subtotal()));
                }
                break;
            case SUBTOTAL:
                for (InvoiceLine invoiceLine : invoiceLines) {
                    if(invoiceLine.getSubtotal() == null)
                        continue;
                    if(discounts == null)
                        discounts = new ArrayList<>();
                    discounts.add(Double.valueOf(invoiceLine.getSubtotal()));
                }
                break;
        }
        return discounts;
    }

    public static Double sum(List<Double> doubles) {
        if(doubles == null)
            return 0d;
        double sum = 0d;
        for(Double d : doubles) {
            sum += d;
        }
        return sum;
    }

    public static List<Double> getAllProductDiscount(List<InvoiceLine> invoiceLines) {
        return addAllField(invoiceLines, PRODUCT_DISCOUNT);
    }
    public static List<Double> getAllCompanyDiscount(List<InvoiceLine> invoiceLines) {
        return addAllField(invoiceLines, COMPANY_DISCOUNT);
    }
    public static List<Double> consolidateCustomerDiscount(List<InvoiceLine> invoiceLines) {
        return addAllField(invoiceLines, CUSTOMER_DISCOUNT);
    }
    public static Double addNoDiscountSubtotals(List<InvoiceLine> invoiceLines) {
        return sum(addAllField(invoiceLines, NO_DISCOUNT_SUBTOTAL));
    }
    public static Double addSubtotals(List<InvoiceLine> invoiceLines) {
        return sum(addAllField(invoiceLines, SUBTOTAL));
    }
    /*@Nullable
    public static Location getLastKnownLocation(Activity activity) {
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
