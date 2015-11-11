package net.nueca.imonggosdk.objects.deprecated;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable2;
import net.nueca.imonggosdk.objects.document.DocumentLine;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Created by gama on 7/20/15.
 */
@Deprecated
public class ExtendedAttributes extends BaseTable2 {
    @Expose
    @DatabaseField
    protected String delivery_date;
    @Expose
    @DatabaseField
    protected String brand;
    @Expose
    @DatabaseField
    protected String batch_no;
    @Expose
    @DatabaseField
    protected String outright_return;
    @Expose
    @DatabaseField
    protected String discrepancy;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "document_line_id")
    protected transient DocumentLine documentLine;

    public ExtendedAttributes() {}

    protected ExtendedAttributes(Builder builder) {
        documentLine = builder.documentLine;

        delivery_date = builder.delivery_date;
        brand = builder.brand;
        batch_no = builder.batch_no;
        outright_return = builder.outright_return;
        discrepancy = builder.discrepancy;
    }

    public String getDelivery_date() {
        return delivery_date;
    }

    public void setDelivery_date(String delivery_date) {
        this.delivery_date = delivery_date;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getBatch_no() {
        return batch_no;
    }

    public void setBatch_no(String batch_no) {
        this.batch_no = batch_no;
    }

    public String getOutright_return() {
        return outright_return;
    }

    public void setOutright_return(String outright_return) {
        this.outright_return = outright_return;
    }

    public String getDiscrepancy() {
        return discrepancy;
    }

    public void setDiscrepancy(String discrepancy) {
        this.discrepancy = discrepancy;
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new Gson();
        return new JSONObject(gson.toJson(this));
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
            dbHelper.dbOperations(this, Table.EXTENDED_ATTRIBUTES, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.EXTENDED_ATTRIBUTES, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.EXTENDED_ATTRIBUTES, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class Builder {
        protected String delivery_date;
        protected String brand;
        protected String batch_no;
        protected String outright_return;
        protected String discrepancy;
        protected DocumentLine documentLine;

        public Builder document_line(DocumentLine documentLine) {
            this.documentLine = documentLine;
            return this;
        }

        public Builder delivery_date(String delivery_date) {
            this.delivery_date = delivery_date;
            return this;
        }
        public Builder brand(String brand) {
            this.brand = brand;
            return this;
        }
        public Builder batch_no(String batch_no) {
            this.batch_no = batch_no;
            return this;
        }
        public Builder outright_return(String outright_return) {
            this.outright_return = outright_return;
            return this;
        }
        public Builder discrepancy(String discrepancy) {
            this.discrepancy = discrepancy;
            return this;
        }

        public boolean isEmpty() {
            return  delivery_date == null &&
                    brand == null &&
                    batch_no == null &&
                    outright_return == null &&
                    discrepancy == null;
        }

        public ExtendedAttributes buildIfNotEmpty() {
            if(isEmpty())
                return null;
            return new ExtendedAttributes(this);
        }
        public ExtendedAttributes build() {
            return new ExtendedAttributes(this);
        }
    }
}
