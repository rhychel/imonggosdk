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
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class ProductTag extends DBTable {

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private String tag;
    @DatabaseField
    private String searchKey = "";
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "product_id")
    private Product product;

    public static String TAG = "ProductTag";

    public ProductTag() { }

    public ProductTag(String tag, Product product) {
        this.tag = tag;
        this.searchKey = tag.toLowerCase();
        this.product = product;
    }

    public ProductTag(Product product) {
        this.product = product;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public String toString() {
        return "ProductTag{" +
                "id=" + id +
                ", tag='" + tag + '\'' +
                ", searchKey='" + searchKey + '\'' +
                ", product=" + product +
                '}';
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(ProductTag.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(ProductTag.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(ProductTag.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
