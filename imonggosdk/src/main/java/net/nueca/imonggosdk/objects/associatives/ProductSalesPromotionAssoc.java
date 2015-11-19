package net.nueca.imonggosdk.objects.associatives;

import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.base.DBTable;
import net.nueca.imonggosdk.objects.invoice.SalesPromotion;

import java.sql.SQLException;

/**
 * Created by rhymart on 11/11/15.
 */
public class ProductSalesPromotionAssoc extends DBTable {
    public static final String PRODUCT_ID_FIELD_NAME = "product_id";
    public static final String SALES_PROMOTION_ID_FIELD_NAME = "sales_promotion_id";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = PRODUCT_ID_FIELD_NAME)
    private Product product;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = SALES_PROMOTION_ID_FIELD_NAME)
    private SalesPromotion salesPromotion;

    public ProductSalesPromotionAssoc() {
    }

    public ProductSalesPromotionAssoc(Product product, SalesPromotion salesPromotion) {
        this.product = product;
        this.salesPromotion = salesPromotion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public SalesPromotion getSalesPromotion() {
        return salesPromotion;
    }

    public void setSalesPromotion(SalesPromotion salesPromotion) {
        this.salesPromotion = salesPromotion;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(ProductSalesPromotionAssoc.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(ProductSalesPromotionAssoc.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(ProductSalesPromotionAssoc.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
