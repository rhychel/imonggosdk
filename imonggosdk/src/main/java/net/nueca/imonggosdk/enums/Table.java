package net.nueca.imonggosdk.enums;

import android.util.Log;

import net.nueca.imonggosdk.objects.AccountPrice;
import net.nueca.imonggosdk.objects.ApplicationSettings;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchPrice;
import net.nueca.imonggosdk.objects.BranchTag;
import net.nueca.imonggosdk.objects.DailySales;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.LastUpdatedAt;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.branchentities.BranchUnit;
import net.nueca.imonggosdk.objects.invoice.Discount;
import net.nueca.imonggosdk.objects.price.Price;
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
import net.nueca.imonggosdk.objects.routeplan.RoutePlanDetail;
import net.nueca.imonggosdk.objects.salespromotion.SalesPromotion;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
public enum Table {
    // ----- Main APIs
    TOKENS(API_TYPE.API, "Tokens", null),
    CUSTOMERS(API_TYPE.API, "Customers", Customer.class, "customers"),
    INVENTORIES(API_TYPE.API, "Inventories", Inventory.class, "inventories"),
    PRODUCTS(API_TYPE.API, "Products", Product.class, "products"),
    TAX_SETTINGS(API_TYPE.API, "Tax Settings", TaxSetting.class, "tax_settings"),
    UNITS(API_TYPE.API, "Units", Unit.class, "units"),
    ACCOUNT_PRICES(API_TYPE.API, "Account Prices", AccountPrice.class, "account_prices"),
    USERS(API_TYPE.API, "Users", User.class, "users"),
    USERS_ME(API_TYPE.API, "Users", User.class, "users_me"),
    BRANCHES(API_TYPE.API, "Branches", Branch.class, "branches"),
    INVOICES(API_TYPE.API, "Invoices", Invoice.class, "invoices"),
    SETTINGS(API_TYPE.API, "Settings", Settings.class, "settings"),
    APPLICATION_SETTINGS(API_TYPE.API, "Application Settings", ApplicationSettings.class, "application_settings"),
    ORDERS(API_TYPE.API, "Orders", Order.class, "orders"),
    ORDERS_PURCHASES(API_TYPE.API, "Purchase Orders", Order.class, "orders_purchase_orders"),
    ORDERS_STOCK_REQUEST(API_TYPE.API, "Stock Requests", Order.class, "orders_stock_requests"),
    POS_DEVICES(API_TYPE.API, "Pos Devices", null),
    DAILY_SALES(API_TYPE.API, "Daily Sales", DailySales.class, "daily_sales"),
    DOCUMENTS(API_TYPE.API, "Documents", Document.class, "documents"),
    DOCUMENT_TYPES(API_TYPE.API, "Document Types", DocumentType.class, "document_types"),
    DOCUMENT_PURPOSES(API_TYPE.API, "Document Purposes", DocumentPurpose.class, "document_purposes"),

    // ----- API With Branch IDs
    BRANCH_CUSTOMERS(API_TYPE.API, "Customers", Customer.class, "branch_customers"),
    BRANCH_USERS(API_TYPE.API, "Branches", Branch.class, "branch_users"),
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
    BRANCH_UNIT(API_TYPE.NON_API, "Branch Unit", BranchUnit.class),

