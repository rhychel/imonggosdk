package net.nueca.imonggosdk.enums;

/**
 * Created by gama on 10/06/2016.
 */
public enum  Status {
    SAVED("S"),
    POSTED("P"),
    VOIDED("V"),

    ORDER_UNFULFILLED("U"),
    ORDER_FULFILLED("F");

    private String value;
    Status(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
