package net.nueca.imonggosdk.objects.base;

import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.customer.CustomerCategory;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.invoice.Invoice;

/**
 * Created by rhymart on 11/10/15.
 */
public class Extras {

    // Product
    private String batch_maintained; // true || false

    // DocumentLine
    private String batch_no;
    private String delivery_date;
    private String brand;
    private String outright_return;
    private String discrepancy;

    // Invoice
    private String longitude;
    private String latitude;

    // Customer
    private Integer route_plan_id;
    private Integer user_id;
    private Integer customer_type; // (?)
    private String checkin_count;
    private String last_checkin_at;

    // DocumentPurpose
    private String requires_expiry_date; // true || false


    private DocumentLine documentLine;
    private Product product;
    private Invoice invoice;
    private Customer customer;
    private CustomerCategory customerCategory; // customer_type
    private User user;

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

    public String getBatch_maintained() {
        return batch_maintained;
    }

    public void setBatch_maintained(String batch_maintained) {
        this.batch_maintained = batch_maintained;
    }
}
