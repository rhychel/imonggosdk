package net.nueca.concessioengine.printer.epson.enums;

import android.util.Log;

/**
 * Created by Jn on 22/02/16.
 */
public enum EPSONFilterType {
    FILTER_NAME("NAME", 0),
    FILTER_NONE("NONE", 1);

    private static String TAG = "EPSONFilterType";
    private String name;
    private int filter;

    EPSONFilterType(String name, int filter) {
        this.name = name;
        this.filter = filter;
    }

    public static EPSONFilterType getFilterByName(String name) {
        for (EPSONFilterType p : EPSONFilterType.values()) {
            if (p.getName().equals(name)) {
                Log.e(TAG, "Found! " + p.getName());
                return p;
            }
        }
        return FILTER_NONE;
    }

    public int getFilter() {
        return filter;
    }

    public String getName() {
        return name;
    }
}
