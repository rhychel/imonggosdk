package net.nueca.imonggosdk.swable;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by gama on 6/22/15.
 */
public class ImonggoSwableServiceConnection implements ServiceConnection {
    private SwableService.SwableLocalBinder binder;
    private ImonggoSwable imonggoSwableService;
    private ImonggoSwable.SwableStateListener swableStateListener;
    private boolean isBound;

    public ImonggoSwableServiceConnection(ImonggoSwable.SwableStateListener swableStateListener) {
        this.swableStateListener = swableStateListener;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        binder = (SwableService.SwableLocalBinder)iBinder;
        imonggoSwableService = (ImonggoSwable)binder.getServerInstance();
        imonggoSwableService.setSwableStateListener(swableStateListener);
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
