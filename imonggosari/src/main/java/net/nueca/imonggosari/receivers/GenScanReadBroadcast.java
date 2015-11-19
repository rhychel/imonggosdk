package net.nueca.imonggosari.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.generalscan.NotifyStyle;
import com.generalscan.OnConnectedListener;
import com.generalscan.OnDataReceive;
import com.generalscan.OnDisconnectListener;
import com.generalscan.OnReadDataReceive;
import com.generalscan.SendConstant;
import com.generalscan.bluetooth.BluetoothConnect;
import com.generalscan.bluetooth.BluetoothSettings;

/**
 * Created by gama on 17/11/2015.
 */
public class GenScanReadBroadcast extends BroadcastReceiver {
    private ScannerListener scannerListener;
    private String scanData = "";

    public GenScanReadBroadcast() {}
    public GenScanReadBroadcast(ScannerListener scannerListener) {
        this.scannerListener = scannerListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //接受电量数据广播
        if (intent.getAction().equals(SendConstant.GetBatteryDataAction)) {

            String data = intent.getStringExtra(SendConstant.GetBatteryData);

            if(scannerListener != null)
                scannerListener.onGetBatteryData(data);
        }
        // 接收数据的广播
        if (intent.getAction().equals(SendConstant.GetDataAction)) {
            String data = intent.getStringExtra(SendConstant.GetData);

            scanData += data;
            if(scanData.contains("\n")) {
                scanData = scanData.replaceAll("\\r|\\n", "");
                if(scannerListener != null)
                    scannerListener.onGetData(scanData);
                scanData = "";
                return;
            }
        }
        // 接收发送数据的广播
        /**if (intent.getAction().equals(SendConstant.GetReadDataAction)) {
            String name = intent.getStringExtra(SendConstant.GetReadName);
            String data = intent.getStringExtra(SendConstant.GetReadData);

            // 如果接受到的是充电类型
            if (name.equals(myActivity.getString(R.string.gs_read_charge))) {
                // 获取0，1标示
                data = data.substring(7, 8);
                if (data.equals("0")) {
                    data = myActivity
                            .getString(R.string.gs_usb_charge_fast);

                } else {
                    data = myActivity
                            .getString(R.string.gs_usb_charge_normal);

                }
                ((EditText) findViewById(R.id.editText1)).append(name + ":"
                        + data);
            } else {
                ((EditText) findViewById(R.id.editText1)).append(name + ":"
                        + data);
            }
        }*/
    }

    public interface ScannerListener {
        void onGetBatteryData(String data);
        void onGetData(String data);
    }
}
