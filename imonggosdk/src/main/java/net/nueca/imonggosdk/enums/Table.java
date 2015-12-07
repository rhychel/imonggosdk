package net.nueca.imonggosdk.enums;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
public enum Table {
    // ----- Main APIs
    TOKENS(API_TYPE.API, "Tokens"),
    CUSTOMERS(API_TYPE.API, "Customers"),
    INVENTORIES(API_TYPE.API, "Inventories"),
    PRODUCTS(API_TYPE.API, "Products"),
    TAX_SETTINGS(API_TYPE.API, "Tax Settings"),
    UNITS(API_TYPE.API, "Units"),
    USERS(API_TYPE.API, "Users"),
    BRANCHES(API_TYPE.API, "Branches"),
    INVOICES(API_TYPE.API, "Invoices"),
    SETTINGS(API_TYPE.API, "Settings"),
    APPLICATION_SETTINGS(API_TYPE.API, "Application Settings"),
    ORDERS(API_TYPE.API, "Orders"),
    POS_DEVICES(API_TYPE.API, "Pos Devices"),
    DAILY_SALES(API_TYPE.API, "Daily Sales"),
    DOCUMENTS(API_TYPE.API, "Documents"),
    DOCUMENT_TYPES(API_TYPE.API, "Document Types"),
    DOCUMENT_PURPOSES(API_TYPE.API, "Document Purposes"),

    // ----- API With Branch IDs
    BRANCH_CUSTOMERS(API_TYPE.API, "Customers"),
    BRANCH_USERS(API_TYPE.API, "Branches"),
    BRANCH_UNITS(API_TYPE.API, "Units"),
    BRANCH_ROUTE_PLANS(API_TYPE.API, "Route Plans"),

    // ----- API with Products
    PRODUCT_TAGS(API_TYPE.NON_API, "Product Tags"),
    PRODUCT_EXTRAS(API_TYPE.NON_API, "Product Extras"),
    PRODUCT_TAX_RATES(API_TYPE.NON_API, "Product Tax Rates"),

    // ----- FOR CONNECTION
    CUSTOMER_CUSTOMER_GROUP(API_TYPE.NON_API, "Customer Customer Groups"),
    PRODUCT_SALES_PROMOTION(API_TYPE.NON_API, "Product Sales Promotion"),
    INVOICE_LINES(API_TYPE.NON_API, "Invoice Lines"),
    INVOICE_TAX_RATES(API_TYPE.NON_API, "Invoice Tax Rates"),
    DOCUMENT_LINE_EXTRAS(API_TYPE.NON_API, "Document Line Extras"),
    PRICES(API_TYPE.NON_API, "Prices"),
    BRANCH_PRICES(API_TYPE.NON_API, "Prices"),

    // ----- APP Custom Table
    SESSIONS(API_TYPE.NON_API, "Sessions"),
    BRANCH_TAGS(API_TYPE.NON_API, "Branch Tags"),
    LAST_UPDATED_AT(API_TYPE.NON_API, "Last Updated At"),
    OFFLINEDATA(API_TYPE.NON_API, "Offline Date"),
    ORDER_LINES(API_TYPE.NON_API, "Order Lines"),
    TAX_RATES(API_TYPE.NON_API, "Tax Rates"),
    PAYMENTS(API_TYPE.NON_API, "Payments"),
    DOCUMENT_LINES(API_TYPE.NON_API, "Document Lines"),
    EXTENDED_ATTRIBUTES(API_TYPE.NON_API, "Extended Attributes"),
    EXTRAS(API_TYPE.NON_API, "Extras"),
    SALES_PROMOTIONS,
    SALES_PROMOTION_LINES,

    // ----- FOR REBISCO
    BRANCH_PRODUCTS(API_TYPE.API, "Products"),
    INVOICE_PURPOSES(API_TYPE.API, "Invoice Purposes"),
    PAYMENT_TERMS(API_TYPE.API, "Payment Terms"),
    CUSTOMER_CATEGORIES(API_TYPE.API, "Customer Categories"),
    PAYMENT_TYPES(API_TYPE.API, "Payment Types"),
    CUSTOMER_GROUPS(API_TYPE.API, "Customer Groups"),
    PRICE_LISTS(API_TYPE.API, "Price Lists"),
    BRANCH_PRICE_LISTS(API_TYPE.API, "Price Lists"),
    PRICE_LISTS_DETAILS(API_TYPE.API, "Details"),
    SALES_PROMOTIONS(API_TYPE.API, "Sales Promotions"),
    SALES_PROMOTIONS_DISCOUNT(API_TYPE.API, "Discount"),
    SALES_PUSH(API_TYPE.API, "Sales Push"),
    ROUTE_PLANS(API_TYPE.API, "Route Plans");

    private final API_TYPE api_type;
    private final String name;
    Table(API_TYPE api, String name) {
        this.api_type = api;
        this.name = name;
    }

    public boolean isAPI() {return this.api_type.equals(API_TYPE.API);}
    public boolean isNoNAPI() {return this.api_type.equals(API_TYPE.NON_API);}

    public String getStringName() {
        return this.name;
    }
}
