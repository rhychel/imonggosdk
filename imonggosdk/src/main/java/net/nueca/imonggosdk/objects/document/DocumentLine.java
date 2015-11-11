package net.nueca.imonggosdk.objects.document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.base.BaseTable2;
import net.nueca.imonggosdk.objects.deprecated.DocumentLineExtras;
import net.nueca.imonggosdk.objects.deprecated.ExtendedAttributes;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Created by gama on 7/20/15.
 */
public class DocumentLine extends BaseTable2 {

    @Expose
    @DatabaseField
    protected int line_no;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "product_obj_id")
    protected transient Product product;
    @Expose
    @DatabaseField
    protected int product_id;
    @Expose
    @DatabaseField
    protected double quantity;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "document_id")
    protected transient Document document;

    @Expose
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "extras")
    protected DocumentLineExtras extras;

    //@Expose
    //@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "extras")
    //protected DocumentLineExtras_Old extras;

    @Expose
    @DatabaseField
    protected String discount_text;
    @Expose
    @DatabaseField
    protected Double price;
    @Expose
    @DatabaseField
    protected Double retail_price;
    @Expose
    @DatabaseField
    protected Integer unit_id;
    @Expose
    @DatabaseField
    protected Double unit_content_quantity;
    @Expose
    @DatabaseField
    protected String unit_name;
    @Expose
    @DatabaseField
    protected Double unit_quantity;
    @Expose
    @DatabaseField
    protected Double unit_retail_price;

    @Expose
    @DatabaseField
    protected String product_name;
    @Expose
    @DatabaseField
    protected String product_stock_no;
    @Expose
    @DatabaseField
    protected Double subtotal;

    protected transient boolean autoLine_no = false;

    public DocumentLine() {}

    public DocumentLine(Builder builder) {
        autoLine_no = builder.autoLine_no;

        line_no = builder.line_no;
        product_id = builder.product_id;
        quantity = builder.quantity;
        extras = builder.extras;
        if(extras != null)
            extras.setDocumentLine(this);
        discount_text = builder.discount_text;
        price = builder.price;
        retail_price = builder.retail_price;
        unit_id = builder.unit_id;
        unit_content_quantity = builder.unit_content_quantity;
        unit_name = builder.unit_name;
        unit_quantity = builder.unit_quantity;
        unit_retail_price = builder.unit_retail_price;
        document = builder.document;

        //extras = builder.extras;
        //if(extras != null)
        //    extras.setDocumentLine(this);

        product = builder.product;
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

    public DocumentLineExtras getExtras() {
        return extras;
    }

    public void setExtras(DocumentLineExtras extras) {
        this.extras = extras;
    }

    /*@Deprecated
    public DocumentLineExtras_Old getExtras() {
        return extras;
    }

    @Deprecated
    public void setExtras(DocumentLineExtras_Old extras) {
        this.extras = extras;
    }*/

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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return new JSONObject(gson.toJson(this));
    }

    @Override
    public void insertTo(ImonggoDBHelper dbHelper) {
        if(extras != null)
            extras.insertTo(dbHelper);

        if(extras != null)
            extras.insertTo(dbHelper);

        try {
            dbHelper.dbOperations(this, Table.DOCUMENT_LINES, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(extras != null) {
            extras.setDocumentLine(this);
            extras.updateTo(dbHelper);
        }

        if(extras != null) {
            extras.setDocumentLine(this);
            extras.updateTo(dbHelper);
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.DOCUMENT_LINES, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(extras != null) {
            extras.deleteTo(dbHelper);
        }

        if(extras != null) {
            extras.deleteTo(dbHelper);
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.DOCUMENT_LINES, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(extras != null) {
            extras.updateTo(dbHelper);
        }

        if(extras != null) {
            extras.updateTo(dbHelper);
        }
    }

    public static class Builder {
        protected int line_no = 0;
        protected int product_id = 0;
        protected double quantity;
        protected DocumentLineExtras extras;
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
        protected Product product;

        //protected DocumentLineExtras_Old extras;

        protected boolean autoLine_no;

        public Builder product(Product product) {
            this.product = product;
            return this;
        }

        /*public Builder extras(DocumentLineExtras_Old extras) {
            this.extras = extras;
            return this;
        }*/

        public Builder autoLine_no() {
            autoLine_no = true;
            return this;
        }

        public Builder useProductDetails(Product product) {
            this.product = product;

            product_id = product.getId();
            product_name = product.getName();
            product_stock_no = product.getStock_no();
            retail_price = product.getRetail_price();
            unit_id = product.getUnit_id();
            unit_content_quantity = product.getUnit_content_quantity();
            unit_name = product.getUnit();
            unit_quantity = product.getUnit_quantity();

            if(product.getRcv_quantity() != null && product.getRcv_quantity().length() > 0)
                quantity = Double.parseDouble(product.getRcv_quantity().replaceAll("[^0-9.,]", ""));

            return this;
        }

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
        public Builder extras(DocumentLineExtras extras) {
            this.extras = extras;
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
