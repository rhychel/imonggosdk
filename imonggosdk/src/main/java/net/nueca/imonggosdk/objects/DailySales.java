package net.nueca.imonggosdk.objects;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;

import java.sql.SQLException;

/**
 * Created by jN on 9/2/2015.
 */
@DatabaseTable
public class DailySales {
    public static String TAG = "DailySales";

    @DatabaseField(generatedId = true)
    private transient int id;
    @DatabaseField
    private String date_updated_at;
    @DatabaseField
    private String time_updated_at;
    @DatabaseField
    private double average_amount_per_invoice = 0.0;
    @DatabaseField
    private double tax = 0.0;
    @DatabaseField
    private double quantity = 0;
    @DatabaseField
    private int transaction_count = 0;
    @DatabaseField
    private double amount = 0.0;
    @DatabaseField
    private double amount_with_tax = 0.0;
    @DatabaseField
    private double amount_without_tax = 0.0;
    @DatabaseField
    private transient int branch_id = 0;

    public DailySales() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAverage_amount_per_invoice() {
        return average_amount_per_invoice;
    }

    public void setAverage_amount_per_invoice(double average_amount_per_invoice) {
        this.average_amount_per_invoice = average_amount_per_invoice;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public int getTransaction_count() {
        return transaction_count;
    }

    public void setTransaction_count(int transaction_count) {
        this.transaction_count = transaction_count;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAmount_with_tax() {
        return amount_with_tax;
    }

    public void setAmount_with_tax(double amount_with_tax) {
        this.amount_with_tax = amount_with_tax;
    }

    public double getAmount_without_tax() {
        return amount_without_tax;
    }

    public void setAmount_without_tax(double amount_without_tax) {
        this.amount_without_tax = amount_without_tax;
    }

    public String getDate_updated_at() {
        return date_updated_at;
    }

    public void setDate_updated_at(String date_updated_at) {
        this.date_updated_at = date_updated_at;
    }

    public int getBranch_id() {
        return branch_id;
    }

    public void setBranch_id(int branch_id) {
        this.branch_id = branch_id;
    }

    public String getTime_updated_at() {
        return time_updated_at;
    }

    public void setTime_updated_at(String time_updated_at) {
        this.time_updated_at = time_updated_at;
    }

    public void insertTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.DAILY_SALES, DatabaseOperation.INSERT);
            Log.e(TAG, "Inserting dailysales to database");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTo(ImonggoDBHelper dbHelper) {
        try {
            Log.e(TAG, "Deleting dailysales to database");
            dbHelper.dbOperations(this, Table.DAILY_SALES, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTo(ImonggoDBHelper dbHelper) {
        try {
            Log.e(TAG, "updating dailysales to database");
            dbHelper.dbOperations(this, Table.DAILY_SALES, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void dbOperation(ImonggoDBHelper dbHelper, DatabaseOperation databaseOperation) {
        if(databaseOperation == DatabaseOperation.INSERT)
            insertTo(dbHelper);
        else if(databaseOperation == DatabaseOperation.UPDATE)
            updateTo(dbHelper);
        else if(databaseOperation == DatabaseOperation.DELETE)
            deleteTo(dbHelper);
    }

    @Override
    public String toString() {
        return "DailySales{" +
                "id=" + id +
                ", date_updated_at='" + date_updated_at + '\'' +
                ", time_updated_at='" + time_updated_at + '\'' +
                ", average_amount_per_invoice=" + average_amount_per_invoice +
                ", tax=" + tax +
                ", quantity=" + quantity +
                ", transaction_count=" + transaction_count +
                ", amount=" + amount +
                ", amount_with_tax=" + amount_with_tax +
                ", amount_without_tax=" + amount_without_tax +
                ", branch_id=" + branch_id +
                '}';
    }
}
