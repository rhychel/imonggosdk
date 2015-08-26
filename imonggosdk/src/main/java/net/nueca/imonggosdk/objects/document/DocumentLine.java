package net.nueca.imonggosdk.objects.document;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable2;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by gama on 7/20/15.
 */
public class DocumentLine extends BaseTable2 {

    @DatabaseField
    protected int line_no;
    @DatabaseField
    protected int product_id;
    @DatabaseField
    protected double quantity;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "document_id")
    protected transient Document document;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "extended_attributes")
    protected ExtendedAttributes extended_attributes;

    @DatabaseField
    protected String discount_text;
    @DatabaseField
    protected Double price;
    @DatabaseField
    protected Double retail_price;
    @DatabaseField
    protected Integer unit_id;
    @DatabaseField
    protected Double unit_content_quantity;
    @DatabaseField
    protected String unit_name;
    @DatabaseField
    protected Double unit_quantity;
    @DatabaseField
    protected Double unit_retail_price;

    @DatabaseField
    protected String product_name;
    @DatabaseField
    protected String product_stock_no;
    @DatabaseField
    protected Double subtotal;

    public DocumentLine() {}

    public DocumentLine(Builder builder) {
        line_no = builder.line_no;
        product_id = builder.product_id;
        quantity = builder.quantity;
        extended_attributes = builder.extended_attributes;
        discount_text = builder.discount_text;
        price = builder.price;
        retail_price = builder.retail_price;
        unit_id = builder.unit_id;
        unit_content_quantity = builder.unit_content_quantity;
        unit_name = builder.unit_name;
        unit_quantity = builder.unit_quantity;
        unit_retail_price = builder.unit_retail_price;
        document = builder.document;
    }

    public int getLine_no() {
        return line_no;
    }

    public void setLine_no(int line_no) {
        this.line_no = line_no;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public ExtendedAttributes getExtended_attributes() {
        return extended_attributes;
    }

    public void setExtended_attributes(ExtendedAttributes extended_attributes) {
        this.extended_attributes = extended_attributes;
    }

    public String getDiscount_text() {
        return discount_text;
    }

    public void setDiscount_text(String discount_text) {
        this.discount_text = discount_text;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getRetail_price() {
        return retail_price;
    }

    public void setRetail_price(Double retail_price) {
        this.retail_price = retail_price;
    }

    public Integer getUnit_id() {
        return unit_id;
    }

    public void setUnit_id(Integer unit_id) {
        this.unit_id = unit_id;
    }

    public Double getUnit_content_quantity() {
        return unit_content_quantity;
    }

    public void setUnit_content_quantity(Double unit_content_quantity) {
        this.unit_content_quantity = unit_content_quantity;
    }

    public String getUnit_name() {
        return unit_name;
    }

    public void setUnit_name(String unit_name) {
        this.unit_name = unit_name;
    }

    public Double getUnit_quantity() {
        return unit_quantity;
    }

    public void setUnit_quantity(Double unit_quantity) {
        this.unit_quantity = unit_quantity;
    }

    public Double getUnit_retail_price() {
        return unit_retail_price;
    }

    public void setUnit_retail_price(Double unit_retail_price) {
        this.unit_retail_price = unit_retail_price;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_stock_no() {
        return product_stock_no;
    }

    public void setProduct_stock_no(String product_stock_no) {
        this.product_stock_no = product_stock_no;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new Gson();
        return new JSONObject(gson.toJson(this));
    }

    @Override
    public void insertTo(ImonggoDBHelper dbHelper) {
        if(extended_attributes != null)
            extended_attributes.insertTo(dbHelper);

        try {
            dbHelper.dbOperations(this, Table.DOCUMENT_LINES, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(extended_attributes == null)
            return;
        extended_attributes.setDocumentLine(this);
        extended_attributes.updateTo(dbHelper);
    }

    @Override
    public void deleteTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.DOCUMENT_LINES, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(extended_attributes == null)
            return;
        extended_attributes.deleteTo(dbHelper);
    }

    @Override
    public void updateTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.DOCUMENT_LINES, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class Builder {
        protected int line_no = 0;
        protected int product_id = 0;
        protected double quantity;
        protected ExtendedAttributes extended_attributes;
        protected String discount_text;
        protected Double price;
        protected Double retail_price;
        protected Integer unit_id = null;
        protected Double unit_content_quantity = null;
        protected String unit_name = null;
        protected Double unit_quantity = null;
        protected Double unit_retail_price = null;
        protected Document document;

        protected String product_name;
        protected String product_stock_no;
        protected Double subtotal;

        public Builder product_name(String product_name) {
            this.product_name = product_name;
            return this;
        }
        public Builder product_stock_no(String product_stock_no) {
            this.product_stock_no = product_stock_no;
            return this;
        }
        public Builder subtotal(Double subtotal) {
            this.subtotal = subtotal;
            return this;
        }

        public Builder document(Document document) {
            this.document = document;
            return this;
        }

        public Builder line_no(int line_no) {
            this.line_no = line_no;
            return this;
        }
        public Builder product_id(int product_id) {
            this.product_id = product_id;
            return this;
        }
        public Builder quantity(double quantity) {
            this.quantity = quantity;
            return this;
        }
        public Builder extended_attributes(ExtendedAttributes extended_attributes) {
            this.extended_attributes = extended_attributes;
            return this;
        }
        public Builder discount_text(String discount_text) {
            this.discount_text = discount_text;
            return this;
        }
        public Builder price(double price) {
            this.price = price;
            return this;
        }
        public Builder retail_price(double retail_price) {
            this.retail_price = retail_price;
            return this;
        }
        public Builder unit_id(int unit_id) {
            this.unit_id = unit_id;
            return this;
        }
        public Builder unit_content_quantity(double unit_content_quantity) {
            this.unit_content_quantity = unit_content_quantity;
            return this;
        }
        public Builder unit_name(String unit_name) {
            this.unit_name = unit_name;
            return this;
        }
        public Builder unit_quantity(double unit_quantity) {
            this.unit_quantity = unit_quantity;
            return this;
        }
        public Builder unit_retail_price(double unit_retail_price) {
            this.unit_retail_price = unit_retail_price;
            return this;
        }

        public DocumentLine build() {
            return new DocumentLine(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DocumentLine) && ((DocumentLine)o).getId() == id;
    }

    /** Overriding equals() requires an Overridden hashCode() **/
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id;
        return result;
    }
}
