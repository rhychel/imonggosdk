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
 */
public class AccountSettings {

    private static final String                                HAS_ORDER = "has_order";
    private static final String                              HAS_PULLOUT = "has_pullout";
    private static final String                                HAS_COUNT = "has_count";
    private static final String                              HAS_RECEIVE = "has_receive";
    private static final String                                HAS_SALES = "has_sales";
    private static final String                   HAS_CLEAR_TRANSACTIONS = "has_clear_transactions";
    private static final String                 HAS_GET_LATEST_DOCUMENTS = "has_get_latest_documents";
    private static final String                  HAS_ORDER_CUTOFF_PERIOD = "has_order_cutoff_period";
    private static final String                        REQUIRE_WAREHOUSE = "require_warehouse";
    private static final String                 SHOW_CATEGORIES_ON_START = "show_categories_on_start";
    private static final String                               ALLOW_VOID = "allow_void";

    // Developer Options
    private static final String                               DEBUG_MODE = "debug_mode";
    private static final String                      ADD_ORDER_TYPE_CODE = "add_order_type_code";
    private static final String                         SEND_SWABLE_LOGS = "send_swable_logs";
    private static final String                            DISABLE_IMAGE = "disable_image";

    // For improvement of Concessio product specs.
    private static final String                         ORDER_AUTODELETE = "order_autodelete";
    private static final String                 ORDER_VIEW_HISTORY_EVERY = "order_view_history_every";
    private static final String                         COUNT_AUTODELETE = "count_autodelete";
    private static final String                 COUNT_VIEW_HISTORY_EVERY = "count_view_history_every";
    private static final String                       PULLOUT_AUTODELETE = "pullout_autodelete";
    private static final String               PULLOUT_VIEW_HISTORY_EVERY = "pullout_view_history_every";
    private static final String                       RECEIVE_AUTODELETE = "receive_autodelete";
    private static final String               RECEIVE_VIEW_HISTORY_EVERY = "receive_view_history_every";
    private static final String                         SALES_AUTODELETE = "sales_autodelete";
    private static final String                 SALES_VIEW_HISTORY_EVERY = "sales_view_history_every";

    private static final String                       ORDER_TAKING_START = "order_taking_start";
    private static final String                      ORDER_TAKING_CUTOFF = "order_taking_cutoff";
    private static final String ORDER_ALLOW_LIMIT_ORDERS_TO_ONE_CATEGORY = "order_allow_limit_orders_to_one_category";
    private static final String        ORDER_ALLOW_REQUIRE_DELIVERY_DATE = "order_allow_require_delivery_date";

    private static final String               COUNT_ALLOW_ENTERING_BRAND = "count_allow_entering_brand";
    private static final String       COUNT_ALLOW_ENTERING_DELIVERY_DATE = "count_allow_entering_delivery_date";
    private static final String           COUNT_ALLOW_BATCH_NUMBER_INPUT = "count_allow_batch_number_input"; // October 6, 2015

    private static final String             RECEIVE_ALLOW_OUTRIGHT_INPUT = "receive_allow_outright_input";
    private static final String          RECEIVE_ALLOW_DISCREPANCY_INPUT = "receive_allow_discrepancy_input";
    private static final String                      RECEIVE_MULTI_INPUT = "receive_multi_input"; // October 6, 2015
    private static final String         RECEIVE_ALLOW_BATCH_NUMBER_INPUT = "receive_allow_batch_number_input"; // October 6, 2015

    private static final String             PULLOUT_ALLOW_STORE_TRANSFER = "pullout_allow_store_transfer";
    private static final String                      PULLOUT_MULTI_INPUT = "pullout_multi_input"; // October 6, 2015
    private static final String         PULLOUT_ALLOW_BATCH_NUMBER_INPUT = "pullout_allow_batch_number_input"; // October 6, 2015

    private static SharedPreferences concessioSettings = null;

    private static boolean fromIntToBool(int value) {
        return value == 1;
    }

