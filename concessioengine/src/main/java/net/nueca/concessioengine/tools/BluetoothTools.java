package net.nueca.concessioengine.tools;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;

/**
 * Created by Jn on 19/01/16.
 */
public class BluetoothTools {

    public boolean isBluetoothSupported() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter == null;
    }

    public boolean isBluetoothEnabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled();

    }

    public int getBluetoothStatus() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET);
    }


}
