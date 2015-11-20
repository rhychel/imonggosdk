package net.nueca.imonggosdk.objects.base;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.RoutePlan;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.customer.CustomerCategory;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.invoice.Invoice;

import java.sql.SQLException;

/**
 * Created by rhymart on 11/10/15.
 */
@DatabaseTable
public class Extras extends DBTable {

    @DatabaseField(id = true)
    private transient String id;

    // Product
    @Expose
    @DatabaseField
    private Boolean batch_maintained; // true || false

    // DocumentLine
    @Expose
    @DatabaseField
    private String batch_no;
    @Expose
    @DatabaseField
    private String delivery_date;
    @Expose
    @DatabaseField
    private String brand;
    @Expose
    @DatabaseField
    private String outright_return;
    @Expose
    @DatabaseField
    private String discrepancy;
    @Expose
    @DatabaseField
    private Integer customer_id;

    // Invoice
    @Expose
    @DatabaseField
    private String longitude;
    @Expose
    @DatabaseField
    private String latitude;

    // Customer
    @Expose
    @DatabaseField
    private String checkin_count;
    @Expose
    @DatabaseField
    private String last_checkin_at;

    // DocumentPurpose
    @Expose
    @DatabaseField
    private String requires_expiry_date; // true || false

    /** FOREIGN TABLES **/
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "route_plan_id")
    private transient RoutePlan routePlan;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "document_line_id")
    private transient DocumentLine documentLine;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "product_id")
    private transient Product product;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "invoice_id")
    private transient Invoice invoice;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "customer_fr_id")
    private transient Customer customer;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "customer_category_id")
    private transient CustomerCategory customerCategory; // customer_type // (?)
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "user_id")
    private transient User user;

    public Extras() { }

    public Extras(String tableName, int tableId) {
        this.id = tableName+"_"+tableId;
    }

    protected Extras(Builder builder) {
        documentLine = builder.documentLine;
        routePlan = builder.routePlan;
        product = builder.product;
        invoice = builder.invoice;
        customer = builder.customer;
        customerCategory = builder.customerCategory;
        user = builder.user;
        id = builder.id;
        batch_maintained = builder.batch_maintained;
        customer_id = builder.customer_id;
        longitude = builder.longitude;
        latitude = builder.latitude;
        checkin_count = builder.checkin_count;
        last_checkin_at = builder.last_checkin_at;
        requires_expiry_date = builder.requires_expiry_date;
        delivery_date = builder.delivery_date;
        brand = builder.brand;
        batch_no = builder.batch_no;
        outright_return = builder.outright_return;
        discrepancy = builder.discrepancy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setId(String tableName, int tableId) {
        this.id = tableName+"_"+tableId;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getCheckin_count() {
        return checkin_count;
    }

    public void setCheckin_count(String checkin_count) {
        this.checkin_count = checkin_count;
    }

    public String getLast_checkin_at() {
        return last_checkin_at;
    }

    public void setLast_checkin_at(String last_checkin_at) {
        this.last_checkin_at = last_checkin_at;
    }

    public String getRequires_expiry_date() {
        return requires_expiry_date;
    }

    public void setRequires_expiry_date(String requires_expiry_date) {
        this.requires_expiry_date = requires_expiry_date;
    }

    public DocumentLine getDocumentLine() {
        return documentLine;
    }

    public void setDocumentLine(DocumentLine documentLine) {
        this.documentLine = documentLine;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public CustomerCategory getCustomerCategory() {
        return customerCategory;
    }

    public void setCustomerCategory(CustomerCategory customerCategory) {
        this.customerCategory = customerCategory;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDiscrepancy() {
        return discrepancy;
    }

    public void setDiscrepancy(String discrepancy) {
        this.discrepancy = discrepancy;
    }

    public String getOutright_return() {
        return outright_return;
    }

    public void setOutright_return(String outright_return) {
        this.outright_return = outright_return;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDelivery_date() {
        return delivery_date;
    }

    public void setDelivery_date(String delivery_date) {
        this.delivery_date = delivery_date;
    }

    public String getBatch_no() {
        return batch_no;
    }

    public void setBatch_no(String batch_no) {
        this.batch_no = batch_no;
    }

    public RoutePlan getRoutePlan() {
        return routePlan;
    }

    public void setRoutePlan(RoutePlan routePlan) {
        this.routePlan = routePlan;
    }

    public Integer getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(Integer customer_id) {
        this.customer_id = customer_id;
    }

    public Boolean isBatch_maintained() {
        return batch_maintained;
    }

    public void setBatch_maintained(Boolean batch_maintained) {
        this.batch_maintained = batch_maintained;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(Extras.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(Extras.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(Extras.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public interface DoOperationsForExtras {
        void insertExtrasTo(ImonggoDBHelper2 dbHelper);
        void deleteExtrasTo(ImonggoDBHelper2 dbHelper);
        void updateExtrasTo(ImonggoDBHelper2 dbHelper);
    }


    public static class Builder {
        protected Boolean batch_maintained; // true || false
        protected Integer customer_id;
        protected String longitude;
        protected String latitude;
        protected String checkin_count;
        protected String last_checkin_at;
        protected String requires_expiry_date; // true || false
        protected String id;
        protected DocumentLine documentLine;
        protected RoutePlan routePlan;
        protected Product product;
        protected Invoice invoice;
        protected Customer customer;
        protected CustomerCategory customerCategory;
        protected User user;

        protected String delivery_date;
        protected String brand;
        protected String batch_no;
        protected String outright_return;
        protected String discrepancy;

        public Builder route_plan(RoutePlan routePlan) {
            this.routePlan = routePlan;
            return this;
        }

        public Builder product(Product product) {
            this.product = product;
            return this;
        }

        public Builder invoice(Invoice invoice) {
            this.invoice = invoice;
            return this;
        }

        public Builder customer(Customer customer) {
            this.customer = customer;
            return this;
        }

        public Builder customer_category(CustomerCategory customerCategory) {
            this.customerCategory = customerCategory;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder document_line(DocumentLine documentLine) {
            this.documentLine = documentLine;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
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

        public Builder batch_maintained(Boolean batch_maintained) {
            this.batch_maintained = batch_maintained;
            return this;
        }

        public Builder customer_id(Integer customer_id) {
            this.customer_id = customer_id;
            return this;
        }

        public Builder longitude(String longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder latitude(String latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder checkin_count(String checkin_count) {
            this.checkin_count = checkin_count;
            return this;
        }

        public Builder last_checkin_at(String last_checkin_at) {
            this.last_checkin_at = last_checkin_at;
            return this;
        }

        public Builder requires_expiry_date(String requires_expiry_date) {
            this.requires_expiry_date = requires_expiry_date;
            return this;
        }

        public boolean isEmpty() {
            return  delivery_date == null &&
                    brand == null &&
                    batch_no == null &&
                    outright_return == null &&
                    discrepancy == null &&
                    batch_maintained == null &&
                    customer_id == null &&
                    longitude == null &&
                    latitude == null &&
                    checkin_count == null &&
                    last_checkin_at == null &&
                    requires_expiry_date == null;
        }

        public Extras buildIfNotEmpty() {
            if(isEmpty())
                return null;
            return new Extras(this);
        }
        public Extras build() {
            return new Extras(this);
        }
    }
}
