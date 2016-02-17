package net.nueca.concessioengine.printer.enums;

/**
 * Created by Jn on 16/02/16.
 */
public enum StarIOPaperSize {
    p2INCH("2inch (58mm)"),
    p3INCH("3inch (80mm)"),
    p4INCH("4inch (112mm)");

    private static String TAG = "StarIOPaperSize";
    private String stringName;

    StarIOPaperSize(String name) {
        this.stringName = name;
    }

    public String getStringName() {
        return this.stringName;
    }
}
