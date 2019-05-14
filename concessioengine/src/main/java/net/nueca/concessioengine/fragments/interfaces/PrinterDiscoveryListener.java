package net.nueca.concessioengine.fragments.interfaces;

import com.epson.epos2.discovery.DeviceInfo;

/**
 * Created by Jn on 25/01/16.
 */
public interface PrinterDiscoveryListener {
    public void onPrinterSelected(DeviceInfo deviceInfo);
    public void onPrinterDiscovered(DeviceInfo deviceInfo);
}
