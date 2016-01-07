package net.nueca.imonggosdk.objects.price;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.base.BaseTable;

import java.sql.SQLException;

/**
 * Created by rhymart on 11/10/15.
 */
@DatabaseTable
public class Price extends BaseTable {

    @Expose
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "price_list_id")
    private PriceList priceList;
    @Expose
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "product_id")
    private Product product;
    @Expose
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "unit_id")
    private Unit unit;
    @Expose
    @DatabaseField
    private double retail_price = 0.0;

    @Expose
    @DatabaseField
    private String discount_text; /** FORMAT: product_discount,company_discount **/

    public Price() { }

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

    public double getRetail_price() {
        return retail_price;
    }

    public void setRetail_price(double retail_price) {
        this.retail_price = retail_price;
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

    public String getProductDiscount() {
        if (!discount_text.contains(",")) {
            if (discount_text.equals("0"))
                return "";
            return discount_text;
        }

        String[] discounts = discount_text.split(",");
        return discounts[0].trim();
    }
    public String getCompanyDiscount() {
        if (!discount_text.contains(",")) {
            if (discount_text.equals("0"))
                return "";
            return discount_text;
        }

        String[] discounts = discount_text.split(",");
        return discounts[1].trim();
    }

    public String toJSONString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }

    @Override
    public String toString() {
        /*return "Price{" +
                "priceList=" + priceList +
                ", product=" + product +
                ", unit=" + unit +
                ", retail_price=" + retail_price +
                '}';*/
        return "Price(id=" + id + ")\n" + toJSONString();
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(Price.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(Price.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(Price.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
