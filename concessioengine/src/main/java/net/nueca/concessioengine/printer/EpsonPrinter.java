package net.nueca.concessioengine.printer;

import net.nueca.concessioengine.printer.base.BaseEpsonPrinter;
import net.nueca.concessioengine.printer.enums.EpsonPrinterSeries;
import net.nueca.concessioengine.printer.enums.PrinterInterfaceType;
import net.nueca.concessioengine.printer.listener.PrinterSeriesListener;
import net.nueca.concessioengine.printer.listener.PrinterTypeListener;

/**
 * Created by Jn on 19/01/16.
 */
public class EpsonPrinter extends BaseEpsonPrinter implements PrinterSeriesListener, PrinterTypeListener {

    @Override
    public void initializePrinter() {

    }

    @Override
    public PrinterInterfaceType onSelectPrinterType() {

        return null;
    }

    @Override
    public void onStartDiscoverPrinter() {

    }

    @Override
    public void onPrinterModelSelected(EpsonPrinterSeries series) {

    }

    @Override
    public void onPrinterTypeSelected(PrinterInterfaceType pType) {

    }
}
