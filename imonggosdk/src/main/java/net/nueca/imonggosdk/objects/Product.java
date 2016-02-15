package net.nueca.imonggosdk.objects;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.objects.price.PriceList;
import net.nueca.imonggosdk.objects.salespromotion.Discount;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 5/12/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class Product extends BaseTable implements Extras.DoOperationsForExtras {

    @Expose
    @DatabaseField
    protected double cost = 0.0,
            retail_price = 0.0,
            wholesale_price = 0.0,
            wholesale_quantity = 0.0; // special when branch_products is requested
    @Expose
    @DatabaseField
    protected String quantity = "",
            remaining = "",
            stock_no = "",
            name = "",
            description = "",
            thumbnail_url = "",
            barcode_list = "";
    @Expose
    @DatabaseField
    protected String status = "";
    @Expose
    @DatabaseField
    protected boolean allow_decimal_quantities = false,
            enable_decimal_quantities = false,
            disable_discount = false,
            disable_inventory = false,
            enable_open_price = false,
            tax_exempt = false;
    @Expose
    @DatabaseField
    protected String base_unit_name = "";

    @DatabaseField
    protected transient int unit_id = -1;
    @DatabaseField
    protected transient boolean isFavorite = false;
    @DatabaseField
    protected transient boolean isSelected = false;
    @DatabaseField
    protected transient String unit = "";
    @DatabaseField
    private transient boolean isBaseUnitSellable = false;

    private transient String orig_quantity = "0";
    private transient String rcv_quantity = "";
    private transient String ret_quantity = "";
    private transient String dsc_quantity = "";

    private transient int line_no = 0;
    private transient double unit_content_quantity = 0.0;
    private transient double unit_quantity = 0.0;


    public Product() {}

    public String toJSONString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }

    @Override
    public String toString() {
        return toJSONString();
    }

    @ForeignCollectionField
    private transient ForeignCollection<ProductTag> tags;

    @ForeignCollectionField
    private transient ForeignCollection<Unit> units;

    @ForeignCollectionField
    private transient ForeignCollection<DocumentLine> documentLines;

    @ForeignCollectionField
    private transient ForeignCollection<Discount> discounts;

    @ForeignCollectionField
    private transient ForeignCollection<Price> prices;

    @ForeignCollectionField
    private transient ForeignCollection<BranchProduct> branchProducts;

