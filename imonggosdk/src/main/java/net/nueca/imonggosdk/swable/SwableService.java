package net.nueca.imonggosdk.swable;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import net.nueca.imonggosdk.operations.sync.ImonggoService;

public abstract class SwableService extends ImonggoService implements SwableConnectionHandler.OnConnectionChangedListener {

	public static final int NO_INTERNET_DELAY = 3000;
	public static final int INTERNET_DELAY = 10000;

	public interface OnConnectionChangedListener {
		void onConnectionChanged(boolean isConnected);
	}
	
	private IBinder swableLocalBinder = new SwableLocalBinder();
	private SwableConnectionHandler swableConnectionhandler;
	private OnConnectionChangedListener onConnectionChangedListener = null;
	
	private boolean shouldSync = true;
	private boolean shouldStop = false;
	private boolean isSyncing = false;
	private boolean isConnected = false;
    private boolean isReceiverRegistered = false;
	
	public abstract void syncModule();
	public abstract void updateSyncingStatus();
	public abstract void restartSyncingAndQueued();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		swableConnectionhandler = new SwableConnectionHandler();
		swableConnectionhandler.setOnConnectionChangedListener(this);
		registerReceiver(swableConnectionhandler, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        isReceiverRegistered = true;
		Log.d("onStartCommand", "Starting...");
		Thread syncThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(!isShouldStop()) {
					try {
						Thread.sleep(NO_INTERNET_DELAY); // 3 seconds
						Log.d("SwableService", "---syncing started");
						Log.d("SwableService", "---should sync? " + isShouldSync());
						while(isShouldSync()) {
							Log.d("SwableService", "---syncing data set ~ "+isSyncing());
							if (!isSyncing()) {
								Log.d("SwableService", "---syncing data called");
								//runSyncModule.sendEmptyMessage(0);
								syncModule();
							}
							updateSyncingStatus();
							Thread.sleep(INTERNET_DELAY); // 30 seconds
							Log.d("SwableService", "---syncing data");
						}
						restartSyncingAndQueued();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		syncThread.start();
		return START_STICKY;
	}

	/*public Handler runSyncModule = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			syncModule();
		}
	};*/
	
	@Override
	public void onDestroy() {
		Log.d("onDestroy", "Service is about to be destroyed!");
        if(isReceiverRegistered) {
            unregisterReceiver(swableConnectionhandler);
            isReceiverRegistered = false;
        }
		setShouldSync(false);
		setShouldStop(true);
		super.onDestroy();
	}
	
	public class SwableLocalBinder extends Binder {
		public SwableService getServerInstance() {
			return SwableService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return swableLocalBinder;
	}
	
	public void setShouldSync(boolean shouldSync) {
		this.shouldSync = shouldSync;
	}
	
	private boolean isShouldSync() {
		return shouldSync;
	}

	public void setShouldStop(boolean shouldStop) {
		this.shouldStop = shouldStop;
	}
	
	private boolean isShouldStop() {
		return shouldStop;
	}

	@Override
	public void onConnectivityChanged(boolean isConnected) {
		setConnected(isConnected);
		if(getOnConnectionChangedListener() != null)
			getOnConnectionChangedListener().onConnectionChanged(isConnected);
		if(isConnected)
			setShouldSync(true);
		else
			setShouldSync(false);
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public boolean isSyncing() {
		return isSyncing;
	}

	public void setSyncing(boolean isSyncing) {
		this.isSyncing = isSyncing;
	}

	public OnConnectionChangedListener getOnConnectionChangedListener() {
		return onConnectionChangedListener;
	}

	public void setOnConnectionChangedListener(
			OnConnectionChangedListener onConnectionChangedListener) {
		this.onConnectionChangedListener = onConnectionChangedListener;
	}

}
