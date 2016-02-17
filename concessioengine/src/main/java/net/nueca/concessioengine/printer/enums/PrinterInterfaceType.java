package net.nueca.concessioengine.printer.enums;

import android.util.Log;

/**
 * Created by Jn on 31/01/16.
 */
public enum PrinterInterfaceType {
    BLUETOOTH("Bluetooth"),
    WIFI("Wifi"),
    USB("USB"),
    ALL("ALL"),
    NOT_SUPPORTED("Not Supported");

    private static String TAG = "PrinterInterfaceType";
    private String name;

    PrinterInterfaceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static PrinterInterfaceType getPrinterTypeByName(String name) {
        for (PrinterInterfaceType p : PrinterInterfaceType.values()) {
            if(p.getName().equals(name)) {
                Log.e(TAG, "Found! " + p.getName());
                return p;
            }
        }

        return NOT_SUPPORTED;
    }
}