//    @ForeignCollectionField
//    private transient ForeignCollection<BranchPrice> branchPrices;
//
//    @ForeignCollectionField
//    private transient ForeignCollection<BranchProduct> branchProducts;

    @Expose
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "inventory_id")
    private Inventory inventory;

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getRetail_price() {
        return retail_price;
    }

    public void setRetail_price(double retail_price) {
        this.retail_price = retail_price;
    }

    public double getWholesale_price() {
        return wholesale_price;
    }

    public void setWholesale_price(double wholesale_price) {
        this.wholesale_price = wholesale_price;
    }

    public double getWholesale_quantity() {
        return wholesale_quantity;
    }

    public void setWholesale_quantity(double wholesale_quantity) {
        this.wholesale_quantity = wholesale_quantity;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getRemaining() {
        return remaining;
    }

    public void setRemaining(String remaining) {
        this.remaining = remaining;
    }

    public String getStock_no() {
        return stock_no;
    }

    public void setStock_no(String stock_no) {
        this.stock_no = stock_no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnail_url() {
        return thumbnail_url;
    }

    public void setThumbnail_url(String thumbnail_url) {
        this.thumbnail_url = thumbnail_url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBarcode_list() {
        return barcode_list;
    }

    public void setBarcode_list(String barcode_list) {
        this.barcode_list = barcode_list;
    }

    public boolean isAllow_decimal_quantities() {
        return allow_decimal_quantities;
    }

    public void setAllow_decimal_quantities(boolean allow_decimal_quantities) {
        this.allow_decimal_quantities = allow_decimal_quantities;
    }

    public boolean isEnable_decimal_quantities() {
        return enable_decimal_quantities;
    }

    public void setEnable_decimal_quantities(boolean enable_decimal_quantities) {
        this.enable_decimal_quantities = enable_decimal_quantities;
    }

    public boolean isDisable_discount() {
        return disable_discount;
    }

    public void setDisable_discount(boolean disable_discount) {
        this.disable_discount = disable_discount;
    }

    public boolean isDisable_inventory() {
        return disable_inventory;
    }

    public void setDisable_inventory(boolean disable_inventory) {
        this.disable_inventory = disable_inventory;
    }

    public boolean isEnable_open_price() {
        return enable_open_price;
    }

    public void setEnable_open_price(boolean enable_open_price) {
        this.enable_open_price = enable_open_price;
    }

    public boolean isTax_exempt() {
        return tax_exempt;
    }

    public void setTax_exempt(boolean tax_exempt) {
        this.tax_exempt = tax_exempt;
    }

    public String getBase_unit_name() {
        return base_unit_name;
    }

    public void setBase_unit_name(String base_unit_name) {
        this.base_unit_name = base_unit_name;
    }

    public int getUnit_id() {
        return unit_id;
    }

    public void setUnit_id(int unit_id) {
        this.unit_id = unit_id;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getOrig_quantity() {
        return orig_quantity;
    }

    public void setOrig_quantity(String orig_quantity) {
        this.orig_quantity = orig_quantity;
    }

    public String getRcv_quantity() {
        return rcv_quantity;
    }

    public void setRcv_quantity(String rcv_quantity) {
        this.rcv_quantity = rcv_quantity;
    }

    public String getRet_quantity() {
        return ret_quantity;
    }

    public void setRet_quantity(String ret_quantity) {
        this.ret_quantity = ret_quantity;
    }

    public String getDsc_quantity() {
        return dsc_quantity;
    }

    public void setDsc_quantity(String dsc_quantity) {
        this.dsc_quantity = dsc_quantity;
    }

    public int getLine_no() {
        return line_no;
    }

    public void setLine_no(int line_no) {
        this.line_no = line_no;
    }

    public double getUnit_content_quantity() {
        return unit_content_quantity;
    }

    public void setUnit_content_quantity(double unit_content_quantity) {
        this.unit_content_quantity = unit_content_quantity;
    }

    public double getUnit_quantity() {
        return unit_quantity;
    }

    public void setUnit_quantity(double unit_quantity) {
        this.unit_quantity = unit_quantity;
    }

    public ForeignCollection<ProductTag> getTags() {
        return tags;
    }

    public void setTags(ForeignCollection<ProductTag> tags) {
        this.tags = tags;
    }

    public ForeignCollection<Unit> getUnits() {
        return units;
    }

    public ForeignCollection<DocumentLine> getDocumentLines() {
        return documentLines;
    }

    public void setDocumentLines(ForeignCollection<DocumentLine> documentLines) {
        this.documentLines = documentLines;
    }

    public ForeignCollection<Discount> getDiscounts() {
        return discounts;
    }

    public List<Discount> getDiscountsList() {
        return new ArrayList<>(discounts);
    }

    public void setDiscounts(ForeignCollection<Discount> discounts) {
        this.discounts = discounts;
    }

    public void setUnits(ForeignCollection<Unit> units) {
        this.units = units;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public String getInStock() {
        if(inventory == null)
            return "0";
        return inventory.getInventory();
    }

    public ForeignCollection<Price> getPrices() {
        return prices;
    }

    public void setPrices(ForeignCollection<Price> prices) {
        this.prices = prices;
    }

//    public ForeignCollection<BranchPrice> getBranchPrices() {
//        return branchPrices;
//    }
//
//    public void setBranchPrices(ForeignCollection<BranchPrice> branchPrices) {
//        this.branchPrices = branchPrices;
//    }

    public ForeignCollection<BranchProduct> getBranchProducts() {
        return branchProducts;
    }

    public void setBranchProducts(ForeignCollection<BranchProduct> branchProducts) {
        this.branchProducts = branchProducts;
    }

    public boolean isBaseUnitSellable() {
        return isBaseUnitSellable;
    }

    public void setIsBaseUnitSellable(boolean isBaseUnitSellable) {
        this.isBaseUnitSellable = isBaseUnitSellable;
    }

    public Product(double cost, double retail_price, double wholesale_price, double wholesale_quantity, String quantity) {
        this.cost = cost;
        this.retail_price = retail_price;
        this.wholesale_price = wholesale_price;
        this.wholesale_quantity = wholesale_quantity;
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Product) && ((Product)o).getId() == id;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        insertExtrasTo(dbHelper);
        try {
            dbHelper.insert(Product.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateExtrasTo(dbHelper);
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(Product.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        deleteExtrasTo(dbHelper);
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(Product.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateExtrasTo(dbHelper);
    }

    @Override
    public void insertExtrasTo(ImonggoDBHelper2 dbHelper) {
        if(extras != null) {
            extras.setProduct(this);
            extras.setId(Product.class.getName().toUpperCase(), id);
            extras.insertTo(dbHelper);
        }
    }

    @Override
    public void deleteExtrasTo(ImonggoDBHelper2 dbHelper) {
        if(extras != null)
            extras.deleteTo(dbHelper);
    }

    @Override
    public void updateExtrasTo(ImonggoDBHelper2 dbHelper) {
        if(extras != null)
            extras.updateTo(dbHelper);
    }
}
