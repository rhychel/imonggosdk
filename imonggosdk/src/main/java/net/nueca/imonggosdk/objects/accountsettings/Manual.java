package net.nueca.imonggosdk.objects.accountsettings;

import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.base.DBTable;

import java.sql.SQLException;

/**
 * Created by rhymart on 11/17/15.
 */
public class Manual extends DBTable {

    @DatabaseField(generatedId = true)
    private transient int id;
    @DatabaseField
    private boolean is_enabled = false;
    @DatabaseField
    private boolean is_multiinput = false;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "module_setting_id")
    private transient ModuleSetting moduleSetting;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "quantity_input_id")
    private transient QuantityInput quantityInput;

    public Manual() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean is_enabled() {
        return is_enabled;
    }

    public void setIs_enabled(boolean is_enabled) {
        this.is_enabled = is_enabled;
    }

    public boolean is_multiinput() {
        return is_multiinput;
    }

    public void setIs_multiinput(boolean is_multiinput) {
        this.is_multiinput = is_multiinput;
    }

    public ModuleSetting getModuleSetting() {
        return moduleSetting;
    }

    public void setModuleSetting(ModuleSetting moduleSetting) {
        this.moduleSetting = moduleSetting;
    }

    public QuantityInput getQuantityInput() {
        return quantityInput;
    }

    public void setQuantityInput(QuantityInput quantityInput) {
        this.quantityInput = quantityInput;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(Manual.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(Manual.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(Manual.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
