package net.nueca.concessioengine.objects;

import net.nueca.imonggosdk.objects.Unit;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public class ExtendedAttributes {

    private String brand = "";
    private String delivery_date = "";
    private String batch_no = "";

    public ExtendedAttributes() { }

    public ExtendedAttributes(String batch_no, String delivery_date, String brand) {
        this.batch_no = batch_no;
        this.delivery_date = delivery_date;
        this.brand = brand;
    }

    public ExtendedAttributes(String brand, String delivery_date) {
        this.brand = brand;
        this.delivery_date = delivery_date;
    }

    public String getBrand() {
        if(brand.equals(""))
            return "No Brand";
        return brand;
    }

    public String getBatchNo() {
        if(batch_no.equals(""))
            return "No Batch Number";
        return batch_no;
    }

    public net.nueca.imonggosdk.objects.document.ExtendedAttributes convertForDocumentLine() {
        net.nueca.imonggosdk.objects.document.ExtendedAttributes extendedAttributes = new net.nueca.imonggosdk.objects.document.ExtendedAttributes.Builder()
                .brand(getBrand())
                .delivery_date(delivery_date)
                .batch_no(batch_no)
                .build();
        return extendedAttributes;
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

    @Override
    public boolean equals(Object o) {
        ExtendedAttributes extendedAttributes = (ExtendedAttributes)o;
        return brand.equals(extendedAttributes.getBrand()) && delivery_date.equals(extendedAttributes.getDelivery_date());
    }
}
