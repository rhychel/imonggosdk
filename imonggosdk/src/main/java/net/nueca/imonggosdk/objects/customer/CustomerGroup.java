package net.nueca.imonggosdk.objects.customer;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.price.PriceList;

import java.sql.SQLException;

/**
 * Created by rhymart on 11/10/15.
 */
@DatabaseTable
public class CustomerGroup extends BaseTable {

    @DatabaseField
    private String name;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "price_list_id")
    private PriceList priceList;
    @DatabaseField
    private String discount_text;
    @DatabaseField
    private String status;

    public CustomerGroup() { }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PriceList getPriceList() {
        return priceList;
    }

    public void setPriceList(PriceList priceList) {
        this.priceList = priceList;
    }

    public String getDiscount_text() {
        return discount_text;
    }

    public void setDiscount_text(String discount_text) {
        this.discount_text = discount_text;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void insertTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.CUSTOMER_GROUPS, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.CUSTOMER_GROUPS, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.CUSTOMER_GROUPS, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
