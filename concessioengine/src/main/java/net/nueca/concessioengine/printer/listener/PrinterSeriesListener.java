package net.nueca.concessioengine.printer.listener;

import net.nueca.concessioengine.printer.enums.EpsonPrinterSeries;

/**
 * Created by Jn on 01/02/16.
 */
public interface PrinterSeriesListener {
    void onPrinterModelSelected(EpsonPrinterSeries series);
}
