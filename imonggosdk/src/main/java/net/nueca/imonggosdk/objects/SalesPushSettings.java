package net.nueca.imonggosdk.objects;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.base.DBTable;
import net.nueca.imonggosdk.objects.salespromotion.SalesPromotion;

import java.sql.SQLException;

/**
 * Created by Jn on 01/12/15.
 */

@DatabaseTable
public class SalesPushSettings extends DBTable {

    @DatabaseField(generatedId = true, columnName = "id")
    int id;

    @Expose
    @DatabaseField
    private int
            enable_threshold,
            threshold,
            return_trigger;

    @Expose
    @DatabaseField
    private String offline_message = "", encouragement_message = "", trigger = "", congratulation_message = "";

    @Expose
    @DatabaseField
    private double conversion_to_currency;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "sales_push")
    private SalesPromotion salesPromotion;

    @Override
    public String toString() {
        return "SalesPushSettings{" +
                "id=" + id +
                ", enable_threshold=" + enable_threshold +
                ", threshold=" + threshold +
                ", return_trigger=" + return_trigger +
                ", offline_message='" + offline_message + '\'' +
                ", encouragement_message='" + encouragement_message + '\'' +
                ", trigger='" + trigger + '\'' +
                ", congratulation_message='" + congratulation_message + '\'' +
                ", salesPromotion=" + salesPromotion +
                ", conversion_to_currency=" + conversion_to_currency +
                '}';
    }

    public int getEnable_threshold() {
        return enable_threshold;
    }

    public void setEnable_threshold(int enable_threshold) {
        this.enable_threshold = enable_threshold;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getReturn_trigger() {
        return return_trigger;
    }

    public void setReturn_trigger(int return_trigger) {
        this.return_trigger = return_trigger;
    }

    public String getOffline_message() {
        return offline_message;
    }

    public void setOffline_message(String offline_message) {
        this.offline_message = offline_message;
    }

    public String getEncouragement_message() {
        return encouragement_message;
    }

    public void setEncouragement_message(String encouragement_message) {
        this.encouragement_message = encouragement_message;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getCongratulation_message() {
        return congratulation_message;
    }

    public void setCongratulation_message(String congratulation_message) {
        this.congratulation_message = congratulation_message;
    }

    public SalesPromotion getSalesPromotion() {
        return salesPromotion;
    }

    public void setSalesPromotion(SalesPromotion salesPromotion) {
        this.salesPromotion = salesPromotion;
    }

    public double getConversion_to_currency() {
        return conversion_to_currency;
    }

    public void setConversion_to_currency(double conversion_to_currency) {
        this.conversion_to_currency = conversion_to_currency;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(SalesPushSettings.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(SalesPushSettings.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(SalesPushSettings.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
