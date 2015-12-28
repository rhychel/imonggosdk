package net.nueca.imonggosdk.enums;

import net.nueca.imonggosdk.objects.ApplicationSettings;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchPrice;
import net.nueca.imonggosdk.objects.BranchTag;
import net.nueca.imonggosdk.objects.DailySales;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.LastUpdatedAt;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.objects.routeplan.RoutePlan;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.Settings;
import net.nueca.imonggosdk.objects.TaxRate;
import net.nueca.imonggosdk.objects.TaxSetting;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.branchentities.BranchProduct;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerCategory;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.document.DocumentPurpose;
import net.nueca.imonggosdk.objects.document.DocumentType;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.objects.invoice.InvoicePurpose;
import net.nueca.imonggosdk.objects.invoice.InvoiceTaxRate;
import net.nueca.imonggosdk.objects.invoice.PaymentTerms;
import net.nueca.imonggosdk.objects.invoice.PaymentType;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.objects.order.OrderLine;
import net.nueca.imonggosdk.objects.price.PriceList;
import net.nueca.imonggosdk.objects.salespromotion.SalesPromotion;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
public enum Table {
    // ----- Main APIs
    TOKENS(API_TYPE.API, "Tokens", null),
    CUSTOMERS(API_TYPE.API, "Customers", Customer.class),
    INVENTORIES(API_TYPE.API, "Inventories", Inventory.class),
    PRODUCTS(API_TYPE.API, "Products", Product.class),
    TAX_SETTINGS(API_TYPE.API, "Tax Settings", TaxSetting.class),
    UNITS(API_TYPE.API, "Units", Unit.class),
    USERS(API_TYPE.API, "Users", User.class),
    USERS_ME(API_TYPE.API, "Users", User.class),
    BRANCHES(API_TYPE.API, "Branches", Branch.class),
    INVOICES(API_TYPE.API, "Invoices", Invoice.class),
    SETTINGS(API_TYPE.API, "Settings", Settings.class),
    APPLICATION_SETTINGS(API_TYPE.API, "Application Settings", ApplicationSettings.class),
    ORDERS(API_TYPE.API, "Orders", Order.class),
    POS_DEVICES(API_TYPE.API, "Pos Devices", null),
    DAILY_SALES(API_TYPE.API, "Daily Sales", DailySales.class),
    DOCUMENTS(API_TYPE.API, "Documents", Document.class),
    DOCUMENT_TYPES(API_TYPE.API, "Document Types", DocumentType.class),
    DOCUMENT_PURPOSES(API_TYPE.API, "Document Purposes", DocumentPurpose.class),

    // ----- API With Branch IDs
    BRANCH_CUSTOMERS(API_TYPE.API, "Customers", Customer.class),
    BRANCH_USERS(API_TYPE.API, "Branches", Branch.class),
    //BRANCH_UNITS(API_TYPE.API, "Units", Unit.class),

    // ----- API with Products
    PRODUCT_TAGS(API_TYPE.NON_API, "Product Tags"),
    PRODUCT_EXTRAS(API_TYPE.NON_API, "Product Extras"),
    PRODUCT_TAX_RATES(API_TYPE.NON_API, "Product Tax Rates"),

    // ----- FOR CONNECTION
    CUSTOMER_CUSTOMER_GROUP(API_TYPE.NON_API, "Customer Customer Groups"),
    PRODUCT_SALES_PROMOTION(API_TYPE.NON_API, "Product Sales Promotion"),
    INVOICE_LINES(API_TYPE.NON_API, "Invoice Lines", InvoiceLine.class),
    INVOICE_TAX_RATES(API_TYPE.NON_API, "Invoice Tax Rates", InvoiceTaxRate.class),
    DOCUMENT_LINE_EXTRAS(API_TYPE.NON_API, "Document Line Extras"),
    PRICES(API_TYPE.NON_API, "Prices", BranchProduct.class),
    BRANCH_PRICES(API_TYPE.NON_API, "Prices", BranchPrice.class),

    // ----- APP Custom Table
    SESSIONS(API_TYPE.NON_API, "Sessions", Session.class),
    BRANCH_TAGS(API_TYPE.NON_API, "Branch Tags", BranchTag.class),
    LAST_UPDATED_AT(API_TYPE.NON_API, "Last Updated At", LastUpdatedAt.class),
    OFFLINEDATA(API_TYPE.NON_API, "Offline Date", OfflineData.class),
    ORDER_LINES(API_TYPE.NON_API, "Order Lines", OrderLine.class),
    TAX_RATES(API_TYPE.NON_API, "Tax Rates", TaxRate.class),
    PAYMENTS(API_TYPE.NON_API, "Payments"),
    DOCUMENT_LINES(API_TYPE.NON_API, "Document Lines", DocumentLine.class),
    EXTENDED_ATTRIBUTES(API_TYPE.NON_API, "Extended Attributes"),
    EXTRAS(API_TYPE.NON_API, "Extras", Extras.class),

    // ----- FOR REBISCO
    CUSTOMER_BY_SALESMAN(API_TYPE.API, "Customers", Customer.class),
    BRANCH_PRODUCTS(API_TYPE.API, "Prices", BranchProduct.class),
    INVOICE_PURPOSES(API_TYPE.API, "Invoice Purposes", InvoicePurpose.class),
    PAYMENT_TERMS(API_TYPE.API, "Payment Terms", PaymentTerms.class),
    CUSTOMER_CATEGORIES(API_TYPE.API, "Customer Categories", CustomerCategory.class),
    PAYMENT_TYPES(API_TYPE.API, "Payment Types", PaymentType.class),
    CUSTOMER_GROUPS(API_TYPE.API, "Customer Groups", CustomerGroup.class),
    PRICE_LISTS(API_TYPE.API, "Price Lists", PriceList.class),
    BRANCH_PRICE_LISTS(API_TYPE.API, "Price Lists", PriceList.class),
    PRICE_LISTS_DETAILS(API_TYPE.API, "Price Lists Details", PriceList.class),
    SALES_PROMOTIONS(API_TYPE.API, "Sales Promotions", SalesPromotion.class),
    SALES_PROMOTIONS_DISCOUNT(API_TYPE.API, "Discount"),
    SALES_PUSH(API_TYPE.API, "Sales Push"),
    ROUTE_PLANS(API_TYPE.API, "Route Plans", RoutePlan.class),
    ROUTE_PLANS_DETAILS(API_TYPE.API, "Route Details", RoutePlan.class);

    private final API_TYPE api_type;
    private final String name;
    private final Class aClass;

    Table(API_TYPE api, String name) {
        this(api, name, null);
    }

    Table(API_TYPE api, String name, Class a) {
        this.api_type = api;
        this.name = name;
        if(a !=null) {
            this.aClass = a;
        } else {
            this.aClass = null;
        }
    }

    public boolean isAPI() {return this.api_type.equals(API_TYPE.API);}
    public boolean isNoNAPI() {return this.api_type.equals(API_TYPE.NON_API);}
    public Class getTableClass() {return aClass;}
    public String getStringName() {
        return this.name;
    }
}
