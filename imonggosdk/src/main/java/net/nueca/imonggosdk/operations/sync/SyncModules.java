package net.nueca.imonggosdk.operations.sync;

import android.os.Handler;
import android.util.Log;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

import net.nueca.imonggosdk.enums.DailySalesEnums;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Parameter;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchPrice;
import net.nueca.imonggosdk.objects.BranchTag;
import net.nueca.imonggosdk.objects.RoutePlan;
import net.nueca.imonggosdk.objects.SalesPushSettings;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.DailySales;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.LastUpdatedAt;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.objects.Settings;
import net.nueca.imonggosdk.objects.TaxRate;
import net.nueca.imonggosdk.objects.TaxSetting;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;
import net.nueca.imonggosdk.objects.associatives.ProductTaxRateAssoc;
import net.nueca.imonggosdk.objects.base.BatchList;
import net.nueca.imonggosdk.objects.customer.CustomerCategory;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentPurpose;
import net.nueca.imonggosdk.objects.document.DocumentType;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoicePurpose;
import net.nueca.imonggosdk.objects.invoice.PaymentTerms;
import net.nueca.imonggosdk.objects.invoice.PaymentType;
import net.nueca.imonggosdk.objects.invoice.SalesPromotion;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.objects.price.PriceList;
import net.nueca.imonggosdk.operations.ImonggoTools;
import net.nueca.imonggosdk.operations.http.ImonggoOperations;
import net.nueca.imonggosdk.tools.DateTimeTools;
import net.nueca.imonggosdk.tools.LastUpdateAtTools;
import net.nueca.imonggosdk.tools.LoggingTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

            if (mCurrentTableSyncing == Table.PRICE_LISTS_DETAILS) {
                Log.e(TAG, "Previous Count: " + count + "\n getting the count of Price List");

                count = getHelper().fetchObjectsList(PriceList.class).size();

                Log.e(TAG, "Current Count: " + count + "\n of Price List");
            }

            ImonggoOperations.getAPIModule(this, getQueue(), getSession(), this,
                    mCurrentTableSyncing, getSession().getServer(), requestType,
                    getParameters(requestType));

        } else if (requestType == RequestType.LAST_UPDATED_AT) {

            if (mCurrentTableSyncing == Table.DAILY_SALES) {
                ImonggoOperations.getAPIModule(this, getQueue(), getSession(),
                        this, mCurrentTableSyncing, getSession().getServer(),
                        RequestType.DAILY_SALES_TODAY, getParameters(RequestType.DAILY_SALES_TODAY));
                return;
            }

            Log.e(TAG, "LAST UPDATED AT");
            newLastUpdatedAt = null;
            lastUpdatedAt = null;

            // Get the last updated at
            QueryBuilder<LastUpdatedAt, Integer> queryBuilder = getHelper().fetchIntId(LastUpdatedAt.class).queryBuilder();

            if (mCurrentTableSyncing == Table.DOCUMENTS) {
                document_type = "transfer_out";
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
                Log.e(TAG, "TABLE NAME: " + lastUpdatedAt.getTableName());
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
                                    Parameter.FROM,
                                    Parameter.TO,
                                    Parameter.LAST_UPDATED_AT,
                                    Parameter.BRANCH_ID),  // RHY: Parameter.TARGET_BRANCH_ID Changed to cater transfer to branch
                            document_type, intransit_status, DateTimeTools.convertDateForUrl(from), DateTimeTools.convertDateForUrl(to),
                            getTargetBranchId(branchIndex));
                } else {
                    return String.format(ImonggoTools.generateParameter(
                                    Parameter.DOCUMENT_TYPE,
                                    Parameter.LAST_UPDATED_AT, Parameter.TARGET_BRANCH_ID,
                                    Parameter.AFTER), document_type, getTargetBranchId(branchIndex),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }
            }

            if (mCurrentTableSyncing == Table.SALES_PUSH) {
                return ImonggoTools.generateParameter(Parameter.SALES_PUSH, Parameter.LAST_UPDATED_AT);
            }

            // with branch_id
            if(mCurrentTableSyncing == Table.BRANCH_PRODUCTS ||
                    // mCurrentTableSyncing == Table.BRANCH_UNITS ||
                    mCurrentTableSyncing == Table.BRANCH_PRICE_LISTS ||
                    mCurrentTableSyncing == Table.BRANCH_ROUTE_PLANS ||
                    mCurrentTableSyncing == Table.BRANCH_CUSTOMERS) {
                return String.format(ImonggoTools.generateParameter(Parameter.LAST_UPDATED_AT,
                        Parameter.BRANCH_ID), getSession().getCurrent_branch_id());
            }

            return ImonggoTools.generateParameter(Parameter.LAST_UPDATED_AT);

        } else if (requestType == RequestType.API_CONTENT) {

            // Custom for Tax Settings
            if (mCurrentTableSyncing == Table.TAX_SETTINGS)
                return "";

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
                        mCurrentTableSyncing == Table.BRANCH_UNITS ||
                        mCurrentTableSyncing == Table.BRANCH_ROUTE_PLANS ||
                        mCurrentTableSyncing == Table.BRANCH_CUSTOMERS ||
                        mCurrentTableSyncing == Table.BRANCH_PRICE_LISTS) {
                    return String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.BRANCH_ID),
                            String.valueOf(page), getSession().getCurrent_branch_id());
                }

                if (mCurrentTableSyncing == Table.SALES_PUSH) {
                    return String.format(ImonggoTools.generateParameter(Parameter.SALES_PROMOTION_ID, Parameter.SALES_PUSH));
                }

                /*
                    if(mCurrentTableSyncing == Table.SALES_PROMOTIONS_DISCOUNT) {
                    return String.format(ImonggoTools.generateParameter(Parameter.SALES_PUSH, Parameter.DETAILS), String.valueOf(page));
                }*/

                if (mCurrentTableSyncing == Table.PRICE_LISTS_DETAILS) {
                    return String.format(ImonggoTools.generateParameter(Parameter.ACTIVE_ONLY, Parameter.PAGE, Parameter.BRANCH_ID),
                            String.valueOf(page), getSession().getCurrent_branch_id());
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
                            String.valueOf(page)
                    );
                }

                // request with branch id
                if (mCurrentTableSyncing == Table.BRANCH_PRODUCTS ||
                        mCurrentTableSyncing == Table.BRANCH_UNITS ||
                        mCurrentTableSyncing == Table.BRANCH_ROUTE_PLANS ||
                        mCurrentTableSyncing == Table.BRANCH_CUSTOMERS ||
                        mCurrentTableSyncing == Table.BRANCH_PRICE_LISTS) {
                    String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.BRANCH_ID, Parameter.AFTER),
                            String.valueOf(page),
                            getSession().getCurrent_branch_id(),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }

                if (mCurrentTableSyncing == Table.SALES_PUSH) {
                    return String.format(ImonggoTools.generateParameter(Parameter.SALES_PUSH, Parameter.PAGE, Parameter.AFTER),
                            String.valueOf(page), DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }

                if (mCurrentTableSyncing == Table.PRICE_LISTS) {
                    String.format(ImonggoTools.generateParameter(Parameter.ACTIVE_ONLY, Parameter.PAGE, Parameter.BRANCH_ID, Parameter.AFTER),
                            String.valueOf(page),
                            getSession().getCurrent_branch_id(),
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
                if (mCurrentTableSyncing == Table.BRANCH_USERS) {// This is when the module syncing is the User Branches
                    return String.format(ImonggoTools.generateParameter(Parameter.COUNT, Parameter.USER_ID),
                            String.valueOf(getUser().getId()));
                }

                if (mCurrentTableSyncing == Table.SALES_PUSH) {
                    return ImonggoTools.generateParameter(Parameter.SALES_PUSH, Parameter.COUNT);
                }

                // request with branch id
                if (mCurrentTableSyncing == Table.BRANCH_PRODUCTS ||
                        mCurrentTableSyncing == Table.BRANCH_UNITS ||
                        mCurrentTableSyncing == Table.BRANCH_ROUTE_PLANS ||
                        mCurrentTableSyncing == Table.BRANCH_CUSTOMERS ||
                        mCurrentTableSyncing == Table.BRANCH_PRICE_LISTS) {
                    return String.format(ImonggoTools.generateParameter(Parameter.ACTIVE_ONLY,
                            Parameter.COUNT, Parameter.BRANCH_ID), getSession().getCurrent_branch_id());
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

                if (mCurrentTableSyncing == Table.UNITS || mCurrentTableSyncing == Table.BRANCH_UNITS) {
                    if (lastUpdatedAt.getLast_updated_at() == null)
                        return ImonggoTools.generateParameter(Parameter.COUNT);
                }

                // request with branch id
                if (mCurrentTableSyncing == Table.BRANCH_PRODUCTS ||
                        mCurrentTableSyncing == Table.BRANCH_UNITS ||
                        mCurrentTableSyncing == Table.BRANCH_ROUTE_PLANS ||
                        mCurrentTableSyncing == Table.BRANCH_CUSTOMERS ||
                        mCurrentTableSyncing == Table.BRANCH_PRICE_LISTS) {
                    return String.format(ImonggoTools.generateParameter(Parameter.ACTIVE_ONLY,
                                    Parameter.COUNT, Parameter.AFTER, Parameter.BRANCH_ID),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()),
                            getSession().getCurrent_branch_id());
                }

                if (mCurrentTableSyncing == Table.SALES_PUSH) {
                    return String.format(ImonggoTools.generateParameter(Parameter.SALES_PUSH, Parameter.COUNT, Parameter.AFTER),
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

        if (mCurrentTableSyncing == Table.TAX_SETTINGS ||
                mCurrentTableSyncing == Table.DOCUMENT_TYPES ||
                mCurrentTableSyncing == Table.DOCUMENT_PURPOSES ||
                mCurrentTableSyncing == Table.SETTINGS ||
                mCurrentTableSyncing == Table.SALES_PROMOTIONS_DISCOUNT ||
                mCurrentTableSyncing == Table.PRICE_LISTS_DETAILS) {
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
                    // since this is the first
                    page = 1;
                    count = 0;

                    newLastUpdatedAt = gson.fromJson(jsonObject.toString(), LastUpdatedAt.class);

                    if (mCurrentTableSyncing == Table.DOCUMENTS) {
                        newLastUpdatedAt.setTableName(LastUpdateAtTools.getTableToSync(module, getTargetBranchId(branchIndex) + ""));
                        Log.e(TAG, "Table Name: " + newLastUpdatedAt.getTableName());
                    } else {
                        newLastUpdatedAt.setTableName(LastUpdateAtTools.getTableToSync(module));
                    }

                    if (lastUpdatedAt != null) {
                        newLastUpdatedAt.updateTo(getHelper());
                    } else {
                        newLastUpdatedAt.insertTo(getHelper());
                        Log.e(TAG, "New Last Updated At: " + jsonObject.toString());
                    }

                    // USERS and  TAX SETTINGS DON'T SUPPORT COUNT
                    if (mCurrentTableSyncing == Table.USERS || mCurrentTableSyncing == Table.TAX_SETTINGS) {
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
                        syncNext();
                        return;
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
                        case PRODUCTS:

                            BatchList<Product> newProducts = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<Product> updateProducts = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
                            BatchList<Product> deleteProducts = new BatchList<>(DatabaseOperation.DELETE, getHelper());

                            Table productTable = Table.PRODUCTS;

                            Log.e(TAG, productTable + " | size: " + size + " page: " + page + " max page: " + numberOfPages);
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
                                        }

                                        if (json_extras.has("default_ordering_unit")) {
                                            default_ordering_unit_id = json_extras.getString("default_ordering_unit_id");
                                        }

                                        product_extras.setDefault_ordering_unit_id(default_ordering_unit_id);
                                        product_extras.setDefault_selling_unit(default_selling_unit);

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

                                            if (!jsonTaxRateObject.getString("branch_id").equals("null")) {
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

                                    if (jsonObject.has("branch_prices")) {
                                        JSONArray branchPricesArray = jsonObject.getJSONArray("branch_prices");
                                        int branchPriceSize = branchPricesArray.length();
                                        for (int y = 0; y < branchPriceSize; y++) {
                                            JSONObject jsonBranchPriceObject = branchPricesArray.getJSONObject(y);
                                            BranchPrice branchPrice = gson.fromJson(jsonBranchPriceObject.toString(), BranchPrice.class);

                                            if (isExisting(current_branch, Table.BRANCHES)) {
                                                branchPrice.setBranch(current_branch);
                                                branchPrice.setProduct(product);
                                                // check if current branch matches with this branch price
                                                if (branchPrice.getBranch().getId() == current_branch_id) {
                                                    Log.e(TAG, "Product " + branchPrice.getProduct().getName() +
                                                            " Content: " + branchPrice.toString());
                                                    getHelper().delete(BranchPrice.class, branchPrice);
                                                    branchPrice.dbOperation(getHelper(), DatabaseOperation.INSERT);
                                                }


                                            } else {
                                                Log.e(TAG, "Branch ID " + current_branch_id + "does not exist. Skipping Branch Prices");
                                            }
                                        }
                                    }

                                    if (jsonObject.has("tag_list")) {
                                        // Save tags to the database
                                        JSONArray tagsListArray = jsonObject.getJSONArray("tag_list");
                                        int tagsSize = tagsListArray.length();
                                        for (int tagsI = 0; tagsI < tagsSize; tagsI++) {
                                            Log.e(TAG, tagsListArray.getString(tagsI));
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

                                    if (initialSync || lastUpdatedAt == null) {
                                        product.setSearchKey(product.getName() + product.getStock_no());
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
                        case BRANCH_UNITS:
                        case UNITS:
                            if (size == 0) {
                                syncNext();
                                return;
                            } else {
                                BatchList<Unit> newUnits = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                                BatchList<Unit> updateUnits = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
                                BatchList<Unit> deleteUnits = new BatchList<>(DatabaseOperation.DELETE, getHelper());

                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Unit unit = gson.fromJson(jsonObject.toString(), Unit.class);

                                    Product product = getHelper().fetchObjects(Product.class).queryBuilder().where().eq("id", jsonObject.getString("product_id")).queryForFirst();

                                    //Log.e(TAG, "Product name: " + product.getName());

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
                                String name_user_id = "user_id";
                                String name_extras = "extras";

                                int user_id;
                                int customer_category_id;

                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Customer customer = gson.fromJson(jsonObject.toString(), Customer.class);
                                    Extras customer_extras = new Extras();

                                    if (jsonObject.has(name_extras)) {
                                        JSONObject json_extras = jsonObject.getJSONObject(name_extras);

                                        user_id = 0;
                                        customer_category_id = 0;

                                        if (json_extras.has(name_user_id)) {
                                            if (json_extras.getString(name_user_id) != null || json_extras.getString(name_user_id).equals("")) {
                                                user_id = json_extras.getInt(name_user_id);
                                            }
                                        }

                                        if (json_extras.has(name_customer_category)) {
                                            if (json_extras.getString(name_customer_category) != null || json_extras.get(name_customer_category).equals("")) {
                                                customer_category_id = json_extras.getInt(name_customer_category);
                                            }
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

                                    if (initialSync || lastUpdatedAt == null) {
                                        newCustomer.add(customer);
                                        /*customer.insertExtrasTo(getHelper());
                                        customer.insertTo(getHelper());*/
                                    } else {
                                        if (isExisting(customer, Table.CUSTOMERS)) {
                                            if (customer.getStatus() == null) {
                                                updateCustomer.add(customer);
//                                                customer.updateExtrasTo(getHelper());
//                                                customer.updateTo(getHelper());
                                            } else {
                                                deleteCustomer.add(customer);

                                                /*customer.deleteExtrasTo(getHelper());
                                                customer.deleteTo(getHelper());*/
                                            }
                                        } else {
                                            newCustomer.add(customer);
                                            /*customer.insertExtrasTo(getHelper());
                                            customer.insertTo(getHelper());*/

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
                                        if (!document.getIntransit_status().equalsIgnoreCase("received")) {
                                            newDocument.add(document);
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
                        case BRANCH_PRICE_LISTS:
                        case PRICE_LISTS:
                            BatchList<Price> newPrice = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<CustomerGroup> updateCustomerGroup = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
                            BatchList<Customer> updateCustomerPriceList = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                            BatchList<PriceList> newPriceList = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
                            BatchList<PriceList> updatePriceList = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                syncNext();
                                return;
                            } else {

                                JSONObject jsonObject = jsonArray.getJSONObject(0);
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
                                            Log.e(TAG, "Branch is " + branch_pricelist.toString());
                                            priceList.setBranch(branch_pricelist);
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "PRICE_LIST API don't have 'branch_id' field");
                                }

                                if (jsonObject.has("customers")) {
                                    JSONArray customerJsonArray = jsonObject.getJSONArray("customers");
                                    for (int i = 0; i < customerJsonArray.length(); i++) {
                                        JSONObject customerJsonObject = customerJsonArray.getJSONObject(i);
                                        if (customerJsonObject.has("id")) {
                                            int customer_id = !customerJsonObject.getString("id").equals("") ? customerJsonObject.getInt("id") : 0;
                                            if (customer_id != 0) {
                                                Customer customer = getHelper().fetchObjects(Customer.class).queryBuilder().where().eq("id", customer_id).queryForFirst();

                                                if (customer == null) {
                                                    Log.e(TAG, "can't find customer");
                                                } else {
                                                    Log.e(TAG, "Customer Name: " + customer.getName() + priceList.getId());
                                                    customer.setPriceList(priceList);
                                                    updateCustomerPriceList.add(customer);
                                                    ;
                                                }
                                            }
                                        } else {
                                            Log.e(TAG, "PRICE_LIST API dont have 'id' field in customers json array");
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "PRICE_LIST API don't have 'customers' field");
                                }

                                if (jsonObject.has("customer_groups")) {
                                    JSONArray customerGroupsJsonArray = jsonObject.getJSONArray("customer_groups");
                                    for (int i = 0; i < customerGroupsJsonArray.length(); i++) {
                                        JSONObject customerGroupJsonObject = customerGroupsJsonArray.getJSONObject(i);
                                        if (customerGroupJsonObject.has("id")) {
                                            int customer_group_id = !customerGroupJsonObject.getString("id").equals("") ? customerGroupJsonObject.getInt("id") : 0;

                                            if (customer_group_id != 0) {
                                                CustomerGroup customerGroup = getHelper().fetchObjects(CustomerGroup.class).queryBuilder().where().eq("id", customer_group_id).queryForFirst();

                                                if (customerGroup == null) {
                                                    Log.e(TAG, "Can't find Customer Group");
                                                } else {
                                                    Log.e(TAG, "Customer Group Name: " + customerGroup.getName() + priceList.getId());
                                                    customerGroup.setPriceList(priceList);

                                                    updateCustomerGroup.add(customerGroup);
                                                }

                                            }
                                        } else {
                                            Log.e(TAG, "PRICE_LIST API don't have 'id' field in customer_groups array");
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "PRICE_LIST API don't have 'customers_group' field ");
                                }

                                if (jsonObject.has("price_list_items")) {
                                    JSONArray priceListJsonArray = jsonObject.getJSONArray("price_list_items");

                                    for (int i = 0; i < priceListJsonArray.length(); i++) {
                                        JSONObject priceListJsonObject = priceListJsonArray.getJSONObject(i);

                                        Price price = gson.fromJson(priceListJsonObject.toString(), Price.class);

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
                                        newPrice.add(price);
                                    }
                                } else {
                                    Log.e(TAG, "PRICE_LIST API don't have 'price_list_items field");
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

                                updateCustomerPriceList.doOperationBT(Customer.class);
                                updateCustomerGroup.doOperationBT(CustomerGroup.class);
                                newPrice.doOperationBT(Price.class);
                            }

                            updateNext(requestType, count);
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

                                    // TODO: add the price list object
                                    String price_list_id = jsonObject.getString("price_list_id");
                                    customerGroup.setPriceList(null);

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
                                                if (jsonObject.getString("status").equals("A")) {
                                                    paymentTerms.updateTo(getHelper());
                                                } else if (jsonObject.getString("status").equals("I")) {
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
                                                if (jsonObject.getString("status").equals("A")) {
                                                    paymentType.updateTo(getHelper());
                                                } else if (jsonObject.getString("status").equals("I")) {
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
                        case SALES_PUSH:
                        case SALES_PROMOTIONS:
                            if (size == 0) {
                                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                                syncNext();
                                return;
                            } else {
                                for (int i = 0; i < size; i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                                    Log.e(TAG, ">>>> " + jsonObject.toString());
                                    SalesPromotion salesPromotion = gson.fromJson(jsonObject.toString(), SalesPromotion.class);

                                    if (jsonObject.has("settings")) {
                                        SalesPushSettings salesPushSettings = gson.fromJson(jsonObject.getString("settings"), SalesPushSettings.class);
                                        salesPromotion.setSettings(salesPushSettings);
                                    }

                                    if (initialSync || lastUpdatedAt == null) {
                                        salesPromotion.insertTo(getHelper());
                                    } else {
                                        if (isExisting(salesPromotion, Table.SALES_PROMOTIONS)) {
                                            if (jsonObject.getString("status").equals("A")) {
                                                salesPromotion.updateTo(getHelper());
                                            } else if (jsonObject.getString("status").equals("I")) {
                                                salesPromotion.deleteTo(getHelper());
                                            }
                                        } else {
                                            salesPromotion.insertTo(getHelper());
                                        }
                                    }
                                }
                            }
                            updateNext(requestType, size);
                            break;
                        case SALES_PROMOTIONS_DISCOUNT:


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

        if (mSyncModulesListener != null) {
            if (size != 0) {
                if (mCurrentTableSyncing == Table.DOCUMENTS) {
                    mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, branchIndex, getUserBranchesSize());
                } else if (mCurrentTableSyncing == Table.PRICE_LISTS ||
                        mCurrentTableSyncing == Table.BRANCH_PRICE_LISTS) {
                    mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, page, size);
                } else {
                    mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, page, numberOfPages);
                }
            }
        }

        try {
            if (mCurrentTableSyncing == Table.PRICE_LISTS ||
                    mCurrentTableSyncing == Table.BRANCH_PRICE_LISTS) {
                if (page >= size) {
                    Log.e(TAG, "Syncing next price list");
                    syncNext();
                } else {
                    Log.e(TAG, "Downloading next page of price list");
                    page++;
                    if (page <= size) {
                        startSyncModuleContents(requestType);
                    }
                }
            } else {

                Log.e(TAG, "Size: " + size);
               if (size <= max_size_per_page) {
                    Log.e(TAG, "Syncing next table");
                   page++;
                   if(page > numberOfPages) {
                       syncNext();
                   } else {
                       startSyncModuleContents(requestType);
                   }
                } else {
                    Log.e(TAG, "Error Page Size Exceeded Max Size Per Page");
                   LoggingTools.showToast(this, "Error Page Size Exceeded Max Size Per Page");
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

        if (mCurrentTableSyncing == Table.USERS) {
            startSyncModuleContents(RequestType.LAST_UPDATED_AT);
        } else if (mCurrentTableSyncing == Table.DAILY_SALES) {
            startSyncModuleContents(RequestType.DAILY_SALES_TODAY);
        } else {
            startSyncModuleContents(RequestType.LAST_UPDATED_AT);
        }
    }
}