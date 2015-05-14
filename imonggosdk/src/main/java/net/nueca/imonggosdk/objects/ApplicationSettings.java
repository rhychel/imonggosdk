package net.nueca.imonggosdk.objects;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rhymart on 3/19/15.
 * ImonggoLibrary (c)2015
 *
     {
         "settings": {
             "has_order": "1",
             "has_pullout": "1",
             "has_count": "0",
             "has_receive": "1",
             "has_sales": "0",
             "has_clear_transactions": "0",
             "has_get_latest_documents": "1",
             "has_order_cutoff_period": "1",
             "order_taking_start": "08:00 AM",
             "order_taking_cutoff": "05:00 PM",
             "order_allow_limit_orders_to_one_category": "1",
             "order_allow_require_delivery_date": "0",
             "count_allow_entering_brand": "0",
             "count_allow_entering_delivery_date": "0",
             "receive_allow_outright_input": "1",
             "receive_allow_discrepancy_input": "1",
             "pullout_allow_store_transfer": "1"
         }
     }
 */
public class ApplicationSettings {

    private static final String                                HAS_ORDER = "has_order";
    private static final String                              HAS_PULLOUT = "has_pullout";
    private static final String                                HAS_COUNT = "has_count";
    private static final String                              HAS_RECEIVE = "has_receive";
    private static final String                                HAS_SALES = "has_sales";
    private static final String                   HAS_CLEAR_TRANSACTIONS = "has_clear_transactions";
    private static final String                 HAS_GET_LATEST_DOCUMENTS = "has_get_latest_documents";
    private static final String                  HAS_ORDER_CUTOFF_PERIOD = "has_order_cutoff_period";

    private static final String                       ORDER_TAKING_START = "order_taking_start";
    private static final String                      ORDER_TAKING_CUTOFF = "order_taking_cutoff";
    private static final String ORDER_ALLOW_LIMIT_ORDERS_TO_ONE_CATEGORY = "order_allow_limit_orders_to_one_category";
    private static final String        ORDER_ALLOW_REQUIRE_DELIVERY_DATE = "order_allow_require_delivery_date";

    private static final String               COUNT_ALLOW_ENTERING_BRAND = "count_allow_entering_brand";
    private static final String       COUNT_ALLOW_ENTERING_DELIVERY_DATE = "count_allow_entering_delivery_date";

    private static final String             RECEIVE_ALLOW_OUTRIGHT_INPUT = "receive_allow_outright_input";
    private static final String          RECEIVE_ALLOW_DISCREPANCY_INPUT = "receive_allow_discrepancy_input";

    private static final String             PULLOUT_ALLOW_STORE_TRANSFER = "pullout_allow_store_transfer";

    private static SharedPreferences concessioSettings = null;

    private static boolean fromIntToBool(int value) {
        return value == 1;
    }

