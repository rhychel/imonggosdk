package net.nueca.concessioengine.printer.epson.enums;

import android.util.Log;

/**
 * Created by Jn on 22/02/16.
 */
public enum EPSONLanguage {
    ENGLISH("ENGLISH", 0),
    JAPANESE("JAPANESE", 1),
    CHINESE("CHINESE", 2),
    TAIWAN("TAIWAN", 3),
    KOREAN("KOREAN", 4),
    THAI("THAI", 5),
    SOUTHASIA("SOUTHASIA", 6),
    NOT_SUPPORTED("NOT SUPPORTED", -1);

    private static String TAG = "EPSONLanguage";
    private String name;
    private int language;

    EPSONLanguage(String name, int language) {
        this.name = name;
        this.language = language;
    }

    public static EPSONLanguage getLanguageTypeByName(String name) {
        for (EPSONLanguage p : EPSONLanguage.values()) {
            if (p.getName().equals(name)) {
                Log.e(TAG, "Found! " + p.getName());
                return p;
            }
        }
        return NOT_SUPPORTED;
    }

    public int getLanguage() {
        return language;
    }

    public String getName() {
        return name;
    }
}

