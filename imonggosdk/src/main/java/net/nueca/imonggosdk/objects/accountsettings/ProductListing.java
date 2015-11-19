package net.nueca.imonggosdk.objects.accountsettings;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.base.DBTable;

import java.sql.SQLException;

/**
 * Created by rhymart on 11/17/15.
 */
@DatabaseTable
public class ProductListing extends DBTable {

    @DatabaseField(generatedId = true)
    private transient int id;
    @DatabaseField
    private boolean show_price = false;
    @DatabaseField
    private boolean show_unit = false;
    @DatabaseField
    private boolean lock_category = false;
    @DatabaseField
    private boolean show_on_hand = false;
    @DatabaseField
    private boolean show_categories_on_start = false;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "module_setting_id")
    private transient ModuleSetting moduleSetting;

    public ProductListing() { }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isShow_price() {
        return show_price;
    }

    public void setShow_price(boolean show_price) {
        this.show_price = show_price;
    }

    public boolean isShow_unit() {
        return show_unit;
    }

    public void setShow_unit(boolean show_unit) {
        this.show_unit = show_unit;
    }

    public boolean isLock_category() {
        return lock_category;
    }

    public void setLock_category(boolean lock_category) {
        this.lock_category = lock_category;
    }

    public ModuleSetting getModuleSetting() {
        return moduleSetting;
    }

    public void setModuleSetting(ModuleSetting moduleSetting) {
        this.moduleSetting = moduleSetting;
    }

    public boolean isShow_on_hand() {
        return show_on_hand;
    }

    public void setShow_on_hand(boolean show_on_hand) {
        this.show_on_hand = show_on_hand;
    }

    public boolean isShow_categories_on_start() {
        return show_categories_on_start;
    }

    public void setShow_categories_on_start(boolean show_categories_on_start) {
        this.show_categories_on_start = show_categories_on_start;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(ProductListing.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(ProductListing.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(ProductListing.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
