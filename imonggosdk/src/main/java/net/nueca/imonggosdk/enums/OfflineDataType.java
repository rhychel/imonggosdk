package net.nueca.imonggosdk.enums;

import net.nueca.imonggosdk.objects.OfflineData;

/**
 * Created by gama on 6/24/15.
 */
public enum OfflineDataType {
    SEND_ORDER      (0),
    SEND_INVOICE    (1),
    SEND_DOCUMENT   (2),
    ADD_CUSTOMER    (3),

    UPDATE_CUSTOMER    (53),

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
}
