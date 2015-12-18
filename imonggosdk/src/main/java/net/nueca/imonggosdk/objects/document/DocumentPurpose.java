package net.nueca.imonggosdk.objects.document;

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
 * Created by rhymart on 5/15/15.
 * imonggosdk (c)2015
 * {
 *  "status": "A",
 *  "name": "Transfer to Warehouse",
 *  "code": null,
 *  "id": 296,
 *  "document_type_id": 4,
 *  "utc_created_at": "2015-03-01T05:55:23Z",
 *  "document_type_code": "release_branch",
 *  "utc_updated_at": "2015-03-01T05:55:23Z"
 * }
 */
@DatabaseTable
public class DocumentPurpose extends BaseTable implements Extras.DoOperationsForExtras {

    @DatabaseField
    private String status = "A";
    @DatabaseField
    private String name;
    @DatabaseField
    private String code;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "document_type_id")
    private transient DocumentType documentType;

    public DocumentPurpose() { }

    public DocumentPurpose(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        insertExtrasTo(dbHelper);
        try {
            dbHelper.insert(DocumentPurpose.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateExtrasTo(dbHelper);
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(DocumentPurpose.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        deleteExtrasTo(dbHelper);
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(DocumentPurpose.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        updateExtrasTo(dbHelper);
    }

    @Override
    public void insertExtrasTo(ImonggoDBHelper2 dbHelper) {
        if(extras != null) {
            extras.setDocumentPurpose(this);
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

    public boolean isSourceDestinationBranchDependent() {
        return name.toLowerCase().equals("transfer to branch");
    }

    @Override
    public String toString() {
        return name;
    }

    public String toObjectString() {
        return "DocumentPurpose{" +
                "status='" + status + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", documentType=" + documentType +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        return id == ((DocumentPurpose)o).getId();
    }
}
