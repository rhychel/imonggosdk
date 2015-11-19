package net.nueca.imonggosdk.objects;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.DBTable;

import java.sql.SQLException;

/**
 * Created by jN on 9/2/2015.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class DailySales extends DBTable {
    public static String TAG = "DailySales";

    @DatabaseField(generatedId = true)
    private transient int id;
    @DatabaseField
    private String date_of_sales;
    @DatabaseField
    private String date_requested_at;
    @DatabaseField
    private Double average_amount_per_invoice = 0.0;
    @DatabaseField
    private Double tax = 0.0;
    @DatabaseField
    private Double quantity = 0.0;
    @DatabaseField
    private int transaction_count = 0;
    @DatabaseField
    private String amount = "0";
    @DatabaseField
    private String amount_with_tax = "0";
    @DatabaseField
    private String amount_without_tax = "0";

    @DatabaseField
    private transient int branch_id = 0;

    public DailySales() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double getAverage_amount_per_invoice() {
        return average_amount_per_invoice;
    }

    public void setAverage_amount_per_invoice(double average_amount_per_invoice) {
        this.average_amount_per_invoice = average_amount_per_invoice;
    }

    public Double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public int getTransaction_count() {
        return transaction_count;
    }

    public void setTransaction_count(int transaction_count) {
        this.transaction_count = transaction_count;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAmount_with_tax() {
        return amount_with_tax;
    }

    public void setAmount_with_tax(String amount_with_tax) {
        this.amount_with_tax = amount_with_tax;
    }

    public String getAmount_without_tax() {
        return amount_without_tax;
    }

    public void setAmount_without_tax(String amount_without_tax) {
        this.amount_without_tax = amount_without_tax;
    }

    public String getDate_of_sales() {
        return date_of_sales;
    }

    public void setDate_of_sales(String date_of_sales) {
        this.date_of_sales = date_of_sales;
    }

    public int getBranch_id() {
        return branch_id;
    }

    public void setBranch_id(int branch_id) {
        this.branch_id = branch_id;
    }

    public String getDate_requested_at() {
        return date_requested_at;
    }

    public void setDate_requested_at(String date_requested_at) {
        this.date_requested_at = date_requested_at;
    }

    @Override
    public String toString() {
        return "DailySales{" +
                "id=" + id +
                ", date_of_sales='" + date_of_sales + '\'' +
                ", date_requested_at='" + date_requested_at + '\'' +
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

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(DailySales.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(DailySales.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(DailySales.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
