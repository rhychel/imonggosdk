package net.nueca.imonggosdk.objects.base;

import android.util.Log;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchTag;
import net.nueca.imonggosdk.objects.Customer;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.objects.TaxRate;
import net.nueca.imonggosdk.objects.TaxSetting;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;
import net.nueca.imonggosdk.objects.associatives.ProductTaxRateAssoc;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.document.ExtendedAttributes;

import java.util.ArrayList;

/**
 * Created by rhymart on 5/30/15.
 * imonggosdk (c)2015
 */
public class BatchList<T> extends ArrayList<T> {

    private String TAG = "BatchList";
    private DatabaseOperation databaseOperation = DatabaseOperation.INSERT;
    private ImonggoDBHelper dbHelper;

    public BatchList() { }

    public BatchList(ImonggoDBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public BatchList(DatabaseOperation databaseOperation) {
        this.databaseOperation = databaseOperation;
    }

    public BatchList(DatabaseOperation databaseOperation, ImonggoDBHelper dbHelper) {
        this.databaseOperation = databaseOperation;
        this.dbHelper = dbHelper;
    }

    public void doOperation() {
        if(dbHelper == null)
            throw new NullPointerException("Oops! Your dbHelper is null!");
        doOperation(dbHelper);
    }

    public void doOperation(ImonggoDBHelper dbHelper) {
        if(size() == 0) {
            Log.e(TAG, "Ooops! There's nothing to " + databaseOperation.toString());
            return;
        }
        if(get(0) instanceof User) {
            dbHelper.batchCreateOrUpdateUsers(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to Users table");
        }
        if(get(0) instanceof Product) {
            dbHelper.batchCreateOrUpdateProducts(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to Product table");
        }
        if(get(0) instanceof ProductTag) {
            dbHelper.batchCreateOrUpdateProductTags(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to ProductTag table");
        }
        if(get(0) instanceof Unit) {
            dbHelper.batchCreateOrUpdateUnits(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to Unit table");
        }
        if(get(0) instanceof Branch) {
            dbHelper.batchCreateOrUpdateBranches(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to Branch table");
        }
        if(get(0) instanceof BranchTag) {
            dbHelper.batchCreateOrUpdateBranchTags(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to BranchTag table");
        }
        if(get(0) instanceof BranchUserAssoc) {
            dbHelper.batchCreateOrUpdateBranchUsers(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to BranchUser Assoc table");
        }
        if(get(0) instanceof Customer) {
            dbHelper.batchCreateOrUpdateCustomers(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to Customer table");
        }
        if(get(0) instanceof Inventory) {
            dbHelper.batchCreateOrUpdateInventories(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to Inventory table");
        }
        if(get(0) instanceof TaxSetting) {
            dbHelper.batchCreateOrUpdateTaxSettings(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to TaxSetting table");
        }
        if(get(0) instanceof TaxRate) {
            dbHelper.batchCreateOrUpdateTaxRates(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to TaxRate table");
        }
        if(get(0) instanceof ProductTaxRateAssoc) {
            dbHelper.batchCreateOrUpdateProductTaxRates(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to ProductTaxRate Assoc table");
        }


        /** Documents -- gama **/
        if(get(0) instanceof Document) {
            dbHelper.batchCreateOrUpdateDocuments(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to Document table");
        }
        if(get(0) instanceof DocumentLine) {
            dbHelper.batchCreateOrUpdateDocumentLines(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to Document Line table");
        }
        if(get(0) instanceof ExtendedAttributes) {
            dbHelper.batchCreateOrUpdateExtendedAttributes(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to Extended Attributes table");
        }
    }
}
