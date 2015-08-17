package net.nueca.concessioengine.objects;

import net.nueca.imonggosdk.objects.Unit;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public class ExtendedAttributes {

    private String brand = "";
    private String delivery_date = "";

    public ExtendedAttributes() { }

    public ExtendedAttributes(String brand, String delivery_date) {
        this.brand = brand;
        this.delivery_date = delivery_date;
    }

    public String getBrand() {
        if(brand.equals(""))
            return "No Brand";
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

    @Override
    public boolean equals(Object o) {
        ExtendedAttributes extendedAttributes = (ExtendedAttributes)o;
        return brand.equals(extendedAttributes.getBrand()) && delivery_date.equals(extendedAttributes.getDelivery_date());
    }
}
