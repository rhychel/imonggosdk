package net.nueca.imonggosdk.tools;

import net.nueca.imonggosdk.enums.Table;

/**
 * Created by Jn on 17/11/15.
 * imonggosdk (c)2015
 */
public class TableTools {

    public static Table convertStringToTableName(String name) {

        switch (name.toUpperCase()) {
            case "BRANCHES":
                return Table.BRANCH_USERS;

            case "BRANCH_PRICES":
                return Table.BRANCH_PRICES;

            case "BRANCH PRICES":
                return Table.BRANCH_PRICES;

            case "BRANCH_TAGS":
                return Table.BRANCH_TAGS;

            case "BRANCH TAGS":
                return Table.BRANCH_TAGS;

            case "CUSTOMERS":
                return Table.CUSTOMERS;

            case "INVENTORIES":
                return Table.INVENTORIES;

            case "PRODUCTS":
                return Table.PRODUCTS;

            case "PRODUCT_TAGS":
                return Table.PRODUCT_TAGS;

            case "PRODUCT TAGS":
                return Table.PRODUCT_TAGS;

            case "PRODUCT_EXTRAS":
                return Table.PRODUCT_EXTRAS;

            case "PRODUCT EXTRAS":
                return Table.PRODUCT_EXTRAS;

            case "SESSIONS":
                return Table.SESSIONS;

            case "TAX_RATES":
                return Table.TAX_RATES;

            case "TAX RATES":
                return Table.TAX_RATES;

            case "TAX_SETTINGS":
                return Table.TAX_SETTINGS;

            case "TAX SETTINGS":
                return Table.TAX_SETTINGS;

            case "UNITS":
                return Table.UNITS;

            case "USERS":
                return Table.USERS;

            case "BRANCH_USERS":
                return Table.BRANCH_USERS;

            case "BRANCH USERS":
                return Table.BRANCH_USERS;

            case "PRODUCT_TAX_RATES":
                return Table.PRODUCT_TAX_RATES;

            case "PRODUCT TAX RATES":
                return Table.PRODUCT_TAX_RATES;

            case "CUSTOMER_CUSTOMER_GROUP":
                return Table.CUSTOMER_CUSTOMER_GROUP;

            case "CUSTOMER CUSTOMER GROUP":
                return Table.CUSTOMER_CUSTOMER_GROUP;

            case "PRODUCT_SALES_PROMOTION":
                return Table.PRODUCT_SALES_PROMOTION;

            case "PRODUCT SALES PROMOTION":
                return Table.PRODUCT_SALES_PROMOTION;

            case "DOCUMENTS":
                return Table.DOCUMENTS;

            case "DOCUMENT_TYPES":
                return Table.DOCUMENT_TYPES;

            case "DOCUMENT TYPES":
                return Table.DOCUMENT_TYPES;

            case "DOCUMENT_PURPOSES":
                return Table.DOCUMENT_PURPOSES;

            case "DOCUMENT PURPOSES":
                return Table.DOCUMENT_PURPOSES;

            case "DOCUMENT_LINES":
                return Table.DOCUMENT_LINES;

            case "DOCUMENT LINES":
                return Table.DOCUMENT_LINES;

            case "EXTENDED_ATTRIBUTES":
                return Table.EXTENDED_ATTRIBUTES;

            case "EXTENDED ATTRIBUTES":
                return Table.EXTENDED_ATTRIBUTES;

            case "DOCUMENT_LINE_EXTRAS":
                return Table.DOCUMENT_LINE_EXTRAS;

            case "DOCUMENT LINE EXTRAS":
                return Table.DOCUMENT_LINE_EXTRAS;

            case "INVOICES":
                return Table.INVOICES;

            case "INVOICE_LINES":
                return Table.INVOICE_LINES;

            case "INVOICE LINES":
                return Table.INVOICE_LINES;

            case "INVOICE_TAX_RATES":
                return Table.INVOICE_TAX_RATES;

            case "INVOICE TAX RATES":
                return Table.INVOICE_TAX_RATES;

            case "PAYMENTS":
                return Table.PAYMENTS;

            case "SALES_PROMOTIONS":
                return Table.SALES_PROMOTIONS;

            case "SALES PROMOTIONS":
                return Table.SALES_PROMOTIONS;

            case "SETTINGS":
                return Table.SETTINGS;

            case "TOKENS":
                return Table.TOKENS;

            case "APPLICATION_SETTINGS":
                return Table.APPLICATION_SETTINGS;

            case "APPLICATION SETTINGS":
                return Table.APPLICATION_SETTINGS;

            case "LAST_UPDATED_AT":
                return Table.LAST_UPDATED_AT;

            case "LAST UPDATED AT":
                return Table.LAST_UPDATED_AT;

            case "POS_DEVICES":
                return Table.POS_DEVICES;

            case "POS DEVICES":
                return Table.POS_DEVICES;

            case "OFFLINEDATA":
                return Table.OFFLINEDATA;

            case "ORDERS":
                return Table.ORDERS;

            case "ORDER_LINES":
                return Table.ORDER_LINES;

            case "ORDER LINES":
                return Table.ORDER_LINES;

            case "DAILY_SALES":
                return Table.DAILY_SALES;

            case "DAILY SALES":
                return Table.DAILY_SALES;

            case "CUSTOMER_GROUPS":
                return Table.CUSTOMER_GROUPS;

            case "CUSTOMER GROUPS":
                return Table.CUSTOMER_GROUPS;

            case "CUSTOMER_CATEGORIES":
                return Table.CUSTOMER_CATEGORIES;

            case "CUSTOMER CATEGORIES":
                return Table.CUSTOMER_CATEGORIES;

            case "PRICES":
                return Table.PRICES;

            case "PRICE_LISTS":
                return Table.PRICE_LISTS;

            case "PRICE LISTS":
                return Table.PRICE_LISTS;

            case "ROUTE_PLANS":
                return Table.ROUTE_PLANS;

            case "ROUTE PLANS":
                return Table.ROUTE_PLANS;

            case "INVOICE_PURPOSES":
                return Table.INVOICE_PURPOSES;

            case "INVOICE PURPOSES":
                return Table.INVOICE_PURPOSES;

            case "PAYMENT_TERMS":
                return Table.PAYMENT_TERMS;

            case "PAYMENT TERMS":
                return Table.PAYMENT_TERMS;

            case "PAYMENT_TYPES":
                return Table.PAYMENT_TYPES;

            case "PAYMENT TYPES":
                return Table.PAYMENT_TYPES;

            case "EXTRAS":
                return Table.EXTRAS;

            default:
                return null;
        }
    }
}