    public static void initializeApplicationSettings(Context context, JSONObject jsonObject) {
        try {
            if(jsonObject.has(HAS_ORDER))
                updateHasOrder(context, fromIntToBool(jsonObject.getInt(HAS_ORDER)));
            if(jsonObject.has(HAS_COUNT))
                updateHasCount(context, fromIntToBool(jsonObject.getInt(HAS_COUNT)));
            if(jsonObject.has(HAS_PULLOUT))
                updateHasPullout(context, fromIntToBool(jsonObject.getInt(HAS_PULLOUT)));
            if(jsonObject.has(HAS_RECEIVE))
                updateHasReceive(context, fromIntToBool(jsonObject.getInt(HAS_RECEIVE)));
            if(jsonObject.has(HAS_CLEAR_TRANSACTIONS))
                updateHasClearTransaction(context, fromIntToBool(jsonObject.getInt(HAS_CLEAR_TRANSACTIONS)));
            if(jsonObject.has(HAS_GET_LATEST_DOCUMENTS))
                updateHasGetLatestDocuments(context, fromIntToBool(jsonObject.getInt(HAS_GET_LATEST_DOCUMENTS)));
            if(jsonObject.has(HAS_ORDER_CUTOFF_PERIOD))
                updateHasOrderCutoffPeriod(context, fromIntToBool(jsonObject.getInt(HAS_ORDER_CUTOFF_PERIOD)));
            if(jsonObject.has(ALLOW_VOID))
                updateAllowVoid(context, fromIntToBool(jsonObject.getInt(ALLOW_VOID)));

            if(jsonObject.has(ORDER_TAKING_START))
                updateOrderTakingStart(context, jsonObject.getString(ORDER_TAKING_START));
            if(jsonObject.has(ORDER_TAKING_CUTOFF))
                updateOrderTakingCutoff(context, jsonObject.getString(ORDER_TAKING_CUTOFF));
            if(jsonObject.has(ORDER_ALLOW_LIMIT_ORDERS_TO_ONE_CATEGORY))
                updateOrderAllowLimitOrdersToOneCategory(context, fromIntToBool(jsonObject.getInt(ORDER_ALLOW_LIMIT_ORDERS_TO_ONE_CATEGORY)));
            if(jsonObject.has(ORDER_ALLOW_REQUIRE_DELIVERY_DATE))
                updateOrderAllowRequireDeliveryDate(context, fromIntToBool(jsonObject.getInt(ORDER_ALLOW_REQUIRE_DELIVERY_DATE)));
            if(jsonObject.has(ORDER_AUTODELETE))
                updateOrderAutodelete(context, jsonObject.getInt(ORDER_AUTODELETE));
            if(jsonObject.has(ORDER_VIEW_HISTORY_EVERY))
                updateOrderViewHistoryEvery(context, jsonObject.getInt(ORDER_VIEW_HISTORY_EVERY));

            if(jsonObject.has(COUNT_ALLOW_ENTERING_BRAND))
                updateCountAllowEnteringBrand(context, fromIntToBool(jsonObject.getInt(COUNT_ALLOW_ENTERING_BRAND)));
            if(jsonObject.has(COUNT_ALLOW_ENTERING_DELIVERY_DATE))
                updateCountAllowEnteringDeliveryDate(context, fromIntToBool(jsonObject.getInt(COUNT_ALLOW_ENTERING_DELIVERY_DATE)));
            if(jsonObject.has(COUNT_ALLOW_BATCH_NUMBER_INPUT))
                updateCountAllowBatchNumberInput(context, fromIntToBool(jsonObject.getInt(COUNT_ALLOW_BATCH_NUMBER_INPUT)));
            if(jsonObject.has(COUNT_AUTODELETE))
                updateCountAutodelete(context, jsonObject.getInt(COUNT_AUTODELETE));
            if(jsonObject.has(COUNT_VIEW_HISTORY_EVERY))
                updateCountViewHistoryEvery(context, jsonObject.getInt(COUNT_VIEW_HISTORY_EVERY));

            if(jsonObject.has(RECEIVE_ALLOW_OUTRIGHT_INPUT))
                updateReceiveAllowOutrightInput(context, fromIntToBool(jsonObject.getInt(RECEIVE_ALLOW_OUTRIGHT_INPUT)));
            if(jsonObject.has(RECEIVE_ALLOW_DISCREPANCY_INPUT))
                updateReceiveAllowDiscrepancyInput(context, fromIntToBool(jsonObject.getInt(RECEIVE_ALLOW_DISCREPANCY_INPUT)));
            if(jsonObject.has(RECEIVE_MULTI_INPUT))
                updateReceiveMultiInput(context, fromIntToBool(jsonObject.getInt(RECEIVE_MULTI_INPUT)));
            if(jsonObject.has(RECEIVE_ALLOW_BATCH_NUMBER_INPUT))
                updateReceiveAllowBatchNumberInput(context, fromIntToBool(jsonObject.getInt(RECEIVE_ALLOW_BATCH_NUMBER_INPUT)));
            if(jsonObject.has(RECEIVE_AUTODELETE))
                updateReceiveAutodelete(context, jsonObject.getInt(RECEIVE_AUTODELETE));
            if(jsonObject.has(RECEIVE_VIEW_HISTORY_EVERY))
                updateReceiveViewHistoryEvery(context, jsonObject.getInt(RECEIVE_VIEW_HISTORY_EVERY));

            if(jsonObject.has(PULLOUT_ALLOW_STORE_TRANSFER))
                updatePulloutAllowStoreTransfer(context, fromIntToBool(jsonObject.getInt(PULLOUT_ALLOW_STORE_TRANSFER)));
            if(jsonObject.has(PULLOUT_MULTI_INPUT))
                updatePulloutMultiInput(context, fromIntToBool(jsonObject.getInt(PULLOUT_MULTI_INPUT)));
            if(jsonObject.has(PULLOUT_ALLOW_BATCH_NUMBER_INPUT))
                updatePulloutAllowBatchNumberInput(context, fromIntToBool(jsonObject.getInt(PULLOUT_ALLOW_BATCH_NUMBER_INPUT)));
            if(jsonObject.has(PULLOUT_AUTODELETE))
                updatePulloutAutodelete(context, jsonObject.getInt(PULLOUT_AUTODELETE));
            if(jsonObject.has(PULLOUT_VIEW_HISTORY_EVERY))
                updatePulloutViewHistoryEvery(context, jsonObject.getInt(PULLOUT_ALLOW_STORE_TRANSFER));

            if(jsonObject.has(SALES_AUTODELETE))
                updateSalesAutodelete(context, jsonObject.getInt(SALES_AUTODELETE));
            if(jsonObject.has(SALES_VIEW_HISTORY_EVERY))
                updateSalesViewHistoryEvery(context, jsonObject.getInt(SALES_VIEW_HISTORY_EVERY));

            if(jsonObject.has(REQUIRE_WAREHOUSE))
                updateRequireWarehouse(context, fromIntToBool(jsonObject.getInt(REQUIRE_WAREHOUSE)));
            if(jsonObject.has(DEBUG_MODE))
                updateDebugMode(context, fromIntToBool(jsonObject.getInt(DEBUG_MODE)));
            if(jsonObject.has(SHOW_CATEGORIES_ON_START))
                updateShowCategoriesOnStart(context, fromIntToBool(jsonObject.getInt(SHOW_CATEGORIES_ON_START)));
            if(jsonObject.has(ADD_ORDER_TYPE_CODE))
                updateAddOrderTypeCode(context, fromIntToBool(jsonObject.getInt(ADD_ORDER_TYPE_CODE)));
            if(jsonObject.has(SEND_SWABLE_LOGS))
                updateSendSwableLogs(context, fromIntToBool(jsonObject.getInt(SEND_SWABLE_LOGS)));
            if (jsonObject.has(DISABLE_IMAGE))
                updateDisableImage(context, fromIntToBool(jsonObject.getInt(DISABLE_IMAGE)));
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
    // ----- New after October 5, 2015

    public static void updateReceiveMultiInput(Context context, boolean isMultiInput) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + RECEIVE_MULTI_INPUT, isMultiInput);
        editor.apply();
    }

