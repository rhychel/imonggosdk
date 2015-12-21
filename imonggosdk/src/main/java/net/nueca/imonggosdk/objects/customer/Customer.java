package net.nueca.imonggosdk.objects.customer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.PaymentTerms;
import net.nueca.imonggosdk.objects.price.PriceList;
import net.nueca.imonggosdk.objects.routeplan.RoutePlan;
import net.nueca.imonggosdk.objects.routeplan.RoutePlanDetail;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Created by rhymart on 5/12/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class Customer extends BaseTable implements Extras.DoOperationsForExtras {

    public static final transient String CODE = "code";
    public static final transient String ALTERNATE_CODE = "alternate_code";
    public static final transient String FIRST_NAME = "first_name";
    public static final transient String MIDDLE_NAME = "middle_name";
    public static final transient String LAST_NAME = "last_name";
    public static final transient String COMPANY_NAME = "company_name";
    public static final transient String TIN = "tin";
    public static final transient String STREET = "street";
    public static final transient String CITY = "city";
    public static final transient String STATE = "state";
    public static final transient String ZIPCODE = "zipcode";
    public static final transient String COUNTRY = "country";
    public static final transient String TELEPHONE = "telephone";
    public static final transient String FAX = "fax";
    public static final transient String MOBILE = "mobile";
    public static final transient String EMAIL = "email";
    public static final transient String REMARK = "remark";
    public static final transient String CUSTOMER_TYPE_ID = "customer_type_id";
    public static final transient String CUSTOMER_TYPE_NAME = "customer_type_name";
    public static final transient String DISCOUNT_TEXT = "discount_text";
    public static final transient String AVAILABLE_POINTS = "available_points";
    public static final transient String BIRTHDATE = "birthdate";
    public static final transient String STATUS = "status";
    public static final transient String BIRTHDAY = "birthday";
    public static final transient String MEMBERSHIP_EXPIRED_AT = "membership_expired_at";
    public static final transient String MEMBERSHIP_START_AT = "membership_start_at";
    public static final transient String BIOMETRIC_SIGNATURE = "biometric_signature";
    public static final transient String GENDER = "gender";
    public static final transient String POINT_TO_AMOUNT_RATIO = "point_to_amount_ratio";
    public static final transient String TAX_EXEMPT = "tax_exempt";
    public static final transient String PAYMENT_TERMS_ID = "payment_terms_id";

    public static final transient String EXTRAS_CATEGORY_ID = "category_id";
    public static final transient String EXTRAS_SALESMAN_ID = "salesman_id";

    @Expose
    @DatabaseField
    private Integer point_to_amount_ratio;
    @Expose
    @DatabaseField
    private int payment_terms_id;
    @Expose
    @DatabaseField
    private String code, alternate_code, first_name, middle_name, last_name, name, company_name,
            tin, street = "", city, state, zipcode, country, telephone, fax,
            mobile, email, remark, customer_type_id, customer_type_name, discount_text,
            available_points, birthdate, status, birthday,
            membership_expired_at, membership_start_at, biometric_signature, gender;
    @Expose
    @DatabaseField
    private boolean tax_exempt;
    @DatabaseField
    private transient boolean is_favorite = false;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "price_list_id")
    private transient PriceList priceList;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "branch_id")
    private transient Branch branch;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "payment_term_id")
    private transient PaymentTerms paymentTerms;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "customer_category_id")
    private transient CustomerCategory customerCategory; // customer_type_id (?)
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "route_plan_id")
    private transient RoutePlan routePlan;
    @ForeignCollectionField(orderColumnName = "id")
    private transient ForeignCollection<Invoice> invoices;
    @ForeignCollectionField
    private transient ForeignCollection<Document> documents;
    @ForeignCollectionField
    private transient ForeignCollection<RoutePlanDetail> routePlanDetails;

    /**
     * THESE ARE FOR THE LETTER HEADER
     */
    private transient int sectionFirstPosition = -1;
    private transient boolean isHeader = false;
    private transient String letterHeader = "A";
    /** -- END --
     * THESE ARE FOR THE LETTER HEADER
     */
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "offlinedata_id")
    protected transient OfflineData offlineData;

    public Customer() { }

    public Customer(String first_name, String last_name, String name, String company_name,
                    String telephone, String mobile, String fax, String email, String street,
                    String city, String zipcode, String country, String state, String tin, String gender) {
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

    // TODO: complete all fields
    public Customer(Builder builder) {
        this.first_name = builder.first_name;
        this.last_name = builder.last_name;
        this.name = builder.name;
        this.company_name = builder.company_name;
        this.telephone = builder.telephone;
        this.mobile = builder.mobile;
        this.fax = builder.fax;
        this.email = builder.email;
        this.street = builder.street;
        this.city = builder.city;
        this.zipcode = builder.zipcode;
        this.country = builder.country;
        this.state = builder.state;
        this.tin = builder.tin;
        this.gender = builder.gender;
    }

    public Customer(String s, String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8, String s9, String s10, String s11, String s12, String gender) {
        super();
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

    public OfflineData getOfflineData() {
        return offlineData;
    }

    public void setOfflineData(OfflineData offlineData) {
        this.offlineData = offlineData;
    }

    public int getSectionFirstPosition() {
        return sectionFirstPosition;
    }

    public void setSectionFirstPosition(int sectionFirstPosition) {
        this.sectionFirstPosition = sectionFirstPosition;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setIsHeader(boolean isHeader) {
        this.isHeader = isHeader;
    }

    public String getLetterHeader() {
        return letterHeader;
    }

    public void setLetterHeader(String letterHeader) {
        this.letterHeader = letterHeader;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    public int getPayment_terms_id() {
        return payment_terms_id;
    }

    public void setPayment_terms_id(int payment_terms_id) {
        this.payment_terms_id = payment_terms_id;
    }

    public ForeignCollection<RoutePlanDetail> getRoutePlanDetails() {
        return routePlanDetails;
    }

    public void setRoutePlanDetails(ForeignCollection<RoutePlanDetail> routePlanDetails) {
        this.routePlanDetails = routePlanDetails;
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

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return new JSONObject(gson.toJson(this));
    }
    public String toJSONString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }

    public static Customer fromJSONObject(JSONObject jsonObject) throws JSONException {
        Gson gson = new Gson();
        if(jsonObject.has("customer")) {
            jsonObject = jsonObject.getJSONObject("customer");
        }
        Customer customer = gson.fromJson(jsonObject.toString(), Customer.class);
        return customer;
    }

    @Override
    public String toString() {
        return name;
    }

    public String generateFullName() {
        String name = "";
        if(first_name != null && !first_name.isEmpty()) {
            name += first_name;
        }
        if(middle_name != null && !middle_name.isEmpty()) {
            if(!name.isEmpty())
                name += " ";
            name += middle_name;
        }
        if(last_name != null && !last_name.isEmpty()) {
            if(!name.isEmpty())
                name += " ";
            name += last_name;
        }
        this.name = name;
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
            insertExtrasTo(dbHelper);
            dbHelper.insert(Customer.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            deleteExtrasTo(dbHelper);
            dbHelper.delete(Customer.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            updateExtrasTo(dbHelper);
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

    // TODO: complete all fields
    public static class Builder {
        protected Extras extras;
        protected String utc_created_at, utc_updated_at,
                code, alternate_code, first_name, last_name, name, company_name,
                tin, street, city, state, zipcode, country, telephone, fax,
                mobile, email, remark, customer_type_id, customer_type_name,
                discount_text, available_points, birthdate, status, birthday,
                membership_expired_at = "", membership_start_at = "", biometric_signature = "", gender = "";

        public Customer build() {
            return new Customer(this);
        }

        public Builder first_name(String first_name) {
            this.first_name = first_name;
            return this;
        }
        public Builder last_name(String last_name) {
            this.last_name = last_name;
            return this;
        }
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        public Builder company_name(String company_name) {
            this.company_name = company_name;
            return this;
        }
        public Builder code(String code) {
            this.code = code;
            return this;
        }
        public Builder alternate_code(String alternate_code) {
            this.alternate_code = alternate_code;
            return this;
        }
        public Builder tin(String tin) {
            this.tin = tin;
            return this;
        }
        public Builder street(String street) {
            this.street = street;
            return this;
        }
        public Builder city(String city) {
            this.city = city;
            return this;
        }
        public Builder state(String state) {
            this.state = state;
            return this;
        }
        public Builder zipcode(String zipcode) {
            this.zipcode = zipcode;
            return this;
        }
        public Builder country(String country) {
            this.country = country;
            return this;
        }
        public Builder telephone(String telephone) {
            this.telephone = telephone;
            return this;
        }
        public Builder fax(String fax) {
            this.fax = fax;
            return this;
        }
        public Builder mobile(String mobile) {
            this.mobile = mobile;
            return this;
        }
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }
        public Builder birthdate(String birthdate) {
            this.birthdate = birthdate;
            return this;
        }
        public Builder birthday(String birthday) {
            this.birthday = birthday;
            return this;
        }
        public Builder status(String status) {
            this.status = status;
            return this;
        }

    }
}