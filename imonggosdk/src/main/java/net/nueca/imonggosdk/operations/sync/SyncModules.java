package net.nueca.imonggosdk.operations.sync;

import android.os.Handler;
import android.util.Log;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;


import net.nueca.imonggosdk.enums.DailySalesEnums;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Parameter;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchTag;
import net.nueca.imonggosdk.objects.DailySales;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.LastUpdatedAt;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.objects.SalesPushSettings;
import net.nueca.imonggosdk.objects.Settings;
import net.nueca.imonggosdk.objects.TaxRate;
import net.nueca.imonggosdk.objects.TaxSetting;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;
import net.nueca.imonggosdk.objects.associatives.CustomerCustomerGroupAssoc;
import net.nueca.imonggosdk.objects.associatives.ProductTaxRateAssoc;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.base.BatchList;
import net.nueca.imonggosdk.objects.salespromotion.Discount;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.branchentities.BranchProduct;
import net.nueca.imonggosdk.objects.branchentities.BranchUnit;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerCategory;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentPurpose;
import net.nueca.imonggosdk.objects.document.DocumentType;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoicePurpose;
import net.nueca.imonggosdk.objects.invoice.PaymentTerms;
import net.nueca.imonggosdk.objects.invoice.PaymentType;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.objects.price.PriceList;
import net.nueca.imonggosdk.objects.routeplan.RoutePlan;
import net.nueca.imonggosdk.objects.routeplan.RoutePlanDetail;
import net.nueca.imonggosdk.objects.salespromotion.Discount;
import net.nueca.imonggosdk.objects.salespromotion.SalesPromotion;
import net.nueca.imonggosdk.operations.ImonggoTools;
import net.nueca.imonggosdk.operations.http.ImonggoOperations;
import net.nueca.imonggosdk.tools.DateTimeTools;
import net.nueca.imonggosdk.tools.LastUpdateAtTools;
import net.nueca.imonggosdk.tools.LoggingTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Created by Jn on 7/15/2015.
 * imonggosdk(2015)
 * <p/>
 * basic flow of SyncModules
 * + on startFetchingModules()
 * - last_upated_at
 * - count
 * - api_content
 * ~ paging
 */
public class SyncModules extends BaseSyncService implements VolleyRequestListener {

    private static final String TAG = "SyncModules";

    private void startSyncModuleContents(RequestType requestType) throws SQLException {

        mCurrentRequestType = requestType;

        if (getHelper() == null) {
            Log.e(TAG, "helper is null");
        }

        if (requestType == RequestType.COUNT) {
            count = 0;
            page = 1;
            Log.e(TAG, "COUNT");
            ImonggoOperations.getAPIModule(this, getQueue(), getSession(), this,
                    mCurrentTableSyncing, getSession().getServer(), requestType,
                    getParameters(requestType));


        } else if (requestType == RequestType.API_CONTENT) {
            Log.e(TAG, "API CONTENT");

            if (mCurrentTableSyncing == Table.CUSTOMER_BY_SALESMAN ||
                    mCurrentTableSyncing == Table.CUSTOMERS ||
                    mCurrentTableSyncing == Table.CUSTOMER_GROUPS ||
                    mCurrentTableSyncing == Table.BRANCH_CUSTOMERS) {

                if (listOfPricelistIds == null)
                    listOfPricelistIds = new ArrayList<>();

                if (listPriceListStorage == null)
                    listPriceListStorage = new ArrayList<>();
            }

            ImonggoOperations.getAPIModule(this, getQueue(), getSession(), this,
                    mCurrentTableSyncing, getSession().getServer(), requestType,
                    getParameters(requestType));

        } else if (requestType == RequestType.LAST_UPDATED_AT) {
            newLastUpdatedAt = null;
            lastUpdatedAt = null;

            // Get the last updated at
            QueryBuilder<LastUpdatedAt, Integer> queryBuilder = getHelper().fetchIntId(LastUpdatedAt.class).queryBuilder();

            if (mCurrentTableSyncing == Table.DOCUMENTS) {
                document_type = "adjustment_out";
                intransit_status = "1";

                initializeFromTo();

                branchUserAssoc = getHelper().fetchObjectsList(BranchUserAssoc.class);
                queryBuilder.where().eq("tableName", LastUpdateAtTools.getTableToSync(mCurrentTableSyncing, getTargetBranchId(branchIndex) + ""));
            } else {
                queryBuilder.where().eq("tableName", LastUpdateAtTools.getTableToSync(mCurrentTableSyncing));
            }



            // get the last updated at
            lastUpdatedAt = getHelper().fetchObjects(LastUpdatedAt.class).queryForFirst(queryBuilder.prepare());

            if(lastUpdatedAt != null) {
                Log.e(TAG, ">> last update at not null " + lastUpdatedAt.toString());
            } else {
                Log.e(TAG, ">> last update at is null");
            }

            if (mCurrentTableSyncing == Table.DAILY_SALES) {
                ImonggoOperations.getAPIModule(this, getQueue(), getSession(),
                        this, mCurrentTableSyncing, getSession().getServer(),
                        RequestType.DAILY_SALES_TODAY, getParameters(RequestType.DAILY_SALES_TODAY));
                return;
            }

            if (mCurrentTableSyncing == Table.PRICE_LISTS_DETAILS ||
                    mCurrentTableSyncing == Table.PRICE_LISTS_FROM_CUSTOMERS) {
                ImonggoOperations.getAPIModule(this, getQueue(), getSession(), this, Table.PRICE_LISTS,
                        getSession().getServer(), requestType, getParameters(requestType));
                return;
            }

            ImonggoOperations.getAPIModule(this, getQueue(), getSession(), this, mCurrentTableSyncing,
                    getSession().getServer(), requestType, getParameters(requestType));

        } else if (requestType == RequestType.DAILY_SALES_TODAY) {
            ImonggoOperations.getAPIModule(this, getQueue(), getSession(), this, mCurrentTableSyncing,
                    getSession().getServer(), requestType, getParameters(requestType));
            return;
        }
    }

    protected void initializeFromTo() {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        SimpleDateFormat convertStringToDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
        convertStringToDate.setTimeZone(timeZone);
        Calendar now = Calendar.getInstance(timeZone);
        now.setTimeZone(timeZone);
        to = convertStringToDate.format(now.getTime());
        now.add(Calendar.MONTH, -3);
        now.set(Calendar.HOUR_OF_DAY, 23);
        now.set(Calendar.MINUTE, 59);
        now.set(Calendar.SECOND, 59);
        from = convertStringToDate.format(now.getTime());
        Log.e(TAG, "From: " + from + " - To: " + to);
    }

    public int getTargetBranchId(int index) {
        return branchUserAssoc.get(index).getBranch().getId();
    }

    public int getUserBranchesSize() {
        if (branchUserAssoc == null) {
            return 0;
        } else {
            return branchUserAssoc.size();
        }
    }

