package net.nueca.concessioengine.tools;

import android.util.Log;

import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
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
import net.nueca.imonggosdk.objects.invoice.PaymentType;
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
                            .product_discount_amount(generateDiscountAmount(itemValue.getProduct_discounts(), ',',
                                    ProductsAdapterHelper.getDecimalPlace()))
                            .company_discount_text(itemValue.getCompany_discount_text())
                            .company_discount_amount(generateDiscountAmount(itemValue.getCompany_discounts(), ',',
                                    ProductsAdapterHelper.getDecimalPlace()))
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
                    if(invoicePurpose != null) {
                        extrasBuilder.invoice_purpose_id(invoicePurpose.getId());
                        extrasBuilder.invoice_purpose_code(invoicePurpose.getCode());
                        extrasBuilder.invoice_purpose_name(invoicePurpose.getName());
                    }
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

        SelectedProductItemList selectedProductItemList = new SelectedProductItemList();
        selectedProductItemList.setReturns(isReturns);

        /** CustomerGroup **/
        List<CustomerGroup> customerGroups = newSalesCustomer.getCustomerGroups(helper);
        CustomerGroup salesCustomerGroup = null;
        if(customerGroups != null && customerGroups.size() > 0)
            salesCustomerGroup = customerGroups.get(0);

        for(InvoiceLine invoiceLine : invoice.getInvoiceLines()) {
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
        }

        return selectedProductItemList;
    }

    public static String generateDiscountAmount(List<Double> discountAmounts, char delimiter, int decimalPlaces) {
        if(discountAmounts == null)
            return null;
        String str = "";
        for (Double discount : discountAmounts) {
            str += String.valueOf(NumberTools.formatDouble(discount, decimalPlaces));
            if(discountAmounts.indexOf(discount) < discountAmounts.size()-1)
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
        private Customer selectedCustomer = null;
        private int decimalPlace = 2;

        private List<InvoiceLine> invoiceLines = new ArrayList<>();
        private List<InvoicePayment> payments = new ArrayList<>();

        private List<Double> customerDiscount = new ArrayList<>();
        private List<Double> companyDiscount = new ArrayList<>();
        private List<Double> productDiscount = new ArrayList<>();

        private BigDecimal totalCustomerDiscount = BigDecimal.ZERO;
        private BigDecimal totalCompanyDiscount = BigDecimal.ZERO;
        private BigDecimal totalProductDiscount = BigDecimal.ZERO;

        private BigDecimal totalPayableNoDiscount = BigDecimal.ZERO;

        private BigDecimal totalPayable = BigDecimal.ZERO;
        private BigDecimal totalPaymentMade = BigDecimal.ZERO;

        private PaymentType rsSlip, creditMemo, points;
        private BigDecimal totalReturnsPayment = BigDecimal.ZERO;
        private List<InvoicePayment> cmPayments = new ArrayList<>();
        private List<InvoicePayment> rsPayments = new ArrayList<>();
        private BigDecimal totalPointsPayment = BigDecimal.ZERO;

        public PaymentsComputation() {
            selectedCustomer = ProductsAdapterHelper.getSelectedCustomer();
            decimalPlace = ProductsAdapterHelper.getDecimalPlace();
            try {
                findPaymentType(ProductsAdapterHelper.getDbHelper());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void addAllInvoiceLines(List<InvoiceLine> invoiceLines) {
            if(invoiceLines == null)
                return;
            for(InvoiceLine invoiceLine : invoiceLines)
                addInvoiceLine(invoiceLine);
        }

        public void removeAllInvoiceLines() {
            for(int i = 0 ; i < invoiceLines.size(); i++)
                removeInvoiceLine(i);
        }

        public void addInvoiceLine(InvoiceLine invoiceLine) {
            invoiceLines.add(invoiceLine);

            if(rsSlip == null && creditMemo == null) {
                totalPayable = totalPayable.add(new BigDecimal(invoiceLine.getSubtotal()));
                totalPayableNoDiscount = totalPayableNoDiscount.add(new BigDecimal(invoiceLine.getNo_discount_subtotal()));
            }
            else {
                Double subtotal = Double.parseDouble(invoiceLine.getSubtotal());
                Double noDiscount = Double.parseDouble(invoiceLine.getNo_discount_subtotal());

                if(subtotal >= 0d || invoiceLine.getExtras() == null) {
                    rsPayments.add(null);
                    cmPayments.add(null);
                    totalPayable = totalPayable.add(new BigDecimal(subtotal));
                    totalPayableNoDiscount = totalPayableNoDiscount.add(new BigDecimal(noDiscount));
                } else {
                    InvoicePayment.Builder builder = new InvoicePayment.Builder();
                    //builder.amount(Math.abs(subtotal));
                    builder.amount(subtotal);
                    if(invoiceLine.getExtras().getIs_bad_stock()) {
                        if(creditMemo != null) {
                            builder.paymentType(creditMemo);
                            rsPayments.add(null);
                            cmPayments.add(builder.build());
                            //totalPayable = totalPayable.add(new BigDecimal(Math.abs(subtotal)));
                            //totalPayableNoDiscount = totalPayableNoDiscount.add(new BigDecimal(Math.abs(noDiscount)));
                        } else {
                            rsPayments.add(null);
                            cmPayments.add(null);
                            totalPayable = totalPayable.add(new BigDecimal(subtotal));
                            totalPayableNoDiscount = totalPayableNoDiscount.add(new BigDecimal(noDiscount));
                        }
                    } else {
                        if(rsSlip != null) {
                            builder.paymentType(rsSlip);
                            rsPayments.add(builder.build());
                            cmPayments.add(null);
                            //totalPayable = totalPayable.add(new BigDecimal(Math.abs(subtotal)));
                            //totalPayableNoDiscount = totalPayableNoDiscount.add(new BigDecimal(Math.abs(noDiscount)));
                        } else {
                            rsPayments.add(null);
                            cmPayments.add(null);
                            totalPayable = totalPayable.add(new BigDecimal(subtotal));
                            totalPayableNoDiscount = totalPayableNoDiscount.add(new BigDecimal(noDiscount));
                        }
                    }

                    totalReturnsPayment = totalReturnsPayment.add(new BigDecimal(Math.abs(subtotal)));
                }
            }

            if(selectedCustomer != null) {
                customerDiscount = new ArrayList<>();
                totalCustomerDiscount = totalPayable.subtract(DiscountTools.applyMultipleDiscounts(totalPayable, BigDecimal.ONE, customerDiscount,
                        selectedCustomer.getDiscount_text(), ","));
            }

            addDiscounts(invoiceLine);
            trimValues();
        }

        public void removeInvoiceLine(int location) {
            if(location >= invoiceLines.size())
                return;

            InvoiceLine forDelete = invoiceLines.remove(location);

            if(rsSlip == null && creditMemo == null) {
                totalPayable = totalPayable.subtract(new BigDecimal(forDelete.getSubtotal()));
                totalPayableNoDiscount = totalPayableNoDiscount.subtract(new BigDecimal(forDelete.getNo_discount_subtotal()));
            }
            else {
                Double subtotal = Double.parseDouble(forDelete.getSubtotal());
                Double noDiscount = Double.parseDouble(forDelete.getNo_discount_subtotal());

                if(rsPayments.get(location) == null && cmPayments.get(location) == null) {
                    totalPayable = totalPayable.subtract(new BigDecimal(subtotal));
                    totalPayableNoDiscount = totalPayableNoDiscount.subtract(new BigDecimal(noDiscount));
                } else {
                    //totalPayable = totalPayable.subtract(new BigDecimal(Math.abs(subtotal)));
                    //totalPayableNoDiscount = totalPayableNoDiscount.subtract(new BigDecimal(Math.abs(noDiscount)));
                }

                rsPayments.remove(location);
                cmPayments.remove(location);

                totalReturnsPayment = totalReturnsPayment.subtract(new BigDecimal(Math.abs(subtotal)));
            }

            if(selectedCustomer != null) {
                customerDiscount = new ArrayList<>();
                totalCustomerDiscount = totalPayable.subtract(DiscountTools.applyMultipleDiscounts(totalPayable, BigDecimal.ONE, customerDiscount,
                        selectedCustomer.getDiscount_text(), ","));
            }

            removeDiscounts(location);
            trimValues();
        }

        private void addDiscounts(InvoiceLine invoiceLine) {
            if(invoiceLine.getExtras() == null)
                return;
            double itemDiscount;

            if(invoiceLine.getExtras().getProduct_discount_amount() != null) {
                itemDiscount = sum(parseDiscountAmount(invoiceLine.getExtras().getProduct_discount_amount(), ','));
                totalProductDiscount = totalProductDiscount.add(new BigDecimal(itemDiscount));
                productDiscount.add(itemDiscount);
            } else {
                productDiscount.add(0d);
            }

            if(invoiceLine.getExtras().getCompany_discount_amount() != null) {
                itemDiscount = sum(parseDiscountAmount(invoiceLine.getExtras().getCompany_discount_amount(), ','));
                totalCompanyDiscount = totalCompanyDiscount.add(new BigDecimal(itemDiscount));
                companyDiscount.add(itemDiscount);
            } else {
                companyDiscount.add(0d);
            }

            /*if(invoiceLine.getExtras().getCustomer_discount_amounts() != null) {
                itemDiscount = sum(parseDiscountAmount(invoiceLine.getExtras().getCustomer_discount_amounts(), ','));
                totalCustomerDiscount = totalCustomerDiscount.add(new BigDecimal(itemDiscount));
                customerDiscount.add(itemDiscount);
            } else {
                customerDiscount.add(0d);
            }*/
        }

        private void removeDiscounts(int position) {
            double itemDiscount;

            itemDiscount = productDiscount.remove(position);
            totalProductDiscount = totalProductDiscount.subtract(new BigDecimal(itemDiscount));

            itemDiscount = companyDiscount.remove(position);
            totalCompanyDiscount = totalCompanyDiscount.subtract(new BigDecimal(itemDiscount));

            /*itemDiscount = customerDiscount.remove(position);
            totalCustomerDiscount = totalCustomerDiscount.subtract(new BigDecimal(itemDiscount));*/
        }

        public void addAllPayments(List<InvoicePayment> payments) {
            if(payments == null)
                return;
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

            if(rsSlip == null && creditMemo == null) {
                totalPaymentMade = totalPaymentMade.add(new BigDecimal(payment.getTender()));
                payments.add(payment);
            }
            else {
                if (creditMemo != null && payment.getPayment_type_id() == creditMemo.getId()) {
                    //totalReturnsPayment = totalReturnsPayment.add(new BigDecimal(payment.getTender()).abs());
                    //cmPayments.add(payment);
                }
                else if (rsSlip != null && payment.getPayment_type_id() == rsSlip.getId()) {
                    //totalReturnsPayment = totalReturnsPayment.add(new BigDecimal(payment.getTender()).abs());
                    //rsPayments.add(payment);
                }
                else {
                    totalPaymentMade = totalPaymentMade.add(new BigDecimal(payment.getTender()));
                    payments.add(payment);
                }
            }

            if(points != null && payment.getPayment_type_id() == points.getId())
                totalPointsPayment = totalPointsPayment.add(new BigDecimal(payment.getTender()));

            trimValues();
        }

        public void removePayment(int location) {
            if(location >= payments.size())
                return;

            InvoicePayment forDelete = payments.remove(location);
            totalPaymentMade = totalPaymentMade.subtract(new BigDecimal(forDelete.getTender()));

            if(points != null && forDelete.getPayment_type_id() == points.getId())
                totalPointsPayment = totalPointsPayment.add(new BigDecimal(forDelete.getTender()));

            refresh();
            trimValues();
        }

        public InvoicePayment getPayment(int location) {
            if(location >= payments.size())
                return null;

            return payments.get(location);
        }

        public void setPayment(int location, InvoicePayment payment) {
            if(location >= payments.size()) {
                addPayment(payment);
                return;
            }
            payments.set(location, payment);

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
            totalPaymentMade = BigDecimal.ZERO;
            totalPointsPayment = BigDecimal.ZERO;
        }

        public void clearAll() {
            invoiceLines = new ArrayList<>();
            payments = new ArrayList<>();
            totalPayable = BigDecimal.ZERO;
            totalPaymentMade = BigDecimal.ZERO;

            customerDiscount = new ArrayList<>();
            companyDiscount = new ArrayList<>();
            productDiscount = new ArrayList<>();
            totalCustomerDiscount = BigDecimal.ZERO;
            totalCompanyDiscount = BigDecimal.ZERO;
            totalProductDiscount = BigDecimal.ZERO;

            totalPayableNoDiscount = BigDecimal.ZERO;

            cmPayments = new ArrayList<>();
            rsPayments = new ArrayList<>();
            totalReturnsPayment = BigDecimal.ZERO;

            totalPointsPayment = BigDecimal.ZERO;
        }

        private void refresh() {
            List<InvoicePayment> t_payments = payments;
            clearPayments();
            addAllPayments(t_payments);
        }

        public List<InvoicePayment> getReturnsPayments() {
            InvoicePayment cmPayment = null, rspPayment = null;
            if(creditMemo != null && cmPayments != null && cmPayments.size() != 0) {
                cmPayment = new InvoicePayment.Builder()
                    .payment_type_id(creditMemo.getId()).build();
                Double total = 0d;
                for(InvoicePayment payment : cmPayments)
                    if(payment != null)
                        total += payment.getAmount();
                cmPayment.setAmount(total);
                cmPayment.setTender(total);
                if(total == 0d)
                    cmPayment = null;
            }
            if(rsSlip != null && rsPayments != null && rsPayments.size() != 0) {
                rspPayment = new InvoicePayment.Builder()
                        .payment_type_id(rsSlip.getId()).build();
                Double total = 0d;
                for(InvoicePayment payment : rsPayments)
                    if(payment != null)
                        total += payment.getAmount();
                rspPayment.setAmount(total);
                rspPayment.setTender(total);
                if(total == 0d)
                    rspPayment = null;
            }

            List<InvoicePayment> returnsPayments = new ArrayList<>();
            if(cmPayment != null)
                returnsPayments.add(cmPayment);
            if(rspPayment != null)
                returnsPayments.add(rspPayment);
            Log.e("RETURNS PAYMENTS", "size "+(returnsPayments.size()));
            return returnsPayments;
        }

        public BigDecimal getTotalPaymentMade() {
            return totalPaymentMade;
        }

        public BigDecimal getTotalPayableNoReturns(boolean applyCustomerDiscount) {
            if(applyCustomerDiscount)
                return totalPayable.subtract(totalCustomerDiscount);
            return totalPayable;
        }

        public BigDecimal getTotalPayable(boolean applyCustomerDiscount) {
            if(applyCustomerDiscount)
                return totalPayable.subtract(totalReturnsPayment).subtract(totalCustomerDiscount);
            return totalPayable.subtract(totalReturnsPayment);
        }

        public BigDecimal getRemaining() {
            return getTotalPayable(true).subtract(totalPaymentMade);
        }

        public List<InvoicePayment> getPayments() {
            return payments;
        }

        public List<InvoiceLine> getInvoiceLines() {
            return invoiceLines;
        }

        public List<Double> getCustomerDiscount() {
            return customerDiscount;
        }

        public List<Double> getCompanyDiscount() {
            return companyDiscount;
        }

        public List<Double> getProductDiscount() {
            return productDiscount;
        }

        public BigDecimal getTotalCustomerDiscount() {
            return totalCustomerDiscount;
        }

        public BigDecimal getTotalCompanyDiscount() {
            return totalCompanyDiscount;
        }

        public BigDecimal getTotalProductDiscount() {
            return totalProductDiscount;
        }

        public BigDecimal getTotalPayableNoDiscount() {
            return totalPayableNoDiscount;
        }

        public BigDecimal getTotalPointsPayment() {
            return totalPointsPayment;
        }

        public void setRsSlip(PaymentType rsSlip) {
            this.rsSlip = rsSlip;
        }

        public void setCreditMemo(PaymentType creditMemo) {
            this.creditMemo = creditMemo;
        }

        public void setPoints(PaymentType points) {
            this.points = points;
        }

        public PaymentType getPoints() {
            return points;
        }

        public PaymentType getCreditMemo() {
            return creditMemo;
        }

        public PaymentType getRsSlip() {
            return rsSlip;
        }

        public int getDecimalPlace() {
            return decimalPlace;
        }

        private void findPaymentType(ImonggoDBHelper2 helper) throws SQLException {
            //if(helper != null) {
                Where<PaymentType, ?> where = helper.fetchObjects(PaymentType.class).queryBuilder().where();
                List<PaymentType> cm = helper.fetchObjects(PaymentType.class).query(where.like("name", "credit memo").prepare());
                if(cm != null && cm.size() != 0)
                    creditMemo = cm.get(0);

                where = helper.fetchObjects(PaymentType.class).queryBuilder().where();
                List<PaymentType> rs = helper.fetchObjects(PaymentType.class).query(where.like("name", "rs slip").prepare());
                if(rs != null && rs.size() != 0)
                    rsSlip = rs.get(0);

                where = helper.fetchObjects(PaymentType.class).queryBuilder().where();
                List<PaymentType> p = helper.fetchObjects(PaymentType.class).query(where.like("name", "%point%").prepare());
                if(p != null && p.size() != 0)
                    points = p.get(0);
            //}
        }

        public void trimValues() {
            totalCustomerDiscount = new BigDecimal(NumberTools.formatDouble(totalCustomerDiscount.doubleValue(), decimalPlace));
            totalCompanyDiscount = new BigDecimal(NumberTools.formatDouble(totalCompanyDiscount.doubleValue(), decimalPlace));
            totalProductDiscount = new BigDecimal(NumberTools.formatDouble(totalProductDiscount.doubleValue(), decimalPlace));

            totalPayableNoDiscount = new BigDecimal(NumberTools.formatDouble(totalPayableNoDiscount.doubleValue(), decimalPlace));

            totalPayable = new BigDecimal(NumberTools.formatDouble(totalPayable.doubleValue(), decimalPlace));
            totalPaymentMade = new BigDecimal(NumberTools.formatDouble(totalPaymentMade.doubleValue(), decimalPlace));

            totalReturnsPayment = new BigDecimal(NumberTools.formatDouble(totalReturnsPayment.doubleValue(), decimalPlace));

            totalPointsPayment = new BigDecimal(NumberTools.formatDouble(totalPointsPayment.doubleValue(), decimalPlace));
        }
    }


}
