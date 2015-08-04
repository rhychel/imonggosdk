package net.nueca.imonggosdk.objects.document;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gama on 7/20/15.
 */
public class ExtendedAttributes {
    protected String delivery_date;
    protected String brand;

    protected ExtendedAttributes(Builder builder) {
        delivery_date = builder.delivery_date;
        brand = builder.brand;
    }

    public String getDelivery_date() {
        return delivery_date;
    }

    public void setDelivery_date(String delivery_date) {
        this.delivery_date = delivery_date;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new Gson();
        return new JSONObject(gson.toJson(this));
    }

    public static class Builder {
        protected String delivery_date;
        protected String brand;

        public Builder delivery_date(String delivery_date) {
            this.delivery_date = delivery_date;
            return this;
        }
        public Builder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public ExtendedAttributes build() {
            return new ExtendedAttributes(this);
        }
    }
}
