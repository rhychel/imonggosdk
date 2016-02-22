package net.nueca.concessioengine.printer.epson.listener;

import java.util.List;

/**
 * Created by Jn on 22/02/16.
 */
public interface OnShowDiscoveryDialog {
    void onCreateDialog(List<String> portTypes, List<String> models, List<String> series, List<String> devices, List<String> language, List<String> filter);
}