    public static void initializeApplicationSettings(Context context, JSONObject jsonObject) {
        try {
            updateHasOrder(context, fromIntToBool(jsonObject.getInt(HAS_ORDER)));
            updateHasCount(context, fromIntToBool(jsonObject.getInt(HAS_COUNT)));
            updateHasPullout(context, fromIntToBool(jsonObject.getInt(HAS_PULLOUT)));
            updateHasReceive(context, fromIntToBool(jsonObject.getInt(HAS_RECEIVE)));
            updateHasClearTransaction(context, fromIntToBool(jsonObject.getInt(HAS_CLEAR_TRANSACTIONS)));
            updateHasGetLatestDocuments(context, fromIntToBool(jsonObject.getInt(HAS_GET_LATEST_DOCUMENTS)));
            updateHasOrderCutoffPeriod(context, fromIntToBool(jsonObject.getInt(HAS_ORDER_CUTOFF_PERIOD)));
            updateOrderTakingStart(context, jsonObject.getString(ORDER_TAKING_START));
            updateOrderTakingCutoff(context, jsonObject.getString(ORDER_TAKING_CUTOFF));
            updateOrderAllowLimitOrdersToOneCategory(context, fromIntToBool(jsonObject.getInt(ORDER_ALLOW_LIMIT_ORDERS_TO_ONE_CATEGORY)));
            updateOrderAllowRequireDeliveryDate(context, fromIntToBool(jsonObject.getInt(ORDER_ALLOW_REQUIRE_DELIVERY_DATE)));

            updateCountAllowEnteringBrand(context, fromIntToBool(jsonObject.getInt(COUNT_ALLOW_ENTERING_BRAND)));
            updateCountAllowEnteringDeliveryDate(context, fromIntToBool(jsonObject.getInt(COUNT_ALLOW_ENTERING_DELIVERY_DATE)));

            updateReceiveAllowOutrightInput(context, fromIntToBool(jsonObject.getInt(RECEIVE_ALLOW_OUTRIGHT_INPUT)));
            updateReceiveAllowDiscrepancyInput(context, fromIntToBool(jsonObject.getInt(RECEIVE_ALLOW_DISCREPANCY_INPUT)));

            updatePulloutAllowStoreTransfer(context, fromIntToBool(jsonObject.getInt(PULLOUT_ALLOW_STORE_TRANSFER)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String getPackageName(Context context) {
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pinfo.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "NONE---";
    }

    private static SharedPreferences getSettings(Context context) {
        if(concessioSettings == null)
            concessioSettings = PreferenceManager.getDefaultSharedPreferences(context);
        return concessioSettings;
    }

    public static void updateCountAllowEnteringBrand(Context context, boolean countAllowEnteringBrand) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + COUNT_ALLOW_ENTERING_BRAND, countAllowEnteringBrand);
        editor.apply();
    }

    public static boolean allowEnteringBrand(Context context) {
        return getSettings(context).getBoolean(getPackageName(context) + COUNT_ALLOW_ENTERING_BRAND, false);
    }

    public static void updateCountAllowEnteringDeliveryDate(Context context, boolean countAllowEnteringDeliveryDate) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + COUNT_ALLOW_ENTERING_DELIVERY_DATE, countAllowEnteringDeliveryDate);
        editor.apply();
    }

