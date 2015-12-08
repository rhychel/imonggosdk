package net.nueca.imonggosdk.objects.salespromotion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.SalesPushSettings;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.base.BatchList;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by rhymart on 10/14/15.
 * imonggosdk2 (c)2015
 */
@DatabaseTable
public class SalesPromotion extends BaseTable {
    public transient static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss Z";

    @Expose
    @DatabaseField
    private String promotion_type_name = "", // Generate a [__promotion_type_name__] for every purchase amount given below. <---- rebisco, points
            status = null,
            name = "",
            code = "",
            photos_ids = "",
            to_date = "",
            from_date = "";

    @DatabaseField(foreign = true, foreignAutoCreate = true)
    private SalesPushSettings settings;

    @DatabaseField
    private transient Date toDate, fromDate;

    @DatabaseField
    private transient String salesPromotionType; // type=custom[rewards points], type=sales_push[sales push products]

    @Expose
    protected List<Discount> discounts;

    @ForeignCollectionField
    private transient ForeignCollection<Discount> discounts_fc;

    public SalesPromotion() { }

    public SalesPushSettings getSettings() {
        return settings;
    }

    public void setSettings(SalesPushSettings    settings) {
        this.settings = settings;
    }

    public String getPromotion_type_name() {
        return promotion_type_name;
    }

    public void setPromotion_type_name(String promotion_type_name) {
        this.promotion_type_name = promotion_type_name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPhotos_ids() {
        return photos_ids;
    }

    public void setPhotos_ids(String photos_ids) {
        this.photos_ids = photos_ids;
    }

    public String getTo_date() {
        return to_date;
    }

    public void setTo_date(String to_date) {
        this.to_date = to_date;
    }

    public String getFrom_date() {
        return from_date;
    }

    public void setFrom_date(String from_date) {
        this.from_date = from_date;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    private void convertToDate() {
        SimpleDateFormat convertStringToDate = new SimpleDateFormat(DATE_FORMAT);
        try {
            this.toDate = convertStringToDate.parse(to_date);
            this.fromDate = convertStringToDate.parse(from_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public List<Discount> getDiscounts() {
        refresh();
        return discounts;
    }

    public void setDiscounts(BatchList<Discount> invoice_tax_rates) {
        this.discounts = invoice_tax_rates;
    }

    public void insertDiscount(Discount discount) {
        discounts.add(discount);
    }

    public void refresh() {
        if (discounts_fc != null && discounts == null) {
            for (Discount discount : discounts_fc) {
                insertDiscount(discount);
            }
        }
    }

    public String toJSONString() {
        refresh();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return (gson.toJson(this));
    }

    public JSONObject toJSONObject() throws JSONException {
        refresh();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return new JSONObject(gson.toJson(this));
    }

    @Override
    public String toString() {
        return toJSONString();
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(SalesPromotion.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        refresh();
        if(discounts != null) {
            for (Discount discount : discounts) {
                discount.setSalesPromotion(this);
                try {
                    Product product = dbHelper.getDao(Product.class).queryBuilder().where().eq("id", discount.getProduct_id())
                            .queryForFirst();
                    if (product != null)
                        discount.setProduct(product);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                discount.insertTo(dbHelper);
            }
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(SalesPromotion.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        refresh();
        if(discounts != null) {
            for (Discount discount : discounts) {
                discount.deleteTo(dbHelper);
            }
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(SalesPromotion.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        refresh();
        if(discounts != null) {
            for (Discount discount : discounts) {
                discount.setSalesPromotion(this);
                try {
                    Product product = dbHelper.getDao(Product.class).queryBuilder().where().eq("id", discount.getProduct_id())
                            .queryForFirst();
                    if (product != null)
                        discount.setProduct(product);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                discount.updateTo(dbHelper);
            }
        }
    }
}
