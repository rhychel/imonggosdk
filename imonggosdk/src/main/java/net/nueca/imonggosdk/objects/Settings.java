package net.nueca.imonggosdk.objects;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;

import java.sql.SQLException;

/**
 * Created by Jn on 10/05/15.
 * imonggosdk (c)2015
 */

@DatabaseTable
public class Settings {
    public final static String SETTINGS_ID_FIELD_NAME = "id";
    public final static String SETTINGS_NAME_FIELD_NAME = "name";
    public final static String SETTINGS_VALUE_FIELD_NAME = "value";

    @DatabaseField(generatedId = true, columnName = SETTINGS_ID_FIELD_NAME)
    int id;

    @DatabaseField(columnName = SETTINGS_NAME_FIELD_NAME)
    String name;

    @DatabaseField(columnName = SETTINGS_VALUE_FIELD_NAME)
    String value;

    public Settings() {
    }

    public Settings(int id, String name, String value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Settings{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void insertTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.SETTINGS, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.SETTINGS, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.SETTINGS, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void dbOperation(ImonggoDBHelper dbHelper, DatabaseOperation databaseOperation) {
        if (databaseOperation == DatabaseOperation.INSERT)
            insertTo(dbHelper);
        else if (databaseOperation == DatabaseOperation.UPDATE)
            updateTo(dbHelper);
        else if (databaseOperation == DatabaseOperation.DELETE)
            deleteTo(dbHelper);
    }
}
