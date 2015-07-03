package net.nueca.imonggosdk.objects.invoice;

import com.google.gson.Gson;
import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable2;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Created by gama on 7/1/15.
 */
public class Payment extends BaseTable2 {
    @DatabaseField
    protected int payment_type_id;

    @DatabaseField
    protected double amount;

    @DatabaseField
    protected double tender;

    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "invoice_id")
    private Invoice invoice;

    public int getPaymentTypeId() {
        return payment_type_id;
    }

    public void setPaymentTypeId(int payment_type_id) {
        this.payment_type_id = payment_type_id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getTender() {
        return tender;
    }

    public void setTender(double tender) {
        this.tender = tender;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Payment() {}

    public Payment(Invoice invoice, int payment_type_id, double amount, double tender) {
        this.invoice = invoice;
        this.payment_type_id = payment_type_id;
        this.amount = amount;
        this.tender = tender;
    }

    @Override
    public void insertTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.PAYMENTS, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.PAYMENTS, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.PAYMENTS, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new Gson();
        return new JSONObject(gson.toJson(this));
    }
}
