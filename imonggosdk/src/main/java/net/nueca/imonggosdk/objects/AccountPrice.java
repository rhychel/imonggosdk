package net.nueca.imonggosdk.objects;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.base.BaseTable2;

import java.sql.SQLException;

/**
 *  {
     "product_id": 80,
 "utc_created_at": "2016-02-18T06:48:37Z",
 "utc_updated_at": "2016-02-18T06:48:37Z",

    "wholesale_quantity": null,
     "retail_price": 3047,
     "unit_id": null,
     "wholesale_price": 0
     }
 * Created by rhymartmanchus on 08/03/2016.
 */
@DatabaseTable
public class AccountPrice extends BaseTable2 {

    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "product_id")
    private Product product;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "unit_id")
    private Unit unit;
    @DatabaseField
    private Double wholesale_quantity;
    @DatabaseField
    private Double retail_price;
    @DatabaseField
    private Double wholesale_price;

    public AccountPrice() {
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Double getWholesale_quantity() {
        return wholesale_quantity;
    }

    public void setWholesale_quantity(Double wholesale_quantity) {
        this.wholesale_quantity = wholesale_quantity;
    }

    public Double getRetail_price() {
        return retail_price;
    }

    public void setRetail_price(Double retail_price) {
        this.retail_price = retail_price;
    }

    public Double getWholesale_price() {
        return wholesale_price;
    }

    public void setWholesale_price(Double wholesale_price) {
        this.wholesale_price = wholesale_price;
    }

    @Override
    public String toString() {
        return "id: " + id;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(AccountPrice.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(AccountPrice.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(AccountPrice.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
