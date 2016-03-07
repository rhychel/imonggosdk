package net.nueca.concessioengine.printer.epson.enums;

import android.util.Log;

import com.epson.epos2.printer.Printer;

/**
 * Created by Jn on 22/02/16.
 */
public enum EPSONLanguage {
    ENGLISH("ENGLISH", Printer.MODEL_ANK),
    JAPANESE("JAPANESE", Printer.MODEL_JAPANESE),
    CHINESE("CHINESE", Printer.MODEL_CHINESE),
    TAIWAN("TAIWAN", Printer.MODEL_TAIWAN),
    KOREAN("KOREAN", Printer.MODEL_KOREAN),
    THAI("THAI", Printer.MODEL_THAI),
    SOUTHASIA("SOUTHASIA", Printer.MODEL_SOUTHASIA),
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

