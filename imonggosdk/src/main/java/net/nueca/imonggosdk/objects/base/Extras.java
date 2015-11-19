package net.nueca.imonggosdk.objects.base;

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
    private String id;

    // Product
    @DatabaseField
    private boolean batch_maintained; // true || false

    // DocumentLine
    @DatabaseField
    private String batch_no;
    @DatabaseField
    private String delivery_date;
    @DatabaseField
    private String brand;
    @DatabaseField
    private String outright_return;
    @DatabaseField
    private String discrepancy;

    // Invoice
    @DatabaseField
    private String longitude;
    @DatabaseField
    private String latitude;

    // Customer
    @DatabaseField
    private String checkin_count;
    @DatabaseField
    private String last_checkin_at;

    // DocumentPurpose
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
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "customer_id")
    private transient Customer customer;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "customer_category_id")
    private transient CustomerCategory customerCategory; // customer_type // (?)
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "user_id")
    private transient User user;

    public Extras() { }

    public Extras(String tableName, int tableId) {
        this.id = tableName+"_"+tableId;
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

    public boolean isBatch_maintained() {
        return batch_maintained;
    }

    public void setBatch_maintained(boolean batch_maintained) {
        this.batch_maintained = batch_maintained;
    }

    public RoutePlan getRoutePlan() {
        return routePlan;
    }

    public void setRoutePlan(RoutePlan routePlan) {
        this.routePlan = routePlan;
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
}
