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
            if (initialSync || lastUpdatedAt == null) {

                if (mCurrentTableSyncing == Table.BRANCH_USERS) {
                    return String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.USER_ID),
                            String.valueOf(page), String.valueOf(getUser().getId()));
                }
                Log.e(TAG, String.format(ImonggoTools.generateParameter(Parameter.PAGE), String.valueOf(page)));
                return String.format(ImonggoTools.generateParameter(Parameter.PAGE), String.valueOf(page));
            } else {
                return String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.AFTER),
                        String.valueOf(page), DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at()));
            }
        } else if (requestType == RequestType.COUNT) {
            if (initialSync || lastUpdatedAt == null) {

                return ImonggoTools.generateParameter(Parameter.COUNT);
            }
        }
        return "";
    }

    private boolean syncNext() throws SQLException {
        if (mCurrentTableSyncing == Table.USERS) { //check if its a User, then resume downloading the user branches on count request and so on...
            if (mModulesToSync[mModulesIndex+1] == Table.BRANCH_USERS) {
                mCurrentTableSyncing = Table.BRANCH_USERS;

                Log.e(TAG, "preparing to sync " + mCurrentTableSyncing);
                page = 1;
                numberOfPages = 1;
                count = 0;
                startSyncModuleContents(RequestType.COUNT);
                mModulesIndex++;
                return true;
            }
        }

        mModulesIndex++;

        if (mModulesIndex >= mModulesToSync.length) {  // this is when there are no left tables to sync
            Log.e(TAG, "finished downloading tables");
            if (mSyncModulesListener != null) {
                Log.e(TAG, "finished downloading tables");
                // When the request is successful
                mSyncModulesListener.onEndDownload(mCurrentTableSyncing);
                mSyncModulesListener.onFinishDownload();
            }
            return false;
        } else { // if there are still tables to sync, then;
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
        Log.e(TAG, "onStart downloading " + module.toString());
        if (mSyncModulesListener != null) {
            mSyncModulesListener.onStartDownload(module);
        }
    }

    @Override
    public void onSuccess(Table module, RequestType requestType, Object response) {
        Log.e(TAG, "succesfully downloaded " + module.toString());

        try {
            // JSONObject
            if (response instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) response;
                Log.e(TAG, "content:" + jsonObject.toString());

                // Last Updated At
                if (requestType == RequestType.LAST_UPDATED_AT) {
                    newLastUpdatedAt = gson.fromJson(jsonObject.toString(), LastUpdatedAt.class);

                    //TODO: documents?
                    newLastUpdatedAt.setTableName(LastUpdateAtTools.getTableToSync(module));

                    // since this is the first
                    page = 1;
                    count = 0;

                    // USERS and  TAX SETTINGS DON'T SUPPORT COUNT
                    if (mCurrentTableSyncing == Table.USERS || mCurrentTableSyncing == Table.TAX_SETTINGS) {
                        startSyncModuleContents(RequestType.API_CONTENT);
                    } else {
                        startSyncModuleContents(RequestType.COUNT);
                        Log.e(TAG, "COUNT");
                    }

                    // COUNT
                } else if (requestType == RequestType.COUNT) {
                    count = jsonObject.getInt("count");

                    // if table don't have data
                    if (count == 0) {
                        if (!syncNext()) {
                            return;
                        }
                    }
                    numberOfPages = ((int) Math.ceil(count / 50.0));
                    Log.e(TAG, "number of pages: " + numberOfPages);
                    startSyncModuleContents(RequestType.API_CONTENT);
                    // API CONTENT
                } else if (requestType == RequestType.API_CONTENT) {

                }
                // JSONArray
            } else if (response instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) response;
                int size = jsonArray.length();
                Log.e(TAG, "content size: " + size);

                switch (module) {
                    case USERS:
                        if (size == 0) {
                            syncNext();
                            return;
                        }

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
                                    // check is status is D
                                    if (user.getStatus().equals("D")) {
                                        deleteUsers.add(user);
                                    } else {
                                        updateUsers.add(user);
                                    }
                                } else {  // if not then add it
                                    newUsers.add(user);
                                }

                            }
                        }
                        newUsers.doOperation();
                        updateUsers.doOperation();
                        deleteUsers.doOperation();

                        if (mSyncModulesListener != null) {
                            mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, page, numberOfPages);
                        }

                        break;
                    case BRANCH_USERS:

                        if (page == 1) {
                            getHelper().dbOperations(null, Table.BRANCHES, DatabaseOperation.DELETE_ALL);
                            getHelper().dbOperations(null, Table.BRANCH_TAGS, DatabaseOperation.DELETE_ALL);
                            getHelper().dbOperations(null, Table.BRANCH_USERS, DatabaseOperation.DELETE_ALL);
                        }

                        BatchList<Branch> newBranches = new BatchList<>(getHelper());
                        BatchList<BranchTag> newBranchTags = new BatchList<>(getHelper());
                        BatchList<BranchUserAssoc> newBranchUserAssocs = new BatchList<>(getHelper());

                        for (int i = 0; i < size; i++) {
                            //get the object in the array
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Branch branch = gson.fromJson(jsonObject.toString(), Branch.class);
                            // TODO: finish this
                        }

                        if (mSyncModulesListener != null) {
                            mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, page, numberOfPages);
                        }

                        break;
                    case PRODUCTS:
                        Log.e(TAG, "Products | size: " + size + " page: " + page + " max page: " + numberOfPages);
                        Log.e(TAG, "Syncing Page " + page);

                        if (size == 0) {
                            syncNext();
                            return;
                        }

                        BatchList<Product> newProducts = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                        BatchList<Product> updateProducts = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
                        BatchList<Product> deleteProducts = new BatchList<>(DatabaseOperation.DELETE, getHelper());
                        BatchList<ProductTag> productTags = new BatchList<>(getHelper());

                        for (int i = 0; i < size; i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Product product = gson.fromJson(jsonObject.toString(), Product.class);
                            if (initialSync || lastUpdatedAt == null) {
                                product.setSearchKey(product.getName()+product.getStock_no());
                                newProducts.add(product);
                            } else if (isExisting(product, Table.PRODUCTS)) {
                                DeleteBuilder<ProductTag, Integer> deleteProductsHelper = getHelper().getProductTags().deleteBuilder();
                                deleteProductsHelper.where().eq("product_id", product);
                                deleteProductsHelper.delete();

                                if (product.getStatus().equals("D")) {
                                    deleteProducts.add(product);
                                } else {
                                    updateProducts.add(product);
                                }

                            } else {
                                newProducts.add(product);
                            }
                            // Save tags to the database
                            JSONArray tagsListArray = jsonObject.getJSONArray("tag_list");
                            int tagsSize = tagsListArray.length();
                            for(int tagsI = 0;tagsI < tagsSize;tagsI++) {
                                ProductTag productTag = new ProductTag(tagsListArray.getString(tagsI), product);
                                productTags.add(productTag);
                            }
                        }

                        newProducts.doOperation();
                        updateProducts.doOperation();
                        deleteProducts.doOperation();
                        productTags.doOperation();

                        if (mSyncModulesListener != null) {
                            mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, page, numberOfPages);
                        }

                        break;
                    case UNITS:
                        if (size == 0) {
                            syncNext();
                            return;
                        }

                        BatchList<Unit> newUnits = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                        BatchList<Unit> updateUnits = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
                        BatchList<Unit> deleteUnits = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                        for(int i=0; i<size; i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Unit unit = gson.fromJson(jsonObject.toString(),Unit.class);
                            if(initialSync || lastUpdatedAt == null) {
                                newUnits.add(unit);
                            } else if(isExisting(unit, Table.UNITS)){
                                if(unit.getStatus().equals("D")) {
                                    deleteUnits.add(unit);
                                } else {
                                    updateUnits.add(unit);
                                }
                            }
                        }

                        newUnits.doOperation();
                        updateUnits.doOperation();
                        deleteUnits.doOperation();

                        if (mSyncModulesListener != null) {
                            mSyncModulesListener.onDownloadProgress(mCurrentTableSyncing, page, numberOfPages);
                        }

                        break;
                    default:
                        break;
                }

                if (size < 50) {
                    Log.e(TAG, "Sync Next Table");
                    syncNext();
                } else {
                    page++;
                    if (page <= numberOfPages) {
                        startSyncModuleContents(RequestType.API_CONTENT);
                    }
                }

            }
        } catch (SQLException | JSONException e) {
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

    public void startFetchingModules() {
        Log.e(TAG, "LAST UPDATED AT");
        ImonggoOperations.getAPIModule(this, getQueue(), getSession(), this, mCurrentTableSyncing, getSession().getServer(), RequestType.LAST_UPDATED_AT, getParameters(RequestType.LAST_UPDATED_AT));
    }
}
