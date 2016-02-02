package net.nueca.concessioengine.printer.enums;

/**
 * Created by Jn on 31/01/16.
 */
public enum  PrinterManufacturer {
    EPSON("EpsonPrinter"),
    OTHERS("Others");

    private String name;
    PrinterManufacturer(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
