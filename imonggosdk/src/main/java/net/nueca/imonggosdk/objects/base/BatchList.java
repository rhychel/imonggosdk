package net.nueca.imonggosdk.objects.base;

import android.util.Log;

import com.google.gson.reflect.TypeToken;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchTag;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.base.Extras;
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
import net.nueca.imonggosdk.objects.deprecated.DocumentLineExtras;
import net.nueca.imonggosdk.objects.deprecated.ExtendedAttributes;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;
import net.nueca.imonggosdk.objects.invoice.InvoiceTaxRate;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.objects.order.OrderLine;

import java.util.ArrayList;

/**
 * Created by rhymart on 5/30/15.
 * imonggosdk (c)2015
 */
public class BatchList<T> extends ArrayList<T> {

    private String TAG = "BatchList";
    private DatabaseOperation databaseOperation = DatabaseOperation.INSERT;
    private ImonggoDBHelper dbHelper;
    private ImonggoDBHelper2 dbHelper2;

    public BatchList() { }

    public BatchList(ImonggoDBHelper2 dbHelper) {
        this.dbHelper2 = dbHelper;
    }

    @Deprecated
    public BatchList(ImonggoDBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public BatchList(DatabaseOperation databaseOperation) {
        this.databaseOperation = databaseOperation;
    }

    @Deprecated
    public BatchList(DatabaseOperation databaseOperation, ImonggoDBHelper dbHelper) {
        this.databaseOperation = databaseOperation;
        this.dbHelper = dbHelper;
    }

    public BatchList(DatabaseOperation databaseOperation, ImonggoDBHelper2 dbHelper) {
        this.databaseOperation = databaseOperation;
        this.dbHelper2 = dbHelper;
    }

    public ImonggoDBHelper getDbHelper() {
        return dbHelper;
    }

    public void setDbHelper(ImonggoDBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public ImonggoDBHelper2 getDbHelper2() {
        return dbHelper2;
    }

    public void setDbHelper2(ImonggoDBHelper2 dbHelper2) {
        this.dbHelper2 = dbHelper2;
    }

    public <T extends BaseTable> void doOperationBT(Class<T> objClass) {
        if(dbHelper2 == null)
            throw new NullPointerException("Oops! Your dbHelper2 is null!");
        doOperationBT(objClass, dbHelper2);
    }

    public <T extends BaseTable> void doOperationBT(Class<T> objClass, ImonggoDBHelper2 dbHelper) {
        if(size() == 0) {
            Log.e(TAG, "Ooops! There's nothing to " + databaseOperation.toString());
            return;
        }
        dbHelper.batchCreateOrUpdateBT(objClass, this, databaseOperation);
    }

    public <T extends BaseTable2> void doOperationBT2(Class<T> objClass) {
        if(dbHelper2 == null)
            throw new NullPointerException("Oops! Your dbHelper2 is null!");
        doOperationBT2(objClass, dbHelper2);
    }

    public <T extends BaseTable2> void doOperationBT2(Class<T> objClass, ImonggoDBHelper2 dbHelper) {
        if(size() == 0) {
            Log.e(TAG, "Ooops! There's nothing to " + databaseOperation.toString());
            return;
        }
        dbHelper.batchCreateOrUpdateBT2(objClass, this, databaseOperation);
    }

    public <T extends BaseTable3> void doOperationBT3(Class<T> objClass) {
        if(dbHelper2 == null)
            throw new NullPointerException("Oops! Your dbHelper2 is null!");
        doOperationBT3(objClass, dbHelper2);
    }

    public <T extends BaseTable3> void doOperationBT3(Class<T> objClass, ImonggoDBHelper2 dbHelper) {
        if(size() == 0) {
            Log.e(TAG, "Ooops! There's nothing to " + databaseOperation.toString());
            return;
        }
        dbHelper.batchCreateOrUpdateBT3(objClass, this, databaseOperation);
    }

    public <T extends DBTable> void doOperation(Class<T> objClass) {
        if(dbHelper2 == null)
            throw new NullPointerException("Oops! Your dbHelper2 is null!");
        doOperation(objClass, dbHelper2);
    }

    public <T extends DBTable> void doOperation(Class<T> objClass, ImonggoDBHelper2 dbHelper) {
        if(size() == 0) {
            Log.e(TAG, "Ooops! There's nothing to " + databaseOperation.toString());
            return;
        }
        dbHelper.batchCreateOrUpdate(objClass, this, databaseOperation);
    }

//    @Deprecated
//    public void doOperation() {
//        if(dbHelper == null)
//            throw new NullPointerException("Oops! Your dbHelper is null!");
//        doOperation(dbHelper);
//    }
//
//    @Deprecated
//    public void doOperation(ImonggoDBHelper dbHelper) {
//        if(size() == 0) {
//            Log.e(TAG, "Ooops! There's nothing to " + databaseOperation.toString());
//            return;
//        }
//        if(get(0) instanceof User) {
//            dbHelper.batchCreateOrUpdateUsers(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to Users table");
//        }
//        if(get(0) instanceof Product) {
//            dbHelper.batchCreateOrUpdateProducts(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to Product table");
//        }
//        if(get(0) instanceof ProductTag) {
//            dbHelper.batchCreateOrUpdateProductTags(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to ProductTag table");
//        }
//        if(get(0) instanceof Extras) {
//            dbHelper.batchCreateOrUpdateProductExtras(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to ProductExtras table");
//        }
//        if(get(0) instanceof Unit) {
//            dbHelper.batchCreateOrUpdateUnits(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to Unit table");
//        }
//        if(get(0) instanceof Branch) {
//            dbHelper.batchCreateOrUpdateBranches(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to Branch table");
//        }
//        if(get(0) instanceof BranchTag) {
//            dbHelper.batchCreateOrUpdateBranchTags(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to BranchTag table");
//        }
//        if(get(0) instanceof BranchUserAssoc) {
//            dbHelper.batchCreateOrUpdateBranchUsers(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to BranchUser Assoc table");
//        }
//        if(get(0) instanceof Customer) {
//            dbHelper.batchCreateOrUpdateCustomers(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to Customer table");
//        }
//        if(get(0) instanceof Inventory) {
//            dbHelper.batchCreateOrUpdateInventories(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to Inventory table");
//        }
//        if(get(0) instanceof TaxSetting) {
//            dbHelper.batchCreateOrUpdateTaxSettings(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to TaxSetting table");
//        }
//        if(get(0) instanceof TaxRate) {
//            dbHelper.batchCreateOrUpdateTaxRates(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to TaxRate table");
//        }
//        if(get(0) instanceof ProductTaxRateAssoc) {
//            dbHelper.batchCreateOrUpdateProductTaxRates(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to ProductTaxRate Assoc table");
//        }
//
//
//        /** Documents -- gama **/
//        if(get(0) instanceof Document) {
//            dbHelper.batchCreateOrUpdateDocuments(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to Document table");
//        }
//        if(get(0) instanceof DocumentLine) {
//            dbHelper.batchCreateOrUpdateDocumentLines(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to Document Line table");
//        }
//        if(get(0) instanceof DocumentLineExtras) {
//            dbHelper.batchCreateOrUpdateDocumentLineExtras(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to Document Line Extras table");
//        }
//        /*if(get(0) instanceof DocumentLineExtras_Old) {
//            dbHelper.batchCreateOrUpdateDocumentLineExtras(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to Document Line Extras table");
//        }*/
//        /** Orders -- gama **/
//        if(get(0) instanceof Order) {
//            dbHelper.batchCreateOrUpdateOrders(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to Order table");
//        }
//        if(get(0) instanceof OrderLine) {
//            dbHelper.batchCreateOrUpdateOrderLines(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to Order Line table");
//        }
//        /** Invoices -- gama **/
//        if(get(0) instanceof Invoice) {
//            dbHelper.batchCreateOrUpdateInvoices(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to Order table");
//        }
//        if(get(0) instanceof InvoiceLine) {
//            dbHelper.batchCreateOrUpdateInvoiceLines(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to Order table");
//        }
//        if(get(0) instanceof InvoicePayment) {
//            dbHelper.batchCreateOrUpdateInvoicePayments(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to Order table");
//        }
//        if(get(0) instanceof InvoiceTaxRate) {
//            dbHelper.batchCreateOrUpdateInvoiceTaxRates(this, databaseOperation);
//            Log.e(TAG, databaseOperation.toString() + "ING to Order table");
//        }
//
//    }
}
