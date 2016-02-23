package net.nueca.concessioengine.printer.epson.listener;

import java.util.HashMap;

/**
 * Created by Jn on 22/02/16.
 */
public interface DiscoverySettingsListener {
    void onPortTypeSelected(int portType);

    void onModelTypeSelected(int model);

    void onDeviceTypeSelected(int deviceType);

    void onLanguageSelected(int language);

    void onPrinterSeriesSelected(int series);

    void onFilterTypeSelected(int filterType);

    void onPrinterDiscovered(HashMap<String, String> printer);

    void onDiscoveryError(Exception e);

    void onTargetPrinterSelected(String targetPrinter);

}
