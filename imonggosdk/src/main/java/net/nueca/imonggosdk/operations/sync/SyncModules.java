package net.nueca.imonggosdk.operations.sync;

import android.os.Handler;
import android.util.Log;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import net.nueca.imonggosdk.enums.DailySalesEnums;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.OfflineDataType;
import net.nueca.imonggosdk.enums.Parameter;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.AccountPrice;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchProduct;
import net.nueca.imonggosdk.objects.BranchTag;
import net.nueca.imonggosdk.objects.DailySales;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.LastUpdatedAt;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.objects.SalesPushSettings;
import net.nueca.imonggosdk.objects.Settings;
import net.nueca.imonggosdk.objects.TaxRate;
import net.nueca.imonggosdk.objects.TaxSetting;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;
import net.nueca.imonggosdk.objects.associatives.CustomerCustomerGroupAssoc;
import net.nueca.imonggosdk.objects.associatives.ProductTaxRateAssoc;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.base.BaseTable3;
import net.nueca.imonggosdk.objects.base.BatchList;
import net.nueca.imonggosdk.objects.base.DBTable;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerCategory;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentPurpose;
import net.nueca.imonggosdk.objects.document.DocumentType;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.objects.invoice.InvoicePurpose;
import net.nueca.imonggosdk.objects.invoice.PaymentTerms;
import net.nueca.imonggosdk.objects.invoice.PaymentType;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.objects.price.PriceList;
import net.nueca.imonggosdk.objects.routeplan.RoutePlan;
import net.nueca.imonggosdk.objects.routeplan.RoutePlanDetail;
import net.nueca.imonggosdk.objects.salespromotion.Discount;
import net.nueca.imonggosdk.objects.salespromotion.SalesPromotion;
import net.nueca.imonggosdk.operations.ImonggoTools;
import net.nueca.imonggosdk.operations.http.ImonggoOperations;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.DateTimeTools;
import net.nueca.imonggosdk.tools.LastUpdateAtTools;
import net.nueca.imonggosdk.tools.LoggingTools;
import net.nueca.imonggosdk.tools.NumberTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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

        mSkipNextModule = false;
        mCurrentRequestType = requestType;

        Log.e(TAG, "initialSync: " + initialSync + " lastUpdatedAt: " + lastUpdatedAt);

        if (getHelper() == null) {
            Log.e(TAG, "helper is null");
        }

        if (requestType == RequestType.COUNT) {
            count = 0;
            mCustomPageIndex = 1;
            mCustomIdIndex = 0;
            page = 1;
            Log.e(TAG, "COUNT");
            ImonggoOperations.getAPIModule(this, getQueue(), getSession(), this,
                    mCurrentTableSyncing, getSession().getServer(), requestType,
                    getParameters(requestType));

        } else if (requestType == RequestType.API_CONTENT) {
            Log.e(TAG, "API CONTENT: " + mCurrentTableSyncing);

            //updating
            if (!initialSync) {
                if (mCurrentTableSyncing == Table.PRICE_LISTS_FROM_CUSTOMERS ||
                        mCurrentTableSyncing == Table.PRICE_LISTS) {
                    if (listOfIdsPriceListSorted == null)
                        listOfIdsPriceListSorted = new ArrayList<>();

                    if (listOfPricelistIds == null)
                        listOfPricelistIds = new ArrayList<>();
                }
            }

            if (mCurrentTableSyncing == Table.CUSTOMER_BY_SALESMAN ||
                    mCurrentTableSyncing == Table.CUSTOMERS ||
                    mCurrentTableSyncing == Table.CUSTOMER_GROUPS ||
                    mCurrentTableSyncing == Table.BRANCH_CUSTOMERS) {

                if (listOfPricelistIds == null)
                    listOfPricelistIds = new ArrayList<>();

                if (listPriceListStorage == null)
                    listPriceListStorage = new ArrayList<>();

                if (listOfIdsPriceListSorted == null)
                    listOfIdsPriceListSorted = new ArrayList<>();
            }

            if (mCurrentTableSyncing == Table.CUSTOMER_BY_SALESMAN) {
                Log.e(TAG, "setting Updating of PricList From Customer to true: ");
                mUpdatingPriceListFromCustomer = true;
            }

            Table t = mCurrentTableSyncing;

            if (!initialSync) {
                if (!mUpdatingPriceListFromCustomer) {
                    if (mCurrentTableSyncing == Table.PRICE_LISTS_FROM_CUSTOMERS) {
                        t = Table.PRICE_LISTS;
                        Log.e(TAG, "changing to price list for updating not from customer");
                    }
                } else {
                    Log.e(TAG, "mUpdatingPriceListFromCustomer: " + mUpdatingPriceListFromCustomer + "table: " + Table.PRICE_LISTS_FROM_CUSTOMERS);
                }

            }

            if (mCurrentTableSyncing == Table.BRANCH_PRODUCTS) {
                listOfBranchIds = getHelper().fetchObjects(BranchUserAssoc.class).queryBuilder().where().eq("user_id", getSession().getUser()).query();
            }

            ImonggoOperations.getAPIModule(this, getQueue(), getSession(), this,
                    t, getSession().getServer(), requestType,
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
            if (lastUpdatedAt != null) {
                Log.e(TAG, "lastUpdateAt:  " + lastUpdatedAt);
            } else {
                Log.e(TAG, "lastUpdateAt is null");
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
                            Parameter.LAST_UPDATED_AT,
                            Parameter.BRANCH_ID),  // RHY: Parameter.TARGET_BRANCH_ID Changed to cater transfer to branch
                            document_type, intransit_status,
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
                    mCurrentTableSyncing == Table.BRANCH_CUSTOMERS ||
                    mCurrentTableSyncing == Table.ORDERS) {
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

            if (mCurrentTableSyncing == Table.ORDERS_PURCHASES) {
                if (initialSync || lastUpdatedAt == null) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.LAST_UPDATED_AT,
                            Parameter.BRANCH_ID,
                            Parameter.ORDER_TYPE),
                            getSession().getCurrent_branch_id(),
                            Order.ORDERTYPE_PURCHASE_ORDER);
                } else {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.LAST_UPDATED_AT,
                            Parameter.BRANCH_ID,
                            Parameter.AFTER,
                            Parameter.ORDER_TYPE),
                            getSession().getCurrent_branch_id(),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()),
                            Order.ORDERTYPE_PURCHASE_ORDER);
                }
            }

            if (mCurrentTableSyncing == Table.ORDERS_STOCK_REQUEST) {
                if (initialSync || lastUpdatedAt == null) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.LAST_UPDATED_AT,
                            Parameter.BRANCH_ID,
                            Parameter.ORDER_TYPE),
                            getSession().getCurrent_branch_id(),
                            Order.ORDERTYPE_STOCK_REQUEST);
                } else {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.LAST_UPDATED_AT,
                            Parameter.BRANCH_ID,
                            Parameter.AFTER,
                            Parameter.ORDER_TYPE),
                            getSession().getCurrent_branch_id(),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()),
                            Order.ORDERTYPE_STOCK_REQUEST);
                }
            }

            if (mCurrentTableSyncing == Table.ROUTE_PLANS) {
                return String.format(ImonggoTools.generateParameter(Parameter.SALESMAN_ID, Parameter.LAST_UPDATED_AT), getSession().getUser_id());
            }

            if (mCurrentTableSyncing == Table.CUSTOMER_BY_SALESMAN) {
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
                        mCurrentTableSyncing == Table.BRANCH_PRICE_LISTS ||
                        mCurrentTableSyncing == Table.ORDERS) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.PAGE,
                            Parameter.BRANCH_ID),
                            String.valueOf(page),
                            getSession().getCurrent_branch_id());
                }

                if (mCurrentTableSyncing == Table.ORDERS_STOCK_REQUEST) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.PAGE,
                            Parameter.BRANCH_ID,
                            Parameter.ORDER_TYPE),
                            String.valueOf(page),
                            getSession().getCurrent_branch_id(),
                            Order.ORDERTYPE_STOCK_REQUEST);
                }

                if (mCurrentTableSyncing == Table.ORDERS_PURCHASES) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.PAGE,
                            Parameter.BRANCH_ID,
                            Parameter.ORDER_TYPE),
                            String.valueOf(page),
                            getSession().getCurrent_branch_id(),
                            Order.ORDERTYPE_PURCHASE_ORDER);
                }

                if (mCurrentTableSyncing == Table.PRICE_LISTS_FROM_CUSTOMERS) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.ID),
                            listOfIdsPriceListSorted.get(mCustomIdIndex));
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
                        mCurrentTableSyncing == Table.SALES_PROMOTIONS_POINTS_DETAILS) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.ID,
                            Parameter.DETAILS,
                            Parameter.PAGE),
                            listOfSalesPromotionIds.get(mCustomIdIndex),
                            mCustomPageIndex);
                }

                if (mCurrentTableSyncing == Table.ROUTE_PLANS_DETAILS) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.ID,
                            Parameter.DETAILS,
                            Parameter.ACTIVE_ONLY,
                            Parameter.BRANCH_ID,
                            Parameter.PAGE),
                            listOfIds.get(mCustomIdIndex).getId(),
                            getSession().getCurrent_branch_id(),
                            mCustomPageIndex);
                }

                if (mCurrentTableSyncing == Table.PRICE_LISTS_DETAILS) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.ID,
                            Parameter.DETAILS,
                            Parameter.ACTIVE_ONLY,
                            Parameter.BRANCH_ID,
                            Parameter.PAGE),
                            listOfIdsPriceListSorted.get(mCustomIdIndex),
                            getSession().getCurrent_branch_id(),
                            mCustomPageIndex);
                }


                if (mCurrentTableSyncing == Table.ROUTE_PLANS ||
                        mCurrentTableSyncing == Table.CUSTOMER_BY_SALESMAN) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.SALESMAN_ID,
                            Parameter.PAGE),
                            getSession().getUser_id(),
                            String.valueOf(page));
                }

                if (mCurrentTableSyncing == Table.LAYAWAYS) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.SALESMAN_ID,
                            Parameter.PAGE,
                            Parameter.LAYAWAYS),
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

                if (mCurrentTableSyncing == Table.PRICE_LISTS_FROM_CUSTOMERS) {
                    Log.e(TAG, "=-----getParameters: " + mCurrentTableSyncing);
                    if (mUpdatingPriceListFromCustomer) {
                        return String.format(ImonggoTools.generateParameter(
                                Parameter.ID),
                                listOfIdsPriceListSorted.get(mCustomIdIndex));
                    } else {
                        return String.format(ImonggoTools.generateParameter(
                                Parameter.AFTER,
                                Parameter.PAGE),
                                DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()),
                                page);
                    }
                }

                // request with branch id
                if (mCurrentTableSyncing == Table.BRANCH_PRODUCTS ||
                        mCurrentTableSyncing == Table.BRANCH_CUSTOMERS ||
                        mCurrentTableSyncing == Table.BRANCH_PRICE_LISTS ||
                        mCurrentTableSyncing == Table.ORDERS) {
                    String.format(ImonggoTools.generateParameter(
                            Parameter.PAGE,
                            Parameter.BRANCH_ID,
                            Parameter.AFTER),
                            String.valueOf(page),
                            getSession().getCurrent_branch_id(),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }

                if (mCurrentTableSyncing == Table.ORDERS_PURCHASES) {
                    String.format(ImonggoTools.generateParameter(
                            Parameter.PAGE,
                            Parameter.BRANCH_ID,
                            Parameter.AFTER,
                            Parameter.ORDER_TYPE),
                            String.valueOf(page),
                            getSession().getCurrent_branch_id(),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()),
                            Order.ORDERTYPE_PURCHASE_ORDER);
                }

                if (mCurrentTableSyncing == Table.ORDERS_STOCK_REQUEST) {
                    String.format(ImonggoTools.generateParameter(
                            Parameter.PAGE,
                            Parameter.BRANCH_ID,
                            Parameter.AFTER,
                            Parameter.ORDER_TYPE),
                            String.valueOf(page),
                            getSession().getCurrent_branch_id(),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()),
                            Order.ORDERTYPE_STOCK_REQUEST);
                }

                if (mCurrentTableSyncing == Table.ORDERS_STOCK_REQUEST) {
                    String.format(ImonggoTools.generateParameter(
                            Parameter.PAGE,
                            Parameter.BRANCH_ID,
                            Parameter.AFTER,
                            Parameter.ORDER_TYPE),
                            String.valueOf(page),
                            getSession().getCurrent_branch_id(),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()),
                            Order.ORDERTYPE_STOCK_REQUEST);
                }

                if (mCurrentTableSyncing == Table.ORDERS_PURCHASES) {
                    String.format(ImonggoTools.generateParameter(
                            Parameter.PAGE,
                            Parameter.BRANCH_ID,
                            Parameter.AFTER,
                            Parameter.ORDER_TYPE),
                            String.valueOf(page),
                            getSession().getCurrent_branch_id(),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()),
                            Order.ORDERTYPE_PURCHASE_ORDER);
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
                        mCurrentTableSyncing == Table.SALES_PROMOTIONS_POINTS_DETAILS) {

                    return String.format(ImonggoTools.generateParameter(
                            Parameter.ID,
                            Parameter.DETAILS,
                            Parameter.PAGE),
                            listOfSalesPromotionIds.get(mCustomIdIndex),
                            mCustomPageIndex);
                }

                if (mCurrentTableSyncing == Table.ROUTE_PLANS_DETAILS) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.ID,
                            Parameter.DETAILS,
                            Parameter.PAGE),
                            listOfIds.get(mCustomIdIndex).getId(),
                            mCustomPageIndex);
                }

                if (mCurrentTableSyncing == Table.PRICE_LISTS_DETAILS) {

                    return String.format(ImonggoTools.generateParameter(
                            Parameter.ID,
                            Parameter.DETAILS,
                            Parameter.PAGE),
                            listOfIdsPriceListSorted.get(mCustomIdIndex),
                            mCustomPageIndex);
                }

                if (mCurrentTableSyncing == Table.ROUTE_PLANS ||
                        mCurrentTableSyncing == Table.CUSTOMER_BY_SALESMAN) {

                    return String.format(ImonggoTools.generateParameter(
                            Parameter.SALESMAN_ID,
                            Parameter.AFTER),
                            getSession().getUser_id(),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at())
                    );
                }

                if (lastUpdatedAt != null) {
                    if (lastUpdatedAt.getLast_updated_at() != null) {
                        return String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.AFTER),
                                String.valueOf(page), DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                    } else {
                        return String.format(ImonggoTools.generateParameter(Parameter.PAGE),
                                String.valueOf(page));
                    }
                }

                // Default
                return String.format(ImonggoTools.generateParameter(Parameter.PAGE),
                        String.valueOf(page));
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
                        mCurrentTableSyncing == Table.BRANCH_PRICE_LISTS ||
                        mCurrentTableSyncing == Table.ORDERS) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.ACTIVE_ONLY,
                            Parameter.COUNT,
                            Parameter.BRANCH_ID),
                            getSession().getCurrent_branch_id());
                }

                if (mCurrentTableSyncing == Table.ORDERS_PURCHASES) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.COUNT,
                            Parameter.BRANCH_ID,
                            Parameter.ORDER_TYPE),
                            getSession().getCurrent_branch_id(),
                            Order.ORDERTYPE_PURCHASE_ORDER);
                }

                if (mCurrentTableSyncing == Table.ORDERS_STOCK_REQUEST) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.COUNT,
                            Parameter.BRANCH_ID,
                            Parameter.ORDER_TYPE),
                            getSession().getCurrent_branch_id(),
                            Order.ORDERTYPE_STOCK_REQUEST);
                }

                if (mCurrentTableSyncing == Table.ROUTE_PLANS || mCurrentTableSyncing == Table.CUSTOMER_BY_SALESMAN) {
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
                            String.valueOf(getUser().getId()), DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
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
                        mCurrentTableSyncing == Table.BRANCH_PRICE_LISTS ||
                        mCurrentTableSyncing == Table.ORDERS) {
                    return String.format(ImonggoTools.generateParameter(Parameter.ACTIVE_ONLY,
                            Parameter.COUNT, Parameter.AFTER, Parameter.BRANCH_ID),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()),
                            getSession().getCurrent_branch_id());
                }

                if (mCurrentTableSyncing == Table.ORDERS_STOCK_REQUEST) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.COUNT,
                            Parameter.AFTER,
                            Parameter.BRANCH_ID,
                            Parameter.ORDER_TYPE),

                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()),
                            getSession().getCurrent_branch_id(),
                            Order.ORDERTYPE_STOCK_REQUEST);
                }

                if (mCurrentTableSyncing == Table.ORDERS_PURCHASES) {
                    return String.format(ImonggoTools.generateParameter(
                            Parameter.COUNT,
                            Parameter.AFTER,
                            Parameter.BRANCH_ID,
                            Parameter.ORDER_TYPE),

                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()),
                            getSession().getCurrent_branch_id(),
                            Order.ORDERTYPE_PURCHASE_ORDER);
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

                if (mCurrentTableSyncing == Table.ROUTE_PLANS || mCurrentTableSyncing == Table.CUSTOMER_BY_SALESMAN) {


                    if (lastUpdatedAt.getLast_updated_at() != null) {
                        return String.format(ImonggoTools.generateParameter(
                                Parameter.SALESMAN_ID,
                                Parameter.COUNT,
                                Parameter.AFTER),
                                getSession().getUser_id(),
                                DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                    } else {
                        return String.format(ImonggoTools.generateParameter(
                                Parameter.SALESMAN_ID,
                                Parameter.COUNT),
                                getSession().getUser_id());
                    }
                }

                if (mCurrentTableSyncing == Table.PRICE_LISTS_FROM_CUSTOMERS) {
                    return ".json" + String.format(ImonggoTools.generateParameter(
                            Parameter.COUNT,
                            Parameter.AFTER),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }

                if (mCurrentTableSyncing == Table.DOCUMENT_PURPOSES) {
                    return String.format(ImonggoTools.generateParameter(Parameter.COUNT, Parameter.AFTER),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }

                return String.format(ImonggoTools.generateParameter(Parameter.COUNT, Parameter.AFTER),
                        DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
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
        } else {
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
        page = 1;
        numberOfPages = 1;
        count = 0;

        mCurrentTableSyncing = mModulesToSync[mModulesIndex];
        Log.e(TAG, "there are still tables to sync table: " + mCurrentTableSyncing);

        if (mCurrentTableSyncing == Table.PRICE_LISTS_FROM_CUSTOMERS) {
            //check if price lists is existing

            if (mSkipNextModule) {
                Log.e(TAG, "skipping " + mCurrentTableSyncing + "syncing next.. ");
                mSyncModulesListener.onDownloadProgress(Table.PRICE_LISTS_FROM_CUSTOMERS, 1, 1);
                syncNext();
            } else {
               /* if(!initialSync) {
                    Log.e(TAG, "Price list is for updating...");
                    mUpdatingPriceListFromCustomer = false;
                    startSyncModuleContents(RequestType.LAST_UPDATED_AT);
                } else {*/

                    if (listOfPricelistIds != null) {
                        if (listOfPricelistIds.size() != 0) {
                            count = listOfPricelistIds.size();
                            mCustomIdIndex = 0;

                            Log.e(TAG, "listOfPriceListOfIds: " + listOfPricelistIds.size());
                            Log.e(TAG, "listOfIdsPriceListSorted: " + listOfIdsPriceListSorted.size());
                            Log.e(TAG, "mUpdatingPriceListFromCustomer: " + mUpdatingPriceListFromCustomer);

                            if (mUpdatingPriceListFromCustomer) {
                           /* mModulesIndex++;
                            mCurrentTableSyncing = Table.PRICE_LISTS_DETAILS;
                            mSyncModulesListener.onDownloadProgress(Table.PRICE_LISTS_FROM_CUSTOMERS, 1, 1);*/
                                startSyncModuleContents(RequestType.API_CONTENT);
                            } else {
                                startSyncModuleContents(RequestType.LAST_UPDATED_AT);
                            }
                        } else {
                            Log.e(TAG, mCurrentTableSyncing + ". There's no Price Lists... Downloading Next Modulex");
                            mSyncModulesListener.onDownloadProgress(Table.PRICE_LISTS_FROM_CUSTOMERS, 1, 1);
                            syncNext();
                        }
                    } else {
                        Log.e(TAG, mCurrentTableSyncing + ". There's no Price Lists... Downloading Next Modulez");
                        mSyncModulesListener.onDownloadProgress(Table.PRICE_LISTS_FROM_CUSTOMERS, 1, 1);
                        syncNext();
                    }
               // }
            }

        } else if (mCurrentTableSyncing == Table.ROUTE_PLANS_DETAILS) {
            listOfIds = new ArrayList<>();

            Log.e(TAG, "Setting Up Route Plan Details...");

            //check if route plan is existing
            if (getHelper().fetchObjectsList(RoutePlan.class).size() != 0 && !mSkipNextModule) {
                listOfIds = getHelper().fetchObjectsList(RoutePlan.class);
                count = listOfIds.size();
                mCustomIdIndex = 0;

                for (int i = 0; i < listOfIds.size(); i++) {
                    Log.e(TAG, "ID: " + listOfIds.get(i).getId());
                }

                Log.e(TAG, "Size of Route Plan: " + listOfIds.size());

                startSyncModuleContents(RequestType.API_CONTENT);
            } else {
                Log.e(TAG, "There's no Route Plan... Downloading Next Module");
                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                syncNext();
            }
        } else if (mCurrentTableSyncing == Table.PRICE_LISTS_DETAILS) {
            listOfPricelistIds = new ArrayList<>();

            Log.e(TAG, "Setting Up Price List Details...");

            if (mSkipNextModule) {
                Log.e(TAG, "skipping " + mCurrentTableSyncing + " syncing next.. ");
                mSyncModulesListener.onDownloadProgress(Table.PRICE_LISTS_DETAILS, 1, 1);
                syncNext();
            } else {
                if (listOfIdsPriceListSorted != null) {
                    if (listOfIdsPriceListSorted.size() != 0) {
                        //check if price lists is existing
                        if (getHelper().fetchObjectsList(PriceList.class).size() != 0 && !mSkipNextModule) {
                            //listOfIds = getHelper().fetchObjectsList(PriceList.class);
                            count = listOfIdsPriceListSorted.size();
                            mCustomIdIndex = 0;
                            Log.e(TAG, "Size of Price List: " + count);
                            Log.e(TAG, "Starting downloading details of id: " + listOfIdsPriceListSorted.get(mCustomIdIndex));

                            startSyncModuleContents(RequestType.API_CONTENT);
                        } else {
                            Log.e(TAG, "There's no Price List... Downloading Next Modulec");
                            mSyncModulesListener.onDownloadProgress(Table.PRICE_LISTS_DETAILS, 1, 1);
                            syncNext();
                        }
                    } else {
                        Log.e(TAG, "There's no Price List... Downloading Next Moduled");
                        mSyncModulesListener.onDownloadProgress(Table.PRICE_LISTS_DETAILS, 1, 1);
                        syncNext();
                    }

                } else {
                    Log.e(TAG, "There's no Price List... Downloading Next Modulei");
                    mSyncModulesListener.onDownloadProgress(Table.PRICE_LISTS_DETAILS, 1, 1);
                    syncNext();
                }
            }
        } else if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_DISCOUNT_DETAILS ||
                mCurrentTableSyncing == Table.SALES_PROMOTIONS_POINTS_DETAILS) {
            listOfIds = new ArrayList<>();


            Log.e(TAG, "Setting Up Sales Promotions Details...");

            if (!mSkipNextModule) {
                if (listOfSalesPromotionIds != null) {
                    if (listOfSalesPromotionIds.size() != 0) {
                        count = listOfSalesPromotionIds.size();
                        mCustomIdIndex = 0;

                        Log.e(TAG, "Size of Sales Promotions Discount: " + listOfSalesPromotionIds.size());

                        startSyncModuleContents(RequestType.API_CONTENT);
                    } else {
                        Log.e(TAG, "There's no Sales Promotions... Downloading Next Module");
                        if (mSyncModulesListener != null) {
                            mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                        }
                        syncNext();
                    }
                } else {
                    Log.e(TAG, "There's no Sales Promotions... Downloading Next Module");
                    if (mSyncModulesListener != null) {
                        mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                    }
                    syncNext();
                }
            } else {
                syncNext();
            }

        } else if (mCurrentTableSyncing == Table.TAX_SETTINGS ||
                mCurrentTableSyncing == Table.DOCUMENT_TYPES ||
                mCurrentTableSyncing == Table.SETTINGS ||
                mCurrentTableSyncing == Table.ROUTE_PLANS ||
                mCurrentTableSyncing == Table.LAYAWAYS) {
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
                    listOfIdsPriceListSorted = null;
                    listOfPricelistIds = null;
                    listOfSalesPromotionIds = null;
                    listOfSalesPromotionStorage = null;
                    listPriceListStorage = null;
                    mUpdatingPriceListFromCustomer = false;
                    stopSelf();
                }
            }
        }, 1000);
    }

    @Override
    public void onStart(Table module, RequestType requestType) {
        Log.e(TAG, "onStart downloading " + module.toString() + " " + requestType);
        if (mSyncModulesListener != null) {
            mSyncModulesListener.onStartDownload(mCurrentTableSyncing);
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
                    Log.e(TAG, "Last Updated At " + module);

                    // since this is the first
                    count = 0;
                    page = 1;

                    newLastUpdatedAt = gson.fromJson(jsonObject.toString(), LastUpdatedAt.class);

                    if (mCurrentTableSyncing == Table.DOCUMENTS) {
                        newLastUpdatedAt.setTableName(LastUpdateAtTools.getTableToSync(module, getTargetBranchId(branchIndex) + ""));
                        Log.e(TAG, "Table Name: " + newLastUpdatedAt.getTableName());
                    } else {
                        newLastUpdatedAt.setTableName(mCurrentTableSyncing.getStringName());
                        Log.e(TAG, "Setting table name of last update at: " + newLastUpdatedAt.toString());
                    }

                    if (lastUpdatedAt != null) {
                        Log.e(TAG, "From Server: " + newLastUpdatedAt.getLast_updated_at());
                        Log.e(TAG, "From DB: " + lastUpdatedAt.getLast_updated_at());

                        /*lastUpdatedAt.setLast_updated_at("2016/04/25 11:30:00 +0000");
                        lastUpdatedAt.updateTo(getHelper());*/

                        if (lastUpdatedAt.getLast_updated_at() != null) {

                            SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            if (newLastUpdatedAt.getLast_updated_at() == null) {
                                newLastUpdatedAt.setLast_updated_at(DateTimeTools.getCurrentDateTime());
                                Log.e(TAG, "server return is null use this date: " + DateTimeTools.getCurrentDateTime());
                            }

                            try {
                                Date date1 = dateFormat1.parse(lastUpdatedAt.getLast_updated_at());
                                Date date2 = dateFormat1.parse(newLastUpdatedAt.getLast_updated_at());

                                if (date1.equals(date2)) {
                                    if (mCurrentTableSyncing == Table.ROUTE_PLANS ||
                                            mCurrentTableSyncing == Table.PRICE_LISTS ||
                                            mCurrentTableSyncing == Table.PRICE_LISTS_FROM_CUSTOMERS ||
                                            mCurrentTableSyncing == Table.SALES_PROMOTIONS ||
                                            mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_DISCOUNT ||
                                            mCurrentTableSyncing == Table.SALES_PROMOTIONS_POINTS) {
                                        mSkipNextModule = true;
                                    }

                                    if (module == Table.PRICE_LISTS) {
                                        Log.e(TAG, "Updating Price Lists daa..");
                                        mSyncModulesListener.onDownloadProgress(Table.PRICE_LISTS_FROM_CUSTOMERS, 1, 1);
                                        mSyncModulesListener.onDownloadProgress(Table.PRICE_LISTS_DETAILS, 1, 1);

                                    } else {
                                        mSyncModulesListener.onDownloadProgress(module, 1, 1);
                                    }

                                    Log.e(TAG, ">> parehas: table: " + module + " skip next: " + mSkipNextModule);
                                    syncNext();
                                    return;
                                } else {
                                    LastUpdatedAt lastUpdatedAt = getHelper().fetchObjects(LastUpdatedAt.class).queryBuilder().where().eq("tableName", mCurrentTableSyncing.getStringName()).queryForFirst();

                                    if (lastUpdatedAt != null) {
                                        Log.e(TAG, "Updating Last Updated At from: " + lastUpdatedAt.toString() + " to newLastUpdatedAt: " + newLastUpdatedAt.toString());
                                        lastUpdatedAt = newLastUpdatedAt;
                                        lastUpdatedAt.updateTo(getHelper());
                                    } else {
                                        Log.e(TAG, "lastUpdatedAt from DB is null");
                                    }

                                    Log.e(TAG, ">> Hindi parehas");
                                }

                            } catch (ParseException e) {
                                Log.e(TAG, e.toString());
                            }
                        } else {
                            Log.e(TAG, "Inserting lastUpdatedAt.. " + newLastUpdatedAt.toString());
                            newLastUpdatedAt.insertTo(getHelper());
                            if (lastUpdatedAt.getLast_updated_at() == null) {
                                lastUpdatedAt = newLastUpdatedAt;
                                lastUpdatedAt.updateTo(getHelper());
                                Log.e(TAG, "lastUpdatedAt is null querying from database this: " + lastUpdatedAt.toString());
                            }
                        }
                    } else {
                        Log.e(TAG, "Inserting lastUpdatedAt.. " + newLastUpdatedAt.toString());
                        newLastUpdatedAt.insertTo(getHelper());
                        lastUpdatedAt = new LastUpdatedAt();
                        if (lastUpdatedAt.getLast_updated_at() == null) {
                            lastUpdatedAt.setTableName(newLastUpdatedAt.getTableName());
                            lastUpdatedAt.setLast_updated_at(newLastUpdatedAt.getLast_updated_at());
                            lastUpdatedAt.updateTo(getHelper());
                            Log.e(TAG, "lastUpdatedAt is null querying from database this: " + lastUpdatedAt.toString());
                        }
                    }

                    // USERS and  TAX SETTINGS DON'T SUPPORT COUNT
                    if (mCurrentTableSyncing == Table.USERS ||
                            mCurrentTableSyncing == Table.TAX_SETTINGS ||
                            mCurrentTableSyncing == Table.ROUTE_PLANS ||
                            mCurrentTableSyncing == Table.PRICE_LISTS ||
                            mCurrentTableSyncing == Table.PRICE_LISTS_FROM_CUSTOMERS) {
                        startSyncModuleContents(RequestType.API_CONTENT);
                    } else {
                        // UPDATING
                        // lastUpdatedAt is null
                        // count
                        startSyncModuleContents(RequestType.COUNT);
                    }
                } else if (requestType == RequestType.COUNT) { // COUNT
                    count = jsonObject.getInt("count");
                    Log.e(TAG, "Response: count is " + count);
                    // if table don't have data
                    if (count == 0) {

                        mSyncModulesListener.onDownloadProgress(module, 1, 1);

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
                            Extras extras = new Extras(User.class.getName().toUpperCase(), user.getId());

                            if (jsonObject.has("extras")) {
                                JSONObject json_extras = jsonObject.getJSONObject("extras");
                                if (json_extras.has("is_salesman")) {
                                    if (!json_extras.getString("is_salesman").isEmpty()) {
                                        extras.setIs_salesman(json_extras.getInt("is_salesman"));
                                        if (!isExisting(extras, Table.EXTRAS)) {
                                            extras.insertTo(getHelper());
                                        } else {
                                            extras.updateTo(getHelper());
                                        }

                                        user.setExtras(extras);
                                        Log.e(TAG, "User.extras");
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
                                Log.e(TAG, "User.insertTo");
                                if (isExisting(user, Table.USERS)) {
                                    if (user.getStatus() == null) {
                                        user.updateTo(getHelper());
                                    } else {
                                        user.insertTo(getHelper());
                                    }
                                } else {
                                    user.insertTo(getHelper());
                                }

                            } else {
                                // check if the user tables exist in the database
                                if (isExisting(user, Table.USERS)) {
                                    if (user.getStatus() == null) {
                                        user.updateTo(getHelper());
                                        if (user.getId() == getSession().getUser().getId()) {
                                            Log.e(TAG, "Updating sessions user from " + getSession().getUser().getName() + " to " + user.getName());
                                        }
                                    } else {
                                        user.insertTo(getHelper());
                                    }
                                } else {  // if not then add it
                                    Log.e(TAG, "adding user entry to be inserted");
                                    user.insertTo(getHelper());
                                }
                            }

                            Log.e(TAG, ">>>> INSERTING USER TO SESSION....");

                            getSession().setUser(user);
                            getSession().setCurrent_branch_id(user.getHome_branch_id());
                            getSession().updateTo(getHelper());

                            // TESTING?
                            user.setSession(getSession());
                            user.updateTo(getHelper());

                            if (user.getStatus() == null)
                                AccountTools.updateUserActiveStatus(getApplicationContext(), true);

                            Log.e(TAG, "User: " + getSession().getUser().getName() + "User Home Branch ID: " + getSession().getUser().getHome_branch_id());

                        } else {
                            String message = "Something went wrong.. please contact developer and tell them that there's No User Found";
                            Log.e(TAG, message);
                            LoggingTools.showToast(getApplicationContext(), message);
                        }

                        mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                        syncNext();
                    }

                    if (mCurrentTableSyncing == Table.PRICE_LISTS_FROM_CUSTOMERS) {


                        BatchList<PriceList> newPriceList = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                        BatchList<PriceList> updatePriceList = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
                        BatchList<PriceList> deletePriceList = new BatchList<>(DatabaseOperation.DELETE, getHelper());

                        mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, mCustomIdIndex, listOfIdsPriceListSorted.size());

                        Log.e(TAG, "Custom Index: " + mCustomIdIndex + "  PriceList Size: " + listOfIdsPriceListSorted.size());

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

                            Log.e(TAG, "PriceList Custom Index: " + mCustomIdIndex);

                            //BaseTable tempObject;
                            if (jsonObject.has("customers")) {
                                JSONArray customerArray = jsonObject.getJSONArray("customers");

                                if (customerArray.length() != 0) {
                                    for (int t = 0; t < customerArray.length(); t++) {
                                        Log.e(TAG, "customerArray From PriceList size: " + customerArray.length());
                                        JSONObject customerFromPriceList = customerArray.getJSONObject(t);
                                        Log.e(TAG, "customerFromPriceList: " + customerFromPriceList.toString());

                                        if (customerFromPriceList.has("id")) {
                                            int customerPriceLIstId = customerFromPriceList.getInt("id");

                                            //listOfCustomerId.add(customerPriceLIstId);
                                            Log.e(TAG, "Customer From PriceList ID is " + customerPriceLIstId);

                                            Customer xPriceListCustomer = getHelper().fetchObjects(Customer.class).queryBuilder().where().eq("returnId", customerPriceLIstId).queryForFirst();

                                            if (xPriceListCustomer != null) {
                                                xPriceListCustomer.setPriceList(priceList);
                                                xPriceListCustomer.updateTo(getHelper());
                                                //updating / deleting
                                                if (isExisting(priceList, Table.PRICE_LISTS)) {
                                                    if (priceList.getStatus().equals("A")) {
                                                        Log.e(TAG, "updating price list.. currentSyncing: " + mCurrentTableSyncing + "size: " + listOfIdsPriceListSorted.size());

                                                        updatePriceList.add(priceList);
                                                    } else {
                                                        Log.e(TAG, "deleting price list..");
                                                        deletePriceList.add(priceList);
                                                    }
                                                } else { // inserting
                                                    newPriceList.add(priceList);
                                                }
                                            } else {
                                                Log.e(TAG, "cannot find Customer ");
                                            }

                                        } else {
                                            Log.e(TAG, "customerFromPriceList don't have id.. skipping...");
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "customerArray is null skipping");
                                }
                            } else {
                                Log.e(TAG, "API " + mCurrentTableSyncing + " don't have 'customers' field");
                            }

                            if (jsonObject.has("customer_groups")) {
                                JSONArray customerGroupArray = jsonObject.getJSONArray("customer_groups");

                                if (customerGroupArray.length() != 0) {
                                    for (int t = 0; t < customerGroupArray.length(); t++) {
                                        JSONObject customerGroupFromPriceList = customerGroupArray.getJSONObject(t);

                                        if (customerGroupFromPriceList.has("id")) {
                                            int customerGroupPriceLIstId = customerGroupFromPriceList.getInt("id");
                                            // listOfCustomerGroupId.add(customerGroupPriceLIstId);
                                            Log.e(TAG, "CustomerGroup From PriceList ID is " + customerGroupPriceLIstId);
                                            CustomerGroup xPriceListCustomerGroup = CustomerGroup.fetchById(getHelper(), CustomerGroup.class, customerGroupPriceLIstId);

                                            if (xPriceListCustomerGroup != null) {
                                                xPriceListCustomerGroup.setPriceList(priceList);
                                                xPriceListCustomerGroup.updateTo(getHelper());
                                                if (isExisting(priceList, Table.PRICE_LISTS)) {
                                                    if (priceList.getStatus().equals("A")) {
                                                        Log.e(TAG, "updating price list.. currentSyncing: " + mCurrentTableSyncing + "size: " + listOfIdsPriceListSorted.size());
                                                        updatePriceList.add(priceList);
                                                    } else {
                                                        Log.e(TAG, "deleting price list..");
                                                        deletePriceList.add(priceList);
                                                    }
                                                } else { // inserting
                                                    newPriceList.add(priceList);
                                                }
                                            } else {
                                                Log.e(TAG, "cannot find customer group ");
                                            }

                                        } else {
                                            Log.e(TAG, "customerGroupFromPriceList don't have id.. skipping...");
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "customerGroupArray is null skipping");
                                }
                            } else {
                                Log.e(TAG, "API " + mCurrentTableSyncing + " don't have 'customer_groups' field");
                            }
                            /*if (listPriceListStorage.get(mCustomIdIndex) instanceof Customer) {
                                Log.e(TAG, "PriceList came from customer ");
                                BaseTable3 tempObject = (Customer) listPriceListStorage.get(mCustomIdIndex);

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

                            } else if (listPriceListStorage.get(mCustomIdIndex) instanceof CustomerGroup) {
                                Log.e(TAG, "PriceList came from customer group ");
                                BaseTable tempObject = (CustomerGroup) listPriceListStorage.get(mCustomIdIndex);

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
                                Log.e(TAG, "pricelist is existing...");
                                if (initialSync) {
                                    Log.e(TAG, "initial sync updateing...");
                                    priceList.updateTo(getHelper());
                                } else {
                                    Log.e(TAG, "updating sync updateing...");
                                    if (lastUpdatedAt != null && newLastUpdatedAt != null) {
                                        try {
                                            if (DateTimeTools.stringToDate(lastUpdatedAt.getLast_updated_at()).before(DateTimeTools.stringToDate(newLastUpdatedAt.getLast_updated_at()))) {
                                                priceList.updateTo(getHelper());
                                            } else {
                                                Log.e(TAG, "Skipping Price Lists");
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        priceList.updateTo(getHelper());
                                    }
                                }
                            } else {
                                Log.e(TAG, "pricelist not existing... inserting");
                                priceList.insertTo(getHelper());
                            }*/

                            List<Price> price = Price.fetchWithConditionInt(getHelper(), Price.class, new DBTable.ConditionsWindow<Price, Integer>() {
                                @Override
                                public Where<Price, Integer> renderConditions(Where<Price, Integer> where) throws SQLException {
                                    where.in("price_list_id", listOfIdsPriceListSorted);
                                    return where;
                                }
                            });

                            Log.e(TAG, "Size of price to delete: " + price.size());

                            BatchList<Price> deletePrice = new BatchList<>(DatabaseOperation.DELETE, getHelper());

                            if(price.size() != 0) {
                                deletePrice.addAll(price);
                                Log.e(TAG, "Deleting Price... ");
                                deletePrice.doOperationBT(Price.class);
                            } else {
                                Log.e(TAG, "There's nothing to delete on prices" );
                            }

                            newPriceList.doOperationBT(PriceList.class);
                            updatePriceList.doOperationBT(PriceList.class);
                            deletePriceList.doOperationBT(PriceList.class);

                            Log.e(TAG, "This Price List ID is: " + listOfIdsPriceListSorted.get(mCustomIdIndex));
                        }
                        updateNext(requestType, listOfIdsPriceListSorted.size());
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

                                // User current_user = getUser();
                                if (getSession().getUser() == null) {
                                    User current_user = getHelper().fetchObjects(User.class).queryBuilder().where().eq("email", getSession().getEmail()).queryForFirst();
                                    if (current_user != null) {
                                        getSession().setUser(current_user);
                                        getSession().setCurrent_branch_id(current_user.getHome_branch_id());
                                        Log.e(TAG, "User Home Branch ID: " + current_user.getHome_branch_id());
                                        getSession().dbOperation(getHelper(), DatabaseOperation.UPDATE);
                                    }
                                }

                                updateNext(requestType, size);
                            }
                            break;
                        case BRANCH_PRODUCTS:
                            /**
                             * app = ModuleSetting.module_type == "app"
                             * app.isShow_only_sellable_products() <boolean> /-> false
                             *
                             * if(app.isShow_only_sellable_products()) {
                             *      // do the current implementation
                             * }
                             * else {
                             *      // GSON to Product..
                             *          // check if existing... blah blah algo
                             *      // if not existing, save!
                             *      // do the current implementation
                             *          // unit -> product
                             * }
                             *
                             */
                            if (size == 0) {
                                syncNext();
                                return;
                            } else {
                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                                    //TODO: support multiple branch
                                    int current_branch_id = getSession().getCurrent_branch_id();
                                    Branch current_branch = getHelper().fetchIntId(Branch.class).queryForId(current_branch_id);

                                    // details
                                    int BRANCH_PRICE_ID = 0;
                                    Branch BRANCH = null;
                                    Product PRODUCT = null;
                                    Unit UNIT;
                                    BranchProduct BRANCH_PRODUCT;

                                    Boolean show_only_sellable_products;

                                    Log.e(TAG, "---");
                                    Log.e(TAG, "Branch Product:");

                                    // ID
                                    if (jsonObject.has("branch_price_id")) {
                                        if (!jsonObject.getString("branch_price_id").isEmpty()) {
                                            BRANCH_PRICE_ID = jsonObject.getInt("branch_price_id");
                                            Log.e(TAG, "Branch Price Id: " + BRANCH_PRICE_ID);
                                        } else {
                                            Log.e(TAG, mCurrentTableSyncing + " API 'branch_price_id' field don't have value");
                                        }
                                    } else {
                                        Log.e(TAG, mCurrentTableSyncing + " API don't have 'branch_price_id field");
                                    }

                                    // BRANCH
                                    if (current_branch_id != -1) {
                                        BRANCH = getHelper().fetchObjects(Branch.class).queryBuilder().where().eq("id", current_branch_id).queryForFirst();
                                        if (BRANCH != null)
                                            Log.e(TAG, "Branch: " + BRANCH.getName());
                                        else
                                            Log.e(TAG, "Branch is null");
                                    } else {
                                        Log.e(TAG, "Session don't have the current branch id");
                                    }

                                    if (app != null) {
                                        if (app.isShow_only_sellable_products()) {
                                            Log.e(TAG, ">>show_only_sellable_products is true");
                                            show_only_sellable_products = true;
                                        } else {
                                            Log.e(TAG, ">>show_only_sellable_products is false");
                                            show_only_sellable_products = false;
                                        }
                                    } else {
                                        Log.e(TAG, ">>show_only_sellable_products is false");
                                        show_only_sellable_products = false;
                                    }

                                    Log.e(TAG, ">>Showing Default Products");
                                    // PRODUCT
                                    if (jsonObject.has("id")) {
                                        if (!jsonObject.getString("id").isEmpty()) {

                                            if (show_only_sellable_products) {
                                                PRODUCT = gson.fromJson(jsonObject.toString(), Product.class);

                                                if (PRODUCT != null) {

                                                    Extras product_extras;

                                                    // Extras
                                                    if (jsonObject.has("extras")) {
                                                        product_extras = new Extras();
                                                        product_extras.setId(Product.class.getName().toUpperCase(), PRODUCT.getId());
                                                        JSONObject json_extras = jsonObject.getJSONObject("extras");

                                                        if (!isExisting(product_extras, Table.EXTRAS)) {
                                                            String default_selling_unit = "";
                                                            String default_ordering_unit_id = "";

                                                            if (json_extras.has("default_selling_unit")) {
                                                                default_selling_unit = json_extras.getString("default_selling_unit");
                                                                Log.e(TAG, "API: " + mCurrentTableSyncing + " API has extras field 'default_selling_unit' on " + PRODUCT.getName());
                                                            } else {
                                                                Log.e(TAG, "API: " + mCurrentTableSyncing + " API don't have extras field 'default_selling_unit' on " + PRODUCT.getName());
                                                            }

                                                            if (json_extras.has("default_ordering_unit_id")) {
                                                                default_ordering_unit_id = json_extras.getString("default_ordering_unit_id");
                                                                Log.e(TAG, "API: " + mCurrentTableSyncing + " API has extras field 'default_ordering_unit_id' on " + PRODUCT.getName());
                                                            } else {
                                                                Log.e(TAG, "API: " + mCurrentTableSyncing + " API don't have extras field 'default_ordering_unit_id' on " + PRODUCT.getName());
                                                            }

                                                            product_extras.setDefault_ordering_unit_id(default_ordering_unit_id);
                                                            product_extras.setDefault_selling_unit(default_selling_unit);
                                                            product_extras.insertTo(getHelper());
                                                        } else {
                                                            Log.e(PRODUCT.getName() + " EXTRAS", "is on the database!");
                                                            product_extras = getHelper().fetchObjects(Extras.class).queryBuilder()
                                                                    .where().eq("id", product_extras.getId()).queryForFirst();
                                                        }

                                                        PRODUCT.setExtras(product_extras);

                                                        Log.e(TAG, "Extras Created. Tagging to Products. Inserting to DB: " + product_extras.toString());

                                                    } else {
                                                        Log.e(TAG, "This Product don't have extras");
                                                    }

                                                    // TAX RATES
                                                    int tax_branch_id;
                                                    int tax_rate_id;
                                                    if (jsonObject.has("tax_rates")) {
                                                        List<ProductTaxRateAssoc> pTaxRateList = getHelper().fetchObjectsList(ProductTaxRateAssoc.class);

                                                        // Deleting Product's ProducTaxRate Entry
                                                        for (ProductTaxRateAssoc pTaxRate : pTaxRateList) {
                                                            if (PRODUCT.getId() == pTaxRate.getProduct().getId()) {
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

                                                                Log.e(TAG, "Product " + PRODUCT.getName() + " tax is " + current_taxRate.getName());

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
                                                                            productTaxRate = new ProductTaxRateAssoc(PRODUCT, current_taxRate);
                                                                            productTaxRate.insertTo(getHelper());
                                                                        } else {
                                                                            Log.e(TAG, "The product tax rate is not for your branch. skipping...");
                                                                        }
                                                                    } else {
                                                                        productTaxRate = new ProductTaxRateAssoc(PRODUCT, current_taxRate);
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

                                                    // TAX LISTS
                                                    if (jsonObject.has("tag_list")) {
                                                        // Save tags to the database
                                                        JSONArray tagsListArray = jsonObject.getJSONArray("tag_list");
                                                        int tagsSize = tagsListArray.length();
                                                        for (int tagsI = 0; tagsI < tagsSize; tagsI++) {
                                                            ProductTag productTag = new ProductTag(tagsListArray.getString(tagsI), PRODUCT);
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

                                                    PRODUCT.setSearchKey(PRODUCT.getName() + PRODUCT.getStock_no());

                                                    if (!isExisting(PRODUCT, Table.PRODUCTS)) {
                                                        if (jsonObject.getString("status").equals("A")) {
                                                            Log.e(TAG, "setting status to A");
                                                            PRODUCT.setStatus("A");
                                                            PRODUCT.insertTo(getHelper());
                                                        } else
                                                            Log.e(TAG, "skipping save of product..");
                                                    } else {
                                                        Log.e(TAG, "setting status to A. return of API is " + jsonObject.getString("status") + " updating products");
                                                        PRODUCT.setStatus("A");
                                                        PRODUCT.updateTo(getHelper());
                                                    }
                                                } else {
                                                    Log.e(TAG, "Product from gson is null. skipping");
                                                }

                                            } else {
                                                int product_id = jsonObject.getInt("id");
                                                PRODUCT = getHelper().fetchObjects(Product.class).queryBuilder().where().eq("id", product_id).queryForFirst();
                                            }

                                            if (PRODUCT != null) {
                                                Log.e(TAG, "Product: " + PRODUCT.getName());
                                            } else {
                                                Log.e(TAG, "Product is null");
                                            }
                                        } else {
                                            Log.e(TAG, "'id' field from " + mCurrentTableSyncing + " API is empty");
                                        }
                                    } else {
                                        Log.e(TAG, mCurrentTableSyncing + "API don't have 'id' field");
                                    }

                                    // BRANCH PRODUCT
                                    if (BRANCH != null && PRODUCT != null) {
                                        BRANCH_PRODUCT = new BranchProduct(PRODUCT, BRANCH);
                                        BRANCH_PRODUCT.setId(BRANCH_PRICE_ID);

                                        Log.e(TAG, "branchProduct created ");

                                        Log.e(TAG, "Product Extras: " + PRODUCT.getExtras().toString());

                                        // NAME
                                        if (jsonObject.has("name")) {
                                            if (!jsonObject.getString("name").isEmpty()) {
                                                Log.e(TAG, "Branch Product Name: " + jsonObject.getString("name"));
                                                BRANCH_PRODUCT.setName(jsonObject.getString("name"));
                                            } else {
                                                Log.e(TAG, "Branch Product API 'name' field is empty");
                                            }
                                        } else {
                                            Log.e(TAG, mCurrentTableSyncing + " API don't have 'name' 'field");
                                        }

                                        // DESCRIPTION
                                        if (jsonObject.has("description")) {
                                            if (!jsonObject.getString("description").isEmpty()) {
                                                Log.e(TAG, "Description: " + jsonObject.getString("description"));
                                                BRANCH_PRODUCT.setDescription(jsonObject.getString("description"));
                                            } else {
                                                Log.e(TAG, "Branch Product API 'description' field is empty");
                                            }
                                        } else {
                                            Log.e(TAG, mCurrentTableSyncing + " API don't have description 'field");
                                        }

                                        // UNIT RETAIL PRICE
                                        if (jsonObject.has("unit_retail_price")) {
                                            if (!jsonObject.getString("unit_retail_price").isEmpty()) {
                                                Log.e(TAG, "Unit Retail Price: " + jsonObject.getString("unit_retail_price"));
                                                BRANCH_PRODUCT.setUnit_retail_price(jsonObject.getDouble("unit_retail_price"));
                                            } else {
                                                Log.e(TAG, "Branch Product API 'unit_retail_price' field is empty");
                                            }
                                        } else {
                                            Log.e(TAG, mCurrentTableSyncing + " API don't have unit_retail_price 'field");
                                        }


                                        // UTC UPDATED AT & UTC UPDATED AT
                                        if (jsonObject.has("utc_updated_at")) {
                                            if (!jsonObject.getString("utc_updated_at").isEmpty()) {
                                                Log.e(TAG, "UTC UPDATED AT: " + jsonObject.getString("utc_updated_at"));
                                                BRANCH_PRODUCT.setUtc_updated_at(jsonObject.getString("utc_updated_at"));
                                            }
                                        } else {
                                            Log.e(TAG, mCurrentTableSyncing + " API don't have 'utc_updated_at' field");
                                        }

                                        if (jsonObject.has("utc_created_at")) {
                                            if (!jsonObject.getString("utc_created_at").isEmpty()) {
                                                Log.e(TAG, "UTC CREATED AT: " + jsonObject.getString("utc_created_at"));
                                                BRANCH_PRODUCT.setUtc_created_at(jsonObject.getString("utc_created_at"));
                                            }
                                        } else {
                                            Log.e(TAG, mCurrentTableSyncing + " API don't have 'utc_updated_at' field");
                                        }

                                        // UNIT -- NULLABLE
                                        if (jsonObject.has("unit_id")) {
                                            if (!jsonObject.getString("unit_id").isEmpty() && !jsonObject.isNull("unit_id")) {
                                                int unit_id = jsonObject.getInt("unit_id");
                                                UNIT = getHelper().fetchObjects(Unit.class).queryBuilder().where().eq("id", unit_id).queryForFirst();
                                                if (UNIT != null) {
                                                    Log.e(TAG, "ID: " + PRODUCT.getId() + " Product: " + PRODUCT.getName() + " Unit: " + UNIT.getName() + " ID: " + UNIT.getId());
                                                    UNIT.setProduct(PRODUCT);
                                                    UNIT.updateTo(getHelper());
                                                    BRANCH_PRODUCT.setUnit(UNIT);
                                                } else {
                                                    Log.e(TAG, "Err Can't find 'unit' field from database");
                                                }
                                            } else {
                                                BRANCH_PRODUCT.setBaseUnitSellable(true);
                                            }

                                        } else {
                                            BRANCH_PRODUCT.setBaseUnitSellable(true);
                                            Log.e(TAG, mCurrentTableSyncing + " API don't have 'unit_id' field. setting Branch Product's is sellable unit to true.");
                                        }

                                        // SAVING
                                        // 1. if initial sync
                                        //    1.1 saved all the branch products
                                        // 2. if updating sync
                                        //    2.2 last updated at after > BP.utc_updated_at <-local
                                        //    2.3 delete all branchproducts

                                        // if branch product is not existing
                                        if (!isExisting(BRANCH_PRODUCT, Table.BRANCH_PRODUCTS)) {
                                            // check products status
                                            if (jsonObject.getString("status").equals("A")) {
                                                BRANCH_PRODUCT.insertTo(getHelper());
                                            } else { // insert to database
                                                Log.e(TAG, "skipping... because status is not A ");
                                            }
                                        } else { // if branch product is existing
                                            if (jsonObject.getString("status").equals("A")) {
                                                Log.e(TAG, "updating... Branch Prices. " + jsonObject.getString("name"));
                                                BRANCH_PRODUCT.updateTo(getHelper());
                                            } else {
                                                Log.e(TAG, "status is: " + jsonObject.getString("status"));
                                                // BRANCH PRODUCT is EXISTING
                                                // PRODUCT STATUS is "I" -- update status

                                                if (jsonObject.getString("status").equals("I")) {
                                                    Log.e(TAG, "branch product status is I");
                                                    BRANCH_PRODUCT.updateTo(getHelper());
                                                } else if (jsonObject.getString("status").equals("D")) {
                                                    // BRANCH PRODUCT is "D" -- check is
                                                    //BRANCH.deleteTo(getHelper());

                                                    Log.e(TAG, "branch product status is D.. querying product...");
                                                    //query
                                                    Product product = getHelper().fetchObjects(Product.class).queryBuilder().where().eq("id", PRODUCT.getId()).queryForFirst();

                                                    // if product is null delete na branch product
                                                    if (product == null) {
                                                        Log.e(TAG, "product is null deleting branch product");
                                                        BRANCH_PRODUCT.deleteTo(getHelper());
                                                    } else { // else

                                                        // query all branch products based sa product
                                                        List<BranchProduct> bp = getHelper().fetchObjects(BranchProduct.class).queryBuilder().where().eq("product_id", product).query();
                                                        Log.e(TAG, "branch product size is " + bp.size());
                                                        if (bp == null) {
                                                            BRANCH_PRODUCT.deleteTo(getHelper());
                                                        } else {
                                                            if (bp.size() == 1) {
                                                                Log.e(TAG, "branch product size is 1 deleting BP and PRODUCT");
                                                                BRANCH_PRODUCT.deleteTo(getHelper());
                                                                PRODUCT.deleteTo(getHelper());
                                                            } else {
                                                                Boolean deleteThisProduct = true;

                                                                for (BranchProduct b : bp) {
                                                                    Log.e(TAG, "branch product: " + b.getProduct().getName() + " unit: " + b.getUnit().getName() + " product status: " + b.getProduct().getStatus());
                                                                    if (b.getProduct().getStatus().equals("A")) {
                                                                        Log.e(TAG, "other branch products exist with this product skipping delete...");
                                                                        deleteThisProduct = false;
                                                                    }
                                                                }

                                                                Log.e(TAG, "deleting BRANCH PRODUCT...");
                                                                BRANCH_PRODUCT.deleteTo(getHelper());

                                                                if (deleteThisProduct) {
                                                                    Log.e(TAG, "deleting product..");
                                                                    PRODUCT.deleteTo(getHelper());
                                                                } else {
                                                                    Log.e(TAG, "OOOPS! don't delete this product");
                                                                }

                                                            }

                                                        }
                                                    }
                                                }
                                            }
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
                                        product_extras = new Extras();
                                        product_extras.setId(Product.class.getName().toUpperCase(), product.getId());
                                        JSONObject json_extras = jsonObject.getJSONObject("extras");

                                        if (!isExisting(product_extras, Table.EXTRAS)) {
                                            String default_selling_unit = "";
                                            String default_ordering_unit_id = "";

                                            if (json_extras.has("default_selling_unit")) {
                                                default_selling_unit = json_extras.getString("default_selling_unit");
                                                Log.e(TAG, "API: " + mCurrentTableSyncing + " API has extras field 'default_selling_unit' on " + product.getName());
                                            } else {
                                                Log.e(TAG, "API: " + mCurrentTableSyncing + " API don't have extras field 'default_selling_unit' on " + product.getName());
                                            }

                                            if (json_extras.has("default_ordering_unit_id")) {
                                                default_ordering_unit_id = json_extras.getString("default_ordering_unit_id");
                                                Log.e(TAG, "API: " + mCurrentTableSyncing + " API has extras field 'default_ordering_unit_id' on " + product.getName());
                                            } else {
                                                Log.e(TAG, "API: " + mCurrentTableSyncing + " API don't have extras field 'default_ordering_unit_id' on " + product.getName());
                                            }

                                            product_extras.setDefault_ordering_unit_id(default_ordering_unit_id);
                                            product_extras.setDefault_selling_unit(default_selling_unit);
                                            product_extras.insertTo(getHelper());
                                        } else {
                                            Log.e(product.getName() + " EXTRAS", "is on the database!");
                                            product_extras = getHelper().fetchObjects(Extras.class).queryBuilder()
                                                    .where().eq("id", product_extras.getId()).queryForFirst();
                                        }

                                        product.setExtras(product_extras);

                                        Log.e(TAG, "Extras Created. Tagging to Products. Inserting to DB: " + product_extras.toString());

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

                                    /**
                                     * app = ModuleSetting.module_type == "app"
                                     * app.isShow_only_sellable_products() <boolean> /-> false
                                     *
                                     * if(app.isShow_only_sellable_products()) { // true
                                     *      // no tagging of unit to the product, kasi wala pang product
                                     * }
                                     * else {
                                     *      // do the usual tagging
                                     * }
                                     */

                                    if (app != null) {

                                        if (!app.isShow_only_sellable_products()) {
                                            Product product = getHelper().fetchObjects(Product.class).queryBuilder().where().eq("id", jsonObject.getString("product_id")).queryForFirst();
                                            if (product != null) {
                                                Log.e(TAG, "Tagging " + product.getName() + " to Units");
                                                unit.setProduct(product);
                                            } else {
                                                Log.e(TAG, "Can't find product with id: " + jsonObject.getString("product_id"));
                                            }
                                        } else {
                                            Log.e(TAG, "Skipping Tagging of Products to Units. isShow_only_sellable_products is false");
                                        }

                                    } else {
                                        Log.e(TAG, "Skipping Tagging of Products to Units. Module Setting is null");
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
                                BatchList<Branch> deleteBranches = new BatchList<>(DatabaseOperation.DELETE, getHelper());

                                BatchList<BranchTag> newBranchTags = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                                BatchList<BranchTag> updateBranchTags = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
                                BatchList<BranchTag> deleteBranchTags = new BatchList<>(DatabaseOperation.DELETE, getHelper());

                                BatchList<BranchUserAssoc> newBranchUserAssocs = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                                BatchList<BranchUserAssoc> updateBranchUserAssocs = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
                                BatchList<BranchUserAssoc> deleteBranchUserAssocs = new BatchList<>(DatabaseOperation.DELETE, getHelper());

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
                                            if (branch.getStatus().equals("D")) {
                                                deleteBranches.add(branch);
                                                deleteBranchUserAssocs.add(branchUserAssoc);
                                            } else {
                                                updateBranches.add(branch);
                                                updateBranchUserAssocs.add(branchUserAssoc);
                                            }
                                        } else {
                                            newBranches.add(branch);
                                            newBranchUserAssocs.add(branchUserAssoc);
                                        }
                                    }


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

                            BatchList<CustomerCustomerGroupAssoc> newCustomerCustomerGroup = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<CustomerCustomerGroupAssoc> updateCustomerCustomerGroup = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

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
                                    customer.setReturnId(customer.getId());
                                    customer.setSearchKey(customer.getName() + customer.getCode() + customer.getAlternate_code()); // # searchkey
                                    Extras customer_extras;
                                    PriceList priceList;

                                    Log.e(TAG, ">>> customer is: " + customer.toString());

                                    // --- ADDED by RHY
                                    if (jsonObject.has("payment_terms_id")) {
                                        String paymentTermsId = jsonObject.getString("payment_terms_id");
                                        if (paymentTermsId != null && !paymentTermsId.equals("null")) {
                                            PaymentTerms paymentTerms = PaymentTerms.fetchById(getHelper(), PaymentTerms.class, Integer.valueOf(paymentTermsId));
                                            if (paymentTerms != null)
                                                customer.setPaymentTerms(paymentTerms);
                                        }
                                    }

                                    // EXTRAS
                                    if (jsonObject.has(name_extras)) {
                                        JSONObject json_extras = jsonObject.getJSONObject(name_extras);

                                        customer_extras = new Extras();
                                        customer_extras.setId(Customer.class.getName().toUpperCase(), customer.getId());
                                        if (!isExisting(customer_extras, Table.EXTRAS)) {
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
                                            CustomerCategory customerCategory;
                                            if (customer_category_id != 0) {
                                                customerCategory = getHelper().fetchObjects(CustomerCategory.class).queryBuilder().where().eq("id", customer_category_id).queryForFirst();

                                                if (customerCategory != null) {
                                                    customer_extras.setCustomerCategory(customerCategory);
                                                } else {
                                                    Log.e(TAG, "Customer Category not found");
                                                }
                                            }
                                        } else {
                                            Log.e(customer.getName() + " EXTRAS", "is on the database!");
                                            customer_extras = getHelper().fetchObjects(Extras.class).queryBuilder()
                                                    .where().eq("id", customer_extras.getId()).queryForFirst();
                                        }

                                        customer.setExtras(customer_extras);
                                    }

                                    // PRICE_LIST
                                    if (jsonObject.has("price_list_id")) {
                                        if (!jsonObject.isNull("price_list_id")) {
                                            int price_list_id = jsonObject.getInt("price_list_id");

                                            Log.e(TAG, "price list: " + price_list_id);
                                            listOfPricelistIds.add(price_list_id);
                                            listPriceListStorage.add(customer);

                                            if (listOfIdsPriceListSorted == null) {
                                                Log.e(TAG, "price list sorted is null, creating instance");
                                                listOfIdsPriceListSorted = new ArrayList<>();
                                            } else {
                                                Log.e(TAG, "price list sorted is no null");
                                            }


                                            if (listOfIdsPriceListSorted.size() == 0) {
                                                Log.e(TAG, "price list: " + price_list_id + " addingx...");
                                                listOfIdsPriceListSorted.add(price_list_id);
                                            } else if (listOfIdsPriceListSorted.indexOf(price_list_id) < 0) {
                                                Log.e(TAG, "price list: " + price_list_id + " addingc...");
                                                listOfIdsPriceListSorted.add(price_list_id);
                                            }

                                            priceList = getHelper().fetchObjects(PriceList.class).queryBuilder().where().eq("id", price_list_id).queryForFirst();

                                            if (priceList != null) {
                                                //Log.e(TAG, "Price Lists found! " + priceList.toString());
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
                                        // Log.e(TAG, "has CustomerGroups");
                                        if (!jsonObject.isNull("customer_groups")) {
                                            // Log.e(TAG, "not null CustomerGroups");
                                            JSONArray customerGroupJSONArray = jsonObject.getJSONArray("customer_groups");
                                            for (int l = 0; l < customerGroupJSONArray.length(); l++) {
                                                JSONObject customerGroupJSONObject = customerGroupJSONArray.getJSONObject(l);

                                                if (customerGroupJSONObject.has("id")) {
                                                    //Log.e(TAG, "has id CustomerGroups");
                                                    if (!customerGroupJSONObject.isNull("id")) {
                                                        //  Log.e(TAG, "id is not null CustomerGroups");
                                                        CustomerCustomerGroupAssoc customerCustomerGroupAssoc;
                                                        CustomerGroup xcustomerGroup = getHelper().fetchObjects(CustomerGroup.class).queryBuilder().where().eq("id", customerGroupJSONObject.getInt("id")).queryForFirst();
                                                        CustomerGroup customerGroupNet = gson.fromJson(customerGroupJSONObject.toString(), CustomerGroup.class);

                                                        Log.e(TAG, customerGroupJSONObject.toString());

                                                        if (customerGroupJSONObject.has("price_list_id")) {
                                                            if (!customerGroupJSONObject.isNull("price_list_id")) {
                                                                int price_list_id = customerGroupJSONObject.getInt("price_list_id");

                                                                //Log.e(TAG, "Price List from customer group of customer: " + price_list_id);
                                                                listOfPricelistIds.add(price_list_id);
                                                                listPriceListStorage.add(customerGroupNet);

                                                                if (listOfIdsPriceListSorted == null) {
                                                                    Log.e(TAG, "price list sorted is null, creating instance");
                                                                    listOfIdsPriceListSorted = new ArrayList<>();
                                                                } else {
                                                                    Log.e(TAG, "price list sorted is no null");
                                                                }

                                                                if (listOfIdsPriceListSorted.size() == 0) {
                                                                    Log.e(TAG, "price list: " + price_list_id + " addingxx...");
                                                                    listOfIdsPriceListSorted.add(price_list_id);
                                                                } else if (listOfIdsPriceListSorted.indexOf(price_list_id) < 0) {
                                                                    Log.e(TAG, "price list: " + price_list_id + " addingcc  ...");
                                                                    listOfIdsPriceListSorted.add(price_list_id);
                                                                }


                                                            } else {
                                                                Log.e(TAG, "Price List ID don't have value");
                                                            }

                                                        } else {
                                                            Log.e(TAG, mCurrentTableSyncing + " API's CustomerGroup JSONObject field don't have field 'price_list_id'");
                                                        }

                                                        if (xcustomerGroup == null) {
                                                            Log.e(TAG, "adding customerCustomerGroup... ");
                                                            customerGroupNet.insertTo(getHelper());
                                                            customerCustomerGroupAssoc = new CustomerCustomerGroupAssoc(customer, customerGroupNet);
                                                            newCustomerCustomerGroup.add(customerCustomerGroupAssoc);
                                                        } else {
                                                            Log.e(TAG, "adding customerCustomerGroup... ");
                                                            customerCustomerGroupAssoc = new CustomerCustomerGroupAssoc(customer, xcustomerGroup);
                                                            newCustomerCustomerGroup.add(customerCustomerGroupAssoc);
                                                        }

                                                        //customerCustomerGroupAssoc.insertTo(getHelper());
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

                                    Log.e(TAG, "customer status: " + customer.getStatus());
                                    if (initialSync || lastUpdatedAt == null) {
                                        if (customer.getStatus() == null) {
                                            Log.e(TAG, "saving customer: " + customer.getName());
                                            newCustomer.add(customer);
                                        }
                                    } else {
                                        if (isExisting(customer, Table.CUSTOMERS)) {
                                            Customer trueCustomer = getHelper().fetchObjects(Customer.class).queryBuilder().where().eq("returnId", customer.getReturnId()).queryForFirst();
                                            customer.setId(trueCustomer.getId());

                                            if (customer.getStatus() == null) {
                                                Log.e(TAG, "updating customer: " + customer.toString());
                                                updateCustomer.add(customer);
                                            } else {
                                                Log.e(TAG, "deleting customer: " + customer.toString());
                                                deleteCustomer.add(customer);
                                            }
                                        } else {
                                            if (customer.getStatus() == null) {
                                                Log.e(TAG, "saving customer: " + customer.toString());
                                                newCustomer.add(customer);
                                            } else {
                                                Log.e(TAG, "skipping customer: " + customer.toString());
                                            }
                                        }
                                    }
                                }

                                newCustomer.doOperationBT3(Customer.class, getHelper());
                                updateCustomer.doOperationBT3(Customer.class, getHelper());
                                deleteCustomer.doOperationBT3(Customer.class, getHelper());

                                newCustomerCustomerGroup.doOperation(CustomerCustomerGroupAssoc.class, getHelper());
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
                                //TODO
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

                                    if (jsonObject.has("document_type_id")) {
                                        if (jsonObject.isNull("document_type_id")) {
                                            int document_id = jsonObject.getInt("document_type_id");
                                            DocumentType documentType = getHelper().fetchIntId(DocumentType.class).queryForId(document_id);

                                            if (isExisting(documentType, Table.DOCUMENT_TYPES)) {
                                                documentPurpose.setDocumentType(documentType);
                                            } else {
                                                Log.e(TAG, "Document Type's don't have Doc Type");
                                            }
                                        } else {
                                            Log.e(TAG, "API " + mCurrentTableSyncing + " 'document_type_id' field is null");
                                        }

                                    } else {
                                        Log.e(TAG, "API " + mCurrentTableSyncing + " dont have 'document_type_id' field.");
                                    }

                                    if (initialSync || lastUpdatedAt == null) {
                                        documentPurpose.dbOperation(getHelper(), DatabaseOperation.INSERT);
                                    } else {
                                        if (documentPurpose != null) {
                                            if (isExisting(documentPurpose, Table.DOCUMENT_PURPOSES)) {
                                                if (documentPurpose.getStatus().equalsIgnoreCase("D")) {
                                                    documentPurpose.dbOperation(getHelper(), DatabaseOperation.DELETE);
                                                } else {
                                                    documentPurpose.dbOperation(getHelper(), DatabaseOperation.UPDATE);
                                                }
                                            } else {
                                                documentPurpose.dbOperation(getHelper(), DatabaseOperation.INSERT);
                                            }
                                        } else {
                                            Log.e(TAG, "documentPurpose is null, skipping");
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

                            newDocument.doOperationBT3(Document.class);
                            updateDocument.doOperationBT3(Document.class);
                            deleteDocument.doOperationBT3(Document.class);
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
                        case LAYAWAYS:
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
                                    for (InvoiceLine invoiceLine : invoice.getInvoiceLines()) {
                                        if (invoiceLine.getExtras() == null)
                                            invoiceLine.setNo_discount_subtotal(invoiceLine.getSubtotal());
                                        else {
                                            Extras extras = invoiceLine.getExtras();
                                            double no_discount = Double.parseDouble(invoiceLine.getSubtotal());
                                            if (extras.getProduct_discount_amount() != null && !extras.getProduct_discount_amount().isEmpty()) {
                                                List<String> productDiscountTexts = Arrays.asList(extras.getProduct_discount_amount().split(","));
                                                for (String discount : productDiscountTexts) {
                                                    if (!discount.isEmpty())
                                                        no_discount += Double.parseDouble(discount);
                                                }
                                            }
                                            if (extras.getCompany_discount_amount() != null && !extras.getCompany_discount_amount().isEmpty()) {
                                                List<String> companyDiscountTexts = Arrays.asList(extras.getCompany_discount_amount().split(","));
                                                for (String discount : companyDiscountTexts) {
                                                    if (!discount.isEmpty())
                                                        no_discount += Double.parseDouble(discount);
                                                }
                                            }
                                            invoiceLine.setNo_discount_subtotal(String.valueOf(no_discount));
                                        }
                                    }

                                    Log.e(">>LAYAWAYS", jsonObject.toString());

                                    if (mCurrentTableSyncing == Table.LAYAWAYS) {
                                        //LOGIN

                                        if (isExisting(invoice, Table.INVOICES)) {
                                            OfflineData offlineData = getHelper().fetchObjects(OfflineData.class).queryBuilder().where().eq("reference_no", invoice.getReference()).queryForFirst();
                                            offlineData.setReturnId(jsonObject.getString("id"));
                                            Invoice existing_invoice = offlineData.getObjectFromData(Invoice.class);
                                            existing_invoice.setPayments(invoice.getPayments());
                                            existing_invoice.markSentPayment(jsonObject.getInt("id"));

                                            existing_invoice.updateTo(getHelper());
                                            offlineData.updateTo(getHelper());
                                        } else {
                                            int customer_id = jsonObject.getInt("customer_id");

                                            Customer layawayCustomer = getHelper().fetchObjects(Customer.class).queryBuilder().where().eq("returnId",
                                                    customer_id).queryForFirst();

                                            Branch currentBranch = getHelper().fetchObjects(Branch.class).queryBuilder().where().eq("id", getSession().getCurrent_branch_id()).queryForFirst();

                                            invoice.setInvoice_date(jsonObject.getString("utc_invoice_date"));
                                            invoice.setBranch(currentBranch);
                                            if (layawayCustomer != null) {
                                                invoice.setCustomer(layawayCustomer);
                                            } else {
                                                Log.e(TAG, "customer is null!");
                                            }

                                            OfflineData offlineData = new OfflineData(invoice, OfflineDataType.SEND_INVOICE);
                                            offlineData.setBranch_id(currentBranch.getId());
                                            offlineData.setBranchName(currentBranch.getName());
                                            offlineData.setSynced(true);

                                            offlineData.insertTo(getHelper());
                                            invoice = offlineData.getObjectFromData(Invoice.class);
                                            invoice.markSentPayment(jsonObject.getInt("id"));
                                            invoice.updateTo(getHelper());

                                            offlineData.setReturnId(jsonObject.getString("id"));
                                            offlineData.updateTo(getHelper());
                                        }

                                    } else {

                                        if (initialSync || lastUpdatedAt == null) {
                                            newInvoice.add(invoice);
                                        } else {
                                            if (isExisting(invoice, Table.INVOICES)) {
                                                updateInvoice.add(invoice);
                                            } else {
                                                newInvoice.add(invoice);
                                            }
                                        }
                                        newInvoice.doOperationBT3(Invoice.class);
                                        updateInvoice.doOperationBT3(Invoice.class);
                                    }
                                }
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, page, numberOfPages);
                            }

                            updateNext(requestType, size);
                            break;
                        case INVOICE_PURPOSES:
                            BatchList<InvoicePurpose> newInvoicePurpose = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<InvoicePurpose> updateInvoicePurpose = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
                            BatchList<InvoicePurpose> deleteInvoicePurpose = new BatchList<>(DatabaseOperation.DELETE, getHelper());


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
                                                extras.setId(InvoicePurpose.class.getName().toUpperCase(), invoicePurpose.getId());
                                                extras.setRequire_date(json_extras.getBoolean("require_date"));

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
                                        // status A  I D
                                        if (!jsonObject.get("status").equals("D")) {
                                            newInvoicePurpose.add(invoicePurpose);
                                        } else {
                                            Log.e(TAG, "skipping saving of invoice purposes");
                                        }
                                    } else {
                                        if (isExisting(invoicePurpose, Table.INVOICE_PURPOSES)) {
                                            Log.e(TAG, "invoice purposes is existing.. processing...");
                                            if (jsonObject.get("status").equals("A")) {
                                                Log.e(TAG, "invoice purposes is Active.. updating...");
                                                updateInvoicePurpose.add(invoicePurpose);
                                            }

                                            if (jsonObject.get("status").equals("I")) {
                                                Log.e(TAG, "invoice purposes is Inactive.. updating...");

                                                updateInvoicePurpose.add(invoicePurpose);
                                            }

                                            if (jsonObject.get("status").equals("D")) {
                                                Log.e(TAG, "invoice purposes is D.. deleting...");

                                                deleteInvoicePurpose.add(invoicePurpose);
                                            }


                                        } else {
                                            if (jsonObject.getString("status").equals("A")) {
                                                Log.e(TAG, "invoice purposes is Active.. inserting...");
                                                newInvoicePurpose.add(invoicePurpose);

                                            } else if (jsonObject.getString("status").equals("I")) {
                                                Log.e(TAG, "invoice purposes is Inactive.. inserting...");
                                                newInvoicePurpose.add(invoicePurpose);
                                            } else {
                                                Log.e(TAG, "skipping saving of invoice purpose list ");
                                            }
                                        }
                                    }
                                }

                                newInvoicePurpose.doOperationBT(InvoicePurpose.class);
                                updateInvoicePurpose.doOperationBT(InvoicePurpose.class);
                                deleteInvoicePurpose.doOperationBT(InvoicePurpose.class);
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
                            BatchList<CustomerCategory> deleteCustomerCategory = new BatchList<>(DatabaseOperation.DELETE, getHelper());

                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                syncNext();
                                return;
                            } else {
                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    CustomerCategory customerCategory = gson.fromJson(jsonObject.toString(), CustomerCategory.class);

                                    if (initialSync || lastUpdatedAt == null) {

                                        //status A, I, D,
                                        if (jsonObject.getString("status").equals("A")) {
                                            newCustomerCategory.add(customerCategory);
                                        }

                                        if (jsonObject.getString("status").equals("I")) {
                                            newCustomerCategory.add(customerCategory);
                                        }

                                    } else {
                                        if (isExisting(customerCategory, Table.CUSTOMER_CATEGORIES)) {
                                            if (jsonObject.getString("status").equals("A")) {
                                                updateCustomerCategory.add(customerCategory);
                                            }

                                            if (jsonObject.getString("status").equals("I")) {
                                                updateCustomerCategory.add(customerCategory);
                                            }

                                            if (jsonObject.getString("status").equals("D")) {
                                                deleteCustomerCategory.add(customerCategory);
                                            }

                                        } else {

                                            if (jsonObject.getString("status").equals("A")) {
                                                newCustomerCategory.add(customerCategory);
                                            }

                                            if (jsonObject.getString("status").equals("I")) {
                                                newCustomerCategory.add(customerCategory);
                                            }
                                        }
                                    }
                                }
                            }

                            newCustomerCategory.doOperationBT(CustomerCategory.class);
                            updateCustomerCategory.doOperationBT(CustomerCategory.class);
                            deleteCustomerCategory.doOperationBT(CustomerCategory.class);

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

                                    Extras paymentType_extras = null;

                                    if (jsonObject.has("extras")) {
                                        JSONObject json_extras = jsonObject.getJSONObject("extras");

                                        paymentType_extras = new Extras();
                                        paymentType_extras.setId(PaymentType.class.getName().toUpperCase(), paymentType.getId());

                                        if (!isExisting(paymentType_extras, Table.EXTRAS)) {
                                            if (json_extras.has("additional_fields")) {
                                                if (!json_extras.getString("additional_fields").isEmpty()) {
                                                    paymentType_extras.setAdditional_fields(json_extras.getString("additional_fields"));
                                                } else {
                                                    Log.e(TAG, mCurrentTableSyncing + " API " + " 'additional_fields' field don't have value");
                                                }
                                            } else {
                                                Log.e(TAG, mCurrentTableSyncing + " API don't have " + "'additional_fields' field");
                                            }
                                            if (json_extras.has("show")) {
                                                if (!json_extras.getString("show").isEmpty()) {
                                                    paymentType_extras.setShow(json_extras.getInt("show"));
                                                } else {
                                                    Log.e(TAG, mCurrentTableSyncing + " API " + " 'show' field don't have value");
                                                }
                                            } else {
                                                Log.e(TAG, mCurrentTableSyncing + " API don't have " + "'show' field");
                                            }

                                        } else {
                                            Log.e(TAG, "Payment Type API don't have 'extras' field");
                                        }
                                    } else {
                                        Log.e(paymentType.getName() + " EXTRAS", "is on the database!");
                                        paymentType_extras = getHelper().fetchObjects(Extras.class).queryBuilder()
                                                .where().eq("id", paymentType.getId()).queryForFirst();
                                    }

                                    if (paymentType_extras != null) {
                                        Log.e(TAG, "Putting Extras to Payment Type");
                                        paymentType.setExtras(paymentType_extras);
                                    } else {
                                        Log.e(TAG, "Payment Type extras is null,=... moving on");
                                    }

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

                                if (mCustomIdIndex == 0) {
                                    listOfSalesPromotionIds = new ArrayList<>();
                                    listOfSalesPromotionStorage = new ArrayList<>();
                                } else {
                                    if (listOfSalesPromotionIds == null) {
                                        listOfSalesPromotionIds = new ArrayList<>();
                                    }

                                    if (listOfSalesPromotionStorage == null) {
                                        listOfSalesPromotionStorage = new ArrayList<>();
                                    }
                                }

                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                                    SalesPromotion salesPromotion = gson.fromJson(jsonObject.toString(), SalesPromotion.class);

                                    if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_PUSH) {
                                        Log.e(TAG, ">>>> sales push ");
                                        salesPromotion.setSalesPromotionType("sales_push");
                                    }

                                    if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_SALES_DISCOUNT) {
                                        Log.e(TAG, ">>>> sales discounts ");
                                        salesPromotion.setSalesPromotionType("sales_discounts");
                                    }

                                    if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_POINTS) {
                                        Log.e(TAG, ">>>> points ");
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


                                    boolean status = true;

                                    if (jsonObject.get("status").equals("D")) {
                                        status = false;
                                    }

                                    if (jsonObject.get("status").equals("I")) {
                                        status = false;
                                    }

                                    if (jsonObject.get("status").equals("A")) {
                                        status = true;
                                    }

                                    if (status) {
                                        Log.e(TAG, "Adding id and object");
                                        salesPromotion.insertTo(getHelper());
                                        if (salesPushSettings != null) {
                                            Log.e(TAG, "Sales push settings update");
                                            salesPushSettings.setSalesPromotion(salesPromotion); // connection
                                            salesPushSettings.updateTo(getHelper());
                                        }

                                        listOfSalesPromotionIds.add(salesPromotion.getId());
                                        listOfSalesPromotionStorage.add(salesPromotion);
                                    } else {
                                        Log.e(TAG, "Skipping sales promotion with status: " + jsonObject.getString("status"));
                                        if (!initialSync || lastUpdatedAt != null) {
                                            List<Discount> discountList = getHelper().fetchObjects(Discount.class).queryBuilder().where().eq("sales_promotion_id", salesPromotion).query();
                                            Log.e(TAG, "status is either I or D deleting it all");

                                            //delete discount connected
                                            for (Discount discount : discountList) {
                                                discount.deleteTo(getHelper());
                                            }

                                            salesPromotion.deleteTo(getHelper());

                                            if (salesPushSettings != null) {
                                                salesPushSettings.deleteTo(getHelper());
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
                            BatchList<PriceList> deletePriceList = new BatchList<>(DatabaseOperation.DELETE, getHelper());

                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                syncNext();
                                return;
                            } else {

                                if (listOfIdsPriceListSorted == null) {
                                    Log.e(TAG, "price list sorted is null, creating instance");
                                    listOfIdsPriceListSorted = new ArrayList<>();
                                } else {
                                    Log.e(TAG, "price list sorted is not null");
                                }

                                if (listOfPricelistIds == null) {
                                    Log.e(TAG, "price list ids is null, creating instance");
                                    listOfPricelistIds = new ArrayList<>();
                                } else {
                                    Log.e(TAG, "price list ids is not null");
                                }

                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    PriceList priceList = gson.fromJson(jsonObject.toString(), PriceList.class);
                                    /*if (isExisting(priceList, Table.PRICE_LISTS))
                                        continue;*/

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


                                    // 2 WAYS UPDATING initialLogin and SyncModules

                                    // check customer & customergroup
                                    // if not existing

                                    if (initialSync || lastUpdatedAt == null) {
                                        if (priceList.getStatus().equals("A")) {
                                            // no need to tag
                                            listOfIdsPriceListSorted.add(priceList.getId());
                                            listOfPricelistIds.add(priceList.getId());
                                            Log.e(TAG, "saving price list.. currentSyncing: " + mCurrentTableSyncing + "size: " + listOfIdsPriceListSorted.size());
                                            newPriceList.add(priceList);
                                        }
                                    } else { // UPDATING

                                        Log.e(TAG, "executing updating of price_lists ");

                                        if (jsonObject.has("customers")) {
                                            JSONArray customerArray = jsonObject.getJSONArray("customers");

                                            if (customerArray.length() != 0) {
                                                for (int t = 0; t < customerArray.length(); t++) {
                                                    Log.e(TAG, "customerArray From PriceList size: " + customerArray.length());
                                                    JSONObject customerFromPriceList = customerArray.getJSONObject(t);
                                                    Log.e(TAG, "customerFromPriceList: " + customerFromPriceList.toString());

                                                    if (customerFromPriceList.has("id")) {
                                                        int customerPriceLIstId = customerFromPriceList.getInt("id");

                                                        //listOfCustomerId.add(customerPriceLIstId);
                                                        Log.e(TAG, "Customer From PriceList ID is " + customerPriceLIstId);

                                                        Customer xPriceListCustomer = getHelper().fetchObjects(Customer.class).queryBuilder().where().eq("returnId", customerPriceLIstId).queryForFirst();

                                                        if (xPriceListCustomer != null) {
                                                            xPriceListCustomer.setPriceList(priceList);
                                                            xPriceListCustomer.updateTo(getHelper());
                                                            //updating / deleting
                                                            if (isExisting(priceList, Table.PRICE_LISTS)) {
                                                                if (priceList.getStatus().equals("A")) {
                                                                    listOfIdsPriceListSorted.add(priceList.getId());
                                                                    listOfPricelistIds.add(priceList.getId());
                                                                    Log.e(TAG, "updating price list.. currentSyncing: " + mCurrentTableSyncing + "size: " + listOfIdsPriceListSorted.size());

                                                                    updatePriceList.add(priceList);
                                                                } else {
                                                                    Log.e(TAG, "deleting price list..");
                                                                    deletePriceList.add(priceList);
                                                                }
                                                            } else { // inserting
                                                                listOfIdsPriceListSorted.add(priceList.getId());
                                                                listOfPricelistIds.add(priceList.getId());
                                                                newPriceList.add(priceList);
                                                            }
                                                        } else {
                                                            Log.e(TAG, "cannot find Customer ");
                                                        }

                                                    } else {
                                                        Log.e(TAG, "customerFromPriceList don't have id.. skipping...");
                                                    }
                                                }
                                            } else {
                                                Log.e(TAG, "customerArray is null skipping");
                                            }
                                        } else {
                                            Log.e(TAG, "API " + mCurrentTableSyncing + " don't have 'customers' field");
                                        }

                                        if (jsonObject.has("customer_groups")) {
                                            JSONArray customerGroupArray = jsonObject.getJSONArray("customer_groups");

                                            if (customerGroupArray.length() != 0) {
                                                for (int t = 0; t < customerGroupArray.length(); t++) {
                                                    JSONObject customerGroupFromPriceList = customerGroupArray.getJSONObject(t);

                                                    if (customerGroupFromPriceList.has("id")) {
                                                        int customerGroupPriceLIstId = customerGroupFromPriceList.getInt("id");
                                                        // listOfCustomerGroupId.add(customerGroupPriceLIstId);
                                                        Log.e(TAG, "CustomerGroup From PriceList ID is " + customerGroupPriceLIstId);
                                                        CustomerGroup xPriceListCustomerGroup = CustomerGroup.fetchById(getHelper(), CustomerGroup.class, customerGroupPriceLIstId);

                                                        if (xPriceListCustomerGroup != null) {
                                                            xPriceListCustomerGroup.setPriceList(priceList);
                                                            xPriceListCustomerGroup.updateTo(getHelper());
                                                            if (isExisting(priceList, Table.PRICE_LISTS)) {
                                                                if (priceList.getStatus().equals("A")) {
                                                                    listOfIdsPriceListSorted.add(priceList.getId());
                                                                    listOfPricelistIds.add(priceList.getId());
                                                                    Log.e(TAG, "updating price list.. currentSyncing: " + mCurrentTableSyncing + "size: " + listOfIdsPriceListSorted.size());
                                                                    updatePriceList.add(priceList);
                                                                } else {
                                                                    Log.e(TAG, "deleting price list..");
                                                                    deletePriceList.add(priceList);
                                                                }
                                                            } else { // inserting
                                                                listOfIdsPriceListSorted.add(priceList.getId());
                                                                listOfPricelistIds.add(priceList.getId());
                                                                newPriceList.add(priceList);
                                                            }
                                                        } else {
                                                            Log.e(TAG, "cannot find customer group ");
                                                        }

                                                    } else {
                                                        Log.e(TAG, "customerGroupFromPriceList don't have id.. skipping...");
                                                    }
                                                }
                                            } else {
                                                Log.e(TAG, "customerGroupArray is null skipping");
                                            }
                                        } else {
                                            Log.e(TAG, "API " + mCurrentTableSyncing + " don't have 'customer_groups' field");
                                        }
                                    }
                                }

                                // for updating delete all details for the updated Price List Header

                                List<Price> price = Price.fetchWithConditionInt(getHelper(), Price.class, new DBTable.ConditionsWindow<Price, Integer>() {
                                    @Override
                                    public Where<Price, Integer> renderConditions(Where<Price, Integer> where) throws SQLException {
                                        where.in("price_list_id", listOfIdsPriceListSorted);
                                        return where;
                                    }
                                });

                                Log.e(TAG, "Size of price list to delete: " + price.size());

                                BatchList<Price> deletePrice = new BatchList<>(DatabaseOperation.DELETE, getHelper());

                                deletePrice.addAll(price);
                                Log.e(TAG, "Deleting Price List... ");
                                deletePrice.doOperationBT(Price.class);

                                newPriceList.doOperationBT(PriceList.class);
                                updatePriceList.doOperationBT(PriceList.class);
                                deletePriceList.doOperationBT(PriceList.class);
                            }
                            updateNext(requestType, size);
                            break;
                        case ROUTE_PLANS_DETAILS:
                            BatchList<RoutePlanDetail> newRoutePlanDetails = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<RoutePlanDetail> updateRoutePlanDetails = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
                            if (size == 0) {
                                Log.e(TAG, "eto Oh. <<<<");
                                mCustomPageIndex = 1;
                                updateNext(requestType, 0);
                                return;
                            } else {
                                RoutePlan xRoutePlan = (RoutePlan) listOfIds.get(mCustomIdIndex);

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
                                            Customer customer = getHelper().fetchObjects(Customer.class).queryBuilder().where().eq("returnId",
                                                    customer_id).queryForFirst();
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
                            }
                            mCustomPageIndex++;
                            updateNext(requestType, listOfIds.size());
                            break;
                        case PRICE_LISTS_DETAILS:
                            BatchList<Price> newPrice = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                mCustomPageIndex = 1;
                                updateNext(requestType, 0);
                                return;
                            } else {
                                listOfIds = getHelper().fetchObjectsList(PriceList.class);
                                PriceList priceList /*= (PriceList) listOfIds.get(mCustomIdIndex)*/;

                                for (int i = 0; i < size; i++) {
                                    JSONObject priceListJsonObject = jsonArray.getJSONObject(i);

                                    priceList = getHelper().fetchObjects(PriceList.class).queryBuilder().where().eq("id", priceListJsonObject
                                            .getInt("price_list_id")).queryForFirst();

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

                                    Price priceListX = getHelper().fetchObjects(Price.class).queryBuilder().where().eq("id", price.getId()).queryForFirst();

                                    if (priceListX != null) {
                                        priceListX.deleteTo(getHelper());
                                    }
                                    newPrice.add(price);
                                }

                                newPrice.doOperation(Price.class);
                            }
                            Log.e(TAG, "Price List Details");
                            mCustomPageIndex++;
                            updateNext(requestType, listOfIdsPriceListSorted.size());
                            break;
                        case SALES_PROMOTIONS_POINTS_DETAILS:
                        case SALES_PROMOTIONS_SALES_DISCOUNT_DETAILS:
                            BatchList<Discount> newDiscount = new BatchList<>(DatabaseOperation.INSERT, getHelper());

                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                mCustomPageIndex = 1;
                                updateNext(requestType, 0);
                                return;
                            } else {
                                SalesPromotion salesPromotion = SalesPromotion.fetchById(getHelper(), SalesPromotion.class, listOfSalesPromotionIds.get(mCustomIdIndex));

                                for (int i = 0; i < size; i++) {
                                    JSONObject discountJsonObject = jsonArray.getJSONObject(i);

                                    Discount discount = gson.fromJson(discountJsonObject.toString(), Discount.class);
                                    if (salesPromotion != null) {
                                        Log.e(TAG, "sales Promotion is not null: ");
                                        discount.setSalesPromotion(salesPromotion);

                                        if (discountJsonObject.has("product_id")) {
                                            if (!discountJsonObject.isNull("product_id")) {
                                                int product_id = discountJsonObject.getInt("product_id");
                                                Product product = getHelper().fetchObjects(Product.class).queryBuilder().where().eq("id", product_id).queryForFirst();

                                                if (product != null) {
                                                    discount.setProduct(product);
                                                    Log.e(TAG, "Product IDx: " + product.getName());
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

                                        Log.e(TAG, "adding to new Discount ");
                                        newDiscount.add(discount);

                                    } else {
                                        Log.e(TAG, "Can't add discount Sales Promotion Does not exist in database");
                                    }
                                }

                                newDiscount.doOperationBT2(Discount.class);
                            }
                            //Log.e(TAG, "Sales Promotions Discount");
                            mCustomPageIndex++;
                            updateNext(requestType, listOfSalesPromotionIds.size());
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

                                    List<RoutePlan> routePlen = RoutePlan.fetchAll(getHelper(), RoutePlan.class);

                                    for (RoutePlan p : routePlen) {
                                        p.deleteTo(getHelper());
                                    }

                                    if (initialSync || lastUpdatedAt == null) {
                                        if (routePlan.getStatus() == null) {
                                            Log.e(TAG, "route plan status is not null adding");
                                            newRoutePlans.add(routePlan);
                                        } else {
                                            Log.e(TAG, "route plan status is null ");
                                        }
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
                        case ACCOUNT_PRICES:
                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                syncNext();
                                return;
                            } else {
                                BatchList<AccountPrice> newAccountPrice = new BatchList<>();
                                BatchList<AccountPrice> updateAccountPrice = new BatchList<>();
                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    AccountPrice accountPrice = gson.fromJson(jsonObject.toString(), AccountPrice.class);

                                    int product_id = 0;
                                    int unit_id = 0;

                                    if (jsonObject.has("product_id")) {
                                        if (!jsonObject.isNull("product_id")) {
                                            product_id = jsonObject.getInt("product_id");
                                        } else {
                                            Log.e(TAG, "product_id is null");
                                        }
                                    } else {
                                        Log.e(TAG, mCurrentTableSyncing + " API don't have 'product_id' field");
                                    }

                                    if (jsonObject.has("unit_id")) {
                                        if (!jsonObject.isNull("unit_id")) {
                                            unit_id = jsonObject.getInt("unit_id");
                                        } else {
                                            Log.e(TAG, "unit_id is null");
                                        }
                                    } else {
                                        Log.e(TAG, mCurrentTableSyncing + " API don't have 'product_id' field");
                                    }

                                    if (product_id != 0) {
                                        Product px = Product.fetchById(getHelper(), Product.class, product_id);
                                        accountPrice.setProduct(px);
                                        if (px != null) {
                                            Log.e(TAG, "Product with id " + product_id + " found.");
                                        } else {
                                            Log.e(TAG, "Product with id " + product_id + " not found.");
                                        }
                                    }

                                    if (unit_id != 0) {
                                        Unit u = Unit.fetchById(getHelper(), Unit.class, unit_id);
                                        accountPrice.setUnit(u);
                                        if (u != null) {
                                            Log.e(TAG, "Unit with id " + unit_id + " found.");
                                        } else {
                                            Log.e(TAG, "Unit with id " + unit_id + " not found.");
                                        }
                                    }

                                    if (initialSync || lastUpdatedAt == null) {
                                        newAccountPrice.add(accountPrice);
                                    } else {
                                        if (isExisting(accountPrice, Table.ACCOUNT_PRICES)) {
                                            updateAccountPrice.add(accountPrice);
                                        } else {
                                            newAccountPrice.add(accountPrice);
                                        }
                                    }
                                }
                                newAccountPrice.doOperation(AccountPrice.class, getHelper());
                                updateAccountPrice.doOperation(AccountPrice.class, getHelper());
                            }
                            updateNext(requestType, size);
                            break;
                        case ORDERS:
                        case ORDERS_PURCHASES:
                        case ORDERS_STOCK_REQUEST:
                            Log.e(TAG, "onSuccess: " + mCurrentTableSyncing);
                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                syncNext();
                                return;
                            } else {
                                BatchList<Order> newOrders = new BatchList<>();
                                BatchList<Order> updateOrders = new BatchList<>();

                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Order orders = gson.fromJson(jsonObject.toString(), Order.class);

                                    if (initialSync || lastUpdatedAt == null) {
                                        newOrders.add(orders);
                                    } else {
                                        if (isExisting(orders, Table.ORDERS)) {
                                            updateOrders.add(orders);
                                        } else {
                                            newOrders.add(orders);
                                        }
                                    }
                                }
                                newOrders.doOperation(Order.class, getHelper());
                                updateOrders.doOperation(Order.class, getHelper());
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
        } catch (SQLException |
                JSONException e
                )

        {
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

                Log.e(TAG, "Custom Page: " + mCustomPageIndex + " size: " + size);

                if (mCustomPageIndex > 1) {
                    Log.e(TAG, "Custom Page Index is greater than 1: " + mCustomPageIndex);
                    startSyncModuleContents(requestType);
                } else {
                    Log.e(TAG, "Custom ID Index is: " + mCustomIdIndex + " incrementing..");
                    mCustomIdIndex++;
                    Log.e(TAG, "Custom ID Index is: " + mCustomIdIndex);

                    if (mCurrentTableSyncing == Table.PRICE_LISTS_FROM_CUSTOMERS) {
                        if(mUpdatingPriceListFromCustomer) {
                            if (mCustomIdIndex < listOfIdsPriceListSorted.size()) {
                                Log.e(TAG, "ssCustom ID Index is less than size of details: " + listOfIdsPriceListSorted.size() + " staring request id index: " + mCustomIdIndex);
                                startSyncModuleContents(requestType);
                            } else {
                                mSyncModulesListener.onDownloadProgress(Table.PRICE_LISTS_FROM_CUSTOMERS, 1, 1);
                                Log.e(TAG, "ssSyncing next price list details");
                                syncNext();
                            }
                        } else {
                            Log.e(TAG, ">>> Custom ID Index is less than size of details: " + listOfIdsPriceListSorted.size() + " staring request id index: " + mCustomIdIndex);
                            page++;
                            startSyncModuleContents(requestType);
                        }
                    } else if (mCurrentTableSyncing == Table.PRICE_LISTS_DETAILS) {
                        if (mCustomIdIndex < listOfIdsPriceListSorted.size()) {
                            Log.e(TAG, "Custom ID Index is less than size of details: " + listOfIdsPriceListSorted.size() + " staring request id index: " + mCustomIdIndex);
                            startSyncModuleContents(requestType);
                        } else {
                            mSyncModulesListener.onDownloadProgress(Table.PRICE_LISTS_DETAILS, 1, 1);
                            Log.e(TAG, "Syncing next price list details");
                            syncNext();
                        }
                    } else if (mCurrentTableSyncing == Table.SALES_PROMOTIONS_POINTS_DETAILS || mCurrentTableSyncing == Table.SALES_PROMOTIONS_POINTS_DETAILS) {
                        if (mCustomIdIndex < listOfSalesPromotionIds.size()) {
                            Log.e(TAG, "Custom ID Index is less than size of details: " + listOfSalesPromotionIds.size() + " staring request id index: " + mCustomIdIndex);
                            startSyncModuleContents(requestType);
                        } else {
                            Log.e(TAG, "Syncing next list of ids");
                            mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                            syncNext();
                        }
                    } else {
                        if (mCustomIdIndex < listOfIds.size()) {
                            Log.e(TAG, "Custom ID Index is less than size of details: " + listOfIds.size() + " staring request id index: " + mCustomIdIndex);
                            startSyncModuleContents(requestType);
                        } else {
                            Log.e(TAG, "Syncing next list of ids");
                            mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                            syncNext();
                        }
                    }
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
            Log.e(TAG, "onError no Internet" +
                    " ");
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

        if (AccountTools.isLogout(getApplicationContext())) {
            Log.e(TAG, "Account is currently logout, setting initialSync to true ");
            initialSync = true;
        }

        Log.e(TAG, "Getting mCurrentModuleSetting...");
        app = getHelper().fetchObjects(ModuleSetting.class).queryBuilder().where().eq("module_type", "app").queryForFirst();

        if (app != null) {
            Log.e(TAG, "Module Setting App is found: " + app.toString());
        } else {
            Log.e(TAG, "Module is null");
        }

        Log.e(TAG, "Current Table: " + mCurrentTableSyncing);

        if (mCurrentTableSyncing == Table.USERS_ME ||
                mCurrentTableSyncing == Table.TAX_SETTINGS ||
                mCurrentTableSyncing == Table.DOCUMENT_TYPES ||
                mCurrentTableSyncing == Table.SETTINGS ||
                mCurrentTableSyncing == Table.ROUTE_PLANS) {

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