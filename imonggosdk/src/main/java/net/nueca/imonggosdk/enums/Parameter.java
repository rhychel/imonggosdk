package net.nueca.imonggosdk.enums;

/**
 * Created by rhymart on 7/21/14.
 * NuecaLibrary (c)2014
 */
public enum Parameter {
    PER_PAGE, // per_page<page_limit> | JSONArray
    PAGE, // page=<pageNo> | JSONArray
    ACTIVE_ONLY, // active_only=1 | JSONArray
    LAST_UPDATED_AT, // q=last_updated_at | JSONObject
    AFTER, // after=<date> | JSONArray
    COUNT, // q=count | JSONObject
    USER_ID, // user_id= | JSONArray
    CHECKIN_COUNT,
    BRANCH_ID, // branch_id
    FROM,
    TO,
    TARGET_BRANCH_ID,
    DOCUMENT_TYPE, // document_type=transfer_out
    INTRANSIT,
    NONE,
    CURRENT_DATE,
    SALES_PUSH,
    DETAILS,
    SALES_PROMOTION_ID,
    ID,
    UNITS,
    SALESMAN_ID,
    SALES_DISCOUNT,
    SALES_POINTS,
}
