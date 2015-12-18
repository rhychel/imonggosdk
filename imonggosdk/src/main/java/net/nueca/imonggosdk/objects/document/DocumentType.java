package net.nueca.imonggosdk.objects.document;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.base.Extras;

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
public class DocumentType extends BaseTable implements Extras.DoOperationsForExtras {

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
    public String toString() {
        return "DocumentType{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        insertExtrasTo(dbHelper);
        try {
            dbHelper.insert(DocumentType.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateExtrasTo(dbHelper);
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(DocumentType.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        deleteExtrasTo(dbHelper);
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(DocumentType.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        updateExtrasTo(dbHelper);
    }

    @Override
    public void insertExtrasTo(ImonggoDBHelper2 dbHelper) {
        if(extras != null) {
            extras.setDocumentType(this);
            extras.setId(getClass().getName().toUpperCase(), id);
            extras.insertTo(dbHelper);
        }
    }

    @Override
    public void deleteExtrasTo(ImonggoDBHelper2 dbHelper) {
        if(extras != null)
            extras.deleteTo(dbHelper);
    }

    @Override
    public void updateExtrasTo(ImonggoDBHelper2 dbHelper) {
        if(extras != null) {
            String idstr = getClass().getName().toUpperCase() + "_" + id;
            if (idstr.equals(extras.getId()))
                extras.updateTo(dbHelper);
            else {
                extras.deleteTo(dbHelper);
                extras.setId(getClass().getName().toUpperCase(), id);
                extras.insertTo(dbHelper);
            }
        }
    }
}
