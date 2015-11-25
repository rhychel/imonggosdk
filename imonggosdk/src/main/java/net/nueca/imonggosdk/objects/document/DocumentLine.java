package net.nueca.imonggosdk.objects.document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import net.nueca.imonggosdk.objects.deprecated.DocumentLineExtras;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Created by gama on 7/20/15.
 */
public class DocumentLine extends BaseTransactionLine {

    @Expose
    @DatabaseField
    protected int line_no;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "product_obj_id")
    protected transient Product product;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "document_id")
    protected transient Document document;
/*

    @Expose
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "extras")
    protected DocumentLineExtras extras;
*/

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
        super(builder);
        autoLine_no = builder.autoLine_no;

        line_no = builder.line_no;
        extras = builder.extras;
        if(extras != null)
            extras.setDocumentLine(this);
        discount_text = builder.discount_text;
        price = builder.price;
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

//    public DocumentLineExtras getDocumentLineExtras() {
//        return extras;
//    }
//
//    public void setDocumentLineExtras(DocumentLineExtras extras) {
//        this.extras = extras;
//    }

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
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        if(extras != null)
            extras.insertTo(dbHelper);

        if(extras != null)
            extras.insertTo(dbHelper);

        try {
            dbHelper.insert(DocumentLine.class, this);
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
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(DocumentLine.class, this);
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
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(DocumentLine.class, this);
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

    public static class Builder extends BaseTransactionLine.Builder<Builder> {
        protected int line_no = 0;
//        protected DocumentLineExtras extras;
        protected Extras extras;
        protected String discount_text;
        protected Double price;
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
//        public Builder extras(DocumentLineExtras extras) {
//            this.extras = extras;
//            return this;
//        }
        public Builder extras(Extras extras) {
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
