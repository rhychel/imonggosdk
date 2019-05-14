package net.nueca.imonggosdk.enums;

import net.nueca.imonggosdk.objects.OfflineData;

/**
 * Created by gama on 6/24/15.
 */
public enum OfflineDataType {
    /** Sending **/
    SEND_ORDER      (0),
    SEND_INVOICE    (1),
    SEND_DOCUMENT   (2),
    ADD_CUSTOMER    (3),

    /** Updating **/
    //UPDATE_ORDER        (50),
    UPDATE_INVOICE      (51),
    //UPDATE_DOCUMENT     (52),
    UPDATE_CUSTOMER     (53),

    /** Deleting **/
    CANCEL_ORDER    (100),
    CANCEL_INVOICE  (101),
    CANCEL_DOCUMENT (102),
    DELETE_CUSTOMER (103),

    UNKNOWN         (404);

    OfflineDataType (int id) {
        this.id = id;
    }

    private int id;

    public int getNumericValue() {
        return id;
    }

    public static OfflineDataType identify(int i) {
        for(OfflineDataType odt : OfflineDataType.values()) {
            if(odt.getNumericValue() == i)
                return odt;
        }
        return UNKNOWN;
    }

    public boolean isVoiding() {
        switch (this) {
            case CANCEL_ORDER:
            case CANCEL_INVOICE:
            case CANCEL_DOCUMENT:
            case DELETE_CUSTOMER:
                return true;
            default:
                return false;
        }
    }
}
