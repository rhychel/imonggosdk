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
    public ImonggoSwableServiceConnection(ImonggoSwable.SwableStateListener
                                                  swableStateListener) {
        this.swableStateListener = swableStateListener;
        //this.notificationIcon = notificationIcon;
    }
    public ImonggoSwableServiceConnection(int notificationIcon, ImonggoSwable.SwableStateListener swableStateListener) {
        this.swableStateListener = swableStateListener;
        this.notificationIcon = notificationIcon;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        SwableService.SwableLocalBinder binder = (SwableService.SwableLocalBinder)iBinder;
        imonggoSwableService = (ImonggoSwable)binder.getServerInstance();
        imonggoSwableService.setSwableStateListener(swableStateListener);

//        if(notificationIcon >= -1)
//            imonggoSwableService.setNotificationIcon(notificationIcon);

        isBound = true;
        Log.e("ImonggoSwableServiceCon", imonggoSwableService.getClass().getSimpleName() + " connected");
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        isBound = false;
        Log.e("ImonggoSwableServiceCon", imonggoSwableService.getClass().getSimpleName() + " disconnected");
    }

    public boolean isBound() {
        return isBound;
    }

    public boolean forceStart() {
        if(!isBound || imonggoSwableService == null) {
            Log.e(this.getClass().getSimpleName(), "not yet bound to a service");
            return false;
        }
        Log.e(this.getClass().getSimpleName(), "Force Starting Swable Service method syncModule()");
        imonggoSwableService.syncModule();
        return true;
    }
}
