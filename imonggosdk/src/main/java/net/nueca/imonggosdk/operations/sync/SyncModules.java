package net.nueca.imonggosdk.operations.sync;

import android.os.Handler;
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
import net.nueca.imonggosdk.objects.Customer;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.LastUpdatedAt;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.objects.TaxRate;
import net.nueca.imonggosdk.objects.TaxSetting;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;
import net.nueca.imonggosdk.objects.base.BatchList;
import net.nueca.imonggosdk.operations.ImonggoTools;
import net.nueca.imonggosdk.operations.http.ImonggoOperations;
import net.nueca.imonggosdk.tools.DateTimeTools;
import net.nueca.imonggosdk.tools.LastUpdateAtTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Created by Jn on 7/15/2015.
 * imonggosdk(2015)
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
                    mCurrentTableSyncing, getSession().getServer(), requestType, getParameters(requestType));

        } else if (requestType == RequestType.API_CONTENT) {
            // TODO: Documents
            Log.e(TAG, "API CONTENT");
            ImonggoOperations.getAPIModule(this, getQueue(), getSession(), this,
                    mCurrentTableSyncing, getSession().getServer(), requestType, getParameters(requestType));
        } else if (requestType == RequestType.LAST_UPDATED_AT) {
            Log.e(TAG, "LAST UPDATED AT");
            newLastUpdatedAt = null;
            lastUpdatedAt = null;

            // Get the last updated at
            QueryBuilder<LastUpdatedAt, Integer> queryBuilder = getHelper().getLastUpdatedAts().queryBuilder();
            //TODO: Documents

            queryBuilder.where().eq("tableName", LastUpdateAtTools.getTableToSync(mCurrentTableSyncing));
            // get the last updated at
            lastUpdatedAt = getHelper().getLastUpdatedAts().queryForFirst(queryBuilder.prepare());

            ImonggoOperations.getAPIModule(this, getQueue(), getSession(), this, mCurrentTableSyncing,
                    getSession().getServer(), requestType, getParameters(requestType));
        }
    }

    private String getParameters(RequestType requestType) {
        if (requestType == RequestType.LAST_UPDATED_AT) {
            //TODO: documents
            return ImonggoTools.generateParameter(Parameter.LAST_UPDATED_AT);

        } else if (requestType == RequestType.API_CONTENT) {
            if (mCurrentTableSyncing == Table.TAX_SETTINGS)
                return "";

            if (initialSync || lastUpdatedAt == null) {
                if (mCurrentTableSyncing == Table.BRANCH_USERS)
                    return String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.USER_ID),
                            String.valueOf(page), String.valueOf(getUser().getId()));

                // TODO: Documents
                Log.e(TAG, String.format(ImonggoTools.generateParameter(Parameter.PAGE), String.valueOf(page)));
                return String.format(ImonggoTools.generateParameter(Parameter.PAGE), String.valueOf(page));

            } else {
                if (mCurrentTableSyncing == Table.BRANCH_USERS)
                    return String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.USER_ID, Parameter.AFTER),
                            String.valueOf(page), String.valueOf(getUser().getId()), DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));

                return String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.AFTER),
                        String.valueOf(page), DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
            }
        } else if (requestType == RequestType.COUNT) {
            // TODO 1. Support ACTIVE_ONLY
            if (initialSync || lastUpdatedAt == null) {
                // TODO 2. DOCUMENTS
                if (mCurrentTableSyncing == Table.BRANCH_USERS) {// This is when the module syncing is the User Branches
                    return String.format(ImonggoTools.generateParameter(Parameter.COUNT, Parameter.USER_ID),
                            String.valueOf(getUser().getId()));
                }

                return ImonggoTools.generateParameter(Parameter.COUNT);
            } else {
                // TODO 3. DOCUMENTS
                if (mCurrentTableSyncing == Table.BRANCH_USERS) { // TODO last_updated_at of this should relay on NOW at the end of the request...
                    return String.format(ImonggoTools.generateParameter(Parameter.COUNT, Parameter.USER_ID, Parameter.AFTER),
                            String.valueOf(getUser().getId()), DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
                }



                if (mCurrentTableSyncing == Table.UNITS) {
                    if (lastUpdatedAt.getLast_updated_at() == null)
                        return ImonggoTools.generateParameter(Parameter.COUNT);
                }

                return String.format(ImonggoTools.generateParameter(Parameter.COUNT, Parameter.AFTER),
                        DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));

            }
        }
        return "";
    }

    private boolean syncNext() throws SQLException {


        Log.e(TAG, mModulesIndex + ">= " + mModulesToSync.length);

        if (mModulesIndex == (mModulesToSync.length - 1)) {  // this is when there are no left tables to sync

            User current_user = getUser();

            getSession().setUser(current_user);
            getSession().dbOperation(getHelper(), DatabaseOperation.UPDATE);

            Thread sleepFor3Seconds = new Thread() {
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
            sleepFor3Seconds.start();

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

            return false;
        } else { // if there are still tables to sync, then;
            mModulesIndex++;
            Log.e(TAG, "there are still tables to sync");
            page = 1;
            numberOfPages = 1;
            count = 0;
            mCurrentTableSyncing = mModulesToSync[mModulesIndex];
            if (mCurrentTableSyncing == Table.TAX_SETTINGS) {
                startSyncModuleContents(RequestType.API_CONTENT);
            } else { // otherwise, call the last updated at request {
                startSyncModuleContents(RequestType.LAST_UPDATED_AT);
            }
        }
        return true;
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

                    // get the response TODO: documents?
                    newLastUpdatedAt = gson.fromJson(jsonObject.toString(), LastUpdatedAt.class);
                    newLastUpdatedAt.setTableName(LastUpdateAtTools.getTableToSync(module));

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
                        mSyncModulesListener.onDownloadProgress(module, 1, 1);
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
                            taxSetting.dbOperation(getHelper(), DatabaseOperation.INSERT);
                        } else {
                            if (isExisting(taxSetting, Table.TAX_SETTINGS)) {
                                taxSetting.dbOperation(getHelper(), DatabaseOperation.UPDATE);
                            } else {
                                taxSetting.dbOperation(getHelper(), DatabaseOperation.INSERT);
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
                        BatchList<TaxRate> updateTaxRates = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
                        BatchList<TaxRate> deleteTaxRates = new BatchList<>(DatabaseOperation.DELETE, getHelper());

                        JSONArray taxRateArray = jsonObject.getJSONArray("tax_rates");

                        if (taxRateArray.length() != 0) {
                            mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                            syncNext();
                            return;
                        } else {
                            for (int x = 0; x < taxRateArray.length(); x++) {
                                JSONObject jsonObject2 = taxRateArray.getJSONObject(x);
                                TaxRate taxRate = gson.fromJson(jsonObject2.toString(), TaxRate.class);

                                if (initialSync || lastUpdatedAt == null) {
                                    newTaxRates.add(taxRate);
                                } else {
                                    if (isExisting(taxRate, Table.TAX_RATES)) {
                                        if (taxRate.getStatus() == null) {
                                            updateTaxRates.add(taxRate);
                                        } else {
                                            deleteTaxRates.add(taxRate);
                                        }
                                    } else {
                                        newTaxRates.add(taxRate);
                                    }
                                }
                            }

                            newTaxRates.doOperation();
                            updateTaxRates.doOperation();
                            deleteTaxRates.doOperation();
                        }

                        mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, 1, 1);
                        syncNext();
                    }
                }
                // JSONArray
            } else if (response instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) response;
                int size = jsonArray.length();
                Log.e(TAG, "content size: " + size);

                switch (module) {
                    case USERS:
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
                                    if (initialSync)
                                        Log.e(TAG, "initial sync users");
                                    if (lastUpdatedAt == null)
                                        Log.e(TAG, "last Updated At users ");

                                    newUsers.add(user);
                                } else {
                                    // check if the user tables exist in the database
                                    if (isExisting(user, Table.USERS)) {
                                        if (user.getStatus() == null) {
                                            Log.e(TAG, "adding user entry to be updated");
                                            updateUsers.add(user);

                                            if (user.getId() == getSession().getUser().getId()) {
                                                Log.e(TAG, "Updating sessions user from " + getSession().getUser().getName() + " to " + user.getName());
                                            }
                                        } else {
                                            Log.e(TAG, "adding user entry to be deleted ");
                                            deleteUsers.add(user);
                                        }
                                    } else {  // if not then add it
                                        Log.e(TAG, "adding user entry to be inserted");
                                        newUsers.add(user);
                                    }

                                }
                            }
                            newUsers.doOperation();
                            updateUsers.doOperation();
                            deleteUsers.doOperation();

                            updateNext(requestType, size);
                        }
                        break;
                    case PRODUCTS:
                        Log.e(TAG, "Products | size: " + size + " page: " + page + " max page: " + numberOfPages);
                        Log.e(TAG, "Syncing Page " + page);

                        if (size == 0) {
                            syncNext();
                            return;
                        } else {

                            BatchList<Product> newProducts = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<Product> updateProducts = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
                            BatchList<Product> deleteProducts = new BatchList<>(DatabaseOperation.DELETE, getHelper());
                            BatchList<ProductTag> productTags = new BatchList<>(DatabaseOperation.INSERT, getHelper());

                            for (int i = 0; i < size; i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Product product = gson.fromJson(jsonObject.toString(), Product.class);
                                if (initialSync || lastUpdatedAt == null) {
                                    product.setSearchKey(product.getName() + product.getStock_no());
                                    newProducts.add(product);
                                } else {
                                    Log.e(TAG, "This not initial sync " + product.toString());
                                    if (isExisting(product, Table.PRODUCTS)) {
                                        DeleteBuilder<ProductTag, Integer> deleteProductsHelper = getHelper().getProductTags().deleteBuilder();
                                        deleteProductsHelper.where().eq("product_id", product);
                                        deleteProductsHelper.delete();

                                        if (product.getStatus() == null) {
                                            Log.e(TAG, "adding product entry to be updated");
                                            updateProducts.add(product);
                                        } else {
                                            Log.e(TAG, "adding product entry to be deleted");
                                            deleteProducts.add(product);
                                        }
                                    } else {
                                        Log.e(TAG, "adding product entry to be deleted ");
                                        newProducts.add(product);
                                    }
                                }
                                // Save tags to the database
                                JSONArray tagsListArray = jsonObject.getJSONArray("tag_list");
                                int tagsSize = tagsListArray.length();
                                for (int tagsI = 0; tagsI < tagsSize; tagsI++) {
                                    ProductTag productTag = new ProductTag(tagsListArray.getString(tagsI), product);
                                    productTags.add(productTag);
                                }
                            }

                            newProducts.doOperation();
                            updateProducts.doOperation();
                            deleteProducts.doOperation();
                            productTags.doOperation();

                            updateNext(requestType, size);
                        }
                        break;
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


                                Product product = getHelper().getProducts().queryBuilder().where().eq("id", jsonObject.getString("product_id")).queryForFirst();

                                Log.e(TAG, "Unit Product ID: " + jsonObject.getString("product_id") + " name: " + product.getName());

                                unit.setProduct(product);
                                Log.e(TAG, unit.getName());
                                if (initialSync || lastUpdatedAt == null) {
                                    if (initialSync)
                                        Log.e(TAG, "initial sync units");
                                    if (lastUpdatedAt == null)
                                        Log.e(TAG, "last Updated At units");
                                    newUnits.add(unit);
                                } else {
                                    if (isExisting(unit, Table.UNITS)) {
                                        if (unit.getStatus() == null) {
                                            Log.e(TAG, "adding user entry to be updated");
                                            updateUnits.add(unit);
                                        } else {
                                            Log.e(TAG, "adding user entry to be deleted");
                                            deleteUnits.add(unit);
                                        }
                                    } else {
                                        Log.e(TAG, "adding user entry to be inserted");
                                        newUnits.add(unit);
                                    }
                                }
                            }

                            newUnits.doOperation();
                            updateUnits.doOperation();
                            deleteUnits.doOperation();
                            updateNext(requestType, size);
                        }
                        break;
                    case BRANCH_USERS:

                        Log.e(TAG, "Branch | size: " + size + " page: " + page + " max page: " + numberOfPages);

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
                                BranchUserAssoc branchUserAssoc = new BranchUserAssoc(branch, getUser());

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
                            newBranches.doOperation();
                            updateBranches.doOperation();

                            newBranchUserAssocs.doOperation();
                            updateBranchUserAssocs.doOperation();

                            newBranchTags.doOperation();
                            updateBranchTags.doOperation();

                            updateNext(requestType, size);
                        }
                        break;
                    case CUSTOMERS:
                        Log.e(TAG, "Branch | size: " + size + " page: " + page + " max page: " + numberOfPages);
                        if (size == 0) {
                            syncNext();
                            return;
                        } else {

                            BatchList<Customer> newCustomer = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<Customer> updateCustomer = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
                            BatchList<Customer> deleteCustomer = new BatchList<>(DatabaseOperation.DELETE, getHelper());

                            for (int i = 0; i < size; i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Customer customer = gson.fromJson(jsonObject.toString(), Customer.class);

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

                            newCustomer.doOperation();
                            updateCustomer.doOperation();
                            deleteCustomer.doOperation();
                            updateNext(requestType, size);
                        }
                        break;
                    case INVENTORIES:
                        Log.e(TAG, "Inventories | size: " + size + " page: " + page + " max page: " + numberOfPages);
                        if (size == 0) {
                            syncNext();
                            return;
                        } else {
                            BatchList<Inventory> newInventories = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                            BatchList<Inventory> updateInventories = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                            for (int i = 0; i < size; i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Inventory inventory = gson.fromJson(jsonObject.toString(), Inventory.class);

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

                            newInventories.doOperation();
                            updateInventories.doOperation();
                            updateNext(requestType, size);
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateNext(RequestType requestType, int size) {
        Log.e(TAG, requestType + "next table");

        if (mSyncModulesListener != null) {
            if (size != 0) {
                Log.e(TAG, "Size is not 0");
                mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, page, numberOfPages);
            }
        }
        try {

            if (size < 50) {
                Log.e(TAG, "Syncing next table");
                syncNext();
            } else {
                Log.e(TAG, "Downloading next page");
                page++;
                if (page <= numberOfPages) {
                    startSyncModuleContents(requestType);
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
        startSyncModuleContents(RequestType.LAST_UPDATED_AT);

    }
}