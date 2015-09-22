package net.nueca.concessioengine.objects;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.objects.Unit;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public class ExtendedAttributes {

    private String brand = "";
    private String delivery_date = "";
    private String batch_no = "";

    protected String outright_return;
    protected String discrepancy;

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

    public ExtendedAttributes(Double outright_return, Double discrepancy) {
        if(outright_return != null)
            this.outright_return = "" + outright_return;
        if(discrepancy != null)
            this.discrepancy = "" + discrepancy;
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
        return new net.nueca.imonggosdk.objects.document.ExtendedAttributes.Builder()
                .brand(getBrand())
                .delivery_date(delivery_date)
                .batch_no(batch_no)
                .outright_return(outright_return)
                .discrepancy(discrepancy)
                .build();
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

    public String getOutright_return() {
        return outright_return;
    }

    public void setOutright_return(String outright_return) {
        this.outright_return = outright_return;
    }

    public String getDiscrepancy() {
        return discrepancy;
    }

    public void setDiscrepancy(String discrepancy) {
        this.discrepancy = discrepancy;
    }

    @Override
    public boolean equals(Object o) {
        ExtendedAttributes extendedAttributes = (ExtendedAttributes)o;
        return brand.equals(extendedAttributes.getBrand()) && delivery_date.equals(extendedAttributes.getDelivery_date());
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.toJson(this);
    }
}