    // ----- FOR REBISCO
    CUSTOMER_BY_SALESMAN(API_TYPE.API, "Customers", Customer.class, "customer_by_salesman"),
    BRANCH_PRODUCTS(API_TYPE.API, "Branch Prices", BranchProduct.class, "branch_products"),
    INVOICE_PURPOSES(API_TYPE.API, "Invoice Purposes", InvoicePurpose.class, "invoice_purposes"),
    PAYMENT_TERMS(API_TYPE.API, "Payment Terms", PaymentTerms.class, "payment_terms"),
    CUSTOMER_CATEGORIES(API_TYPE.API, "Customer Categories", CustomerCategory.class, "customer_categories"),
    PAYMENT_TYPES(API_TYPE.API, "Payment Types", PaymentType.class, "payment_types"),
    CUSTOMER_GROUPS(API_TYPE.API, "Customer Groups", CustomerGroup.class, "customer_groups"),
    PRICE_LISTS_FROM_CUSTOMERS(API_TYPE.API, "Price Lists", PriceList.class, "price_lists_from_customers"),
    PRICE_LISTS(API_TYPE.API, "Price Lists", PriceList.class, "price_lists"),
    BRANCH_PRICE_LISTS(API_TYPE.API, "Price Lists", PriceList.class, "branch_price_lists"),
    PRICE_LISTS_DETAILS(API_TYPE.API, "Price Lists Details", Price.class, "price_lists_details"),
    SALES_PROMOTIONS(API_TYPE.API, "Sales Promotions", SalesPromotion.class, "sales_promotions"),
    SALES_PROMOTIONS_SALES_PUSH(API_TYPE.API, "Sales Push", SalesPromotion.class, "sales_promotions_sales_push"),
    SALES_PROMOTIONS_POINTS(API_TYPE.API, "Points", SalesPromotion.class, "sales_promotions_points"),
    SALES_PROMOTIONS_POINTS_DETAILS(API_TYPE.API, "Points Details", Discount.class, "sales_promotions_points_details"),
    SALES_PROMOTIONS_SALES_DISCOUNT(API_TYPE.API, "Sales Discount", SalesPromotion.class, "sales_promotions_sales_discount"),
    SALES_PROMOTIONS_SALES_DISCOUNT_DETAILS(API_TYPE.API, "Discount Details", Discount.class, "sales_promotions_sales_discount_details"),
    ROUTE_PLANS(API_TYPE.API, "Route Plans", RoutePlan.class, "route_plans"),
    ROUTE_PLANS_DETAILS(API_TYPE.API, "Route Details", RoutePlanDetail.class, "route_plans_details"),

    NONE(API_TYPE.NON_API, "none"),
    ALL(API_TYPE.NON_API, "All");

    private final API_TYPE api_type;
    private final String name;
    private final Class aClass;
    private final String tableKey;
    private Table[] prerequisites;


    Table(API_TYPE api, String name) {
        this(api, name, null);
    }

    Table(API_TYPE api, String name, Class a) {
        this(api, name, a, null);
    }

    Table(API_TYPE api, String name, Class a, String tableKey) {
        this.api_type = api;
        this.name = name;
        if(a !=null) {
            this.aClass = a;
        } else {
            this.aClass = null;
        }
        this.tableKey = tableKey;
    }

    public boolean isAPI() {return this.api_type.equals(API_TYPE.API);}
    public boolean isNoNAPI() {return this.api_type.equals(API_TYPE.NON_API);}
    public Class getTableClass() {return aClass;}
    public String getStringName() {
        return this.name;
    }
    public String getTableKey() {return tableKey;}
    public Table[] getPrerequisites() {
        /*
            + Branch Products
               - Units
               - Products

            + Route Plans
              - Route Plan Details

            + Price List From Customers / Price Lists
              - Price Lists Details

            + Sales Promotions Sales Discount
              - Sales Promotions Sales Discount Details

            + Sales Promotions Points
              - Sales Promotion Sales Points
         */

        if(this == BRANCH_PRODUCTS)
            prerequisites = new Table[]{PRODUCTS, UNITS};
        if(this == PRICE_LISTS || this == PRICE_LISTS_FROM_CUSTOMERS)
            prerequisites = new Table[]{CUSTOMER_BY_SALESMAN};

        return prerequisites;
    }

    public static Table convertFromKey(String tableKey) {
        for(Table table : Table.values()) {
            if(table.getTableKey() != null) {
                Log.e("convertFromKey", table.getTableKey());
                if (table.getTableKey().equals(tableKey))
                    return table;
            }
        }
        return NONE;
    }

}
