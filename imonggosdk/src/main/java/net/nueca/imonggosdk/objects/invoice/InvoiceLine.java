package net.nueca.imonggosdk.objects.invoice;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.base.BaseTable2;
import net.nueca.imonggosdk.objects.base.BaseTransactionLine;
import net.nueca.imonggosdk.objects.base.Extras;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Created by gama on 7/1/15.
 */
public class InvoiceLine extends BaseTransactionLine implements Extras.DoOperationsForExtras {
    @Expose
    @DatabaseField
    protected String discount_text;
    @Expose
    @DatabaseField
    protected int line_no;
    @Expose
    @DatabaseField
    protected String subtotal;

    @DatabaseField
    protected transient String no_discount_subtotal;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "invoice_id")
    protected transient Invoice invoice;

    public InvoiceLine() {}

    public InvoiceLine (Builder builder) {
        super(builder);
        discount_text = builder.discount_text;
        subtotal = builder.subtotal;
        line_no = builder.line_no;
        no_discount_subtotal = builder.no_discount_subtotal;
    }

    public String getDiscount_text() {
        return discount_text;
    }

    public void setDiscount_text(String discount_text) {
        this.discount_text = discount_text;
    }

    public String getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }

    public int getLine_no() {
        return line_no;
    }

    public void setLine_no(int line_no) {
        this.line_no = line_no;
    }

    public String getNo_discount_subtotal() {
        return no_discount_subtotal;
    }

    public void setNo_discount_subtotal(String no_discount_subtotal) {
        this.no_discount_subtotal = no_discount_subtotal;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new Gson();
        return new JSONObject(gson.toJson(this));
    }

    public static class Builder extends BaseTransactionLine.Builder<Builder> {
        protected String discount_text;
        protected String subtotal;
        protected int line_no = 0;
        protected String no_discount_subtotal;

        public Builder no_discount_subtotal(String no_discount_subtotal) {
            this.no_discount_subtotal = no_discount_subtotal;
            return this;
        }
        public Builder line_no(int line_no) {
            this.line_no = line_no;
            return this;
        }
        public Builder discount_text(String discount_text) {
            this.discount_text = discount_text;
            return this;
        }
        public Builder subtotal(String subtotal) {
            this.subtotal = subtotal;
            return this;
        }

        public InvoiceLine build() {
            return new InvoiceLine(this);
        }
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        insertExtrasTo(dbHelper);
        try {
            dbHelper.insert(InvoiceLine.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateExtrasTo(dbHelper);
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(InvoiceLine.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        deleteExtrasTo(dbHelper);
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(InvoiceLine.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        updateExtrasTo(dbHelper);
    }

    @Override
    public void insertExtrasTo(ImonggoDBHelper2 dbHelper) {
        if(extras != null) {
            extras.setInvoiceLine(this);
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
