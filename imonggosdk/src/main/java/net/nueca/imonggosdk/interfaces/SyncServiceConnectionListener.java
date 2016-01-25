package net.nueca.imonggosdk.interfaces;

import android.content.ComponentName;
import android.os.IBinder;

/**
 * Created by Jn on 20/01/16.
 */
public interface SyncServiceConnectionListener {
    void onServiceConnected(ComponentName name, IBinder service);
    void onServiceDisconnected(ComponentName name);
}
