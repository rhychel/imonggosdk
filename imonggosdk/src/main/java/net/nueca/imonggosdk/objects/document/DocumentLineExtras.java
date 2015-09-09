package net.nueca.imonggosdk.objects.document;

import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable2;

import java.sql.SQLException;

/**
 * Created by gama on 9/8/15.
 */
public class DocumentLineExtras extends BaseTable2 {
    @DatabaseField
    private String batch_no;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "document_line_id")
    protected transient DocumentLine documentLine;

    public DocumentLineExtras() {}

    public DocumentLineExtras(String batch_no) {
        this.batch_no = batch_no;
    }

    public String getBatch_no() {
        return batch_no;
    }

    public void setBatch_no(String batch_no) {
        this.batch_no = batch_no;
    }

    public DocumentLine getDocumentLine() {
        return documentLine;
    }

    public void setDocumentLine(DocumentLine documentLine) {
        this.documentLine = documentLine;
    }

    @Override
    public void insertTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.DOCUMENT_LINE_EXTRAS, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.DOCUMENT_LINE_EXTRAS, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.DOCUMENT_LINE_EXTRAS, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
