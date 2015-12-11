package net.nueca.imonggosdk.objects.base;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.RoutePlan;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.customer.CustomerCategory;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;

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
    @Expose
    @DatabaseField
    private String default_selling_unit; // unit_id
    @Expose
    @DatabaseField
    private String default_ordering_unit_id; // unit_id


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

    // Customer
    @Expose
    @DatabaseField
    private String checkin_count;
    @Expose
    @DatabaseField
    private String last_checkin_at;

    // Invoice
    @Expose
    @DatabaseField
    private String longitude;
    @Expose
    @DatabaseField
    private String latitude;
    @Expose
    @DatabaseField
    private Integer payment_term_id;
    @Expose
    @DatabaseField
    private String payment_term_code;
    @Expose
    @DatabaseField
    private String customer_discount_text_summary;
    @Expose
    @DatabaseField
    private String customer_discount_amounts_summary;
    @Expose
    @DatabaseField
    private String total_unit_retail_price;
    @Expose
    @DatabaseField
    private String total_company_discount;
    @Expose
    @DatabaseField
    private String total_customer_discount;

    // InvoiceLine
    @Expose
    @DatabaseField
    private String product_discount_text;
    @Expose
    @DatabaseField
    private String product_discount_amount;
    @Expose
    @DatabaseField
    private String company_discount_text;
    @Expose
    @DatabaseField
    private String company_discount_amount;
    @Expose
    @DatabaseField
    private String customer_discount_text;
    @Expose
    @DatabaseField
    private String customer_discount_amounts;
    @Expose
    @DatabaseField
    private Boolean is_bad_stock;
    @Expose
    @DatabaseField
    private Integer invoice_purpose_id;
    @Expose
    @DatabaseField
    private String invoice_purpose_code;
    @Expose
    @DatabaseField
    private String invoice_purpose_name;
    @Expose
    @DatabaseField
    private String expiry_date;

    // DocumentPurpose
    @Expose
    @DatabaseField
    private String requires_expiry_date; // true || false

    // Unit
    @Expose
    @DatabaseField
    private Boolean is_default_selling_unit;

    // InvoicePayment
    @Expose
    @DatabaseField
    private String check_name;
    @Expose
    @DatabaseField
    private String check_number;
    @Expose
    @DatabaseField
    private String bank_branch;
    @Expose
    @DatabaseField
    private String check_date;

    /** FOREIGN TABLES **/
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "route_plan_id")
    private transient RoutePlan routePlan;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "document_id")
    private transient Document document;
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
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "unit_id")
    private transient Unit unit;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "payment_id")
    private transient InvoicePayment invoicePayment;

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
        unit = builder.unit;
        invoicePayment = builder.invoicePayment;

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
        is_default_selling_unit = builder.is_default_selling_unit;
        check_name = builder.check_name;
        check_number = builder.check_number;
        bank_branch = builder.bank_branch;
        check_date = builder.check_date;

        payment_term_id = builder.payment_term_id;
        payment_term_code = builder.payment_term_code;
        customer_discount_text_summary = builder.customer_discount_text_summary;
        customer_discount_amounts_summary = builder.customer_discount_amounts_summary;
        total_unit_retail_price = builder.total_unit_retail_price;
        total_company_discount = builder.total_company_discount;
        total_customer_discount = builder.total_customer_discount;
        product_discount_text = builder.product_discount_text;
        product_discount_amount = builder.product_discount_amount;
        company_discount_text = builder.company_discount_text;
        company_discount_amount = builder.company_discount_amount;
        customer_discount_text = builder.customer_discount_text;
        customer_discount_amounts = builder.customer_discount_amounts;
        is_bad_stock = builder.is_bad_stock;
        invoice_purpose_id = builder.invoice_purpose_id;
        invoice_purpose_code = builder.invoice_purpose_code;
        invoice_purpose_name = builder.invoice_purpose_name;
        expiry_date = builder.expiry_date;
        default_selling_unit = builder.default_selling_unit;
        default_ordering_unit_id = builder.default_ordering_unit_id;
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

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
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

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Boolean getIs_default_selling_unit() {
        return is_default_selling_unit;
    }

    public void setIs_default_selling_unit(Boolean is_default_selling_unit) {
        this.is_default_selling_unit = is_default_selling_unit;
    }

    public Boolean getBatch_maintained() {
        return batch_maintained;
    }

    public String getCheck_name() {
        return check_name;
    }

    public void setCheck_name(String check_name) {
        this.check_name = check_name;
    }

    public String getCheck_number() {
        return check_number;
    }

    public void setCheck_number(String check_number) {
        this.check_number = check_number;
    }

    public String getBank_branch() {
        return bank_branch;
    }

    public void setBank_branch(String bank_branch) {
        this.bank_branch = bank_branch;
    }

    public String getCheck_date() {
        return check_date;
    }

    public void setCheck_date(String check_date) {
        this.check_date = check_date;
    }

    public InvoicePayment getInvoicePayment() {
        return invoicePayment;
    }

    public void setInvoicePayment(InvoicePayment invoicePayment) {
        this.invoicePayment = invoicePayment;
    }

    public Integer getPayment_term_id() {        return payment_term_id;
    }

    public void setPayment_term_id(Integer payment_term_id) {
        this.payment_term_id = payment_term_id;
    }

    public String getPayment_term_code() {
        return payment_term_code;
    }

    public void setPayment_term_code(String payment_term_code) {
        this.payment_term_code = payment_term_code;
    }

    public String getCustomer_discount_text_summary() {
        return customer_discount_text_summary;
    }

    public void setCustomer_discount_text_summary(String customer_discount_text_summary) {
        this.customer_discount_text_summary = customer_discount_text_summary;
    }

    public String getCustomer_discount_amounts_summary() {
        return customer_discount_amounts_summary;
    }

    public void setCustomer_discount_amounts_summary(String customer_discount_amounts_summary) {
        this.customer_discount_amounts_summary = customer_discount_amounts_summary;
    }

    public String getTotal_unit_retail_price() {
        return total_unit_retail_price;
    }

    public void setTotal_unit_retail_price(String total_unit_retail_price) {
        this.total_unit_retail_price = total_unit_retail_price;
    }

    public String getTotal_company_discount() {
        return total_company_discount;
    }

    public void setTotal_company_discount(String total_company_discount) {
        this.total_company_discount = total_company_discount;
    }

    public String getTotal_customer_discount() {
        return total_customer_discount;
    }

    public void setTotal_customer_discount(String total_customer_discount) {
        this.total_customer_discount = total_customer_discount;
    }

    public String getProduct_discount_text() {
        return product_discount_text;
    }

    public void setProduct_discount_text(String product_discount_text) {
        this.product_discount_text = product_discount_text;
    }

    public String getProduct_discount_amount() {
        return product_discount_amount;
    }

    public void setProduct_discount_amount(String product_discount_amount) {
        this.product_discount_amount = product_discount_amount;
    }

    public String getCompany_discount_text() {
        return company_discount_text;
    }

    public void setCompany_discount_text(String company_discount_text) {
        this.company_discount_text = company_discount_text;
    }

    public String getCompany_discount_amount() {
        return company_discount_amount;
    }

    public void setCompany_discount_amount(String company_discount_amount) {
        this.company_discount_amount = company_discount_amount;
    }

    public String getCustomer_discount_text() {
        return customer_discount_text;
    }

    public void setCustomer_discount_text(String customer_discount_text) {
        this.customer_discount_text = customer_discount_text;
    }

    public String getCustomer_discount_amounts() {
        return customer_discount_amounts;
    }

    public void setCustomer_discount_amounts(String customer_discount_amounts) {
        this.customer_discount_amounts = customer_discount_amounts;
    }

    public Boolean getIs_bad_stock() {
        return is_bad_stock;
    }

    public void setIs_bad_stock(Boolean is_bad_stock) {
        this.is_bad_stock = is_bad_stock;
    }

    public Integer getInvoice_purpose_id() {
        return invoice_purpose_id;
    }

    public void setInvoice_purpose_id(Integer invoice_purpose_id) {
        this.invoice_purpose_id = invoice_purpose_id;
    }

    public String getInvoice_purpose_code() {
        return invoice_purpose_code;
    }

    public void setInvoice_purpose_code(String invoice_purpose_code) {
        this.invoice_purpose_code = invoice_purpose_code;
    }

    public String getInvoice_purpose_name() {
        return invoice_purpose_name;
    }

    public void setInvoice_purpose_name(String invoice_purpose_name) {
        this.invoice_purpose_name = invoice_purpose_name;
    }

    public String getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(String expiry_date) {
        this.expiry_date = expiry_date;
    }

    public String getDefault_selling_unit() {
        return default_selling_unit;
    }

    public void setDefault_selling_unit(String default_selling_unit) {
        this.default_selling_unit = default_selling_unit;
    }

    public String getDefault_ordering_unit_id() {
        return default_ordering_unit_id;
    }

    public void setDefault_ordering_unit_id(String default_ordering_unit_id) {
        this.default_ordering_unit_id = default_ordering_unit_id;
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
        protected Unit unit;
        protected InvoicePayment invoicePayment;

        protected String delivery_date;
        protected String brand;
        protected String batch_no;
        protected String outright_return;
        protected String discrepancy;
        protected Boolean is_default_selling_unit;
        protected String check_name;
        protected String check_number;
        protected String bank_branch;
        protected String check_date;
        protected String default_selling_unit;
        protected String default_ordering_unit_id;

        protected Integer payment_term_id;
        protected String payment_term_code;
        protected String customer_discount_text_summary;
        protected String customer_discount_amounts_summary;
        protected String total_unit_retail_price;
        protected String total_company_discount;
        protected String total_customer_discount;
        protected String product_discount_text;
        protected String product_discount_amount;
        protected String company_discount_text;
        protected String company_discount_amount;
        protected String customer_discount_text;
        protected String customer_discount_amounts;
        protected Boolean is_bad_stock;
        protected Integer invoice_purpose_id;
        protected String invoice_purpose_code;
        protected String invoice_purpose_name;
        protected String expiry_date;

        public Builder default_selling_unit(String default_selling_unit) {
            this.default_selling_unit = default_selling_unit;
            return this;
        }

        public Builder default_ordering_unit_id(String default_ordering_unit_id) {
            this.default_ordering_unit_id = default_ordering_unit_id;
            return this;
        }

        public Builder payment_term_id(Integer payment_term_id) {
            this.payment_term_id = payment_term_id;
            return this;
        }

        public Builder payment_term_code(String payment_term_code) {
            this.payment_term_code = payment_term_code;
            return this;
        }

        public Builder customer_discount_text_summary(String customer_discount_text_summary) {
            this.customer_discount_text_summary = customer_discount_text_summary;
            return this;
        }

        public Builder customer_discount_amounts_summary(String customer_discount_amounts_summary) {
            this.customer_discount_amounts_summary = customer_discount_amounts_summary;
            return this;
        }

        public Builder total_unit_retail_price(String total_unit_retail_price) {
            this.total_unit_retail_price = total_unit_retail_price;
            return this;
        }

        public Builder total_company_discount(String total_company_discount) {
            this.total_company_discount = total_company_discount;
            return this;
        }

        public Builder total_customer_discount(String total_customer_discount) {
            this.total_customer_discount = total_customer_discount;
            return this;
        }

        public Builder product_discount_text(String product_discount_text) {
            this.product_discount_text = product_discount_text;
            return this;
        }

        public Builder product_discount_amount(String product_discount_amount) {
            this.product_discount_amount = product_discount_amount;
            return this;
        }

        public Builder company_discount_text(String company_discount_text) {
            this.company_discount_text = company_discount_text;
            return this;
        }

        public Builder company_discount_amount(String company_discount_amount) {
            this.company_discount_amount = company_discount_amount;
            return this;
        }

        public Builder customer_discount_text(String customer_discount_text) {
            this.customer_discount_text = customer_discount_text;
            return this;
        }

        public Builder customer_discount_amounts(String customer_discount_amounts) {
            this.customer_discount_amounts = customer_discount_amounts;
            return this;
        }

        public Builder is_bad_stock(Boolean is_bad_stock) {
            this.is_bad_stock = is_bad_stock;
            return this;
        }

        public Builder invoice_purpose_id(Integer invoice_purpose_id) {
            this.invoice_purpose_id = invoice_purpose_id;
            return this;
        }

        public Builder invoice_purpose_code(String invoice_purpose_code) {
            this.invoice_purpose_code = invoice_purpose_code;
            return this;
        }

        public Builder invoice_purpose_name(String invoice_purpose_name) {
            this.invoice_purpose_name = invoice_purpose_name;
            return this;
        }

        public Builder expiry_date(String expiry_date) {
            this.expiry_date = expiry_date;
            return this;
        }

        public Builder invoicePayment(InvoicePayment invoicePayment) {
            this.invoicePayment = invoicePayment;
            return this;
        }

        public Builder is_default_selling_unit(Boolean is_default_selling_unit) {
            this.is_default_selling_unit = is_default_selling_unit;
            return this;
        }

        public Builder check_name(String check_name) {
            this.check_name = check_name;
            return this;
        }

        public Builder check_number(String check_number) {
            this.check_number = check_number;
            return this;
        }

        public Builder bank_branch(String bank_branch) {
            this.bank_branch = bank_branch;
            return this;
        }

        public Builder check_date(String check_date) {
            this.check_date = check_date;
            return this;
        }

        public Builder unit(Unit unit) {
            this.unit = unit;
            return this;
        }

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
