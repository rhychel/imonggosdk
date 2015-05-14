package net.nueca.imonggosdk.objects.associatives;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.TaxRate;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class ProductTaxRateAssoc {

    public static final String PRODUCT_ID_FIELD_NAME = "product_id";
    public static final String TAXRATE_ID_FIELD_NAME = "tax_rate_id";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = PRODUCT_ID_FIELD_NAME)
    private Product product;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = TAXRATE_ID_FIELD_NAME)
    private TaxRate taxRate;

    public ProductTaxRateAssoc() { }

    public ProductTaxRateAssoc(Product product, TaxRate taxRate) {
        this.product = product;
        this.taxRate = taxRate;
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

    public TaxRate getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(TaxRate taxRate) {
        this.taxRate = taxRate;
    }
}
