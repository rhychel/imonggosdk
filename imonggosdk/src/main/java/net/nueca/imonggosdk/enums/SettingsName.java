package net.nueca.imonggosdk.enums;

public enum SettingsName {
    AUTO_UPDATE("auto_update"),
    DEFAULT_BRANCH("default_branch"),
    CURRENT_BRANCH("current_branch"),
    DEFAULT_WAREHOUSE("default_warehouse"),
    ENABLE_CUSTOMER_MEMBERSHIP("enable_customer_membership"),
    ENABLE_REWARD_POINTS("enable_reward_points"),
    ENABLE_SALESMAN("enable_salesman"),
    MERCHANT_ENABLE("merchant_enable"),
    MERCHANT_ENABLE_SWIPE("merchant_enable_swipe"),
    RECEIPT_PRINTING("receipt_printing"),
    INVOICE_FORMAT("invoice_format"),
    DISABLE_PRINT_CONFIRMATION("disable_print_confirmation"),
    AUTO_CLOSE_PREVIEW("auto_close_preview"),
    INVOICE_PRINT_DESCRIPTION("invoice_print_description"),
    INVOICE_PRINT_COMPANY("invoice_print_company"),
    INVOICE_PRINT_ADDRESS("invoice_print_address"),
    INVOICE_PRINT_TELEPHONE("invoice_print_telephone"),
    MERCHANT_ENABLE_TEST_MODE("merchant_enable_test_mode"),
    OPEN_PRICE("open_price"),
    COMPUTE_TAX("compute_tax"),
    TAX_INCLUSIVE("tax_inclusive"),
    RECEIPT_HEADER("receipt_header"),
    RECEIPT_SUB_HEADER("receipt_sub_header"),
    RECEIPT_FOOTER("receipt_footer"),
    RECEIPT_CSS("receipt_css"),
    MERCHANT_RECEIPT_FOOTER("merchant_receipt_footer"),
    MERCHANT_ENABLE_PRINT("merchant_enable_print"),
    BRANCH_NAME("branch_name"),
    RECEIPT_LOGO_URL("receipt_logo_url"),
    RECEIPT_LOGO_BASE64_URL("receipt_logo_base64_url"),
    RECEIPT_LOGO_CONTENT_TYPE("receipt_logo_content_type"),
    ENABLE_CREDIT_CARD_PROCESSING("enable_credit_card_processing"), // TRUE OR FALSE
    ENABLE_LAYAWAY("enable_layaway"), // TRUE OR FALSE
    ENABLE_CASH_MANAGEMENT("enable_cash_management"), // TRUE OR FALSE
    PRODUCTS_DB_VERSION("products_db_version"),
    SUBSCRIPTION_TYPE("subscription_type"),
    FORMAT_NO_OF_DECIMALS("format_no_of_decimals"),
    FORMAT_THOUSANDS_SEP("format_thousands_sep"),
    FORMAT_DECIMAL_SEP("format_decimal_sep"),
    FORMAT_UNIT("format_unit"),
    FORMAT_POSTFIX_UNIT("format_postfix_unit"),
    FORMAT_ROUND_STYLE("format_round_style"),
    FORMAT_ROUND_VALUE("format_round_value"),
    ROUND_CASH_ONLY("round_cash_only"),
    INVOICE_TITLE_SALES("invoice_title_sales"),
    INVOICE_TITLE_RETURN("invoice_title_return"),
    INVOICE_TITLE_LAYAWAY("invoice_title_layaway"),
    INVOICE_TR_ITEM("invoice_tr_item"),
    INVOICE_TR_AMOUNT("invoice_tr_amount"),
    INVOICE_TR_SUBTOTAL("invoice_tr_subtotal"),
    INVOICE_TR_TOTAL("invoice_tr_total"),
    INVOICE_TR_STOCK_NO("invoice_tr_stock_no"),
    INVOICE_TR_QUANTITY("invoice_tr_quantity"),
    INVOICE_TR_PAYMENTS("invoice_tr_payments"),
    MASTER_ACCOUNT_ID("master_account_id"),
    SYNC_FINISHED("sync_finished"),
    SERVERS("servers");

    private String value;

    SettingsName(String stringValue) {
        value = stringValue;
    }

    public String getValue() {
        return value;
    }
}
