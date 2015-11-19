package net.nueca.imonggosdk.operations.sync;

import android.util.Log;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Parameter;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchTag;
import net.nueca.imonggosdk.objects.LastUpdatedAt;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.objects.TaxRate;
import net.nueca.imonggosdk.objects.TaxSetting;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;
import net.nueca.imonggosdk.objects.associatives.ProductTaxRateAssoc;
import net.nueca.imonggosdk.objects.base.BatchList;
import net.nueca.imonggosdk.operations.ImonggoTools;
import net.nueca.imonggosdk.operations.http.ImonggoOperations;
import net.nueca.imonggosdk.tools.Configurations;
import net.nueca.imonggosdk.tools.DateTimeTools;
import net.nueca.imonggosdk.tools.LastUpdateAtTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/14/15.
 * imonggosdk (c)2015
 */
@Deprecated
public class SyncImonggoModules extends BaseSyncModulesService implements VolleyRequestListener {
    private static final String TAG = "SyncImonggoModules";

    private void startSyncContents(RequestType requestType) throws SQLException {


        if(getHelper() == null)
            Log.e(TAG, "helper is null");
        if(requestType == RequestType.LAST_UPDATED_AT) {
            newLastUpdatedAt = null;
            lastUpdatedAt = null;

            // Get the last updated at
            QueryBuilder<LastUpdatedAt, Integer> queryBuilder = getHelper().fetchIntId(LastUpdatedAt.class).queryBuilder();
            if(tableSyncing == Table.DOCUMENTS)
                queryBuilder.where().eq("tableName", LastUpdateAtTools.getTableToSync(tableSyncing, String.valueOf(branches[branchIndex])));
            else
                queryBuilder.where().eq("tableName", LastUpdateAtTools.getTableToSync(tableSyncing));

            lastUpdatedAt = getHelper().fetchIntId(LastUpdatedAt.class).queryForFirst(queryBuilder.prepare());

            ImonggoOperations.getAPIModule(this, getQueue(), getSession(), this,
                    tableSyncing, server, requestType, getParameters(requestType));
        }
        else if(requestType == RequestType.COUNT) {
            count = 0;
            page = 1;
            ImonggoOperations.getAPIModule(this, getQueue(), getSession(), this,
                    tableSyncing, server, requestType, getParameters(requestType));
        }
        else if(requestType == RequestType.API_CONTENT) {
            if(tableSyncing == Table.DOCUMENTS)
                if(branchIndex == 0)
                    initializeFromTo();

            ImonggoOperations.getAPIModule(this, getQueue(), getSession(), this,
                    tableSyncing, server, requestType);

            if (syncModulesListener != null)
                syncModulesListener.onDownloadProgress(tableSyncing, page, 1);
        }
    }