    public static boolean receiveMultiInput(Context context) {
        return getSettings(context).getBoolean(getPackageName(context) + RECEIVE_MULTI_INPUT, false);
    }

    public static void updateReceiveAllowBatchNumberInput(Context context, boolean allowBatchNumber) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + RECEIVE_ALLOW_BATCH_NUMBER_INPUT, allowBatchNumber);
        editor.apply();
    }

    public static boolean receiveAllowBatchNumberInput(Context context) {
        return getSettings(context).getBoolean(getPackageName(context) + RECEIVE_ALLOW_BATCH_NUMBER_INPUT, false);
    }


    public static void updatePulloutMultiInput(Context context, boolean isMultiInput) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + PULLOUT_MULTI_INPUT, isMultiInput);
        editor.apply();
    }

    public static boolean pulloutMultiInput(Context context) {
        return getSettings(context).getBoolean(getPackageName(context) + PULLOUT_MULTI_INPUT, false);
    }

    public static void updatePulloutAllowBatchNumberInput(Context context, boolean allowBatchNumber) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + PULLOUT_ALLOW_BATCH_NUMBER_INPUT, allowBatchNumber);
        editor.apply();
    }

    public static boolean pulloutAllowBatchNumberInput(Context context) {
        return getSettings(context).getBoolean(getPackageName(context) + PULLOUT_ALLOW_BATCH_NUMBER_INPUT, false);
    }

    public static void updateCountAllowBatchNumberInput(Context context, boolean allowBatchNumber) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + COUNT_ALLOW_BATCH_NUMBER_INPUT, allowBatchNumber);
        editor.apply();
    }

    public static boolean countAllowBatchNumberInput(Context context) {
        return getSettings(context).getBoolean(getPackageName(context) + COUNT_ALLOW_BATCH_NUMBER_INPUT, false);
    }

    // -----
    public static void updateSendSwableLogs(Context context, boolean sendSwableLogs) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + SEND_SWABLE_LOGS, sendSwableLogs);
        editor.apply();
    }

    public static boolean enableSendSwableLogs(Context context) {
        return getSettings(context).getBoolean(getPackageName(context) + SEND_SWABLE_LOGS, false);
    }

    public static void updateShowCategoriesOnStart(Context context, boolean debugMode) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + SHOW_CATEGORIES_ON_START, debugMode);
        editor.apply();
    }

    public static boolean showCategoriesOnStart(Context context) {
        return getSettings(context).getBoolean(getPackageName(context) + SHOW_CATEGORIES_ON_START, false);
    }

    public static void updateAddOrderTypeCode(Context context, boolean debugMode) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + ADD_ORDER_TYPE_CODE, debugMode);
        editor.apply();
    }

    public static boolean shouldAddOrderTypeCode(Context context) {
        return getSettings(context).getBoolean(getPackageName(context) + ADD_ORDER_TYPE_CODE, true);
    }

    public static void updateDebugMode(Context context, boolean debugMode) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + DEBUG_MODE, debugMode);
        editor.apply();
    }

    public static boolean enableDebugMode(Context context) {
        return getSettings(context).getBoolean(getPackageName(context) + DEBUG_MODE, false);
    }

    public static void updateRequireWarehouse(Context context, boolean requireWarehouse) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + REQUIRE_WAREHOUSE, requireWarehouse);
        editor.apply();
    }

    public static boolean requireWarehouse(Context context) {
        return getSettings(context).getBoolean(getPackageName(context) + REQUIRE_WAREHOUSE, false);
    }

    public static void updateDisableImage(Context context, boolean disableImage) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + DISABLE_IMAGE, disableImage);
        editor.apply();
    }

    public static boolean disableImage(Context context) {
        return getSettings(context).getBoolean(getPackageName(context) + DISABLE_IMAGE, false);
    }

    public static void updateReceiveAutodelete(Context context, int days) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putInt(getPackageName(context) + RECEIVE_AUTODELETE, days);
        editor.apply();
    }

    public static int receiveAutodelete(Context context) {
        return getSettings(context).getInt(getPackageName(context) + RECEIVE_AUTODELETE, 30);
    }

    public static void updateReceiveViewHistoryEvery(Context context, int days) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putInt(getPackageName(context) + RECEIVE_VIEW_HISTORY_EVERY, days);
        editor.apply();
    }

    public static int receiveViewHistoryEvery(Context context) {
        return getSettings(context).getInt(getPackageName(context) + RECEIVE_VIEW_HISTORY_EVERY, 30);
    }

    public static void updatePulloutAutodelete(Context context, int days) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putInt(getPackageName(context) + PULLOUT_AUTODELETE, days);
        editor.apply();
    }

    public static int pulloutAutodelete(Context context) {
        return getSettings(context).getInt(getPackageName(context) + PULLOUT_AUTODELETE, 30);
    }

    public static void updatePulloutViewHistoryEvery(Context context, int days) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putInt(getPackageName(context) + PULLOUT_VIEW_HISTORY_EVERY, days);
        editor.apply();
    }

    public static int pulloutViewHistoryEvery(Context context) {
        return getSettings(context).getInt(getPackageName(context) + PULLOUT_VIEW_HISTORY_EVERY, 30);
    }

    public static void updateSalesAutodelete(Context context, int days) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putInt(getPackageName(context) + SALES_AUTODELETE, days);
        editor.apply();
    }

    public static int salesAutodelete(Context context) {
        return getSettings(context).getInt(getPackageName(context) + SALES_AUTODELETE, 30);
    }

    public static void updateSalesViewHistoryEvery(Context context, int days) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putInt(getPackageName(context) + SALES_VIEW_HISTORY_EVERY, days);
        editor.apply();
    }

    public static int salesViewHistoryEvery(Context context) {
        return getSettings(context).getInt(getPackageName(context) + SALES_VIEW_HISTORY_EVERY, 30);
    }

    public static void updateCountAutodelete(Context context, int days) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putInt(getPackageName(context) + COUNT_AUTODELETE, days);
        editor.apply();
    }

    public static int countAutodelete(Context context) {
        return getSettings(context).getInt(getPackageName(context) + COUNT_AUTODELETE, 360);
    }

    public static void updateCountViewHistoryEvery(Context context, int days) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putInt(getPackageName(context) + COUNT_VIEW_HISTORY_EVERY, days);
        editor.apply();
    }

    public static int countViewHistoryEvery(Context context) {
        return getSettings(context).getInt(getPackageName(context) + COUNT_VIEW_HISTORY_EVERY, 30);
    }

    public static void updateOrderAutodelete(Context context, int days) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putInt(getPackageName(context) + ORDER_AUTODELETE, days);
        editor.apply();
    }

    public static int orderAutodelete(Context context) {
        return getSettings(context).getInt(getPackageName(context) + ORDER_AUTODELETE, 90);
    }

    public static void updateOrderViewHistoryEvery(Context context, int days) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putInt(getPackageName(context) + ORDER_VIEW_HISTORY_EVERY, days);
        editor.apply();
    }

    public static int orderViewHistoryEvery(Context context) {
        return getSettings(context).getInt(getPackageName(context) + ORDER_VIEW_HISTORY_EVERY, 30);
    }

    public static void updateAllowVoid(Context context, boolean allowVoid) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + ALLOW_VOID, allowVoid);
        editor.apply();
    }

    public static boolean allowVoid(Context context) {
        return getSettings(context).getBoolean(getPackageName(context) + ALLOW_VOID, true);
    }

    public static void updateCountAllowEnteringBrand(Context context, boolean countAllowEnteringBrand) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + COUNT_ALLOW_ENTERING_BRAND, countAllowEnteringBrand);
        editor.apply();
    }

    public static boolean countAllowEnteringBrand(Context context) {
        return getSettings(context).getBoolean(getPackageName(context) + COUNT_ALLOW_ENTERING_BRAND, false);
    }

    public static void updateCountAllowEnteringDeliveryDate(Context context, boolean countAllowEnteringDeliveryDate) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(getPackageName(context) + COUNT_ALLOW_ENTERING_DELIVERY_DATE, countAllowEnteringDeliveryDate);
        editor.apply();
    }

    public static boolean countAllowEnteringDeliveryDate(Context context) {
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
