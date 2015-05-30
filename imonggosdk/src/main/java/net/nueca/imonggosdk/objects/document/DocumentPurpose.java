package net.nueca.imonggosdk.objects.document;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.objects.base.BaseTable;

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
public class DocumentPurpose extends BaseTable {

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
}