    public String getParameters(RequestType requestType) {
        if(requestType == RequestType.LAST_UPDATED_AT) {
            /**
             * Possible parameters for LAST_UPDATED_AT
             */
            if(tableSyncing == Table.DOCUMENTS) // This is when the module syncing is DOCUMENTS
                return String.format(ImonggoTools.generateParameter(Parameter.LAST_UPDATED_AT, Parameter.BRANCH_ID),
                        String.valueOf(branches[branchIndex]));
            return ImonggoTools.generateParameter(Parameter.LAST_UPDATED_AT);
        }
        else if(requestType == RequestType.COUNT) {
            /*
                TODO 1. Support ACTIVE_ONLY
             */
            if(initialSync || lastUpdatedAt == null) { // check whether the flag is initialSync OR lastUpdatedAt queried is null, then;
                if (tableSyncing == Table.BRANCH_USERS) // This is when the module syncing is the User Branches
                    return String.format(ImonggoTools.generateParameter(Parameter.COUNT, Parameter.USER_ID),
                            String.valueOf(getUser().getId()));
                if(tableSyncing == Table.DOCUMENTS)
                    return String.format(ImonggoTools.generateParameter(Parameter.COUNT, Parameter.BRANCH_ID),
                            String.valueOf(getUser().getId()), String.valueOf(branches[branchIndex]));
                return ImonggoTools.generateParameter(Parameter.COUNT);
            } else {
                if(tableSyncing == Table.BRANCH_USERS) // TODO last_updated_at of this should relay on NOW at the end of the request...
                    return String.format(ImonggoTools.generateParameter(Parameter.COUNT, Parameter.USER_ID, Parameter.AFTER),
                            String.valueOf(getUser().getId()), DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                if(tableSyncing == Table.DOCUMENTS)
                    return String.format(ImonggoTools.generateParameter(Parameter.COUNT, Parameter.BRANCH_ID, Parameter.AFTER),
                            String.valueOf(getUser().getId()), String.valueOf(branches[branchIndex]),
                            DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                return String.format(ImonggoTools.generateParameter(Parameter.COUNT, Parameter.AFTER),
                        DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
            }
        }
        else if(requestType == RequestType.API_CONTENT) {
            /*
                TODO 1. Support ACTIVE_ONLY
             */
            if(tableSyncing == Table.TAX_SETTINGS)
                return "";
            if(initialSync || lastUpdatedAt == null) {
                if(tableSyncing == Table.BRANCH_USERS)
                    return String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.USER_ID),
                            String.valueOf(page), String.valueOf(getUser().getId()));
                if(tableSyncing == Table.DOCUMENTS) // Get from Past 3 months til today
                    return String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.BRANCH_ID, Parameter.FROM, Parameter.TO),
                            String.valueOf(page), String.valueOf(branches[branchIndex]),
                            DateTimeTools.convertDateForUrl(from), DateTimeTools.convertDateForUrl(to));
                return String.format(ImonggoTools.generateParameter(Parameter.PAGE), String.valueOf(page));
            }
            else {
                if(tableSyncing == Table.BRANCH_USERS)
                    return String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.USER_ID, Parameter.AFTER),
                            String.valueOf(page), String.valueOf(getUser().getId()), DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                if(tableSyncing == Table.DOCUMENTS)
                    return String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.BRANCH_ID, Parameter.AFTER),
                            String.valueOf(page), String.valueOf(branches[branchIndex]), DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                return String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.AFTER),
                        String.valueOf(page), DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
            }
        }
        return "";
    }

    private boolean syncNext() throws SQLException {
        if(tableSyncing == Table.USERS) { //check if its a User, then resume downloading the user branches on count request and so on...
            tableSyncing = Table.BRANCH_USERS;
            page = 1;
            numberOfPages = 1;
            count = 0;
            startSyncContents(RequestType.COUNT);
        }
        tablesIndex++;

        if(tablesIndex == tablesToSync.length) { // this is when there are no left tables to sync
            if (syncModulesListener != null) {
                // When the request is successful
                syncModulesListener.onEndDownload(tableSyncing);
                syncModulesListener.onFinishDownload();
            }
            return false;
        }
        else { // if there are still tables to sync, then;
            page = 1;
            numberOfPages = 1;
            count = 0;
            tableSyncing = tablesToSync[tablesIndex];
            if(tableSyncing == Table.TAX_SETTINGS)
                startSyncContents(RequestType.API_CONTENT);
            else // otherwise, call the last updated at request
                startSyncContents(RequestType.LAST_UPDATED_AT);
        }
        return true;
    }

    @Override
    public void startSync() {
        //
    }

    @Override
    public void onStart(Table modules, RequestType requestType) {
        Log.e(TAG, "onStart is called = "+ Configurations.API_MODULES.get(modules));
        if(syncModulesListener != null)
            syncModulesListener.onStartDownload(modules);
    }

    @Override
    public void onError(Table modules, boolean hasInternet, Object response, int responseCode) {

    }

    @Override
    public void onRequestError() {

    }

    @Override
    public void onSuccess(Table modules, RequestType requestType, Object response) {
        Log.e(TAG, "onSuccess is called = "+ Configurations.API_MODULES.get(modules));
        try {
            if (response instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) response;
                Log.e(TAG, jsonObject.toString());
                /**
                 * Application Settings //==> During login
                 * Users -- LAST_UPDATED_AT
                 * User Branches -- COUNT
                 * Products -- LAST_UPDATED_AT, COUNT
                 * Customers -- LAST_UPDATED_AT, COUNT
                 * Inventory -- LAST_UPDATED_AT, COUNT
                 * Tax Settings -- LAST_UPDATED_AT <--- NON SENSE
                 *
                 *
                 *

                 * Documents -- LAST_UPDATED_AT, COUNT
                 * Document Types -- #CONSTANT
                 * Document Purposes -- LAST_UPDATED_AT, COUNT
                 * Sales Promotion
                 */
                if (requestType == RequestType.LAST_UPDATED_AT) {
                    newLastUpdatedAt = gson.fromJson(jsonObject.toString(), LastUpdatedAt.class);
                    if (modules == Table.DOCUMENTS)
                        newLastUpdatedAt.setTableName(LastUpdateAtTools.getTableToSync(modules, String.valueOf(branches[branchIndex])));
                    else
                        newLastUpdatedAt.setTableName(LastUpdateAtTools.getTableToSync(modules));

                    page = 1;
                    count = 0;
                    if(tableSyncing == Table.USERS || tableSyncing == Table.TAX_SETTINGS)
                        startSyncContents(RequestType.API_CONTENT);
                    else
                        startSyncContents(RequestType.COUNT);
                } else if (requestType == RequestType.COUNT) {
                    count = jsonObject.getInt("count");

                    if(count == 0)
                        if(!syncNext())
                            return;

                    numberOfPages = ((int)Math.ceil(count/50.0));
                    startSyncContents(RequestType.API_CONTENT);
                } else if(requestType == RequestType.API_CONTENT) {
                    if(modules == Table.TAX_SETTINGS) {
                        /**
                         * Delete all data related to taxes
                         */
                        getHelper().deleteAll(ProductTaxRateAssoc.class);
                        getHelper().deleteAll(TaxSetting.class);
                        getHelper().deleteAll(TaxRate.class);

                        TaxSetting taxSetting = gson.fromJson(jsonObject.toString(), TaxSetting.class);
                        taxSetting.insertTo(getHelper());
                        if(!jsonObject.has("tax_rates")) { //check if there is even a tax_rates field
                            syncNext();
                            return;
                        }

                        JSONArray jsonArray = jsonObject.getJSONArray("tax_rates");

                        int size = jsonArray.length();
                        if(size == 0) { // Check if there are tax_rates
                            syncNext();
                            return;
                        }

                        BatchList<TaxRate> newTaxRates = new BatchList<>(getHelper());
                        for(int i = 0;i < size;i++) {
                            JSONObject taxRatejson = jsonArray.getJSONObject(i);
                            TaxRate taxRate = gson.fromJson(taxRatejson.toString(), TaxRate.class);
                            String branchId = taxRatejson.getString("branch_id");
                            if(!branchId.equals("null")) {
                                Branch branch = getHelper().fetchObjects(Branch.class).queryBuilder()
                                        .where().eq("id", Integer.valueOf(branchId)).queryForFirst(); // Check if the branch is assigned to the USER
                                if(branch != null)
                                    taxRate.setBranch(branch);
                                else // otherwise, do not add the tax rate
                                    continue;
                            }
                            newTaxRates.add(taxRate);
                        }
                        newTaxRates.doOperation(TaxRate.class);

                        syncNext();
                    }
                }
            }
            else if(response instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) response;
                int size = jsonArray.length();
                switch (modules) {
                    /**
                     * These are the tables without the COUNT call.
                     */
                    case USERS: {
                        if(size == 0) {
                            syncNext();
                            return;
                        }

                        BatchList<User> newUsers = new BatchList<>(DatabaseOperation.INSERT, getHelper()); // container for the new users
                        BatchList<User> updateUsers = new BatchList<>(DatabaseOperation.UPDATE, getHelper()); // container for the updated users
                        BatchList<User> deleteUsers = new BatchList<>(DatabaseOperation.DELETE, getHelper()); // container for the deleted users

                        for(int i = 0;i < size;i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            User user = gson.fromJson(jsonObject.toString(), User.class);
                            if(initialSync || lastUpdatedAt == null) {
                                newUsers.add(user); // Essentially, add the new users
                            }
                            else {
                                if(isExisting(user, modules)) {
                                    if (user.getStatus().equals("D")) // When the status is 'D', add to the deleted users
                                        deleteUsers.add(user);
                                    else // otherwise, add as updated
                                        updateUsers.add(user);
                                }
                                else
                                    newUsers.add(user);
                            }
                        }
                        newUsers.doOperationBT(User.class);
                        updateUsers.doOperationBT(User.class);
                        deleteUsers.doOperationBT(User.class);
                    } break;

                    /**
                     * Process the user branches
                     */
                    case BRANCH_USERS: {
                        if(page == 1) {
                            getHelper().deleteAll(BranchUserAssoc.class);
                            getHelper().deleteAll(BranchTag.class);
                            getHelper().deleteAll(Branch.class);
                        }

                        if(size == 0) {
                            syncNext();
                            return;
                        }

                        BatchList<Branch> newBranches = new BatchList<>(getHelper());
                        BatchList<BranchTag> newBranchTags = new BatchList<>(getHelper());
                        BatchList<BranchUserAssoc> newBranchUserAssocs = new BatchList<>(getHelper());

                        for(int i = 0;i < size;i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Branch branch = gson.fromJson(jsonObject.toString(), Branch.class);
                            if(jsonObject.has("tag_list")) {
                                JSONArray tagsListArray = jsonObject.getJSONArray("tag_list");
                                int tagsSize = tagsListArray.length();

                                for(int tagsI = 0;tagsI < tagsSize;tagsI++) {
                                    BranchTag branchTag = new BranchTag(tagsListArray.getString(tagsI), branch);
                                    newBranchTags.add(branchTag);
                                }
                            }
                            newBranches.add(branch);
                            BranchUserAssoc branchUserAssoc = new BranchUserAssoc(branch, getUser());
                            newBranchUserAssocs.add(branchUserAssoc);
                        }

                        newBranches.doOperationBT(Branch.class);
                        newBranchUserAssocs.doOperation(BranchUserAssoc.class);
                        newBranchTags.doOperation(BranchTag.class);
                    } break;
                    /**
                     * Process the products
                     */
                    case PRODUCTS: {
                        if(size == 0) {
                            syncNext();
                            return;
                        }
                        BatchList<Product> newProducts = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                        BatchList<Product> updateProducts = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                        BatchList<Product> deleteProducts = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                        BatchList<ProductTag> productTags = new BatchList<>(getHelper());

                        for(int i = 0;i < size;i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Product product = gson.fromJson(jsonObject.toString(), Product.class);
                            if(initialSync || lastUpdatedAt == null) {
                                newProducts.add(product);
                            }
                            else {
                                if (isExisting(product, Table.PRODUCTS)) {
                                    DeleteBuilder<ProductTag, Integer> deleteProductsHelper = getHelper().fetchIntId(ProductTag.class).deleteBuilder();
                                    deleteProductsHelper.where().eq("product_id", product);
                                    deleteProductsHelper.delete();
                                    if (product.getStatus().equals("D")) {
                                        deleteProducts.add(product);
                                        continue;
                                    }
                                    else
                                        updateProducts.add(product);
                                }
                                else
                                    newProducts.add(product);
                            }

                            // Save tags to the database
                            JSONArray tagsListArray = jsonObject.getJSONArray("tag_list");
                            int tagsSize = tagsListArray.length();
                            for(int tagsI = 0;tagsI < tagsSize;tagsI++) {
                                ProductTag productTag = new ProductTag(tagsListArray.getString(tagsI), product);
                                productTags.add(productTag);
                            }

                            // Save the taxes to the database
                        }

                        newProducts.doOperationBT(Product.class);
                        updateProducts.doOperationBT(Product.class);
                        deleteProducts.doOperationBT(Product.class);
                        productTags.doOperation(ProductTag.class); // Other product tags should be deleted!
                    } break;
                    case INVENTORIES: {

                    } break;
                }
                if(size < 50)
                    syncNext();
                else {
                    page++;
                    startSyncContents(RequestType.API_CONTENT);
                }
            }
        } catch (JSONException | SQLException e) {
            e.printStackTrace();
        }
    }

}