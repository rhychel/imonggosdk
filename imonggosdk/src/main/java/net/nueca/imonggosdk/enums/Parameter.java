package net.nueca.imonggosdk.enums;

/**
 * Created by rhymart on 7/21/14.
 * NuecaLibrary (c)2014
 */
public enum Parameter {
    PAGE, // page=<pageNo> | JSONArray
    ACTIVE_ONLY, // active_only=1 | JSONArray
    LAST_UPDATED_AT, // q=last_updated_at | JSONObject
    AFTER, // after=<date> | JSONArray
    COUNT, // q=count | JSONObject
    USER_ID, // user_id= | JSONArray
    CHECKIN_COUNT,
    BRANCH_ID, // branch_id
    SALES_PROMOTION_DETAIL,
    FROM,
    TO,
    TARGET_BRANCH_ID,
    DOCUMENT_TYPE, // document_type=transfer_out
    INTRANSIT,
    NONE,
    CURRENT_DATE
}
