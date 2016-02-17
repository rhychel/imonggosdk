package net.nueca.concessioengine.printer.base;

import net.nueca.concessioengine.printer.enums.EpsonPrinterSeries;
import net.nueca.concessioengine.printer.enums.PrinterInterfaceType;
import net.nueca.concessioengine.printer.enums.PrinterManufacturer;

/**
 * Created by Jn on 01/02/16.
 */
public abstract class BaseEpsonPrinter {

    public static PrinterManufacturer manufacturer = PrinterManufacturer.EPSON;
    protected PrinterInterfaceType epsonPrinterType;
    protected EpsonPrinterSeries  epsonPrinterSeries;

    public abstract void initializePrinter();
    public abstract PrinterInterfaceType onSelectPrinterType();
    public abstract void onStartDiscoverPrinter();

    public PrinterInterfaceType getEpsonPrinterType() {
        return epsonPrinterType;
    }

    public void setEpsonPrinterType(PrinterInterfaceType epsonPrinterType) {
        this.epsonPrinterType = epsonPrinterType;
    }

    public EpsonPrinterSeries getEpsonPrinterSeries() {
        return epsonPrinterSeries;
    }

    public void setEpsonPrinterSeries(EpsonPrinterSeries epsonPrinterSeries) {
        this.epsonPrinterSeries = epsonPrinterSeries;
    }

    public PrinterManufacturer getManufacturer() {
        return manufacturer;
    }

    public void execute() {
        epsonPrinterType = onSelectPrinterType();
    }

    //                                                         -->  WIFI        -->
    // Select Manufacturer -->  EPSON  -->  Select PrinterType -->  USB         -->  START DISCOVERY PRINTER  -->  SAVE IT TO PREFERENCE
    //                                                         -->  BLUETOOTH   -->
    //
    //                     -->  OTHERS --> Not Yet Supported   --> NONE YET

}
