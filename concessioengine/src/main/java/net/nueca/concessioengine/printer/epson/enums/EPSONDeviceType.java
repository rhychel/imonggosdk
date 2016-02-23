package net.nueca.concessioengine.printer.epson.enums;

import android.util.Log;

/**
 * Created by Jn on 22/02/16.
 */
public enum EPSONDeviceType {
    PRINTER("PRINTER", 1),
    All("ALL", 0),
    DISPLAY("DISPLAY", 2),
    KEYBOARD("KEYBOARD", 3),
    SCANNER("SCANNER", 4),
    SERIAL("SERIAL", 5),
    NOT_SUPPORTED("Not Supported", -1);


    private static String TAG = "EPSONDeviceType";
    private String name;
    private int device_type;

    EPSONDeviceType(String name, int device_type) {
        this.name = name;
        this.device_type = device_type;
    }

    public static EPSONDeviceType getDeviceTypeByName(String name) {
        for (EPSONDeviceType p : EPSONDeviceType.values()) {
            if (p.getName().equals(name)) {
                Log.e(TAG, "Found! " + p.getName());
                return p;
            }
        }
        return NOT_SUPPORTED;
    }

    public int getDevice_type() {
        return device_type;
    }

    public String getName() {
        return name;
    }

}
