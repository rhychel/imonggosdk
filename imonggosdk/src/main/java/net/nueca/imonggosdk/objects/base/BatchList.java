package net.nueca.imonggosdk.objects.base;

import android.util.Log;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.objects.invoice.InvoiceTaxRate;
import net.nueca.imonggosdk.objects.invoice.Payment;
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
            Log.e(TAG, databaseOperation.toString() + "ING to Users tables");
        }
        if(get(0) instanceof Product) {
            dbHelper.batchCreateOrUpdateProducts(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to Product tables");
        }
        if(get(0) instanceof InvoiceLine) {
            dbHelper.batchCreateOrUpdateInvoiceLines(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to Invoiceline Table");
        }
        if(get(0) instanceof InvoiceTaxRate) {
            dbHelper.batchCreateOrUpdateInvoiceTaxRates(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to InvoiceTaxRate Table");
        }
        if(get(0) instanceof Payment) {
            dbHelper.batchCreateOrUpdatePayments(this, databaseOperation);
            Log.e(TAG, databaseOperation.toString() + "ING to Payment Table");
        }
        if(get(0) instanceof OrderLine) {
            Log.e(TAG, databaseOperation.toString() + "ING to OrderLine Table");
            dbHelper.batchCreateOrUpdateOrderLines(this, databaseOperation);
        }
    }
}
