package net.nueca.concessioengine.printer.epson.listener;

import java.util.HashMap;

/**
 * Created by Jn on 18/02/16.
 */
public interface DiscoveryListener {
    void onDiscovered(HashMap<String, String> printer);
    void onDiscoveryError(Exception e);
}
