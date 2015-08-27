package net.nueca.imonggosdk.objects.document;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/15/15.
 * imonggosdk (c)2015
 * {
 *      "name": "Receive from Supplier",
 *      "code": "receive_supplier",
 *      "id": 1,
 *      "utc_created_at": "2012-08-28T21:46:16Z",
 *      "utc_updated_at": "2012-08-28T21:46:16Z"
 * }
 */
@DatabaseTable
public class DocumentType extends BaseTable {

    @DatabaseField
    private String name;
    @DatabaseField
    private String code;
    @ForeignCollectionField
    private transient ForeignCollection<DocumentPurpose> document_purposes;

    public DocumentType() { }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ForeignCollection<DocumentPurpose> getDocument_purposes() {
        return document_purposes;
    }

    public void setDocument_purposes(ForeignCollection<DocumentPurpose> document_purposes) {
        this.document_purposes = document_purposes;
    }

    @Override
    public void insertTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.DOCUMENT_TYPES, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper dbHelper) {

        try {
            dbHelper.dbOperations(this, Table.DOCUMENT_TYPES, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.DOCUMENT_TYPES, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "DocumentType{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
