package net.nueca.concessio;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by rhymart on 7/15/15.
 * imonggosdk (c)2015
 */
public class BindThisService extends Service {

    public interface DisplayListener {
        void displayToActivity(String active);
    }

    private DisplayListener displayListener;

    public class MyBinder extends Binder {
        public BindThisService getService() {
            return BindThisService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("onBind", "this is called");
        return new MyBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("onUnbind", "this is called");
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.e("onRebind", "this is called");
        super.onRebind(intent);
    }

    public void setDisplayListener(DisplayListener displayListener) {
        this.displayListener = displayListener;
    }

    public void displayThisToActivity() {
        if(displayListener != null)
            displayListener.displayToActivity("Yahoo!");
    }
}
