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
public class ProductSorting extends DBTable {

    @DatabaseField(generatedId = true)
    private transient int id;
    @DatabaseField
    private String column;
    @DatabaseField
    private String label;
    @DatabaseField
    private boolean is_default;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "module_setting_id")
    private transient ModuleSetting moduleSetting;

    public ProductSorting() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean is_default() {
        return is_default;
    }

    public void setIs_default(boolean is_default) {
        this.is_default = is_default;
    }

    public ModuleSetting getModuleSetting() {
        return moduleSetting;
    }

    public void setModuleSetting(ModuleSetting moduleSetting) {
        this.moduleSetting = moduleSetting;
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        return is_default == ((ProductSorting)o).is_default();
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(ProductSorting.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(ProductSorting.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(ProductSorting.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}