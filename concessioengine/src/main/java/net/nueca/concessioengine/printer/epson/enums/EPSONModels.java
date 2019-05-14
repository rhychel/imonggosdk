package net.nueca.concessioengine.printer.epson.enums;

import android.util.Log;

/**
 * Created by Jn on 22/02/16.
 */
public enum EPSONModels {
    TM_PRINTER("TM PRINTER", 1),
    ALL("ALL", 0),
    INTELLIGENT("INTELLIGENT", 2),
    NOT_SUPPORTED("NOT SUPPORTED", -1);

    private static String TAG = "EPSONLanguage";
    private String name;
    private int model;

    EPSONModels(String name, int language) {
        this.name = name;
        this.model = language;
    }

    public static EPSONModels getModelByName(String name) {
        for (EPSONModels p : EPSONModels.values()) {
            if (p.getName().equals(name)) {
                Log.e(TAG, "Found! " + p.getName());
                return p;
            }
        }
        return NOT_SUPPORTED;
    }

    public int getModel() {
        return model;
    }

    public String getName() {
        return name;
    }
}

