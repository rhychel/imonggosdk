package net.nueca.imonggosdk.objects.customer;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.RoutePlan;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.price.PriceList;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.invoice.PaymentTerms;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/12/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class Customer extends BaseTable implements Extras.DoOperationsForExtras {

    @DatabaseField
    private int point_to_amount_ratio;
    @DatabaseField
    private String code, alternate_code, first_name, last_name, name, company_name,
            tin, street = "", city, state, zipcode, country, telephone = "", fax,
            mobile, email, remark, customer_type_id, customer_type_name, discount_text,
            available_points, birthdate, status, birthday,
            membership_expired_at = "", membership_start_at = "", biometric_signature = "", gender = "";
    @DatabaseField
    private boolean tax_exempt;
    @DatabaseField
    private transient boolean is_favorite = false;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "price_list_id")
    private transient PriceList priceList;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "branch_id")
    private transient Branch branch;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "payment_terms_id")
    private transient PaymentTerms paymentTerms;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "customer_category_id")
    private transient CustomerCategory customerCategory; // customer_type_id (?)
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "route_plan_id")
    private transient RoutePlan routePlan;
    @ForeignCollectionField(orderColumnName = "id")
    private transient ForeignCollection<Invoice> invoices;
    @ForeignCollectionField
    private transient ForeignCollection<Document> documents;

    public Customer() {

    }

    public Customer(String first_name, String last_name, String name, String company_name, String telephone, String mobile, String fax, String email, String street, String city, String zipcode, String country, String state, String tin, String gender) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.name = name;
        this.company_name = company_name;
        this.telephone = telephone;
        this.mobile = mobile;
        this.fax = fax;
        this.email = email;
        this.street = street;
        this.city = city;
        this.zipcode = zipcode;
        this.country = country;
        this.state = state;
        this.tin = tin;
        this.gender = gender;
    }

    public int getPoint_to_amount_ratio() {
        return point_to_amount_ratio;
    }

    public void setPoint_to_amount_ratio(int point_to_amount_ratio) {
        this.point_to_amount_ratio = point_to_amount_ratio;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAlternate_code() {
        return alternate_code;
    }

    public void setAlternate_code(String alternate_code) {
        this.alternate_code = alternate_code;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public String getTin() {
        return tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCustomer_type_id() {
        return customer_type_id;
    }

    public void setCustomer_type_id(String customer_type_id) {
        this.customer_type_id = customer_type_id;
    }

    public String getCustomer_type_name() {
        return customer_type_name;
    }

    public void setCustomer_type_name(String customer_type_name) {
        this.customer_type_name = customer_type_name;
    }

    public String getDiscount_text() {
        return discount_text;
    }

    public void setDiscount_text(String discount_text) {
        this.discount_text = discount_text;
    }

    public String getAvailable_points() {
        return available_points;
    }

    public void setAvailable_points(String available_points) {
        this.available_points = available_points;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getMembership_expired_at() {
        return membership_expired_at;
    }

    public void setMembership_expired_at(String membership_expired_at) {
        this.membership_expired_at = membership_expired_at;
    }

    public String getMembership_start_at() {
        return membership_start_at;
    }

    public void setMembership_start_at(String membership_start_at) {
        this.membership_start_at = membership_start_at;
    }

    public String getBiometric_signature() {
        return biometric_signature;
    }

    public void setBiometric_signature(String biometric_signature) {
        this.biometric_signature = biometric_signature;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isTax_exempt() {
        return tax_exempt;
    }

    public void setTax_exempt(boolean tax_exempt) {
        this.tax_exempt = tax_exempt;
    }

    public boolean is_favorite() {
        return is_favorite;
    }

    public void setIs_favorite(boolean is_favorite) {
        this.is_favorite = is_favorite;
    }

    public CustomerCategory getCustomerCategory() {
        return customerCategory;
    }

    public void setCustomerCategory(CustomerCategory customerCategory) {
        this.customerCategory = customerCategory;
    }

    public PaymentTerms getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(PaymentTerms paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public PriceList getPriceList() {
        return priceList;
    }

    public void setPriceList(PriceList priceList) {
        this.priceList = priceList;
    }

    public RoutePlan getRoutePlan() {
        return routePlan;
    }

    public void setRoutePlan(RoutePlan routePlan) {
        this.routePlan = routePlan;
    }

    public ForeignCollection<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(ForeignCollection<Invoice> invoices) {
        this.invoices = invoices;
    }

    public ForeignCollection<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(ForeignCollection<Document> documents) {
        this.documents = documents;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Customer) && ((Customer)o).getId() == id;
    }

    /** Overriding equals() requires an Overridden hashCode() **/
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id;
        return result;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getFullAddress() {
        String address = "";

        if(street != null && !street.isEmpty())
            address += street;

        if(city != null && !city.isEmpty()) {
            if(!address.isEmpty())
                address += ", ";
            address += city;
        }

        if(zipcode != null && !zipcode.isEmpty()) {
            if(!address.isEmpty())
                address += " ";
            address += zipcode;
        }

        if(country != null && !country.isEmpty()) {
            if(!address.isEmpty())
                address += ", ";
            address += country;
        }

        return address;
    }

    public String getLastPurchase() {
        try {
            Invoice invoice = invoices.closeableIterator().first();
            invoices.closeLastIterator();

            if(invoice != null)
                return invoice.getInvoice_date();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(Customer.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(Customer.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(Customer.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insertExtrasTo(ImonggoDBHelper2 dbHelper) {
        extras.setCustomer(this);
        extras.setId(Customer.class.getName().toUpperCase(), id);
        extras.insertTo(dbHelper);
    }

    @Override
    public void deleteExtrasTo(ImonggoDBHelper2 dbHelper) {
        extras.deleteTo(dbHelper);
    }

    @Override
    public void updateExtrasTo(ImonggoDBHelper2 dbHelper) {
        extras.updateTo(dbHelper);
    }
}