package net.nueca.concessioengine.enums;

/**
 * Created by gama on 31/05/2016.
 */
public enum PrinterType {
    EPSON,
    STAR_MICRONICS;

    public static PrinterType identify(int printerType_ordinal) {
        for(PrinterType type : values()) {
            if(type.ordinal() == printerType_ordinal)
                return type;
        }
        return null;
    }
}