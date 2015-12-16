package net.nueca.imonggosdk.objects.invoice;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.base.Extras;

import java.sql.SQLException;

/**
 * Created by rhymart on 11/11/15.
 */
@DatabaseTable
public class InvoicePurpose extends BaseTable implements Extras.DoOperationsForExtras {

    @DatabaseField
    private String code, name, status;

    public InvoicePurpose() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            insertExtrasTo(dbHelper);
            dbHelper.insert(InvoicePurpose.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            deleteExtrasTo(dbHelper);
            dbHelper.delete(InvoicePurpose.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            updateExtrasTo(dbHelper);
            dbHelper.update(InvoicePurpose.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insertExtrasTo(ImonggoDBHelper2 dbHelper) {
        extras.setInvoice_purpose_code(code);
        extras.setInvoice_purpose_id(id);
        extras.setInvoice_purpose_name(name);
        extras.setId(InvoicePurpose.class.getName().toUpperCase(), id);
        extras.insertTo(dbHelper);
    }

    @Override
    public void deleteExtrasTo(ImonggoDBHelper2 dbHelper) {
        extras.deleteTo(dbHelper);
    }

    @Override
    public void updateExtrasTo(ImonggoDBHelper2 dbHelper) {
        extras.updateTo(dbHelper);
    }
}
