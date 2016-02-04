package net.nueca.concessioengine.printer;

import android.widget.Toast;

import net.nueca.concessioengine.printer.base.BaseEpsonPrinter;
import net.nueca.concessioengine.printer.enums.EpsonPrinterSeries;
import net.nueca.concessioengine.printer.enums.EpsonPrinterType;
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
    public EpsonPrinterType onSelectPrinterType() {

        return null;
    }

    @Override
    public void onStartDiscoverPrinter() {

    }

    @Override
    public void onPrinterModelSelected(EpsonPrinterSeries series) {

    }

    @Override
    public void onPrinterTypeSelected(EpsonPrinterType pType) {

    }
}
