package net.nueca.concessioengine.printer.epson.listener;

import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;

/**
 * Created by Jn on 18/02/16.
 */
public interface PrintListener {
    Printer initializePrinter();
    Printer onBuildPrintData(Printer printer);
    void onPrinterWarning(String message);
    void onPrinterReceive(Printer printerObj, int code, PrinterStatusInfo status, String printJobId);
    void onPrintError(String message);
    void onPrintSuccess();
}