    private String getParameters(RequestType requestType) {

        if (requestType == RequestType.DAILY_SALES_TODAY) {
            Log.e(TAG, "parameter" + String.format(ImonggoTools.generateParameter(Parameter.CURRENT_DATE, Parameter.BRANCH_ID),
                    DateTimeTools.getCurrentDateTimeWithFormat("yyyy-MM-dd"), getSession().getCurrent_branch_id()) + "");

            return String.format(ImonggoTools.generateParameter(Parameter.CURRENT_DATE, Parameter.BRANCH_ID),
                    DateTimeTools.getCurrentDateTimeWithFormat("yyyy-MM-dd"), getSession().getCurrent_branch_id() + "");
        }

        if (requestType == RequestType.LAST_UPDATED_AT) {

            if (mCurrentTableSyncing == Table.DOCUMENTS) { // This is when the module syncing is DOCUMENTS
                if (initialSync || lastUpdatedAt == null) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.DOCUMENT_TYPE,
                            Parameter.INTRANSIT,
                            Parameter.FROM,
                            Parameter.TO,
                            Parameter.LAST_UPDATED_AT,
                            Parameter.BRANCH_ID),  // RHY: Parameter.TARGET_BRANCH_ID Changed to cater transfer to branch
                            document_type, intransit_status,
                            DateTimeTools.convertDateForUrl(from),
                            DateTimeTools.convertDateForUrl(to),
                            getTargetBranchId(branchIndex));
                } else {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.DOCUMENT_TYPE,
                            Parameter.LAST_UPDATED_AT,
                            Parameter.TARGET_BRANCH_ID,
                            Parameter.AFTER),
                            document_type,
                            getTargetBranchId(branchIndex),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }
            }

            if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_PUSH) {
                return ImonggoTools.generateParameter(Parameter.SALES_PUSH, Parameter.LAST_UPDATED_AT);
            }

            if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_DISCOUNT) {
                return ImonggoTools.generateParameter(Parameter.SALES_DISCOUNT, Parameter.LAST_UPDATED_AT);
            }

            if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_POINTS) {
                return ImonggoTools.generateParameter(Parameter.SALES_POINTS, Parameter.LAST_UPDATED_AT);
            }

            // with branch_id
            if (mCurrentTableSyncing == Table.BRANCH_PRODUCTS ||
                    mCurrentTableSyncing == Table.BRANCH_PRICE_LISTS ||
                    mCurrentTableSyncing == Table.BRANCH_CUSTOMERS) {
                if (initialSync || lastUpdatedAt == null) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.LAST_UPDATED_AT,
                            Parameter.BRANCH_ID),
                            getSession().getCurrent_branch_id());
                } else {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.LAST_UPDATED_AT,
                            Parameter.BRANCH_ID,
                            Parameter.AFTER),
                            getSession().getCurrent_branch_id(),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }
            }

            if (mCurrentTableSyncing == Table.ROUTE_PLANS ) {
                return String.format(ImonggoTools.generateParameter(Parameter.SALESMAN_ID, Parameter.LAST_UPDATED_AT), getSession().getUser_id());
            }

            if(mCurrentTableSyncing == Table.CUSTOMER_BY_SALESMAN) {
                return String.format(ImonggoTools.generateParameter(Parameter.LAST_UPDATED_AT));
            }

            return ImonggoTools.generateParameter(Parameter.LAST_UPDATED_AT);

        } else if (requestType == RequestType.API_CONTENT) {

            // Custom for Tax Settings
            if (mCurrentTableSyncing == Table.TAX_SETTINGS)
                return "";

            if (mCurrentTableSyncing == Table.USERS_ME) {
                return String.format(ImonggoTools.generateParameter(Parameter.ID), getSession().getUser_id());
            }

            if (initialSync || lastUpdatedAt == null) {
                if (mCurrentTableSyncing == Table.BRANCH_USERS)
                    return String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.USER_ID),
                            String.valueOf(page), String.valueOf(getUser().getId()));

                // Custom for Documents
                if (mCurrentTableSyncing == Table.DOCUMENTS) { // Get from Past 3 months til today
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.DOCUMENT_TYPE,
                            Parameter.INTRANSIT,
                            Parameter.PAGE,
                            Parameter.BRANCH_ID, // RHY: Parameter.TARGET_BRANCH_ID Changed to cater transfer to branch
                            Parameter.FROM,
                            Parameter.TO),
                            document_type,
                            intransit_status,
                            String.valueOf(page),
                            getTargetBranchId(branchIndex),
                            DateTimeTools.convertDateForUrl(from),
                            DateTimeTools.convertDateForUrl(to));
                }

                // request with branch id
                if (mCurrentTableSyncing == Table.BRANCH_PRODUCTS ||
                        mCurrentTableSyncing == Table.BRANCH_CUSTOMERS ||
                        mCurrentTableSyncing == Table.BRANCH_PRICE_LISTS) {
                    return String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.BRANCH_ID),
                            String.valueOf(page), getSession().getCurrent_branch_id());
                }

                if (mCurrentTableSyncing == Table.PRICE_LISTS_FROM_CUSTOMERS) {
                    return String.format(ImonggoTools.generateParameter(Parameter.ID),
                            listOfPricelistIds.get(mCustomIndex));
                }

                if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_PUSH) {
                    return ImonggoTools.generateParameter(
                            Parameter.SALES_PUSH);
                }

                if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_POINTS) {
                    return ImonggoTools.generateParameter(
                            Parameter.SALES_POINTS);
                }

                if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_DISCOUNT) {
                    return ImonggoTools.generateParameter(
                            Parameter.SALES_DISCOUNT);
                }

                if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_DISCOUNT_DETAILS ||
                        mCurrentTableSyncing == Table.SALES_PROMOTIONS_POINTS_DETAILS ||
                        mCurrentTableSyncing == Table.ROUTE_PLANS_DETAILS) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.ID,
                            Parameter.DETAILS),
                            listOfIds.get(mCustomIndex).getId());
                }

                if (mCurrentTableSyncing == Table.PRICE_LISTS_DETAILS) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.ID,
                            Parameter.DETAILS,
                            Parameter.ACTIVE_ONLY,
                            Parameter.BRANCH_ID),
                            listOfIds.get(mCustomIndex).getId(),
                            getSession().getCurrent_branch_id());
                }

                if (mCurrentTableSyncing == Table.ROUTE_PLANS ||
                        mCurrentTableSyncing == Table.CUSTOMER_BY_SALESMAN) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.SALESMAN_ID,
                            Parameter.PAGE),
                            getSession().getUser_id(),
                            String.valueOf(page));
                }

                // Default
                return String.format(ImonggoTools.generateParameter(Parameter.PAGE), String.valueOf(page));

            } else {
                // Custom for Branch Users
                if (mCurrentTableSyncing == Table.BRANCH_USERS) {
                    return String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.USER_ID, Parameter.AFTER),
                            String.valueOf(page), String.valueOf(getUser().getId()), DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }

                // Custom for Documents
                if (mCurrentTableSyncing == Table.DOCUMENTS) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.DOCUMENT_TYPE,
                            Parameter.AFTER,
                            Parameter.BRANCH_ID, // RHY: Parameter.TARGET_BRANCH_ID Changed to cater transfer to branch
                            Parameter.PAGE
                            ),
                            document_type,
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()),
                            getTargetBranchId(branchIndex),
                            String.valueOf(page));
                }

                // request with branch id
                if (mCurrentTableSyncing == Table.BRANCH_PRODUCTS ||
                        mCurrentTableSyncing == Table.BRANCH_CUSTOMERS ||
                        mCurrentTableSyncing == Table.BRANCH_PRICE_LISTS) {
                    String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.BRANCH_ID, Parameter.AFTER),
                            String.valueOf(page),
                            getSession().getCurrent_branch_id(),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }

                if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_PUSH) {
                    return String.format(ImonggoTools.generateParameter(Parameter.SALES_PUSH, Parameter.PAGE, Parameter.AFTER),
                            String.valueOf(page), DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }

                if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_DISCOUNT) {
                    return String.format(ImonggoTools.generateParameter(Parameter.SALES_DISCOUNT, Parameter.PAGE, Parameter.AFTER),
                            String.valueOf(page), DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }

                if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_POINTS) {
                    return String.format(ImonggoTools.generateParameter(Parameter.SALES_POINTS, Parameter.PAGE, Parameter.AFTER),
                            String.valueOf(page), DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }

                if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_DISCOUNT_DETAILS ||
                        mCurrentTableSyncing == Table.SALES_PROMOTIONS_POINTS_DETAILS ||
                        mCurrentTableSyncing == Table.ROUTE_PLANS_DETAILS) {
                    String.format(ImonggoTools.generateParameter(
                            Parameter.ID,
                            Parameter.DETAILS,
                            Parameter.AFTER),
                            listOfIds.get(mCustomIndex),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }

                if (mCurrentTableSyncing == Table.PRICE_LISTS_DETAILS) {
                    String.format(ImonggoTools.generateParameter(Parameter.ID, Parameter.DETAILS, Parameter.ACTIVE_ONLY, Parameter.BRANCH_ID, Parameter.AFTER),
                            listOfIds.get(mCustomIndex),
                            getSession().getCurrent_branch_id(),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }

                if (mCurrentTableSyncing == Table.ROUTE_PLANS) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.SALESMAN_ID,
                            Parameter.PAGE,
                            Parameter.AFTER),
                            getSession().getUser_id(),
                            String.valueOf(page),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }

                // Default
                return String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.AFTER),
                        String.valueOf(page), DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
            }
        } else if (requestType == RequestType.COUNT) {
            Log.e(TAG, "Parameters COUNT");

            if (initialSync || lastUpdatedAt == null) {
                if (mCurrentTableSyncing == Table.DOCUMENTS) {
                    return String.format(ImonggoTools.generateParameter(Parameter.DOCUMENT_TYPE,
                            Parameter.INTRANSIT,
                            Parameter.FROM,
                            Parameter.TO,
                            Parameter.COUNT,
                            Parameter.BRANCH_ID), // RHY: Parameter.TARGET_BRANCH_ID Changed to cater transfer to branch
                            document_type, intransit_status, DateTimeTools.convertDateForUrl(from), DateTimeTools.convertDateForUrl(to),
                            getTargetBranchId(branchIndex));
                }

                if (mCurrentTableSyncing == Table.BRANCH_USERS) { // This is when the module syncing is the User Branches
                    return String.format(ImonggoTools.generateParameter(Parameter.COUNT, Parameter.USER_ID),
                            String.valueOf(getUser().getId()));
                }

                if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_PUSH) {
                    return ImonggoTools.generateParameter(Parameter.SALES_PUSH, Parameter.COUNT);
                }

                if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_POINTS) {
                    return ImonggoTools.generateParameter(Parameter.SALES_POINTS, Parameter.COUNT);
                }

                if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_DISCOUNT) {
                    return ImonggoTools.generateParameter(Parameter.SALES_DISCOUNT, Parameter.COUNT);
                }

                // request with branch id
                if (mCurrentTableSyncing == Table.BRANCH_PRODUCTS ||
                        mCurrentTableSyncing == Table.BRANCH_CUSTOMERS ||
                        mCurrentTableSyncing == Table.BRANCH_PRICE_LISTS) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.ACTIVE_ONLY,
                            Parameter.COUNT, Parameter.BRANCH_ID), getSession().getCurrent_branch_id());
                }

                if (mCurrentTableSyncing == Table.ROUTE_PLANS) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.SALESMAN_ID,
                            Parameter.COUNT),
                            getSession().getUser_id());
                }

                if (mCurrentTableSyncing == Table.PRICE_LISTS_FROM_CUSTOMERS) {
                    return ".json" + ImonggoTools.generateParameter(Parameter.COUNT);
                }

                return ImonggoTools.generateParameter(Parameter.COUNT);

            } else {
                if (mCurrentTableSyncing == Table.BRANCH_USERS) { // TODO last_updated_at of this should relay on NOW at the end of the request...
                    return String.format(ImonggoTools.generateParameter(Parameter.COUNT, Parameter.USER_ID, Parameter.AFTER),
                            String.valueOf(getUser().getId()), DateTimeTools.convertDateForUrl(newLastUpdatedAt.getLast_updated_at()));
                }
                if (mCurrentTableSyncing == Table.DOCUMENTS) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.DOCUMENT_TYPE,
                            Parameter.AFTER,
                            Parameter.BRANCH_ID,  // RHY: Parameter.TARGET_BRANCH_ID Changed to cater transfer to branch
                            Parameter.COUNT
                            ),
                            document_type,
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()),
                            getTargetBranchId(branchIndex)
                    );
                }

                // request with branch id
                if (mCurrentTableSyncing == Table.BRANCH_PRODUCTS ||
                        mCurrentTableSyncing == Table.BRANCH_CUSTOMERS ||
                        mCurrentTableSyncing == Table.BRANCH_PRICE_LISTS) {
                    return String.format(ImonggoTools.generateParameter(Parameter.ACTIVE_ONLY,
                            Parameter.COUNT, Parameter.AFTER, Parameter.BRANCH_ID),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()),
                            getSession().getCurrent_branch_id());
                }

                if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_PUSH) {
                    return String.format(ImonggoTools.generateParameter(Parameter.SALES_PUSH, Parameter.COUNT, Parameter.AFTER),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }

                if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_DISCOUNT) {
                    return String.format(ImonggoTools.generateParameter(Parameter.SALES_DISCOUNT, Parameter.COUNT, Parameter.AFTER),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }

                if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_POINTS) {
                    return String.format(ImonggoTools.generateParameter(Parameter.SALES_POINTS, Parameter.COUNT, Parameter.AFTER),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }

                if (mCurrentTableSyncing == Table.ROUTE_PLANS) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.SALESMAN_ID,
                            Parameter.COUNT,
                            Parameter.AFTER),
                            getSession().getUser_id(),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }

                if (mCurrentTableSyncing == Table.PRICE_LISTS_FROM_CUSTOMERS) {
                    return ".json" + String.format(ImonggoTools.generateParameter(
                            Parameter.COUNT,
                            Parameter.AFTER),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }

                return String.format(ImonggoTools.generateParameter(Parameter.COUNT, Parameter.AFTER),
                        DateTimeTools.convertDateForUrl(lastUpdatedAt .getLast_updated_at()));
            }
        }
        return "";
    }

    private boolean syncNext() {
        if (mModulesIndex == (mModulesToSync.length - 1)) {  // this is when there are no left tables to sync
            if (mCurrentTableSyncing == Table.DOCUMENTS) {
                if (branchIndex < (getUserBranchesSize() - 1)) {
                    Log.e(TAG, branchIndex + "-" + (getUserBranchesSize() - 1));
                    try {
                        syncNextDocumentLogic();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    endSyncNext();
                }

            } else {
                endSyncNext();
            }
            return false;
        } else { // if there are still tables to sync, then;
            try {
                syncNextLogic();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void syncNextDocumentLogic() throws SQLException {
        branchIndex++;
        Log.e(TAG, "there are still tables to sync");
        page = 1;
        numberOfPages = 1;
        count = 0;
        startSyncModuleContents(RequestType.LAST_UPDATED_AT);
    }

    private void syncNextLogic() throws SQLException {
        mModulesIndex++;
        Log.e(TAG, "there are still tables to sync");
        page = 1;
        numberOfPages = 1;
        count = 0;

        mCurrentTableSyncing = mModulesToSync[mModulesIndex];

        if (mCurrentTableSyncing == Table.PRICE_LISTS_FROM_CUSTOMERS) {
            //check if price lists is existing
            if (listOfPricelistIds != null) {
                if (listOfPricelistIds.size() != 0) {
                    count = listOfPricelistIds.size();
                    if (mCustomIndex != 0) {
                        mCustomIndex++;
                    }
                    Log.e(TAG, "Added mCustomIndex: " + mCustomIndex);


                    startSyncModuleContents(RequestType.LAST_UPDATED_AT);


                } else {
                    Log.e(TAG, "There's no Price Lists... Downloading Next Module");
                    syncNext();
                }
            } else {
                Log.e(TAG, "There's no Price Lists... Downloading Next Module");
                syncNext();
            }
        } else if (mCurrentTableSyncing == Table.ROUTE_PLANS_DETAILS) {
            listOfIds = new ArrayList<>();

            Log.e(TAG, "Setting Up Route Plan Details...");

            //check if route plan is existing
            if (getHelper().fetchObjectsList(RoutePlan.class).size() != 0) {
                listOfIds = getHelper().fetchObjectsList(RoutePlan.class);
                count = listOfIds.size();
                mCustomIndex = 0;

                Log.e(TAG, "Size of Route Plan: " + listOfIds.size());

                startSyncModuleContents(RequestType.API_CONTENT);
            } else {
                Log.e(TAG, "There's no Route Plan... Downloading Next Module");
                syncNext();
            }
        } else if (mCurrentTableSyncing == Table.PRICE_LISTS_DETAILS) {
            listOfIds = new ArrayList<>();

            Log.e(TAG, "Setting Up Price List Details...");

            //check if price lists is existing
            if (getHelper().fetchObjectsList(PriceList.class).size() != 0) {
                listOfIds = getHelper().fetchObjectsList(PriceList.class);
                count = listOfIds.size();
                mCustomIndex = 0;

                Log.e(TAG, "Size of Price List: " + listOfIds.size());

                startSyncModuleContents(RequestType.API_CONTENT);
            } else {
                Log.e(TAG, "There's no Price List... Downloading Next Module");
                syncNext();
            }

        } else if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_DISCOUNT_DETAILS ||
                mCurrentTableSyncing == Table.SALES_PROMOTIONS_POINTS_DETAILS) {
            listOfIds = new ArrayList<>();

            Log.e(TAG, "Setting Up Sales Promotions Details...");

            if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_DISCOUNT_DETAILS) {
                Log.e(TAG, "HAHAHA SETTING SALES DISCOUNT");
                if (getHelper().fetchObjects(SalesPromotion.class).queryBuilder().where().eq("salesPromotionType", "sales_discounts").query() != null) {
                    listOfIds = getHelper().fetchObjects(SalesPromotion.class).queryBuilder().where().eq("salesPromotionType", "sales_discounts").query();
                }
            }

            if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_POINTS_DETAILS) {
                Log.e(TAG, "HAHAHA SETTING POINTS DISCOUNT");
                if (getHelper().fetchObjects(SalesPromotion.class).queryBuilder().where().eq("salesPromotionType", "points").query() != null) {
                    listOfIds = getHelper().fetchObjects(SalesPromotion.class).queryBuilder().where().eq("salesPromotionType", "points").query();
                }
            }

            if (listOfIds.size() != 0) {
                count = listOfIds.size();
                mCustomIndex = 0;

                Log.e(TAG, "Size of Sales Promotions Discount: " + listOfIds.size());

                startSyncModuleContents(RequestType.API_CONTENT);
            } else {
                Log.e(TAG, "There's no Sales Promotions... Downloading Next Module");
                if (mSyncModulesListener != null) {
                    mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                }
                syncNext();
            }


        } else if (mCurrentTableSyncing == Table.TAX_SETTINGS ||
                mCurrentTableSyncing == Table.DOCUMENT_TYPES ||
                mCurrentTableSyncing == Table.DOCUMENT_PURPOSES ||
                mCurrentTableSyncing == Table.SETTINGS) {
            startSyncModuleContents(RequestType.API_CONTENT);
        } else {
            // otherwise, call the last updated at request {
            startSyncModuleContents(RequestType.LAST_UPDATED_AT);
        }
    }

    private void endSyncNext() {
        Thread sleepFor1Second = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(1000);
                    }
                } catch (InterruptedException ex) {

                }
            }
        };
        sleepFor1Second.start();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (mSyncModulesListener != null) {
                    Log.e(TAG, "finished downloading tables");
                    // When the request is successful
                    mSyncModulesListener.onEndDownload(mCurrentTableSyncing);
                    mSyncModulesListener.onFinishDownload();
                }
            }
        }, 1000);

    }

    @Override
    public void onStart(Table module, RequestType requestType) {
        Log.e(TAG, "onStart downloading " + module.toString() + " " + requestType);
        if (mSyncModulesListener != null) {
            mSyncModulesListener.onStartDownload(module);
        }
    }

    @Override
    public void onSuccess(Table module, RequestType requestType, Object response) {
        Log.e(TAG, "successfully downloaded " + module.toString() + " content: " + response.toString());

        try {
            // JSONObject
            if (response instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) response;
                Log.e(TAG, "content:" + jsonObject.toString());

                // Last Updated At
                if (requestType == RequestType.LAST_UPDATED_AT) {
                    Log.e(TAG, "Last Updated At");
                    // since this is the first
                    count = 0;
                    page = 1;

                    newLastUpdatedAt = gson.fromJson(jsonObject.toString(), LastUpdatedAt.class);

                    if (mCurrentTableSyncing == Table.DOCUMENTS) {
                        newLastUpdatedAt.setTableName(LastUpdateAtTools.getTableToSync(module, getTargetBranchId(branchIndex) + ""));
                        Log.e(TAG, "Table Name: " + newLastUpdatedAt.getTableName());
                    } else {
                        Log.e(TAG, "Setting table name of last update at");
                        newLastUpdatedAt.setTableName(LastUpdateAtTools.getTableToSync(module));
                    }


                    if (lastUpdatedAt != null) {
                        Log.e(TAG, newLastUpdatedAt.toString());
                        Log.e(TAG, lastUpdatedAt.toString());

                        if(newLastUpdatedAt.toString().equals(lastUpdatedAt.toString())) {
                            syncNext();
                            return;
                        } else {
                            Log.e(TAG, ">> Hindi parehas" );
                        }

                        newLastUpdatedAt.updateTo(getHelper());
                    } else {
                        newLastUpdatedAt.insertTo(getHelper());
                        Log.e(TAG, "New Last Updated At: " + jsonObject.toString());
                    }

                    // USERS and  TAX SETTINGS DON'T SUPPORT COUNT
                    if (mCurrentTableSyncing == Table.USERS ||
                            mCurrentTableSyncing == Table.TAX_SETTINGS ||
                            mCurrentTableSyncing == Table.ROUTE_PLANS ||
                            mCurrentTableSyncing == Table.PRICE_LISTS ||
                            mCurrentTableSyncing == Table.PRICE_LISTS_FROM_CUSTOMERS) {

                        startSyncModuleContents(RequestType.API_CONTENT);

                    } else {
                        startSyncModuleContents(RequestType.COUNT);
                    }
                } else if (requestType == RequestType.COUNT) { // COUNT
                    count = jsonObject.getInt("count");
                    Log.e(TAG, "Response: count is " + count);
                    // if table don't have data
                    if (count == 0) {
                        if (mCurrentTableSyncing == Table.DOCUMENTS) {
                            mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, branchIndex, getUserBranchesSize());
                        } else {
                            mSyncModulesListener.onDownloadProgress(module, 1, 1);
                        }
                        if (mCurrentTableSyncing == Table.PRICE_LISTS
                                || mCurrentTableSyncing == Table.PRICE_LISTS_DETAILS
                                || mCurrentTableSyncing == Table.ROUTE_PLANS_DETAILS) {
                            mModulesIndex++;
                        }
                        syncNext();


                    } else {
                        numberOfPages = ((int) Math.ceil(count / 50.0));
                        Log.e(TAG, "number of pages: " + numberOfPages);
                        startSyncModuleContents(RequestType.API_CONTENT);
                    }
                    // API CONTENT
                } else if (requestType == RequestType.API_CONTENT) {
                    Log.e(TAG, "API Content on JSONObject Request");
                    if (mCurrentTableSyncing == Table.TAX_SETTINGS) {

                        TaxSetting taxSetting = gson.fromJson(jsonObject.toString(), TaxSetting.class);

                        if (initialSync || lastUpdatedAt == null) {
                            Log.e(TAG, "Initial Sync Tax Settings");
                            taxSetting.insertTo(getHelper());
                        } else {
                            if (isExisting(taxSetting, Table.TAX_SETTINGS)) {
                                Log.e(TAG, "Table Tax Setting is existing...");
                                taxSetting.updateTo(getHelper());
                            } else {
                                taxSetting.deleteTo(getHelper());
                            }
                        }

                        if (!jsonObject.has("tax_rates")) { //check if there is even a tax_rates field
                            mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                            syncNext();
                            return;
                        }

                        JSONArray jsonArray = jsonObject.getJSONArray("tax_rates");

                        int size = jsonArray.length();
                        if (size == 0) { // Check if there are tax_rates
                            mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                            syncNext();
                            return;
                        }

                        BatchList<TaxRate> newTaxRates = new BatchList<>(DatabaseOperation.INSERT, getHelper());

                        JSONArray taxRateArray = jsonObject.getJSONArray("tax_rates");
                        List<ProductTaxRateAssoc> productTaxRateAssocList = getHelper().fetchObjectsList(ProductTaxRateAssoc.class);

                        List<TaxRate> oldTaxRateList = getHelper().fetchObjectsList(TaxRate.class);
                        List<TaxRate> newTaxRateList = new ArrayList<>();
                        List<TaxRate> deletedTaxRateList = new ArrayList<>();

                        if (taxRateArray.length() == 0) {
                            mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                            syncNext();
                            return;
                        } else {

                            Log.e(TAG, "List of New Tax Rates");
                            for (int x = 0; x < taxRateArray.length(); x++) {
                                JSONObject jsonObject2 = taxRateArray.getJSONObject(x);
                                TaxRate taxRate = gson.fromJson(jsonObject2.toString(), TaxRate.class);
                                newTaxRates.add(taxRate);
                                newTaxRateList.add(taxRate);
                            }

                            if (oldTaxRateList.size() != 0) {
                                Log.e(TAG, "Matching New Tax Rates to Old Tax Rates");
                                for (TaxRate oldTaxRate : oldTaxRateList) {

                                    int count = 0;
                                    for (int i = 0; i < newTaxRateList.size(); i++) {
                                        TaxRate newTaxRate = newTaxRateList.get(i);

                                        if (oldTaxRate.getId() == newTaxRate.getId()) {
                                            count++;
                                            Log.e(TAG, oldTaxRate.getName() + " matched.");
                                        }
                                    }

                                    if (count == 0) {
                                        Log.e(TAG, oldTaxRate.getName() + " don't matched the new tax rates, adding it to be deleted.");
                                        deletedTaxRateList.add(oldTaxRate);
                                    }
                                }

                                if (deletedTaxRateList.size() == 0) {
                                    Log.e(TAG, "There's no deleted tax rates");
                                } else {
                                    Log.e(TAG, "There's " + deletedTaxRateList.size() + " to be deleted tax rates");

                                    for (TaxRate taxRate : deletedTaxRateList) {

                                        getHelper().fetchIntId(TaxRate.class).deleteById(taxRate.getId());
                                    }

                                }
                            }

                            getHelper().deleteAll(TaxRate.class);
                        }

                        newTaxRates.doOperation(TaxRate.class);

                        if (productTaxRateAssocList.size() == 0) {
                            Log.e(TAG, "Product Tax Rate is 0");
                        } else {
                            Log.e(TAG, "Product Tax Rates has values");
                        }

                        for (int i = 0; i < productTaxRateAssocList.size(); i++) {

                            for (int y = 0; y < deletedTaxRateList.size(); y++) {
                                if (productTaxRateAssocList.get(i).getTaxRate().getId() == deletedTaxRateList.get(y).getId()) {
                                    Log.e(TAG, productTaxRateAssocList.get(i).getTaxRate().getName() + " matched! deleting it.");
                                    getHelper().fetchIntId(ProductTaxRateAssoc.class).deleteById(productTaxRateAssocList.get(i).getId());
                                }
                            }
                        }
                        mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                        syncNext();
                    }

                    if (mCurrentTableSyncing == Table.USERS_ME) {
                        if (!jsonObject.isNull("id")) {
                            User user = gson.fromJson(jsonObject.toString(), User.class);
                            Extras extras = new Extras();

                            if (jsonObject.has("extras")) {
                                JSONObject json_extras = jsonObject.getJSONObject("extras");
                                if (json_extras.has("is_salesman")) {
                                    if (!json_extras.getString("is_salesman").isEmpty()) {
                                        extras.setIs_salesman(json_extras.getInt("is_salesman"));
                                        extras.insertTo(getHelper());
                                        user.setExtras(extras);
                                    } else {
                                        Log.e(TAG, "User's Extras' 'is_salesman' field don't have value");
                                    }
                                } else {
                                    Log.e(TAG, "USER Extras don't have 'is_salesman' field");
                                }

                            } else {
                                Log.e(TAG, "USER API don't have field 'extras'");
                            }

                            if (initialSync || lastUpdatedAt == null) {
                                user.insertTo(getHelper());
                            } else {
                                // check if the user tables exist in the database
                                if (isExisting(user, Table.USERS)) {
                                    if (user.getStatus() == null) {
                                        user.updateTo(getHelper());
                                        if (user.getId() == getSession().getUser().getId()) {
                                            Log.e(TAG, "Updating sessions user from " + getSession().getUser().getName() + " to " + user.getName());
                                        }
                                    } else {
                                        user.deleteTo(getHelper());
                                    }
                                } else {  // if not then add it
                                    Log.e(TAG, "adding user entry to be inserted");
                                    user.insertTo(getHelper());
                                }
                            }

                            getSession().setUser(user);
                            getSession().setCurrent_branch_id(user.getHome_branch_id());
                            getSession().updateTo(getHelper());

                            Log.e(TAG, "User: " + user.getName() + "User Home Branch ID: " + user.getHome_branch_id());

                        } else {
                            String message = "Something went wrong.. please contact developer and tell them that there's No User Found";
                            Log.e(TAG, message);
                            LoggingTools.showToast(getApplicationContext(), message);
                        }

                        mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                        syncNext();
                    }

                    if (mCurrentTableSyncing == Table.PRICE_LISTS_FROM_CUSTOMERS) {
                        mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, mCustomIndex, listOfPricelistIds.size());

                        Log.e(TAG, "Custom Index: " + mCustomIndex + "  PriceList Size: " + listOfPricelistIds.size());

                        if (jsonObject.length() == 0) {
                            mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                            syncNext();
                            return;
                        } else {
                            PriceList priceList = gson.fromJson(jsonObject.toString(), PriceList.class);

                            if (jsonObject.has("branch_id")) {
                                // Get The Branch ID
                                int branch_id = !jsonObject.getString("branch_id").equals("") ? jsonObject.getInt("branch_id") : 0;

                                if (branch_id != 0) {
                                    Branch branch_pricelist;
                                    branch_pricelist = getHelper().fetchObjects(Branch.class).queryBuilder().where().eq("id", branch_id).queryForFirst();

                                    if (branch_pricelist == null) {
                                        Log.e(TAG, "can't find branch");
                                    } else {
                                        priceList.setBranch(branch_pricelist);
                                    }
                                }
                            } else {
                                Log.e(TAG, "PRICE_LIST API don't have 'branch_id' field");
                            }

                            Log.e(TAG, "PriceList Custom Index: " + mCustomIndex);

                            BaseTable tempObject;

                            if (listPriceListStorage.get(mCustomIndex) instanceof Customer) {
                                Log.e(TAG, "PriceList came from customer ");
                                tempObject = (Customer) listPriceListStorage.get(mCustomIndex);

                                if (tempObject != null) {
                                    Customer customer = getHelper().fetchObjects(Customer.class).queryBuilder().where().eq("id", tempObject.getId()).queryForFirst();
                                    Log.e(TAG, "Querying for customer with id: " + tempObject.getId());

                                    if (customer != null) {
                                        Log.e(TAG, "Customer found: " + customer.getName());
                                        customer.setPriceList(priceList);
                                        customer.updateTo(getHelper());
                                    } else {
                                        Log.e(TAG, "Customer not found");
                                    }
                                } else {
                                    Log.e(TAG, "Sum ting wong");
                                }

                            } else if (listPriceListStorage.get(mCustomIndex) instanceof CustomerGroup) {
                                Log.e(TAG, "PriceList came from customer group ");
                                tempObject = (CustomerGroup) listPriceListStorage.get(mCustomIndex);

                                if (tempObject != null) {
                                    CustomerGroup customerGroup = getHelper().fetchObjects(CustomerGroup.class).queryBuilder().where().eq("id", tempObject.getId()).queryForFirst();
                                    Log.e(TAG, "Querying for customer group with id: " + tempObject.getId());

                                    if (customerGroup != null) {
                                        Log.e(TAG, "Customer Group found: " + customerGroup.getName());
                                        customerGroup.setPriceList(priceList);
                                        customerGroup.updateTo(getHelper());
                                    } else {
                                        Log.e(TAG, "Customer Group not found");
                                    }
                                } else {
                                    Log.e(TAG, "Sum ting wong");
                                }
                            }


                            if (isExisting(priceList, Table.PRICE_LISTS)) {
                                //TODO: Support last updated at
                                try {
                                    if (DateTimeTools.stringToDate(lastUpdatedAt.getLast_updated_at()).before(DateTimeTools.stringToDate(newLastUpdatedAt.getLast_updated_at())))
                                        priceList.updateTo(getHelper());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                priceList.insertTo(getHelper());
                            }
                            Log.e(TAG, "This Price List ID is: " + listOfPricelistIds.get(mCustomIndex));
                        }

                        updateNext(requestType, listOfPricelistIds.size());
                    }

                } else if (requestType == RequestType.DAILY_SALES_TODAY) {
                    mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                    String date_updated_at = DateTimeTools.getCurrentDateTimeWithFormat("yyyy-MM-dd");
                    String date_requested_at = DateTimeTools.getCurrentDateTimeWithFormat("yyyy-MM-dd HH:mm:ss");

                    DailySales dailySales = gson.fromJson(jsonObject.toString(), DailySales.class);
                    dailySales.setDate_of_sales(date_updated_at);
                    dailySales.setDate_requested_at(date_requested_at);

                    dailySales.setBranch_id(getSession().getCurrent_branch_id());

                    if (!checkDailySales(dailySales, Table.DAILY_SALES, DailySalesEnums.DATE_OF_DAILY_SALES)) {
                        dailySales.insertTo(getHelper());
                    } else {
                        Log.e(TAG, "DailySale is existing checking for time");

                        if (checkDailySales(dailySales, Table.DAILY_SALES, DailySalesEnums.DATE_REQUESTED)) {
                            Log.e(TAG, "Fetched date time is the most recent... updating database");
                            dailySales.updateTo(getHelper());
                        } else {
                            Log.e(TAG, "database data is up to date");
                        }
                    }
                    syncNext();
                }
                // JSONArray //////////////////////////////////////////////////////////////////////
            } else {
                if (response instanceof JSONArray) {
                    JSONArray jsonArray = (JSONArray) response;
                    int size = jsonArray.length();

                    switch (module) { //
                        case USERS:
                            Log.e(TAG, mCurrentTableSyncing + " | size: " + size + " page: " + page + " max page: " + numberOfPages);
                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(module, 1, 1);
                                syncNext();
                                return;
                            } else {
                                // batch list object holder
                                BatchList<User> newUsers = new BatchList<>(DatabaseOperation.INSERT, getHelper()); // container for the new users
                                BatchList<User> updateUsers = new BatchList<>(DatabaseOperation.UPDATE, getHelper()); // container for the updated users
                                BatchList<User> deleteUsers = new BatchList<>(DatabaseOperation.DELETE, getHelper()); // container for the deleted users

                                for (int i = 0; i < size; i++) {
                                    //get the object in the array
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    User user = gson.fromJson(jsonObject.toString(), User.class);
                                    Extras users_extras = new Extras();

                                    if (jsonObject.has("extras")) {
                                        JSONObject json_extras = jsonObject.getJSONObject("extras");
                                        if (json_extras.has("is_salesman")) {
                                            if (!json_extras.getString("is_salesman").isEmpty()) {
                                                // get the extras in json
                                                users_extras.setIs_salesman(json_extras.getInt("is_salesman"));
                                                users_extras.insertTo(getHelper());

                                                // set the extras to users
                                                user.setExtras(users_extras);

                                            } else {
                                                Log.e(TAG, "is_salesman field is empty");
                                            }
                                        } else {
                                            Log.e(TAG, "Users' extras don't have 'is_salesman' field.");
                                        }
                                    } else {
                                        Log.e(TAG, "Users don't have 'extras' field.");
                                    }
                                    if (initialSync || lastUpdatedAt == null) {
                                        newUsers.add(user);
                                    } else {
                                        // check if the user tables exist in the database
                                        if (isExisting(user, Table.USERS)) {
                                            if (user.getStatus() == null) {
                                                updateUsers.add(user);
                                                if (user.getId() == getSession().getUser().getId()) {
                                                    Log.e(TAG, "Updating sessions user from " + getSession().getUser().getName() + " to " + user.getName());
                                                }
                                            } else {
                                                deleteUsers.add(user);
                                            }
                                        } else {  // if not then add it
                                            Log.e(TAG, "adding user entry to be inserted");
                                            newUsers.add(user);
                                        }

                                    }
                                }

                                newUsers.doOperationBT(User.class);
                                updateUsers.doOperationBT(User.class);
                                deleteUsers.doOperationBT(User.class);

                                User current_user = getUser();

                                getSession().setUser(current_user);
                                getSession().setCurrent_branch_id(current_user.getHome_branch_id());
                                Log.e(TAG, "User Home Branch ID: " + current_user.getHome_branch_id());
                                getSession().dbOperation(getHelper(), DatabaseOperation.UPDATE);

                                updateNext(requestType, size);
                            }
                            break;
                        case BRANCH_PRODUCTS:

                            if (size == 0) {
                                syncNext();
                                return;
                            } else {


                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                                    int current_branch_id = getSession().getCurrent_branch_id();

                                    BranchProduct branchProduct = null;
                                    BranchUnit branchUnit = null;
                                    Branch branch = null;
                                    Product product = null;
                                    int branch_product_id = 0;

                                    Log.e(TAG, "---");
                                    // Branch Product Id
                                    if (jsonObject.has("branch_product_id")) {
                                        if (jsonObject.getString("branch_product_id").isEmpty() || !jsonObject.get("branch_product_id").equals(null)) {
                                            branch_product_id = jsonObject.getInt("branch_product_id");
                                            branchProduct.setBranch_product_id(branch_product_id);
                                        } else {
                                            Log.e(TAG, mCurrentTableSyncing + " API 'branch_product_id' field don't have value");
                                        }
                                    } else {
                                        Log.e(TAG, mCurrentTableSyncing + " API don't have 'branch_product_id field");
                                    }

                                    // Branch
                                    if (current_branch_id != -1) {
                                        branch = getHelper().fetchObjects(Branch.class).queryBuilder().where().eq("id", current_branch_id).queryForFirst();
                                        Log.e(TAG, "Branch found: " + branch.getName());
                                    } else {
                                        Log.e(TAG, "Session don't have the current branch id");
                                    }

                                    // Product
                                    if (jsonObject.has("id")) {
                                        if (!jsonObject.getString("id").isEmpty()) {
                                            int product_id = jsonObject.getInt("id");
                                            product = getHelper().fetchObjects(Product.class).queryBuilder().where().eq("id", product_id).queryForFirst();
                                            Log.e(TAG, "Product found: " + product.getName());
                                        } else {
                                            Log.e(TAG, "'id' field from " + mCurrentTableSyncing + " API is empty");
                                        }
                                    } else {
                                        Log.e(TAG, mCurrentTableSyncing + "API don't have 'id' field");
                                    }

                                    if (branch != null || product != null) {
                                        branchProduct = new BranchProduct(product, branch);
                                        Log.e(TAG, "branchProduct created ");

                                        // Name
                                        if (jsonObject.has("name")) {
                                            if (!jsonObject.getString("name").isEmpty()) {
                                                Log.e(TAG, jsonObject.getString("name"));
                                                branchProduct.setName(jsonObject.getString("name"));
                                            } else {
                                                Log.e(TAG, "Branch Product API 'name' field is empty");
                                            }
                                        } else {
                                            Log.e(TAG, mCurrentTableSyncing + " API don't have 'name' 'field");
                                        }

                                        // Description
                                        if (jsonObject.has("description")) {
                                            if (!jsonObject.getString("description").isEmpty()) {
                                                Log.e(TAG, jsonObject.getString("description"));
                                                branchProduct.setDescription(jsonObject.getString("description"));
                                            } else {
                                                Log.e(TAG, "Branch Product API 'description' field is empty");
                                            }
                                        } else {
                                            Log.e(TAG, mCurrentTableSyncing + " API don't have description 'field");
                                        }

                                        // Retail Price
                                        if (jsonObject.has("retail_price")) {
                                            if (!jsonObject.getString("retail_price").isEmpty()) {
                                                Log.e(TAG, jsonObject.getString("retail_price"));
                                                branchProduct.setRetail_price(jsonObject.getDouble("retail_price"));
                                            } else {
                                                Log.e(TAG, "Branch Product API 'retail_price' field is empty");
                                            }
                                        } else {
                                            Log.e(TAG, mCurrentTableSyncing + " API don't have retail_price 'field");
                                        }

                                        // is Base unit
                                        if (jsonObject.has("unit_id")) {

                                            if (!jsonObject.getString("unit_id").isEmpty() && !jsonObject.isNull("unit_id")) {
                                                int unit_id = jsonObject.getInt("unit_id");
                                                Unit unit = getHelper().fetchObjects(Unit.class).queryBuilder().where().eq("id", unit_id).queryForFirst();

                                                if (unit != null) {
                                                    Log.e(TAG, "Unit found! fetching branch unit. Creating Branch Unit");
                                                    branchUnit = new BranchUnit(unit, branch);
                                                    branchUnit.setBranchProduct(branchProduct);
                                                } else {
                                                    Log.e(TAG, "Err Can't find 'unit' field from database");
                                                }

                                            } else {
                                                branchProduct.setIsBaseUnitSellable(true);
                                                branchProduct.setRetail_price(jsonObject.getDouble("unit_retail_price"));
                                                branchProduct.updateTo(getHelper());
                                            }
                                        } else {
                                            branchProduct.setIsBaseUnitSellable(true);
                                            Log.e(TAG, mCurrentTableSyncing + " API don't have 'unit_id' field");
                                        }


                                        // TODO: HOW TO UPDATE flush branch units
                                        // if branch_products exist in database
                                        if (isExisting(branchProduct, Table.BRANCH_PRODUCTS)) {
                                            // Check Last Updated At
                                            try {
                                                if (lastUpdatedAt != null && newLastUpdatedAt != null) {
                                                    if (DateTimeTools.stringToDate(lastUpdatedAt.getLast_updated_at()).before(DateTimeTools.stringToDate(newLastUpdatedAt.getLast_updated_at()))) {
                                                        // get the branch product in the database to delete branch unit
                                                        BranchProduct bp = getHelper().fetchObjects(BranchProduct.class).queryBuilder().
                                                                where().eq("product_id", product).and().eq("branch_id", branch).queryForFirst();

                                                        if (bp != null) {
                                                            List<BranchUnit> branchUnitList = getHelper().fetchObjects(BranchUnit.class).queryBuilder().where().eq("bp_id", bp).query();

                                                            Log.e(TAG, "Branch Unit Size: " + branchUnitList.size());

                                                            for (BranchUnit bU : branchUnitList) {
                                                                Log.e(TAG, "Delete this Branch Unit: " + bU.toString());
                                                                bU.deleteTo(getHelper());
                                                            }
                                                        } else {
                                                            Log.e(TAG, "Can't find branch product");
                                                        }
                                                        branchProduct.updateTo(getHelper());
                                                    } else {
                                                        branchProduct.insertTo(getHelper());
                                                    }
                                                } else {
                                                    branchProduct.insertTo(getHelper());
                                                }
                                            } catch (ParseException e) {
                                                Log.e(TAG, "Date parsing error. " + e.toString());
                                            }
                                        } else {
                                            branchProduct.insertTo(getHelper());
                                        }

                                        Log.e(TAG, "---");

                                        if (branchUnit != null) {
                                            Log.e(TAG, "Inserting Branch Unit");
                                            branchUnit.insertTo(getHelper());
                                        } else {
                                            Log.e(TAG, "Can't insert Branch Unit");
                                        }

                                    } else {
                                        Log.e(TAG, "Can't create Branch Product Object! missing data");
                                    }
                                }

                                updateNext(requestType, size);
                                break;
                            }
                        case PRODUCTS:
                            BatchList<Product> newProducts = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<Product> updateProducts = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
                            BatchList<Product> deleteProducts = new BatchList<>(DatabaseOperation.DELETE, getHelper());

                            Log.e(TAG, "Syncing Page " + page);
                            if (size == 0) {
                                syncNext();
                                return;
                            } else {
                                int current_branch_id = getSession().getCurrent_branch_id();
                                Branch current_branch = getHelper().fetchIntId(Branch.class).queryForId(current_branch_id);

                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Product product = gson.fromJson(jsonObject.toString(), Product.class);
                                    Extras product_extras = new Extras();

                                    if (jsonObject.has("extras")) {
                                        JSONObject json_extras = jsonObject.getJSONObject("extras");

                                        String default_selling_unit = "";
                                        String default_ordering_unit_id = "";

                                        if (json_extras.has("default_selling_unit")) {
                                            default_selling_unit = json_extras.getString("default_selling_unit");
                                        } else {
                                            Log.e(TAG, "API: " + mCurrentTableSyncing + " API don't have extras field 'default_selling_unit' on " + product.getName());
                                        }

                                        if (json_extras.has("default_ordering_unit_id")) {
                                            default_ordering_unit_id = json_extras.getString("default_ordering_unit_id");
                                        } else {
                                            Log.e(TAG, "API: " + mCurrentTableSyncing + " API don't have extras field 'default_ordering_unit_id' on " + product.getName());
                                        }

                                        product_extras.setDefault_ordering_unit_id(default_ordering_unit_id);
                                        product_extras.setDefault_selling_unit(default_selling_unit);
                                        product_extras.insertTo(getHelper());

                                        product.setExtras(product_extras);

                                    } else {
                                        Log.e(TAG, "This Product don't have extras");
                                    }

                                    int tax_branch_id;
                                    int tax_rate_id;
                                    if (jsonObject.has("tax_rates")) {

                                        List<ProductTaxRateAssoc> pTaxRateList = getHelper().fetchObjectsList(ProductTaxRateAssoc.class);

                                        // Deleting Product's ProducTaxRate Entry
                                        for (ProductTaxRateAssoc pTaxRate : pTaxRateList) {
                                            if (product.getId() == pTaxRate.getProduct().getId()) {
                                                Log.e(TAG, "Deleting " + pTaxRate.getProduct().getName() + " Tax Rate is " + pTaxRate.getTaxRate().getName());
                                                getHelper().fetchIntId(ProductTaxRateAssoc.class).deleteById(pTaxRate.getId());
                                            }
                                        }

                                        JSONArray taxRatesArray = jsonObject.getJSONArray("tax_rates");
                                        int taxRateSize = taxRatesArray.length();
                                        for (int x = 0; x < taxRateSize; x++) {
                                            JSONObject jsonTaxRateObject = taxRatesArray.getJSONObject(x);

                                            tax_rate_id = jsonTaxRateObject.getInt("id");

                                            if (!jsonTaxRateObject.getString("branch_id").equals("null") || !jsonTaxRateObject.isNull("branch_id")) {
                                                tax_branch_id = jsonTaxRateObject.getInt("branch_id");
                                            } else {
                                                tax_branch_id = 0;
                                            }

                                            ProductTaxRateAssoc productTaxRate;
                                            TaxRate current_taxRate = getHelper().fetchIntId(TaxRate.class).queryForId(tax_rate_id);

                                            if (isExisting(tax_rate_id, Table.TAX_RATES)) {
                                                // get the tax rate from database

                                                Log.e(TAG, "Product " + product.getName() + " tax is " + current_taxRate.getName());

                                                current_taxRate.setUtc_created_at(jsonTaxRateObject.getString("utc_created_at")); // Created At
                                                current_taxRate.setUtc_updated_at(jsonTaxRateObject.getString("utc_updated_at")); // Updated At
                                                Log.e(TAG, "tax branch id = " + tax_branch_id + ". current branch id " + current_branch_id);


                                                if (!jsonObject.getBoolean("tax_exempt")) {
                                                    Log.e(TAG, "Product is not tax exempted");
                                                    if (tax_branch_id != 0) {
                                                        // check if the tax rate is for you branch
                                                        if (tax_branch_id == current_branch_id) {
                                                            Log.e(TAG, "The product tax rate is for your branch inserting it to database...");
                                                            current_taxRate.setBranch(current_branch);
                                                            productTaxRate = new ProductTaxRateAssoc(product, current_taxRate);
                                                            productTaxRate.insertTo(getHelper());
                                                        } else {
                                                            Log.e(TAG, "The product tax rate is not for your branch. skipping...");
                                                        }
                                                    } else {
                                                        productTaxRate = new ProductTaxRateAssoc(product, current_taxRate);
                                                        Log.e(TAG, "new product tax rate, inserting it to database...");
                                                        productTaxRate.insertTo(getHelper());
                                                    }
                                                } else {
                                                    Log.e(TAG, "Product is tax exempted");
                                                }
                                            }
                                        }
                                    } else {
                                        Log.e(TAG, "Product don't have tax rate");
                                    }

                                    if (jsonObject.has("tag_list")) {
                                        // Save tags to the database
                                        JSONArray tagsListArray = jsonObject.getJSONArray("tag_list");
                                        int tagsSize = tagsListArray.length();
                                        for (int tagsI = 0; tagsI < tagsSize; tagsI++) {
                                            ProductTag productTag = new ProductTag(tagsListArray.getString(tagsI), product);
                                            if (initialSync || lastUpdatedAt == null) {
                                                productTag.dbOperation(getHelper(), DatabaseOperation.INSERT);
                                            } else {
                                                if (isExisting(productTag, Table.PRODUCT_TAGS)) {
                                                    productTag.dbOperation(getHelper(), DatabaseOperation.UPDATE);
                                                } else {
                                                    productTag.dbOperation(getHelper(), DatabaseOperation.INSERT);
                                                }
                                            }
                                        }
                                    }

                                    product.setSearchKey(product.getName() + product.getStock_no());
                                    if (initialSync || lastUpdatedAt == null) {
                                        newProducts.add(product);
                                    } else {
                                        if (isExisting(product, Table.PRODUCTS)) {
                                            DeleteBuilder<ProductTag, Integer> deleteProductsHelper = getHelper().fetchIntId(ProductTag.class).deleteBuilder();
                                            deleteProductsHelper.where().eq("product_id", product);
                                            deleteProductsHelper.delete();

                                            if (product.getStatus() == null) {
                                                updateProducts.add(product);
                                            } else {
                                                deleteProducts.add(product);
                                            }
                                        } else {
                                            newProducts.add(product);
                                        }
                                    }

                                }

                                newProducts.doOperationBT(Product.class);
                                updateProducts.doOperationBT(Product.class);
                                deleteProducts.doOperationBT(Product.class);

                                updateNext(requestType, size);
                            }
                            break;
                        case UNITS:
                            if (size == 0) {
                                syncNext();
                                return;
                            } else {
                                BatchList<Unit> newUnits = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                                BatchList<Unit> deleteUnits = new BatchList<>(DatabaseOperation.DELETE, getHelper());
                                BatchList<Unit> updateUnits = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                                Extras unit_extras = new Extras();

                                for (int i = 0; i < size; i++) {

                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Unit unit = gson.fromJson(jsonObject.toString(), Unit.class);


                                    if (jsonObject.has("extras")) {
                                        JSONObject json_extras = jsonObject.getJSONObject("extras");

                                        if (json_extras.has("is_default_selling_unit")) {

                                            if (!json_extras.getString("is_default_selling_unit").isEmpty()) {
                                                // get the boolean from extras
                                                Boolean is_default_selling_unit = json_extras.getBoolean("is_default_selling_unit");
                                                unit_extras.setIs_default_selling_unit(is_default_selling_unit);
                                                unit_extras.insertTo(getHelper());

                                                // set the extras
                                                unit.setExtras(unit_extras);
                                            } else {
                                                Log.e(TAG, "'is_default_selling_unit' field is null");
                                            }
                                        } else {
                                            Log.e(TAG, "Units don't have extras 'is_default_selling_unit' field ");
                                        }
                                    } else {
                                        Log.e(TAG, "Units don't have 'extras' field");
                                    }

                                    Product product = getHelper().fetchObjects(Product.class).queryBuilder().where().eq("id", jsonObject.getString("product_id")).queryForFirst();

                                    if (product != null) {
                                        unit.setProduct(product);
                                    } else {
                                        Log.e(TAG, "Can't find product with id: " + jsonObject.getString("product_id"));
                                    }

                                    if (initialSync || lastUpdatedAt == null) {
                                        newUnits.add(unit);
                                    } else {
                                        if (isExisting(unit, Table.UNITS)) {
                                            if (unit.getStatus() == null) {
                                                updateUnits.add(unit);
                                            } else {
                                                deleteUnits.add(unit);
                                            }
                                        } else {
                                            newUnits.add(unit);
                                        }
                                    }
                                }

                                newUnits.doOperationBT(Unit.class);
                                updateUnits.doOperationBT(Unit.class);
                                deleteUnits.doOperationBT(Unit.class);

                                updateNext(requestType, size);
                            }
                            break;
                        case BRANCHES:
                        case BRANCH_USERS:
                            if (size == 0) {
                                syncNext();
                                return;
                            } else {

                                BatchList<Branch> newBranches = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                                BatchList<Branch> updateBranches = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                                BatchList<BranchTag> newBranchTags = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                                BatchList<BranchTag> updateBranchTags = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                                BatchList<BranchUserAssoc> newBranchUserAssocs = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                                BatchList<BranchUserAssoc> updateBranchUserAssocs = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Branch branch = gson.fromJson(jsonObject.toString(), Branch.class);

                                    if (branch.getSite_type() != null && branch.getSite_type().equals("head_office"))
                                        continue;

                                    BranchUserAssoc branchUserAssoc = new BranchUserAssoc(branch, getUser());

                                    //  if (jsonArray.getJSONObject(i).getString("site_type").equals("null")) {
                                    Log.e(TAG, jsonArray.getJSONObject(i).toString());

                                    if (initialSync || lastUpdatedAt == null) {
                                        newBranches.add(branch);
                                        newBranchUserAssocs.add(branchUserAssoc);
                                    } else {
                                        if (isExisting(branch, Table.BRANCHES)) {
                                            updateBranches.add(branch);
                                            updateBranchUserAssocs.add(branchUserAssoc);
                                        } else {
                                            newBranches.add(branch);
                                            newBranchUserAssocs.add(branchUserAssoc);
                                        }
                                    }
                                    // }

                                    if (jsonObject.has("tag_list")) {
                                        JSONArray tagsListArray = jsonObject.getJSONArray("tag_list");
                                        int tagsSize = tagsListArray.length();

                                        for (int tagsI = 0; tagsI < tagsSize; tagsI++) {
                                            BranchTag branchTag = new BranchTag(tagsListArray.getString(tagsI), branch);
                                            if (initialSync || lastUpdatedAt == null) {
                                                if (isExisting(branchTag, Table.BRANCH_TAGS)) {
                                                    updateBranchTags.add(branchTag);
                                                } else {
                                                    newBranchTags.add(branchTag);
                                                }
                                            } else {
                                                updateBranchTags.add(branchTag);
                                            }
                                        }
                                    }
                                }
                                newBranches.doOperationBT(Branch.class);
                                updateBranches.doOperationBT(Branch.class);

                                newBranchUserAssocs.doOperation(BranchUserAssoc.class);
                                updateBranchUserAssocs.doOperation(BranchUserAssoc.class);

                                newBranchTags.doOperation(BranchTag.class);
                                updateBranchTags.doOperation(BranchTag.class);

                                updateNext(requestType, size);
                            }
                            break;
                        case CUSTOMER_BY_SALESMAN:
                        case BRANCH_CUSTOMERS:
                        case CUSTOMERS:
                            BatchList<Customer> newCustomer = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<Customer> updateCustomer = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
                            BatchList<Customer> deleteCustomer = new BatchList<>(DatabaseOperation.DELETE, getHelper());
                            if (size == 0) {
                                syncNext();
                                return;
                            } else {
                                String name_customer_category = "customer_category_id";
                                String name_salesman_id = "salesman_id";
                                String name_extras = "extras";

                                int user_id;
                                int customer_category_id;
                                String salesman_id;

                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Customer customer = gson.fromJson(jsonObject.toString(), Customer.class);
                                    customer.setSearchKey(customer.getName() + customer.getCode() + customer.getAlternate_code()); // # searchkey
                                    Extras customer_extras = new Extras();
                                    PriceList priceList;


                                    // EXTRAS
                                    if (jsonObject.has(name_extras)) {
                                        JSONObject json_extras = jsonObject.getJSONObject(name_extras);

                                        user_id = 0;
                                        customer_category_id = 0;

                                        if (json_extras.has(name_salesman_id)) {
                                            if (!json_extras.getString(name_salesman_id).isEmpty()) {
                                                salesman_id = json_extras.getString(name_salesman_id);
                                                customer_extras.setSalesman_id(salesman_id);
                                            } else {
                                                Log.e(TAG, mCurrentTableSyncing + " API '" + name_salesman_id + "' field don't have value.");
                                            }
                                        } else {
                                            Log.e(TAG, mCurrentTableSyncing + " API don't have '" + name_salesman_id + "' field.");
                                        }

                                        if (json_extras.has(name_customer_category)) {
                                            if (!json_extras.getString(name_customer_category).isEmpty()) {
                                                customer_category_id = json_extras.getInt(name_customer_category);
                                            } else {
                                                Log.e(TAG, mCurrentTableSyncing + " API '" + name_customer_category + "' field don't have value.");
                                            }
                                        } else {
                                            Log.e(TAG, mCurrentTableSyncing + " API don't have '" + name_customer_category + "' field.");
                                        }

                                        User user;
                                        if (user_id != 0) {
                                            user = getHelper().fetchObjects(User.class).queryBuilder().where().eq("id", user_id).queryForFirst();
                                            if (user != null) {
                                                customer_extras.setUser(user);
                                            } else {
                                                Log.e(TAG, "User not found");
                                            }
                                        }
                                        CustomerCategory customerCategory = null;
                                        if (customer_category_id != 0) {
                                            customerCategory = getHelper().fetchObjects(CustomerCategory.class).queryBuilder().where().eq("id", customer_category_id).queryForFirst();

                                            if (customerCategory != null) {
                                                customer_extras.setCustomerCategory(customerCategory);
                                            } else {
                                                Log.e(TAG, "Customer Category not found");
                                            }
                                        }
                                        customer.setExtras(customer_extras);
                                    }

                                    // PRICE_LIST
                                    if (jsonObject.has("price_list_id")) {
                                        if (!jsonObject.isNull("price_list_id")) {
                                            int price_list_id = jsonObject.getInt("price_list_id");

                                            Log.e(TAG, "Adding Price List ID: " + price_list_id);
                                            listOfPricelistIds.add(price_list_id);
                                            listPriceListStorage.add(customer);


                                            priceList = getHelper().fetchObjects(PriceList.class).queryBuilder().where().eq("id", price_list_id).queryForFirst();

                                            if (priceList != null) {
                                                Log.e(TAG, "Price Lists found! " + priceList.toString());
                                                customer.setPriceList(priceList); // Connected
                                            } else {
                                                Log.e(TAG, "Price Lists not found!");
                                            }
                                        } else {
                                            Log.e(TAG, "price_list_id of customer don't have value");
                                        }
                                    } else {
                                        Log.e(TAG, mCurrentTableSyncing + " API don't have 'price_list_id' field");
                                    }

                                    // Customer Group
                                    if (jsonObject.has("customer_groups")) {
                                        if (!jsonObject.isNull("customer_groups")) {
                                            JSONArray customerGroupJSONArray = jsonObject.getJSONArray("customer_groups");
                                            for (int l = 0; l < customerGroupJSONArray.length(); l++) {
                                                JSONObject customerGroupJSONObject = customerGroupJSONArray.getJSONObject(l);

                                                if (customerGroupJSONObject.has("id")) {
                                                    if (!customerGroupJSONObject.isNull("id")) {
                                                        CustomerCustomerGroupAssoc customerCustomerGroupAssoc;
                                                        CustomerGroup xcustomerGroup = getHelper().fetchObjects(CustomerGroup.class).queryBuilder().where().eq("id", customerGroupJSONObject.getInt("id")).queryForFirst();
                                                        CustomerGroup customerGroupNet = gson.fromJson(customerGroupJSONObject.toString(), CustomerGroup.class);

                                                        Log.e(TAG, customerGroupJSONObject.toString());

                                                        if (customerGroupJSONObject.has("price_list_id")) {
                                                            if (!customerGroupJSONObject.isNull("price_list_id")) {
                                                                int price_list_id = customerGroupJSONObject.getInt("price_list_id");

                                                                Log.e(TAG, "Price List from customer group of customer: " + price_list_id);
                                                                listOfPricelistIds.add(price_list_id);
                                                                listPriceListStorage.add(customerGroupNet);

                                                            } else {
                                                                Log.e(TAG, "Price List ID don't have value");
                                                            }

                                                        } else {
                                                            Log.e(TAG, mCurrentTableSyncing + " API's CustomerGroup JSONObject field don't have field 'price_list_id'");
                                                        }

                                                        if (xcustomerGroup == null) {
                                                            customerGroupNet.insertTo(getHelper());
                                                            customerCustomerGroupAssoc = new CustomerCustomerGroupAssoc(customer, customerGroupNet);
                                                        } else {
                                                            customerCustomerGroupAssoc = new CustomerCustomerGroupAssoc(customer, xcustomerGroup);
                                                        }


                                                        customerCustomerGroupAssoc.insertTo(getHelper());
                                                    } else {
                                                        Log.e(TAG, "'customer_groups' field don't have value.");
                                                    }

                                                } else {
                                                    Log.e(TAG, mCurrentTableSyncing + "API don't have 'customer_groups' field.");
                                                }
                                            }

                                        } else {
                                            Log.e(TAG, "'customer_groups' field is empty");
                                        }
                                    } else {
                                        Log.e(TAG, mCurrentTableSyncing + " API don't have 'customer_groups' field");
                                    }

                                    if (initialSync || lastUpdatedAt == null) {
                                        newCustomer.add(customer);
                                    } else {
                                        if (isExisting(customer, Table.CUSTOMERS)) {
                                            if (customer.getStatus() == null) {
                                                updateCustomer.add(customer);
                                            } else {
                                                deleteCustomer.add(customer);
                                            }
                                        } else {
                                            newCustomer.add(customer);
                                        }
                                    }
                                }

                                newCustomer.doOperationBT(Customer.class);
                                updateCustomer.doOperationBT(Customer.class);
                                deleteCustomer.doOperationBT(Customer.class);

                                updateNext(requestType, size);
                            }
                            break;
                        case INVENTORIES:
                            if (size == 0) {
                                syncNext();
                                return;
                            } else {
                                BatchList<Inventory> newInventories = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                                BatchList<Inventory> updateInventories = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Inventory inventory = gson.fromJson(jsonObject.toString(), Inventory.class);
                                    //inventory.setProduct();
                                    if (initialSync || lastUpdatedAt == null) {
                                        newInventories.add(inventory);
                                    } else {
                                        if (isExisting(inventory, Table.INVENTORIES)) {
                                            updateInventories.add(inventory);
                                        } else {
                                            newInventories.add(inventory);
                                        }
                                    }
                                }

                                newInventories.doOperation(Inventory.class);
                                updateInventories.doOperation(Inventory.class);
                                updateNext(requestType, size);
                            }
                            break;
                        case DOCUMENT_TYPES:
                            BatchList<DocumentType> newDocumentType = new BatchList<>(DatabaseOperation.INSERT, getHelper());

                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                syncNext();
                                return;
                            } else {

                                getHelper().deleteAll(DocumentType.class);

                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    DocumentType documentType = gson.fromJson(jsonObject.toString(), DocumentType.class);
                                    newDocumentType.add(documentType);
                                }
                            }

                            newDocumentType.doOperationBT(DocumentType.class);
                            updateNext(requestType, size);
                            break;
                        case DOCUMENT_PURPOSES:
                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                syncNext();
                                return;
                            } else {
                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    DocumentPurpose documentPurpose = gson.fromJson(jsonObject.toString(), DocumentPurpose.class);

                                    Log.e(TAG, documentPurpose.toString());

                                    int document_id = jsonObject.getInt("document_type_id");
                                    DocumentType documentType = getHelper().fetchIntId(DocumentType.class).queryForId(document_id);

                                    if (isExisting(documentType, Table.DOCUMENT_TYPES)) {
                                        documentPurpose.setDocumentType(documentType);
                                    } else {
                                        Log.e(TAG, "Document Type's don't have Doc Type");
                                    }

                                    if (initialSync || lastUpdatedAt == null) {
                                        documentPurpose.dbOperation(getHelper(), DatabaseOperation.INSERT);
                                    } else {
                                        if (isExisting(documentPurpose, Table.DOCUMENT_PURPOSES)) {
                                            if (documentPurpose.getStatus().equalsIgnoreCase("D")) {
                                                documentPurpose.dbOperation(getHelper(), DatabaseOperation.DELETE);
                                            } else {
                                                documentPurpose.dbOperation(getHelper(), DatabaseOperation.UPDATE);
                                            }
                                        } else {
                                            documentPurpose.dbOperation(getHelper(), DatabaseOperation.INSERT);
                                        }
                                    }
                                }
                            }
                            updateNext(requestType, size);
                            break;
                        case DOCUMENTS:
                            BatchList<Document> newDocument = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<Document> updateDocument = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
                            BatchList<Document> deleteDocument = new BatchList<>(DatabaseOperation.DELETE, getHelper());

                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, branchIndex, getUserBranchesSize());
                                syncNext();
                                return;
                            } else {
                                int progress = (int) Math.ceil((((double) branchIndex / (double) getUserBranchesSize()) * 100.0));
                                int progress2 = (int) Math.ceil((((double) page / (double) progress) * 100.0));
                                int progress3 = (int) Math.ceil((((double) size / (double) progress2) * 100.0));

                                for (int i = 0; i < size; i++) {
                                    mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, i, progress3);
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Document document = gson.fromJson(jsonObject.toString(), Document.class);

                                    if (initialSync || lastUpdatedAt == null) {
                                        if (jsonObject.has("received")) {
                                            if (!jsonObject.isNull("received") & !jsonObject.getString("received").isEmpty()) {
                                                if (!document.getIntransit_status().equalsIgnoreCase("received")) {
                                                    newDocument.add(document);
                                                }
                                            }
                                        }
                                    } else {
                                        if (isExisting(document, Table.DOCUMENTS)) {

                                            if (document.getIntransit_status().equalsIgnoreCase("received")) {
                                                deleteDocument.add(document);
                                            } else {
                                                updateDocument.add(document);
                                            }
                                        } else {
                                            newDocument.add(document);
                                        }
                                    }
                                    Log.e(TAG, "Document Content: " + document.toString());
                                }
                            }

                            newDocument.doOperationBT(Document.class);
                            updateDocument.doOperationBT(Document.class);
                            deleteDocument.doOperationBT(Document.class);
                            updateNext(requestType, size);
                            break;
                        case SETTINGS:
                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                syncNext();
                                return;
                            } else {
                                getHelper().fetchObjects(Settings.class).deleteBuilder().delete();

                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Log.e(TAG, jsonObject.getString("name") + " - " + jsonObject.getString("value"));
                                    Settings settings = new Settings(i, jsonObject.getString("name"), jsonObject.getString("value"));
                                    settings.insertTo(getHelper());
                                }

                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                            }

                            syncNext();
                            break;

                        case INVOICES:
                            BatchList<Invoice> newInvoice = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<Invoice> updateInvoice = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                syncNext();
                                return;
                            } else {


                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Invoice invoice = gson.fromJson(jsonObject.toString(), Invoice.class);

                                    if (initialSync || lastUpdatedAt == null) {
                                        newInvoice.add(invoice);
                                    } else {
                                        if (isExisting(invoice, Table.INVOICES)) {
                                            updateInvoice.add(invoice);
                                        } else {
                                            newInvoice.add(invoice);
                                        }
                                    }
                                    newInvoice.doOperationBT2(Invoice.class);
                                    updateInvoice.doOperationBT2(Invoice.class);
                                }
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, page, numberOfPages);
                            }

                            updateNext(requestType, size);
                            break;
                        case INVOICE_PURPOSES:
                            BatchList<InvoicePurpose> newInvoicePurpose = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<InvoicePurpose> updateInvoicePurpose = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                syncNext();
                                return;
                            } else {
                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    InvoicePurpose invoicePurpose = gson.fromJson(jsonObject.toString(), InvoicePurpose.class);
                                    Extras extras = new Extras();
                                    if (jsonObject.has("extras")) {
                                        JSONObject json_extras = jsonObject.getJSONObject("extras");
                                        if (json_extras.has("require_date")) {
                                            if (!json_extras.getString("require_date").isEmpty()) {
                                                extras.setRequire_date(json_extras.getBoolean("require_date"));
                                                extras.insertTo(getHelper());

                                                invoicePurpose.setExtras(extras);
                                                Log.e(TAG, "Invoice Purposes Extras: " + json_extras.getString("require_date"));
                                            } else {
                                                Log.e(TAG, "Invoice Purposes' Extras' 'require_date' field don't have value");
                                            }
                                        } else {
                                            Log.e(TAG, "Invoice Purposes' Extras don't have 'require_date' field");
                                        }

                                    } else {
                                        Log.e(TAG, mCurrentTableSyncing + "API don't have 'extras' field");
                                    }

                                    if (initialSync || lastUpdatedAt == null) {
                                        newInvoicePurpose.add(invoicePurpose);
                                    } else {
                                        if (isExisting(invoicePurpose, Table.INVOICE_PURPOSES)) {
                                            updateInvoicePurpose.add(invoicePurpose);
                                        } else {
                                            newInvoicePurpose.add(invoicePurpose);
                                        }
                                    }
                                }

                                newInvoicePurpose.doOperationBT(InvoicePurpose.class);
                                updateInvoicePurpose.doOperationBT(InvoicePurpose.class);
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, page, numberOfPages);
                            }
                            updateNext(requestType, size);
                            break;
                        case CUSTOMER_GROUPS:
                            BatchList<CustomerGroup> newCustomerGroups = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<CustomerGroup> updateCustomerGroups = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                syncNext();
                                return;
                            } else {
                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    CustomerGroup customerGroup = gson.fromJson(jsonObject.toString(), CustomerGroup.class);

                                    if (jsonObject.has("price_list_id")) {

                                        if (!jsonObject.getString("price_list_id").isEmpty() && !jsonObject.get("price_list_id").equals(null)) {
                                            int price_list_id = jsonObject.getInt("price_list_id");

                                            Log.e(TAG, "Price List ID: " + price_list_id);
                                            listOfPricelistIds.add(price_list_id);
                                            listPriceListStorage.add(customerGroup);

                                            PriceList priceList = getHelper().fetchObjects(PriceList.class).queryBuilder().where().eq("id", price_list_id).queryForFirst();

                                            if (priceList != null) {
                                                Log.e(TAG, "Price Lists found! " + priceList.toString());
                                                customerGroup.setPriceList(priceList); // Connected
                                            } else {
                                                Log.e(TAG, "Price Lists not found!");
                                            }
                                        } else {
                                            Log.e(TAG, "Price List ID don't have value");
                                        }

                                    } else {
                                        Log.e(TAG, mCurrentTableSyncing + " API don't have field 'price_list_id'");
                                    }

                                    if (initialSync || lastUpdatedAt == null) {
                                        newCustomerGroups.add(customerGroup);
                                    } else {
                                        if (isExisting(customerGroup, Table.CUSTOMER_GROUPS)) {
                                            updateCustomerGroups.add(customerGroup);
                                        } else {
                                            newCustomerGroups.add(customerGroup);
                                        }
                                    }
                                }

                                newCustomerGroups.doOperationBT(CustomerGroup.class);
                                updateCustomerGroups.doOperationBT(CustomerGroup.class);
                            }

                            updateNext(requestType, size);
                            break;
                        case CUSTOMER_CATEGORIES:
                            BatchList<CustomerCategory> newCustomerCategory = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<CustomerCategory> updateCustomerCategory = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                syncNext();
                                return;
                            } else {
                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    CustomerCategory customerCategory = gson.fromJson(jsonObject.toString(), CustomerCategory.class);

                                    if (initialSync || lastUpdatedAt == null) {
                                        newCustomerCategory.add(customerCategory);
                                    } else {
                                        if (isExisting(customerCategory, Table.CUSTOMER_CATEGORIES)) {
                                            updateCustomerCategory.add(customerCategory);
                                        } else {
                                            newCustomerCategory.add(customerCategory);
                                        }
                                    }
                                }
                            }

                            newCustomerCategory.doOperationBT(CustomerCategory.class);
                            updateCustomerCategory.doOperationBT(CustomerCategory.class);
                            updateNext(requestType, size);
                            break;
                        case PAYMENT_TERMS:
                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                syncNext();
                                return;
                            } else {
                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    PaymentTerms paymentTerms = gson.fromJson(jsonObject.toString(), PaymentTerms.class);

                                    if (initialSync || lastUpdatedAt == null) {
                                        paymentTerms.insertTo(getHelper());
                                    } else {
                                        if (isExisting(paymentTerms, Table.PAYMENT_TERMS)) {
                                            if (jsonObject.has("status") && !jsonObject.isNull("status")) {
                                                if (jsonObject.get("status").equals("A")) {
                                                    paymentTerms.updateTo(getHelper());
                                                } else if (jsonObject.get("status").equals("I")) {
                                                    paymentTerms.deleteTo(getHelper());
                                                }
                                            }
                                        } else {
                                            paymentTerms.insertTo(getHelper());
                                        }
                                    }
                                }
                            }
                            updateNext(requestType, size);
                            break;
                        case PAYMENT_TYPES:
                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                syncNext();
                                return;
                            } else {
                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    PaymentType paymentType = gson.fromJson(jsonObject.toString(), PaymentType.class);

                                    if (initialSync || lastUpdatedAt == null) {
                                        paymentType.insertTo(getHelper());
                                    } else {
                                        if (isExisting(paymentType, Table.PAYMENT_TERMS)) {
                                            if (jsonObject.has("status") && !jsonObject.isNull("status")) {
                                                if (jsonObject.get("status").equals("A")) {
                                                    paymentType.updateTo(getHelper());
                                                } else if (jsonObject.get("status").equals("I")) {
                                                    paymentType.updateTo(getHelper());
                                                }
                                            }
                                        } else {
                                            paymentType.insertTo(getHelper());
                                        }
                                    }
                                }
                            }
                            updateNext(requestType, size);
                            break;
                        case SALES_PROMOTIONS_SALES_DISCOUNT:
                        case SALES_PROMOTIONS_SALES_PUSH:
                        case SALES_PROMOTIONS_POINTS:
                        case SALES_PROMOTIONS:
                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                syncNext();
                                return;
                            } else {
                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                                    SalesPromotion salesPromotion = gson.fromJson(jsonObject.toString(), SalesPromotion.class);

                                    if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_PUSH) {
                                        salesPromotion.setSalesPromotionType("sales_push");
                                    }

                                    if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_DISCOUNT) {
                                        salesPromotion.setSalesPromotionType("sales_discounts");
                                    }

                                    if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_POINTS) {
                                        salesPromotion.setSalesPromotionType("points");
                                    }
                                    SalesPushSettings salesPushSettings = null;
                                    if (jsonObject.has("settings")) {
                                        JSONObject settingsJSONObject = jsonObject.getJSONObject("settings");
                                        if (settingsJSONObject != null) {
                                            salesPushSettings = gson.fromJson(settingsJSONObject.toString(), SalesPushSettings.class);
                                            salesPushSettings.insertTo(getHelper());
                                            salesPromotion.setSettings(salesPushSettings);
                                        } else {
                                            Log.e(TAG, "settings don't have value");
                                        }
                                    } else {
                                        Log.e(TAG, mCurrentTableSyncing + "API don't have 'settings' field");
                                    }

                                    Log.e(TAG, ">> status: " + jsonObject.getString("status"));
                                    if (initialSync || lastUpdatedAt == null) {
                                        boolean status = true;

                                        if (jsonObject.get("status").equals("D")) {
                                            status = false;
                                        }

                                        if (jsonObject.get("status").equals("I")) {
                                            status = false;
                                        }

                                        if (status) {
                                            Log.e(TAG, ">> Saving sales promotion with status: " + jsonObject.getString("status"));
                                            salesPromotion.insertTo(getHelper());
                                            if (salesPushSettings != null) {
                                                salesPushSettings.setSalesPromotion(salesPromotion); // connection
                                                salesPushSettings.updateTo(getHelper());
                                            }
                                        } else {
                                            Log.e(TAG, ">> skipping sales promotion with status: " + jsonObject.getString("status"));
                                        }
                                    } else {
                                        if (isExisting(salesPromotion, Table.SALES_PROMOTIONS)) {
                                            if (jsonObject.get("status").equals("A")) {
                                                salesPromotion.updateTo(getHelper());
                                                if (salesPushSettings != null) {
                                                    salesPushSettings.setSalesPromotion(salesPromotion); // connection
                                                    salesPushSettings.updateTo(getHelper());

                                                }
                                            } else {
                                                List<Discount> discountList = getHelper().fetchObjects(Discount.class).queryBuilder().where().eq("sales_promotion_id", salesPromotion).query();
                                                //delete discount connected
                                                for (Discount discount : discountList) {
                                                    discount.deleteTo(getHelper());
                                                }

                                                salesPromotion.deleteTo(getHelper());

                                                if (salesPushSettings != null) {
                                                    salesPushSettings.deleteTo(getHelper());
                                                }
                                            }
                                        } else {
                                            if (!jsonObject.get("status").equals("D") || !jsonObject.get("status").equals("I")) {
                                                salesPromotion.insertTo(getHelper());
                                                if (salesPushSettings != null) {
                                                    salesPushSettings.setSalesPromotion(salesPromotion); // connection
                                                    salesPushSettings.updateTo(getHelper());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            updateNext(requestType, size);
                            break;
                        case BRANCH_PRICE_LISTS:
                        case PRICE_LISTS:
                            BatchList<PriceList> newPriceList = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<PriceList> updatePriceList = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                syncNext();
                                return;
                            } else {

                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    PriceList priceList = gson.fromJson(jsonObject.toString(), PriceList.class);

                                    if (jsonObject.has("branch_id")) {
                                        // Get The Branch ID
                                        int branch_id = !jsonObject.getString("branch_id").equals("") ? jsonObject.getInt("branch_id") : 0;

                                        if (branch_id != 0) {
                                            Branch branch_pricelist;
                                            branch_pricelist = getHelper().fetchObjects(Branch.class).queryBuilder().where().eq("id", branch_id).queryForFirst();

                                            if (branch_pricelist == null) {
                                                Log.e(TAG, "can't find branch");
                                            } else {
                                                //Log.e(TAG, "Branch is " + branch_pricelist.toString());
                                                priceList.setBranch(branch_pricelist);
                                            }
                                        }
                                    } else {
                                        Log.e(TAG, "PRICE_LIST API don't have 'branch_id' field");
                                    }

                                    if (initialSync || lastUpdatedAt == null) {
                                        newPriceList.add(priceList);
                                    } else {
                                        if (isExisting(priceList, Table.PRICE_LISTS)) {
                                            updatePriceList.add(priceList);
                                        } else {
                                            newPriceList.add(priceList);
                                        }
                                    }

                                    Log.e(TAG, ">> ");


                                    List<CustomerGroup> pCustomerGroups = getHelper().fetchObjects(CustomerGroup.class).
                                            queryBuilder().where().eq("price_list_id", priceList).query();

                                    Log.e(TAG, "Size: " + pCustomerGroups.size());
                                    if (pCustomerGroups != null) {
                                        for (CustomerGroup cg : pCustomerGroups) {
                                            Log.e(TAG, ">>this " + cg.getName());
                                        }
                                    } else {
                                        Log.e(TAG, ">> customer group don't have value");
                                    }

                                }

                                newPriceList.doOperationBT(PriceList.class);
                                updatePriceList.doOperationBT(PriceList.class);
                            }
                            updateNext(requestType, size);
                            break;
                        case ROUTE_PLANS_DETAILS:
                            BatchList<RoutePlanDetail> newRoutePlanDetails = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<RoutePlanDetail> updateRoutePlanDetails = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                            RoutePlan xRoutePlan = (RoutePlan) listOfIds.get(mCustomIndex);

                            for (int i = 0; i < size; i++) {

                                JSONObject routePlanDetailJsonObject = jsonArray.getJSONObject(i);

                                RoutePlanDetail routePlanDetails = gson.fromJson(routePlanDetailJsonObject.toString(), RoutePlanDetail.class);

                                //set Route Plan
                                routePlanDetails.setRoutePlan(xRoutePlan);

                                // set Customer
                                if (routePlanDetailJsonObject.has("customer_id")) {
                                    if (routePlanDetailJsonObject.getString("customer_id").equals("") || routePlanDetailJsonObject.isNull("customer_id")) {
                                        Log.e(TAG, "'customer_id' field is null");
                                    } else {
                                        int customer_id = routePlanDetailJsonObject.getInt("customer_id");
                                        Customer customer = getHelper().fetchObjects(Customer.class).queryBuilder().where().eq("id", customer_id).queryForFirst();
                                        if (customer != null) {
                                            Log.e(TAG, "Customer: " + customer.getName());
                                            routePlanDetails.setCustomer(customer);
                                        } else {
                                            Log.e(TAG, "Can't find customer with id " + routePlanDetailJsonObject.getInt("customer_id"));
                                        }
                                    }

                                } else {
                                    Log.e(TAG, mCurrentTableSyncing + " API don't have 'customer_id' field.");
                                }

                                if (initialSync || lastUpdatedAt == null) {
                                    newRoutePlanDetails.add(routePlanDetails);
                                } else {
                                    if (isExisting(routePlanDetails, Table.ROUTE_PLANS_DETAILS)) {
                                        newRoutePlanDetails.add(routePlanDetails);
                                    } else {
                                        updateRoutePlanDetails.add(routePlanDetails);
                                    }
                                }

                            }

                            newRoutePlanDetails.doOperationBT2(RoutePlanDetail.class);
                            updateRoutePlanDetails.doOperationBT2(RoutePlanDetail.class);

                            Log.e(TAG, "Route Plan Details");
                            updateNext(requestType, count);
                            break;
                        case PRICE_LISTS_DETAILS:
                            BatchList<Price> newPrice = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<Price> updatePrice = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                            PriceList priceList = (PriceList) listOfIds.get(mCustomIndex);

                            for (int i = 0; i < size; i++) {
                                JSONObject priceListJsonObject = jsonArray.getJSONObject(i);
                                Price price = new Price();
                                price.setId(-1);
                                price = gson.fromJson(priceListJsonObject.toString(), Price.class);

                                if (price.getId() == -1) {
                                    price.setId(priceListJsonObject.getInt("id"));
                                }

                                price.setUtc_created_at(priceListJsonObject.getString("created_at"));
                                price.setUtc_updated_at(priceListJsonObject.getString("updated_at"));

                                // PRODUCT
                                if (priceListJsonObject.has("product_id")) {
                                    if (priceListJsonObject.getString("product_id").equals("") || priceListJsonObject.isNull("product_id")) {
                                        Log.e(TAG, "product_id field is null");
                                    } else {
                                        int product_id = priceListJsonObject.getInt("product_id");
                                        Product product = getHelper().fetchObjects(Product.class).queryBuilder().where().eq("id", product_id).queryForFirst();
                                        if (product != null) {
                                            Log.e(TAG, "Product: " + product.getName());
                                            price.setProduct(product);
                                        } else {
                                            Log.e(TAG, "Can't find product with id " + priceListJsonObject.getInt("product_id"));
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "PRICE_LIST API don't have 'product_id' field in price_list_items array");
                                }

                                // UNITS
                                if (priceListJsonObject.has("unit_id")) {
                                    if (priceListJsonObject.getString("unit_id").equals("") || priceListJsonObject.isNull("unit_id")) {
                                        Log.e(TAG, "unit_id field is null");
                                    } else {
                                        int unit_id = priceListJsonObject.getInt("unit_id");
                                        Unit unit = getHelper().fetchObjects(Unit.class).queryBuilder().where().eq("id", unit_id).queryForFirst();
                                        if (unit != null) {
                                            Log.e(TAG, "Unit: " + unit.getName());
                                            price.setUnit(unit);
                                        } else {
                                            Log.e(TAG, "Can't find unit with id " + priceListJsonObject.getInt("unit_id"));
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "PRICE_LIST API don't have 'unit_id' field in price_list_items array");
                                }

                                price.setPriceList(priceList);

                                if (initialSync || lastUpdatedAt == null) {
                                    newPrice.add(price);
                                } else {
                                    if (isExisting(price, Table.PRICE_LISTS_DETAILS)) {
                                        newPrice.add(price);
                                    } else {
                                        updatePrice.add(price);
                                    }
                                }
                            }

                            newPrice.doOperation(Price.class);
                            updatePrice.doOperation(Price.class);

                            Log.e(TAG, "Price List Details");
                            updateNext(requestType, count);
                            break;
                        case SALES_PROMOTIONS_POINTS_DETAILS:
                        case SALES_PROMOTIONS_SALES_DISCOUNT_DETAILS:
                            BatchList<Discount> newDiscount = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<Discount> updateDiscount = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                            SalesPromotion tempSalesPromotion = (SalesPromotion) listOfIds.get(mCustomIndex);
                            SalesPromotion salesPromotion = getHelper().fetchObjects(SalesPromotion.class).queryBuilder().where().eq("id", tempSalesPromotion.getId()).queryForFirst();

                            for (int i = 0; i < size; i++) {
                                JSONObject discountJsonObject = jsonArray.getJSONObject(i);

                                Discount discount = gson.fromJson(discountJsonObject.toString(), Discount.class);
                                if (salesPromotion != null) {
                                    discount.setSalesPromotion(salesPromotion);
                                    Log.e(TAG, "Sales Promotion is " + salesPromotion.getName() + " id: " + salesPromotion.getId());

                                    if (discountJsonObject.has("product_id")) {
                                        if (!discountJsonObject.isNull("product_id")) {
                                            int product_id = discountJsonObject.getInt("product_id");
                                            Product product = getHelper().fetchObjects(Product.class).queryBuilder().where().eq("id", product_id).queryForFirst();

                                            if (product != null) {
                                                discount.setProduct(product);
                                                Log.e(TAG, "Product ID: " + product.getName());
                                            } else {
                                                discount.setProduct(null);
                                                Log.e(TAG, "can't find product with id:  " + product_id);
                                            }
                                        } else {
                                            Log.e(TAG, "'product_id' is null");
                                        }
                                    } else {
                                        Log.e(TAG, mCurrentTableSyncing + " API don't have product id");
                                    }


                                    if (initialSync || lastUpdatedAt == null) {
                                        newDiscount.add(discount);
                                    } else {
                                        if (isExisting(discount, mCurrentTableSyncing)) {
                                            updateDiscount.add(discount);
                                        } else {
                                            newDiscount.add(discount);
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "Can't add discount Sales Promotion Does not exist in database");
                                }
                            }

                            newDiscount.doOperationBT2(Discount.class);
                            updateDiscount.doOperationBT2(Discount.class);

                            //Log.e(TAG, "Sales Promotions Discount");
                            updateNext(requestType, count);
                            break;
                        case ROUTE_PLANS:
                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                syncNext();
                                return;
                            } else {
                                BatchList<RoutePlan> newRoutePlans = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                                BatchList<RoutePlan> updateRoutePlans = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    RoutePlan routePlan = gson.fromJson(jsonObject.toString(), RoutePlan.class);
                                    routePlan.setUser(getUser());

                                    if (initialSync || lastUpdatedAt == null) {
                                        newRoutePlans.add(routePlan);
                                    } else {
                                        if (isExisting(routePlan, Table.ROUTE_PLANS)) {
                                            updateRoutePlans.add(routePlan);
                                        } else {
                                            newRoutePlans.add(routePlan);
                                        }
                                    }
                                }

                                newRoutePlans.doOperationBT(RoutePlan.class);
                                updateRoutePlans.doOperationBT(RoutePlan.class);
                            }
                            updateNext(requestType, size);
                            break;
                        default:
                            LoggingTools.showToast(this, "Something went wrong with the module");
                            mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                            syncNext();
                            break;
                    }
                }
            }
        } catch (SQLException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateNext(RequestType requestType, int size) {
        Log.e(TAG, requestType + " next table");

        try {
            if (mCurrentTableSyncing == Table.PRICE_LISTS_DETAILS ||
                    mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_DISCOUNT_DETAILS ||
                    mCurrentTableSyncing == Table.SALES_PROMOTIONS_POINTS_DETAILS ||
                    mCurrentTableSyncing == Table.ROUTE_PLANS_DETAILS ||
                    mCurrentTableSyncing == Table.PRICE_LISTS_FROM_CUSTOMERS) {
                mCustomIndex++;
                if (mSyncModulesListener != null) {
                    mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, mCustomIndex, size);
                }

                if (mCustomIndex < size) {
                    startSyncModuleContents(requestType);
                } else {
                    syncNext();
                }
            } else {
                if (mSyncModulesListener != null) {
                    if (size != 0) {
                        if (mCurrentTableSyncing == Table.DOCUMENTS) {
                            mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, branchIndex, getUserBranchesSize());
                        } else {
                            mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, page, numberOfPages);
                        }
                    }
                }
                if (size <= max_size_per_page) {
                    Log.e(TAG, "Syncing next table");
                    page++;
                    if (page <= numberOfPages) {
                        startSyncModuleContents(requestType);
                    } else {
                        syncNext();
                    }
                } else {
                    Log.e(TAG, "Error Page Size Exceeded Max Size Per Page");
                    LoggingTools.showToast(this, "Error Page Size Exceeded Max Size Per Page");
                    syncNext();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
        if (hasInternet) {
            Log.e(TAG, "onError " + response.toString());
        } else {
            Log.e(TAG, "onError ");
        }

        Log.e(TAG, "page: " + page);
        Log.e(TAG, "numberOfPages: " + numberOfPages);
        Log.e(TAG, "count: " + count);
        Log.e(TAG, "branchIndex: " + branchIndex);
        Log.e(TAG, "moduleIndex: " + mModulesIndex);

        if (mSyncModulesListener != null) {
            mSyncModulesListener.onErrorDownload(table, "error");
        }
    }

    @Override
    public void onRequestError() {
        Log.e("onRequestError", "Sync Modules");
    }

    public void startFetchingModules() throws SQLException {
        Log.e(TAG, "syncAllModules?=" + syncAllModules + " || Items to sync=" + mModulesToSync.length);
        for (Table t : mModulesToSync) {
            Log.e(TAG, t.toString());
        }

        if (mCurrentTableSyncing == Table.USERS_ME ||
                mCurrentTableSyncing == Table.TAX_SETTINGS ||
                mCurrentTableSyncing == Table.DOCUMENT_TYPES ||
                mCurrentTableSyncing == Table.DOCUMENT_PURPOSES ||
                mCurrentTableSyncing == Table.SETTINGS) {
            startSyncModuleContents(RequestType.API_CONTENT);
        } else if (mCurrentTableSyncing == Table.DAILY_SALES) {
            startSyncModuleContents(RequestType.DAILY_SALES_TODAY);
        } else {
            startSyncModuleContents(RequestType.LAST_UPDATED_AT);
        }
    }

    public void retrySync() throws SQLException {
        prepareModulesToReSync();
        startSyncModuleContents(mCurrentRequestType);
    }


}