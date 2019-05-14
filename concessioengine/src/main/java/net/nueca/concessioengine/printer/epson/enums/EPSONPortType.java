package net.nueca.concessioengine.printer.epson.enums;

import android.util.Log;

/**
 * Created by Jn on 31/01/16.
 */
public enum EPSONPortType {
    BLUETOOTH("BLUETOOTH", 2),
    ALL("ALL", 0),
    USB("USB", 3),
    TCP("TCP", 1),
    NOT_SUPPORTED("Not Supported", -1);

    private static String TAG = "EPSONPortType";
    private String name;
    private int portType;

    EPSONPortType(String name, int portType) {
        this.name = name;
        this.portType = portType;
    }

    public static EPSONPortType getPrinterTypeByName(String name) {
        for (EPSONPortType p : EPSONPortType.values()) {
            if (p.getName().equals(name)) {
                Log.e(TAG, "Found! " + p.getName());
                return p;
            }
        }
        return NOT_SUPPORTED;
    }

    public String getName() {
        return name;
    }

    public int getPortType() {
        return portType;
    }
}
