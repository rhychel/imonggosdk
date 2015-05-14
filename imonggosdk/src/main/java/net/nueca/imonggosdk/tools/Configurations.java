package net.nueca.imonggosdk.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.enums.Table;

import java.util.EnumMap;

public class Configurations {
	public static String API_AUTHENTICATION = "";

    private static String SERVER_KEY = "server";
    private static String ACCOUNT_ID_CACHE = "account_id_cache";
    private static String EMAIL_CACHE = "email_cache";

    private static SharedPreferences imonggoPreference;
    public static void initializePreference(Context context) {
        imonggoPreference = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static int getServer() {
        return imonggoPreference.getInt(SERVER_KEY, Server.IRETAILCLOUD_NET.ordinal());
    }

    public static String getEmail() {
        return imonggoPreference.getString(EMAIL_CACHE, "");
    }

    public static String getAccountId() {
        return imonggoPreference.getString(ACCOUNT_ID_CACHE, "");
    }

    public static void setAccountId(String accountId) {
        SharedPreferences.Editor editor = imonggoPreference.edit();
        editor.putString(ACCOUNT_ID_CACHE, accountId);
        editor.commit();
    }

    public static void setEmail(String email) {
        SharedPreferences.Editor editor = imonggoPreference.edit();
        editor.putString(EMAIL_CACHE, email);
        editor.commit();
    }

    public static void setServer(int server) {
        SharedPreferences.Editor editor = imonggoPreference.edit();
        editor.putInt(SERVER_KEY, server);
        editor.commit();
    }

	/**
	 * 
	 * EnumMap for API Modules in JSON.
	 * 
	 */
	public static EnumMap<Table, String> API_MODULES = new EnumMap<Table, String>(Table.class){

		private static final long serialVersionUID = 9136022492409598128L;

		{
			put(Table.BRANCHES, "branches.json");
			put(Table.USERS, "users.json");
			put(Table.BRANCH_USERS, "branches.json");
			put(Table.CUSTOMERS, "customers.json");
			put(Table.UNITS, "units.json");
			put(Table.TAX_SETTINGS, "tax_settings.json");
			put(Table.PRODUCTS, "products.json");
			put(Table.INVENTORIES, "inventories.json");
			put(Table.SETTINGS, "settings.json");
			put(Table.TOKENS, "tokens.json");

//			put(Modules.INVOICES, "invoices.json");
//			put(Modules.DOCUMENTS, "documents.json");
//            put(Modules.CONCESSIO_SETTINGS, "application_settings/concesio.json");
//            put(Modules.ORDERS, "orders.json");
//            put(Modules.POS_DEVICES, "pos_devices.json");
//            put(Modules.SALES_PROMOTIONS, "sales_promotions.json");
//            put(Modules.DOCUMENT_TYPES, "document_types.json");
//            put(Modules.DOCUMENT_PURPOSES, "document_purposes.json");
		}
	};

    public static EnumMap<Table, String> API_MODULES_ID = new EnumMap<Table, String>(Table.class){

        private static final long serialVersionUID = 9136022492409598128L;

        {
			put(Table.BRANCHES, "branches");
			put(Table.USERS, "users");
			put(Table.BRANCH_USERS, "users");
			put(Table.CUSTOMERS, "customers");
            put(Table.UNITS, "units");
			put(Table.TAX_SETTINGS, "tax_settings");
			put(Table.PRODUCTS, "products");
			put(Table.INVENTORIES, "inventories");
            put(Table.SETTINGS, "settings");
            put(Table.TOKENS, "tokens");

//            put(Modules.INVOICES, "invoices");
//            put(Modules.DOCUMENTS, "documents");
//            put(Modules.ORDERS, "orders");
//            put(Modules.CONCESSIO_SETTINGS, "application_settings");
//            put(Modules.DISCOUNTS, "sales_promotions");
//            put(Modules.DOCUMENT_TYPES, "document_types");
//            put(Modules.DOCUMENT_PURPOSES, "document_purposes");
        }
    };

	/**
	 * 
	 * EnumMap for Settings' name.
	 * 
	 */
	public static EnumMap<SettingsName, String> SETTINGS_NAME = new EnumMap<SettingsName, String>(SettingsName.class){
		
		private static final long serialVersionUID = 3897571923503134179L;

		{
			put(SettingsName.ENABLE_CUSTOMER_MEMBERSHIP, "enable_customer_membership");
			put(SettingsName.ENABLE_REWARD_POINTS, "enable_reward_points");
			put(SettingsName.ENABLE_SALESMAN, "enable_salesman");
			put(SettingsName.MERCHANT_ENABLE, "merchant_enable");
			put(SettingsName.MERCHANT_ENABLE_SWIPE, "merchant_enable_swipe");
			put(SettingsName.RECEIPT_PRINTING, "receipt_printing");
			put(SettingsName.INVOICE_FORMAT, "invoice_format");
			put(SettingsName.DISABLE_PRINT_CONFIRMATION, "disable_print_confirmation");
			put(SettingsName.AUTO_CLOSE_PREVIEW, "auto_close_preview");
			put(SettingsName.INVOICE_PRINT_DESCRIPTION, "invoice_print_description");
			put(SettingsName.INVOICE_PRINT_COMPANY, "invoice_print_company");
			put(SettingsName.INVOICE_PRINT_ADDRESS, "invoice_print_address");
			put(SettingsName.INVOICE_PRINT_TELEPHONE, "invoice_print_telephone");
			put(SettingsName.MERCHANT_ENABLE_TEST_MODE, "merchant_enable_test_mode");
			put(SettingsName.OPEN_PRICE, "open_price");
			put(SettingsName.COMPUTE_TAX, "compute_tax");
			put(SettingsName.TAX_INCLUSIVE, "tax_inclusive");
			put(SettingsName.RECEIPT_HEADER, "receipt_header");
			put(SettingsName.RECEIPT_SUB_HEADER, "receipt_sub_header");
			put(SettingsName.RECEIPT_FOOTER, "receipt_footer");
			put(SettingsName.RECEIPT_CSS, "receipt_css");
			put(SettingsName.MERCHANT_RECEIPT_FOOTER, "merchant_receipt_footer");
			put(SettingsName.MERCHANT_ENABLE_PRINT, "merchant_enable_print");
			put(SettingsName.BRANCH_NAME, "branch_name");
			put(SettingsName.RECEIPT_LOGO_URL, "receipt_logo_url");
			put(SettingsName.RECEIPT_LOGO_BASE64_URL, "receipt_logo_base64_url");
			put(SettingsName.RECEIPT_LOGO_CONTENT_TYPE, "receipt_logo_content_type");
			put(SettingsName.ENABLE_CREDIT_CARD_PROCESSING, "enable_credit_card_processing"); // true or false
			put(SettingsName.ENABLE_LAYAWAY, "enable_layaway"); // true or false
			put(SettingsName.ENABLE_CASH_MANAGEMENT, "enable_cash_management"); // true or false
			put(SettingsName.PRODUCTS_DB_VERSION, "products_db_version");
			put(SettingsName.SUBSCRIPTION_TYPE, "subscription_type");
			put(SettingsName.FORMAT_NO_OF_DECIMALS, "format_no_of_decimals");
			put(SettingsName.FORMAT_THOUSANDS_SEP, "format_thousands_sep");
			put(SettingsName.FORMAT_DECIMAL_SEP, "format_decimal_sep");
			put(SettingsName.FORMAT_UNIT, "format_unit");
			put(SettingsName.FORMAT_POSTFIX_UNIT, "format_postfix_unit");
			put(SettingsName.FORMAT_ROUND_STYLE, "format_round_style");
			put(SettingsName.FORMAT_ROUND_VALUE, "format_round_value");
			put(SettingsName.ROUND_CASH_ONLY, "round_cash_only");
			put(SettingsName.INVOICE_TITLE_SALES, "invoice_title_sales");
			put(SettingsName.INVOICE_TITLE_RETURN, "invoice_title_return");
			put(SettingsName.INVOICE_TITLE_LAYAWAY, "invoice_title_layaway");
			put(SettingsName.INVOICE_TR_ITEM, "invoice_tr_item");
			put(SettingsName.INVOICE_TR_AMOUNT, "invoice_tr_amount");
			put(SettingsName.INVOICE_TR_SUBTOTAL, "invoice_tr_subtotal");
			put(SettingsName.INVOICE_TR_TOTAL, "invoice_tr_total");
			put(SettingsName.INVOICE_TR_STOCK_NO, "invoice_tr_stock_no");
			put(SettingsName.INVOICE_TR_QUANTITY, "invoice_tr_quantity");
			put(SettingsName.INVOICE_TR_PAYMENTS, "invoice_tr_payments");
			put(SettingsName.MASTER_ACCOUNT_ID, "master_account_id");
		}
	};

}
