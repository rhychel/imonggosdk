package net.nueca.imonggosdk.enums;

/**
 * Created by rhymartmanchus on 17/05/2016.
 */
public enum OrderTypeCode {

    PURCHASE_ORDER("purchase_order"),
    STOCK_REQUEST("stock_request");

    String value;

    OrderTypeCode(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
