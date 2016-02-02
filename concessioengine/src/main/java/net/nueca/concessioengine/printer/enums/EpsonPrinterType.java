package net.nueca.concessioengine.printer.enums;

import android.util.Log;

/**
 * Created by Jn on 31/01/16.
 */
public enum EpsonPrinterType {
    BLUETOOTH("Bluetooth"),
    WIFI("Wifi"),
    USB("USB"),
    NOT_SUPPORTED("Not Supported");

    private static String TAG = "EpsonPrinterType";
    private String name;

    EpsonPrinterType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static EpsonPrinterType getPrinterTypeByName(String name) {
        for (EpsonPrinterType p : EpsonPrinterType.values()) {
            if(p.getName().equals(name)) {
                Log.e(TAG, "Found! " + p.getName());
                return p;
            }
        }

        return NOT_SUPPORTED;
    }
}
