package net.nueca.concessioengine.printer.epson.listener;

import java.util.HashMap;

/**
 * Created by Jn on 18/02/16.
 */
public interface EpsonDiscoveryListener {
    void onPrinterDiscovered(HashMap<String, String> printer);
}
