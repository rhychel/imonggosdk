package net.nueca.concessioengine.printer.listener;

import net.nueca.concessioengine.printer.enums.EpsonPrinterType;

/**
 * Created by Jn on 01/02/16.
 */
public interface PrinterTypeListener {
    void onPrinterTypeSelected(EpsonPrinterType pType);
}
