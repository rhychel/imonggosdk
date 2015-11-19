package net.nueca.imonggosdk.objects.accountsettings;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.base.DBTable;

import java.sql.SQLException;

/**
 * Created by rhymart on 11/16/15.
 */
@DatabaseTable
public class Cutoff extends DBTable {
    @DatabaseField(generatedId = true)
    private transient int id;
    @DatabaseField
    private int index = 0;
    @DatabaseField
    private String cutoff_format, from, to;
    @DatabaseField
    private String repeat; // D - daily, M - monthly
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "module_setting_id")
    private transient ModuleSetting moduleSetting;

    public Cutoff() {
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getCutoff_format() {
        return cutoff_format;
    }

    public void setCutoff_format(String cutoff_format) {
        this.cutoff_format = cutoff_format;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(Cutoff.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(Cutoff.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(Cutoff.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
