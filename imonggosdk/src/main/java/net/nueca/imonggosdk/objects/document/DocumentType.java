package net.nueca.imonggosdk.objects.document;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.objects.base.BaseTable;

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
}
