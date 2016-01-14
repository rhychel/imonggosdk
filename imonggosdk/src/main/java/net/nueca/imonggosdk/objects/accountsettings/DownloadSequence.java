package net.nueca.imonggosdk.objects.accountsettings;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable;

import java.sql.SQLException;

/**
 * Created by rhymartmanchus on 13/01/2016.
 */
@DatabaseTable
public class DownloadSequence extends BaseTable {

    @DatabaseField
    private String table_key;

    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "module_setting_id")
    private ModuleSetting moduleSetting;

    public DownloadSequence() { }

    public DownloadSequence(String table_key, ModuleSetting moduleSetting) {
        this.table_key = table_key;
        this.moduleSetting = moduleSetting;
    }

    public DownloadSequence(String table_key) {
        this.table_key = table_key;
    }

    public String getTable_key() {
        return table_key;
    }

    public void setTable_key(String table_key) {
        this.table_key = table_key;
    }

    public ModuleSetting getModuleSetting() {
        return moduleSetting;
    }

    public void setModuleSetting(ModuleSetting moduleSetting) {
        this.moduleSetting = moduleSetting;
    }

    public Table getTableValue() {
        return Table.convertFromKey(table_key);
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(DownloadSequence.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(DownloadSequence.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(DownloadSequence.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
