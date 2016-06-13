package net.nueca.imonggosdk.objects.accountsettings;

import android.util.Log;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.SequenceType;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.DBTable;

import java.sql.SQLException;
import java.util.ArrayList;
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
    @DatabaseField
    private boolean is_view = false;
    @DatabaseField
    private boolean has_get_latest_document = false;
    @DatabaseField
    private boolean has_pin_code = true;
    @DatabaseField
    private int display_sequence = 1;
    @DatabaseField
    private boolean can_add = true;
    @DatabaseField
    private boolean can_edit = true;
    @DatabaseField
    private boolean can_print = false;
    @DatabaseField
    private boolean can_change_inventory = true;
    @DatabaseField
    private boolean can_override_price = false;
    @DatabaseField
    private boolean has_returns = true;
    @DatabaseField
    private boolean has_partial = true;
    @ForeignCollectionField
    private transient ForeignCollection<Cutoff> cutoffs;
    @ForeignCollectionField(orderAscending = true, orderColumnName = "id")
    private transient ForeignCollection<Sequence> sequences;
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
    @DatabaseField
    private boolean show_only_sellable_products = false;
    @DatabaseField
    private boolean is_voidable = true;

    public ModuleSetting() {
    }

    public boolean is_voidable() {
        return is_voidable;
    }

    public void setIs_voidable(boolean is_voidable) {
        this.is_voidable = is_voidable;
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

    public ForeignCollection<Sequence> getSequences() {
        return sequences;
    }

    public void setSequences(ForeignCollection<Sequence> sequences) {
        this.sequences = sequences;
    }

    public int[] modulesToDownload(ImonggoDBHelper2 dbHelper, final boolean show_only_sellable_products) {
        try {
            List<Sequence> sequence = dbHelper.fetchForeignCollection(sequences.closeableIterator(), new ImonggoDBHelper2.Conditional<Sequence>() {
                @Override
                public boolean validate(Sequence obj) {
                    if(show_only_sellable_products)
                        if(obj.getTableValue() == Table.PRODUCTS)
                            return false;
                    return obj.getSequenceType() == SequenceType.DOWNLOAD;
                }
            });
            Log.e("sequence", sequence.size()+"");
            int []modules = new int[sequence.size()];
            int i = 0;
            for(Sequence ds : sequence) {
                modules[i++] = ds.getTableValue().ordinal();
                Log.e("tableValue", ds.getTable_key());
            }
            return modules;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[0];
    }

    public List<Table> modulesToUpdate(ImonggoDBHelper2 dbHelper, boolean show_only_sellable_products) {
        return modulesToUpdate(dbHelper, true, show_only_sellable_products);
    }

    public List<Table> modulesToUpdate(ImonggoDBHelper2 dbHelper, boolean hasAll, final boolean show_only_sellable_products) {
        List<Table> modules = new ArrayList<>();
        try {
            List<Sequence> updateSequence = dbHelper.fetchForeignCollection(sequences.closeableIterator(), new ImonggoDBHelper2.Conditional<Sequence>() {
                @Override
                public boolean validate(Sequence obj) {
                    if(show_only_sellable_products)
                        if(obj.getTableValue() == Table.PRODUCTS)
                            return false;
                    return obj.getSequenceType() == SequenceType.UPDATE;
                }
            });
            if(hasAll)
                modules.add(Table.ALL);
            Log.e("modules", updateSequence.size()+"--");
            for(Sequence us : updateSequence) {
                Log.e("modules", us.getTableValue().getStringName());
                modules.add(us.getTableValue());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modules;
    }

    public boolean is_view() {
        return is_view;
    }

    public void setIs_view(boolean is_view) {
        this.is_view = is_view;
    }

    public boolean isHas_get_latest_document() {
        return has_get_latest_document;
    }

    public void setHas_get_latest_document(boolean has_get_latest_document) {
        this.has_get_latest_document = has_get_latest_document;
    }

    public boolean isHas_pin_code() {
        return has_pin_code;
    }

    public void setHas_pin_code(boolean has_pin_code) {
        this.has_pin_code = has_pin_code;
    }

    public boolean isShow_only_sellable_products() {
        return show_only_sellable_products;
    }

    public void setShow_only_sellable_products(boolean show_only_sellable_products) {
        this.show_only_sellable_products = show_only_sellable_products;
    }

    public int getDisplay_sequence() {
        return display_sequence;
    }

    public void setDisplay_sequence(int display_sequence) {
        this.display_sequence = display_sequence;
    }

    public boolean isCan_add() {
        return can_add;
    }

    public void setCan_add(boolean can_add) {
        this.can_add = can_add;
    }

    public boolean isCan_edit() {
        return can_edit;
    }

    public void setCan_edit(boolean can_edit) {
        this.can_edit = can_edit;
    }

    public boolean isCan_print() {
        return can_print;
    }

    public void setCan_print(boolean can_print) {
        this.can_print = can_print;
    }

    public boolean isCan_change_inventory() {
        return can_change_inventory;
    }

    public void setCan_change_inventory(boolean can_change_inventory) {
        this.can_change_inventory = can_change_inventory;
    }

    public boolean isHas_returns() {
        return has_returns;
    }

    public void setHas_returns(boolean has_returns) {
        this.has_returns = has_returns;
    }

    public boolean isHas_partial() {
        return has_partial;
    }

    public void setHas_partial(boolean has_partial) {
        this.has_partial = has_partial;
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
        if(module_type.equals("customers"))
            return ConcessioModule.CUSTOMERS;
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

    @Override
    public String toString() {
        return "ModuleSetting{" +
                "label='" + label + '\'' +
                ", module_type='" + module_type + '\'' +
                '}';
    }

    public boolean isCan_override_price() {
        return can_override_price;
    }

    public void setCan_override_price(boolean can_override_price) {
        this.can_override_price = can_override_price;
    }
}