    public static boolean allowEnteringDeliveryDate(Context context) {
        return getSettings(context).getBoolean(getPackageName(context) + COUNT_ALLOW_ENTERING_DELIVERY_DATE, false);
    }
// -----
    public static void updateReceiveAllowOutrightInput(Context context, boolean receiveAllowOutrightInput) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + RECEIVE_ALLOW_OUTRIGHT_INPUT, receiveAllowOutrightInput);
        editor.apply();
    }

    public static boolean allowOutrightInput(Context context) {
        return getSettings(context).getBoolean(getPackageName(context) + RECEIVE_ALLOW_OUTRIGHT_INPUT, false);
    }

    public static void updateReceiveAllowDiscrepancyInput(Context context, boolean receiveAllowDiscrepancyInput) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + RECEIVE_ALLOW_DISCREPANCY_INPUT, receiveAllowDiscrepancyInput);
        editor.apply();
    }

    public static boolean allowDiscrepancyInput(Context context) {
        return getSettings(context).getBoolean(getPackageName(context) + RECEIVE_ALLOW_DISCREPANCY_INPUT, false);
    }

    public static void updatePulloutAllowStoreTransfer(Context context, boolean pulloutAllowStoreTransfer) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + PULLOUT_ALLOW_STORE_TRANSFER, pulloutAllowStoreTransfer);
        editor.apply();
    }

    public static boolean allowStoreTransfer(Context context) {
        return getSettings(context).getBoolean(getPackageName(context) + PULLOUT_ALLOW_STORE_TRANSFER, false);
    }

    public static void updateOrderTakingStart(Context context, String orderTakingStart) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putString(getPackageName(context) + ORDER_TAKING_START, orderTakingStart);
        editor.apply();
    }

    public static String orderTakingStart(Context context) {
        return getSettings(context).getString(getPackageName(context) + ORDER_TAKING_START, "8:00 AM");
    }

    public static void updateOrderTakingCutoff(Context context, String orderTakingStart) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putString(getPackageName(context) + ORDER_TAKING_CUTOFF, orderTakingStart);
        editor.apply();
    }

    public static String orderTakingCutoff(Context context) {
        return getSettings(context).getString(getPackageName(context) + ORDER_TAKING_CUTOFF, "5:00 PM");
    }

    public static void updateOrderAllowLimitOrdersToOneCategory(Context context, boolean orderAllowLimitOrdersToOneCategory) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context)+ORDER_ALLOW_LIMIT_ORDERS_TO_ONE_CATEGORY, orderAllowLimitOrdersToOneCategory);
        editor.apply();
    }

    public static boolean allowLimitOrdersToOneCategory(Context context) {
        return getSettings(context).getBoolean(getPackageName(context)+ORDER_ALLOW_LIMIT_ORDERS_TO_ONE_CATEGORY, false);
    }

    public static void updateOrderAllowRequireDeliveryDate(Context context, boolean orderAllowRequireDeliveryDate) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context)+ORDER_ALLOW_REQUIRE_DELIVERY_DATE, orderAllowRequireDeliveryDate);
        editor.apply();
    }

    public static boolean allowRequireDeliveryDate(Context context) {
        return getSettings(context).getBoolean(getPackageName(context)+ORDER_ALLOW_REQUIRE_DELIVERY_DATE, false);
    }

    public static void updateHasSales(Context context, boolean hasSales) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context)+HAS_SALES, hasSales);
        editor.apply();
    }

    public static boolean hasSales(Context context) {
        return getSettings(context).getBoolean(getPackageName(context)+HAS_SALES, false);
    }

    public static void updateHasOrder(Context context, boolean hasOrder) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context)+HAS_ORDER, hasOrder);
        editor.apply();
    }

    public static boolean hasOrder(Context context) {
        return getSettings(context).getBoolean(getPackageName(context)+HAS_ORDER, false);
    }

    public static void updateHasPullout(Context context, boolean hasPullout) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context)+HAS_PULLOUT, hasPullout);
        editor.apply();
    }

    public static boolean hasPullout(Context context) {
        return getSettings(context).getBoolean(getPackageName(context)+HAS_PULLOUT, false);
    }

    public static void updateHasCount(Context context, boolean hasCount) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context)+HAS_COUNT, hasCount);
        editor.apply();
    }

    public static boolean hasCount(Context context) {
        return getSettings(context).getBoolean(getPackageName(context)+HAS_COUNT, false);
    }

    public static void updateHasReceive(Context context, boolean hasReceive) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context)+HAS_RECEIVE, hasReceive);
        editor.apply();
    }

    public static boolean hasReceive(Context context) {
        return getSettings(context).getBoolean(getPackageName(context)+HAS_RECEIVE, false);
    }

    public static void updateHasClearTransaction(Context context, boolean hasClearTransaction) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context)+HAS_CLEAR_TRANSACTIONS, hasClearTransaction);
        editor.apply();
    }

    public static boolean hasClearTransaction(Context context) {
        return getSettings(context).getBoolean(getPackageName(context)+HAS_CLEAR_TRANSACTIONS, false);
    }

    public static void updateHasGetLatestDocuments(Context context, boolean hasGetLatestDocuments) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context)+HAS_GET_LATEST_DOCUMENTS, hasGetLatestDocuments);
        editor.apply();
    }

    public static boolean hasGetLatestDocuments(Context context) {
        return getSettings(context).getBoolean(getPackageName(context)+HAS_GET_LATEST_DOCUMENTS, false);
    }

    public static void updateHasOrderCutoffPeriod(Context context, boolean hasOrderCutoffPeriod) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context)+HAS_ORDER_CUTOFF_PERIOD, hasOrderCutoffPeriod);
        editor.apply();
    }

    public static boolean hasOrderCutoffPeriod(Context context) {
        return getSettings(context).getBoolean(getPackageName(context)+HAS_ORDER_CUTOFF_PERIOD, false);
    }

}
