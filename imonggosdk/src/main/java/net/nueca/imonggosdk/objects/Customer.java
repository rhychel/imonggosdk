package net.nueca.imonggosdk.objects;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.objects.base.BaseTable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rhymart on 5/12/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class Customer extends BaseTable {

    @DatabaseField
    private int point_to_amount_ratio;
    @DatabaseField
    private String code, alternate_code, first_name, last_name, name, company_name,
            tin, street = "", city, state, zipcode, country, telephone = "", fax,
            mobile, email, remark, customer_type_id, customer_type_name, discount_text,
            available_points, birthdate, status, birthday,
            membership_expired_at = "", membership_start_at = "", biometric_signature = "", gender = "";
    @DatabaseField
    private transient String extras = "";
    @DatabaseField
    private boolean tax_exempt;
    @DatabaseField
    private transient boolean is_favorite = false;

    private Extras extra = null;

    public class Extras {
        private String checkin_count = "0", last_checkin_at = "";

        public Extras(JSONObject jsonObject) {
            try {
                if(jsonObject.has("checkin_count")) {
                    this.checkin_count = jsonObject.getString("checkin_count");
                    this.last_checkin_at = jsonObject.getString("last_checkin_at");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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

    public String getExtras() {
        return extras;
    }

    public void setExtras(String extras) {
        this.extras = extras;
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

    public void setExtra(Extras extra) {
        this.extra = extra;
    }

    public Extras getExtra() throws JSONException {
        Log.e("Extra value=", extras + "<----This is the value");
        if(extra == null) {
            if(extras.equals(""))
                extras = "{}";
            extra = new Extras(new JSONObject(extras));
        }
        return extra;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Customer) && ((Customer)o).getId() == id;
    }

    @Override
    public String toString() {
        return name;
    }
}
