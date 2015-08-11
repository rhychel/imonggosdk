package net.nueca.imonggosdk.swable;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.DrawableRes;
import android.util.Log;

/**
 * Created by gama on 6/22/15.
 */
public class ImonggoSwableServiceConnection implements ServiceConnection {
    private ImonggoSwable imonggoSwableService;
    private ImonggoSwable.SwableStateListener swableStateListener;
    private boolean isBound;
    private int notificationIcon;

    public ImonggoSwableServiceConnection(@DrawableRes int notificationIcon, ImonggoSwable.SwableStateListener
            swableStateListener) {
        this.swableStateListener = swableStateListener;
        this.notificationIcon = notificationIcon;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        SwableService.SwableLocalBinder binder = (SwableService.SwableLocalBinder)iBinder;
        imonggoSwableService = (ImonggoSwable)binder.getServerInstance();
        imonggoSwableService.setSwableStateListener(swableStateListener);

        if(notificationIcon >= -1)
            imonggoSwableService.setNotificationIcon(notificationIcon);

        isBound = true;
        Log.d("ImonggoSwableServiceCon", imonggoSwableService.getClass().getSimpleName() + " connected");
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        isBound = false;
        Log.d("ImonggoSwableServiceCon", imonggoSwableService.getClass().getSimpleName() + " disconnected");
    }

    public boolean isBound() {
        return isBound;
    }
}
