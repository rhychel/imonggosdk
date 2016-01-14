package net.nueca.imonggosdk.objects.accountsettings;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.base.DBTable;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by rhymart on 11/16/15.
 */
@DatabaseTable
public class ModuleSetting extends DBTable {

    @DatabaseField(id=true)
    private transient String module_type = "app";
    @DatabaseField
    private String label;
    @DatabaseField
    private boolean show_cutoff = false;
    @DatabaseField
    private String target_delivery_date = null;
    @DatabaseField
    private boolean is_enabled = false;
    @DatabaseField
    private boolean has_store_transfer = false;
    @DatabaseField
    private boolean require_document_reason = false;
    @DatabaseField
    private boolean has_route_plan = false;
    @DatabaseField
    private String show_in = "beginning"; // beginning, middle, last
    @DatabaseField
    private boolean require_warehouse = false;
    @DatabaseField
    private boolean with_purpose = false;
    @DatabaseField
    private boolean with_customer = false;
    @ForeignCollectionField
    private transient ForeignCollection<Cutoff> cutoffs;
    @ForeignCollectionField(orderAscending = true, orderColumnName = "id")
    private transient ForeignCollection<DownloadSequence> downloadSequences;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "product_listing_id")
    private transient ProductListing productListing;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "quantity_input_id")
    private transient QuantityInput quantityInput;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "manual_id")
    private transient Manual manual;
    @ForeignCollectionField
    private transient ForeignCollection<ProductSorting> productSortings;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "debug_id")
    private transient DebugMode debugMode;

    // ---- APP ----
    @DatabaseField
    private boolean show_history_after_transaction = false;
    @DatabaseField
    private boolean has_disable_image = false;

    public ModuleSetting() {
    }

    public String getModule_type() {
        return module_type;
    }

    public void setModule_type(String module_type) {
        this.module_type = module_type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isShow_cutoff() {
        return show_cutoff;
    }

    public void setShow_cutoff(boolean show_cutoff) {
        this.show_cutoff = show_cutoff;
    }

    public String getTarget_delivery_date() {
        return target_delivery_date;
    }

    public void setTarget_delivery_date(String target_delivery_date) {
        this.target_delivery_date = target_delivery_date;
    }

    public boolean is_enabled() {
        return is_enabled;
    }

    public void setIs_enabled(boolean is_enabled) {
        this.is_enabled = is_enabled;
    }

    public boolean isHas_store_transfer() {
        return has_store_transfer;
    }

    public void setHas_store_transfer(boolean has_store_transfer) {
        this.has_store_transfer = has_store_transfer;
    }

    public ForeignCollection<Cutoff> getCutoffs() {
        return cutoffs;
    }

    public void setCutoffs(ForeignCollection<Cutoff> cutoffs) {
        this.cutoffs = cutoffs;
    }

    public ProductListing getProductListing() {
        return productListing;
    }

    public void setProductListing(ProductListing productListing) {
        this.productListing = productListing;
    }

    public QuantityInput getQuantityInput() {
        return quantityInput;
    }

    public void setQuantityInput(QuantityInput quantityInput) {
        this.quantityInput = quantityInput;
    }

    public Manual getManual() {
        return manual;
    }

    public void setManual(Manual manual) {
        this.manual = manual;
    }

    public ForeignCollection<ProductSorting> getProductSortings() {
        return productSortings;
    }

    public void setProductSortings(ForeignCollection<ProductSorting> productSortings) {
        this.productSortings = productSortings;
    }

    public DebugMode getDebugMode() {
        return debugMode;
    }

    public void setDebugMode(DebugMode debugMode) {
        this.debugMode = debugMode;
    }

    public boolean isShow_history_after_transaction() {
        return show_history_after_transaction;
    }

    public void setShow_history_after_transaction(boolean show_history_after_transaction) {
        this.show_history_after_transaction = show_history_after_transaction;
    }

    public boolean isHas_disable_image() {
        return has_disable_image;
    }

    public void setHas_disable_image(boolean has_disable_image) {
        this.has_disable_image = has_disable_image;
    }

    public boolean isRequire_document_reason() {
        return require_document_reason;
    }

    public void setRequire_document_reason(boolean require_document_reason) {
        this.require_document_reason = require_document_reason;
    }

    public String getShow_in() {
        return show_in;
    }

    public void setShow_in(String show_in) {
        this.show_in = show_in;
    }

    public boolean isHas_route_plan() {
        return has_route_plan;
    }

    public void setHas_route_plan(boolean has_route_plan) {
        this.has_route_plan = has_route_plan;
    }

    public boolean isRequire_warehouse() {
        return require_warehouse;
    }

    public void setRequire_warehouse(boolean require_warehouse) {
        this.require_warehouse = require_warehouse;
    }

    public boolean isWith_purpose() {
        return with_purpose;
    }

    public void setWith_purpose(boolean with_purpose) {
        this.with_purpose = with_purpose;
    }

    public boolean isWith_customer() {
        return with_customer;
    }

    public void setWith_customer(boolean with_customer) {
        this.with_customer = with_customer;
    }

    public ForeignCollection<DownloadSequence> getDownloadSequences() {
        return downloadSequences;
    }

    public void setDownloadSequences(ForeignCollection<DownloadSequence> downloadSequences) {
        this.downloadSequences = downloadSequences;
    }

    public int[] modulesToDownload(ImonggoDBHelper2 dbHelper) {
        try {
            List<DownloadSequence> downloadSequence = dbHelper.fetchForeignCollection(downloadSequences.closeableIterator());
            Log.e("downloadSequence", downloadSequence.size()+"");
            int []modules = new int[downloadSequence.size()];
            int i = 0;
            for(DownloadSequence ds : downloadSequence) {
                Log.e("tableValue", ds.getTable_key());
                modules[i++] = ds.getTableValue().ordinal();
            }
            return modules;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[0];
    }

    public ConcessioModule getModuleType() {
        if(module_type.equals("stock_request"))
            return ConcessioModule.STOCK_REQUEST;
        if(module_type.equals("physical_count"))
            return ConcessioModule.PHYSICAL_COUNT;

        if(module_type.equals("receive_branch"))
            return ConcessioModule.RECEIVE_BRANCH;
        if(module_type.equals("release_branch"))
            return ConcessioModule.RELEASE_BRANCH;
        if(module_type.equals("receive_branch_pullout"))
            return ConcessioModule.RECEIVE_BRANCH_PULLOUT;

        if(module_type.equals("receive_adjustment"))
            return ConcessioModule.RECEIVE_ADJUSTMENT;
        if(module_type.equals("release_adjustment"))
            return ConcessioModule.RELEASE_ADJUSTMENT;

        if(module_type.equals("receive_supplier"))
            return ConcessioModule.RECEIVE_SUPPLIER;
        if(module_type.equals("release_supplier"))
            return ConcessioModule.RELEASE_SUPPLIER;

        if(module_type.equals("invoice"))
            return ConcessioModule.INVOICE;
        return ConcessioModule.APP;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(ModuleSetting.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(ModuleSetting.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(ModuleSetting.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
