package net.nueca.imonggosdk.objects;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class ProductTag {

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private String tag;
    @DatabaseField
    private String searchKey = "";
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "product_id")
    private Product product;

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
}
