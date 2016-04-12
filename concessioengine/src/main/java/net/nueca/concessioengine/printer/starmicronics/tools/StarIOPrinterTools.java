package net.nueca.concessioengine.printer.starmicronics.tools;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.printer.starmicronics.enums.StarIOPaperSize;
import net.nueca.concessioengine.printer.starmicronics.enums.StarIOPrinterType;
import net.nueca.concessioengine.tools.BluetoothTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jn on 16/02/16.
 */
public class StarIOPrinterTools {
    private static String TAG = "StarIOPrinterTools";

    public static String TARGET_PRINTER = "_star_target_printer";
    public static String PAPER_TYPE = "_star_paper_type";

    public interface StarIOPrinterListener {
        void onPrinterSelected(PortInfo printer);
    }

    public static void showDiscoverDialog(final Context context, @NonNull StarIOPrinterListener listener) {
        showDiscoveryOptions(context, listener);
    }

    private static void showDiscoveryOptions(final Context context, final StarIOPrinterListener listener) {

        View viewSettings;
        AppCompatRadioButton rbAll, rbBluetooth, rbUSB;
        RadioGroup radioGroup;
        final StarIOPrinterType[] printerType = {StarIOPrinterType.ALL};


        MaterialDialog discoveryDialog = new MaterialDialog.Builder(context)
                .customView(R.layout.discovery_customview_starmicronics, true)
                .title(R.string.discoverPrinterTitle)
                .positiveColor(ColorStateList.valueOf(Color.GRAY))
                .positiveText("OK")
                .negativeColor(ColorStateList.valueOf(Color.GRAY))
                .negativeText("CANCEL")
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (printerType[0] == StarIOPrinterType.BLUETOOTH) {
                            if (BluetoothTools.isEnabled()) {
                                new PortDiscovery(context, listener).execute(printerType);
                                dialog.dismiss();
                            } else {
                                new MaterialDialog.Builder(context)
                                        .title(R.string.discoverPrinterTitle)
                                        .content("Please enable bluetooth in the settings")
                                        .positiveColor(ColorStateList.valueOf(Color.GRAY))
                                        .positiveText("OK")
                                        .cancelable(false)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                                                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                                ComponentName cn = new ComponentName("com.android.settings",
                                                        "com.android.settings.bluetooth.BluetoothSettings");
                                                intent.setComponent(cn);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                context.startActivity(intent);
                                            }
                                        })
                                        .show();

                            }
                        } else {
                            new PortDiscovery(context, listener).execute(printerType);
                            dialog.dismiss();
                        }
                    }
                }).build();

        viewSettings = discoveryDialog.getCustomView();
        assert discoveryDialog.getCustomView() != null;
        rbAll = (AppCompatRadioButton) viewSettings.findViewById(R.id.rbAll);
        rbUSB = (AppCompatRadioButton) viewSettings.findViewById(R.id.rbUSB);
        rbBluetooth = (AppCompatRadioButton) viewSettings.findViewById(R.id.rbBluetooth);
        radioGroup = (RadioGroup) viewSettings.findViewById(R.id.rgstarPrinterType);

        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{
                        Color.GRAY, Color.DKGRAY}
        );

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbAll) {
                    Log.e(TAG, "All selected");
                    printerType[0] = StarIOPrinterType.ALL;
                }

                if (checkedId == R.id.rbUSB) {
                    Log.e(TAG, "USB selected");
                    printerType[0] = StarIOPrinterType.USB;
                }

                if (checkedId == R.id.rbBluetooth) {
                    Log.e(TAG, "Bluetooth selected");
                    printerType[0] = StarIOPrinterType.BLUETOOTH;
                }
            }
        });

        rbAll.setSupportButtonTintList(colorStateList);
        rbUSB.setSupportButtonTintList(colorStateList);
        rbBluetooth.setSupportButtonTintList(colorStateList);

        discoveryDialog.show();
    }

    public static class PortDiscovery extends AsyncTask<StarIOPrinterType, Void, ArrayList<PortInfo>> {
        private Context mContext;
        private StarIOPrinterListener listener;

        public PortDiscovery(Context mContext, StarIOPrinterListener listener) {
            this.mContext = mContext;
            this.listener = listener;
        }

        @Override
        protected ArrayList<PortInfo> doInBackground(StarIOPrinterType... params) {
            Log.e(TAG, "doInBackground: " + params[0]);

            StarIOPrinterType type = params[0];


            Log.e(TAG, "printerType: " + type);
            List<PortInfo> BTPortList;
            List<PortInfo> TCPPortList;
            List<PortInfo> USBPortList;
            final EditText editPortName;

            final ArrayList<PortInfo> arrayDiscovery;
            ArrayList<String> arrayPortName;
            arrayDiscovery = new ArrayList<>();
            arrayPortName = new ArrayList<>();

            try {
                if (type == StarIOPrinterType.BLUETOOTH || type == StarIOPrinterType.ALL) {


                    if (BluetoothTools.isEnabled()) {
                        Log.e(TAG, "executing bluetooth...");
                        BTPortList = StarIOPort.searchPrinter("BT:");
                        for (PortInfo portInfo : BTPortList) {
                            Log.e(TAG, portInfo.getModelName());
                            arrayDiscovery.add(portInfo);
                        }
                    } else {
                        Log.e(TAG, "Cannot Search Printer.. Please Enable Bluetooth..");
                    }
                }

                Log.e(TAG, "size after bt: " + arrayDiscovery.size());

                if (type == StarIOPrinterType.USB || type == StarIOPrinterType.ALL) {
                    Log.e(TAG, "executing usb...");
                    USBPortList = StarIOPort.searchPrinter("USB:", mContext);
                    for (PortInfo portInfo : USBPortList) {
                        Log.e(TAG, portInfo.getModelName());
                        arrayDiscovery.add(portInfo);
                    }
                }

                Log.e(TAG, "size after usb: " + arrayDiscovery.size());


                for (PortInfo discovery : arrayDiscovery) {
                    String portName;

                    portName = discovery.getPortName();

                    if (!discovery.getMacAddress().equals("")) {
                        portName += "\n - " + discovery.getMacAddress();
                        if (!discovery.getModelName().equals("")) {
                            portName += "\n - " + discovery.getModelName();
                        }
                    } else if (type == StarIOPrinterType.USB || type == StarIOPrinterType.ALL) {
                        if (!discovery.getModelName().equals("")) {
                            portName += "\n - " + discovery.getModelName();
                        }
                        if (!discovery.getUSBSerialNumber().equals(" SN:")) {
                            portName += "\n - " + discovery.getUSBSerialNumber();
                        }
                    }
                    arrayPortName.add(portName);
                }

            } catch (StarIOPortException e) {
                e.printStackTrace();
            }

            return arrayDiscovery;
        }

        protected void onPostExecute(final ArrayList<PortInfo> lists) {
            // TODO: check this.exception
            // TODO: do something with the feed
            Log.e(TAG, "list size: " + lists.size());

            final List<String> list = new ArrayList<>();


            for (PortInfo p : lists) {
                Log.e(TAG, "portname:" + p.getPortName());
                list.add(p.getPortName());
            }

            for (String ports : list) {
                Log.e(TAG, "onPostExecute: " + ports);
            }

            ArrayAdapter<String> mPrinterListAdapter = new ArrayAdapter<>(mContext, R.layout.textview_printername, list);
            mPrinterListAdapter.setDropDownViewResource(R.layout.textview_printername);

            final MaterialDialog discoveryDialog = new MaterialDialog.Builder(mContext)
                    .title("Please Select Printer")
                    .customView(R.layout.discovered_printer_starmicronics, false)
                    .positiveColor(ColorStateList.valueOf(Color.GRAY))
                    .negativeColor(ColorStateList.valueOf(Color.GRAY))
                    .negativeText("CANCEL")
                    .cancelable(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Log.e(TAG, "onClick: ");


                        }
                    }).build();

            View viewDiscoveredPrinter = discoveryDialog.getCustomView();
            assert viewDiscoveredPrinter != null;
            final ListView listahan = (ListView) viewDiscoveredPrinter.findViewById(R.id.lsReceiveData);
            listahan.setAdapter(mPrinterListAdapter);
            listahan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    final PortInfo name = lists.get(position);

                    Log.e(TAG, "OnItemClick.. ");

                    new MaterialDialog.Builder(mContext)
                            .title("Are You Sure")
                            .content("You want to use " + name.getPortName())
                            .positiveText("Yes")
                            .negativeText("No")
                            .positiveColor(Color.GRAY)
                            .negativeColor(Color.GRAY)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    updateTargetPrinter(mContext, name.getPortName());
                                    if (listener != null)
                                        listener.onPrinterSelected(name);
                                    discoveryDialog.dismiss();
                                }
                            })
                            .show();
                }
            });

            discoveryDialog.show();


        }

    }

    public static void updateTargetPrinter(Context context, String targetPrinter) {
        Log.e(TAG, "Updating Target Printer: " + targetPrinter);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString(pinfo.packageName + TARGET_PRINTER, targetPrinter);
            editor.apply();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String getTargetPrinter(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            Log.e(TAG, "Target printer is " + preferences.getString(pinfo.packageName + TARGET_PRINTER, ""));
            return preferences.getString(pinfo.packageName + TARGET_PRINTER, "");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Target printer not found");
            return "";
        }
    }

    public static void updatePaperType(Context context, StarIOPaperSize paperType) {
        Log.e(TAG, "Updating Target Printer: " + paperType);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putInt(pinfo.packageName + PAPER_TYPE, paperType.ordinal());
            editor.apply();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static StarIOPaperSize getPaperType(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return StarIOPaperSize.values()[preferences.getInt(pinfo.packageName + PAPER_TYPE, 0)];
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Paper type not found");
            return StarIOPaperSize.p2INCH;
        }
    }

    /**
     * This function shows how to read the MSR data(credit card) of a portable(ESC/POS) printer. The function first puts the printer into MSR read mode, then asks the user to swipe a credit card The function waits for a response from the user. The user can cancel MSR mode or have the printer read the card.
     *
     * @param context      Activity for displaying messages to the user
     * @param portName     Port name to use for communication. This should be (TCP:<IPAddress> or BT:<Device pair name>)
     * @param portSettings Should be portable; escpos, the port settings portable; escpos is used for portable(ESC/POS) printers
     */
    public static boolean print(Context context, String portName, String portSettings, StarIOPaperSize mPaperSize, String toPrintData) {
        ArrayList<byte[]> list = new ArrayList<>();
//        if (mPaperSize == StarIOPaperSize.p2INCH) {
////            list.add(new byte[] { 0x1d, 0x57, 0x40, 0x32 }); // Page Area Setting <GS> <W> nL nH (nL = 64, nH = 2)
//            list.add(new byte[]{0x1d, 0x57, (byte) 0x80, 0x31}); // Page Area Setting <GS> <W> nL nH (nL = 128, nH = 1)
//        } else if (mPaperSize == StarIOPaperSize.p3INCH) {
//            list.add(new byte[]{0x1d, 0x57, 0x40, 0x32}); // Page Area Setting <GS> <W> nL nH (nL = 64, nH = 2)
//
//        } else if (mPaperSize == StarIOPaperSize.p4INCH) {
//            list.add(new byte[]{0x1d, 0x57, 0x40, 0x32}); // Page Area Setting <GS> <W> nL nH (nL = 64, nH = 2)
////            list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Center Justification <ESC> a n (0 Left, 1 Center, 2 Right)
//        }

        list.add(toPrintData.getBytes());
        list.add(new byte[]{0x1b, 0x64, 0x02}); // Cut
        return sendCommand(context, portName, portSettings, list);
    }

    public static boolean print(Context context, String portName, String portSettings, StarIOPaperSize mPaperSize, ArrayList<byte[]> toPrintData) {
//        ArrayList<byte[]> list = new ArrayList<>();
//        if(mPaperSize == StarIOPaperSize.p2INCH) {
//            toPrintData.add(0, new byte[] { 0x1d, 0x57, (byte) 0x80, 0x31 }); // Page Area Setting <GS> <W> nL nH (nL = 128, nH = 1)
//        } else if(mPaperSize == StarIOPaperSize.p3INCH) {
//            toPrintData.add(0, new byte[] { 0x1d, 0x57, 0x40, 0x32 }); // Page Area Setting <GS> <W> nL nH (nL = 64, nH = 2)
//
//        } else if(mPaperSize == StarIOPaperSize.p4INCH) {
//            toPrintData.add(0, new byte[] { 0x1d, 0x57, 0x40, 0x32 }); // Page Area Setting <GS> <W> nL nH (nL = 64, nH = 2)
////            list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Center Justification <ESC> a n (0 Left, 1 Center, 2 Right)
//        }

//        list.addAll(1, toPrintData);
//        list.add(toPrintData.getBytes());
        toPrintData.add(new byte[]{0x1b, 0x64, 0x02}); // Cut
        toPrintData.add(new byte[]{0x07}); // Kick cash drawer
        return sendCommand(context, portName, portSettings, toPrintData);
    }

    private static byte[] convertFromListByteArrayTobyteArray(List<byte[]> ByteArray) {
        int dataLength = 0;
        for (int i = 0; i < ByteArray.size(); i++) {
            dataLength += ByteArray.get(i).length;
        }

        int distPosition = 0;
        byte[] byteArray = new byte[dataLength];
        for (int i = 0; i < ByteArray.size(); i++) {
            System.arraycopy(ByteArray.get(i), 0, byteArray, distPosition, ByteArray.get(i).length);
            distPosition += ByteArray.get(i).length;
        }

        return byteArray;
    }

    private static boolean sendCommand(Context context, String portName, String portSettings, ArrayList<byte[]> byteList) {
        boolean result = true;
        StarIOPort port = null;
        try {
            /*
             * using StarIOPort3.1.jar (support USB Port) Android OS Version: upper 2.2
			 */
            port = StarIOPort.getPort(portName, portSettings, 1000, context);
            /*
             * using StarIOPort.jar Android OS Version: under 2.1 port = StarIOPort.getPort(portName, portSettings, 10000);
			 */
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

//            StarPrinterStatus status = port.retreiveStatus();

			/*
             * portable(ESC/POS) Printer Firmware Version 2.4 later, SM-S220i(Firmware Version 2.0 later)
			 * Using Begin / End Checked Block method for preventing "data detective".
			 * When sending large amounts of raster data, use Begin / End Checked Block method and adjust the value in the timeout in the "StarIOPort.getPort" in order to prevent "timeout" of the "endCheckedBlock method" while a printing.
			 * If receipt print is success but timeout error occurs(Show message which is "There was no response of the printer within the timeout period."), need to change value of timeout more longer in "StarIOPort.getPort" method. (e.g.) 10000 -> 30000When use "Begin / End Checked Block Sample Code", do comment out "query commands Sample code".
			 */

			/* Start of Begin / End Checked Block Sample code */
            StarPrinterStatus status = port.beginCheckedBlock();

            if (status.offline) {
                Log.e("Printer", "is offline");
                throw new StarIOPortException("A printer is offline");
            }

            byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(byteList);
            port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);

            // --- 30000
            port.setEndCheckedBlockTimeoutMillis(120000);// Change the timeout time of endCheckedBlock method.
            status = port.endCheckedBlock();

            if (status.coverOpen) {
                throw new StarIOPortException("Printer cover is open");
            } else if (status.receiptPaperEmpty) {
                throw new StarIOPortException("Receipt paper is empty");
            } else if (status.offline) {
                throw new StarIOPortException("Printer is offline");
            }
			/* End of Begin / End Checked Block Sample code */

			/*
			 * portable(ESC/POS) Printer Firmware Version 2.3 earlier
			 * Using query commands for preventing "data detective".
			 * When sending large amounts of raster data, send query commands after writePort data for confirming the end of printing and adjust the value in the timeout in the "checkPrinterSendToComplete" method in order to prevent "timeout" of the "sending query commands" while a printing.
			 * If receipt print is success but timeout error occurs(Show message which is "There was no response of the printer within the timeout period."), need to change value of timeout more longer in "checkPrinterSendToComplete" method. (e.g.) 10000 -> 30000When use "query commands Sample code", do comment out "Begin / End Checked Block Sample Code".
			 */

			/* Start of query commands Sample code */
            // byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(byteList);
            // port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);
            //
            // checkPrinterSendToComplete(port);
			/* End of query commands Sample code */
        } catch (StarIOPortException e) {
            result = false;
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setNegativeButton("Ok", null);
            AlertDialog alert = dialog.create();
            alert.setTitle("Failure");
            alert.setMessage(e.getMessage());
            alert.setCancelable(false);
//            alert.show();
        } finally {
            if (port != null) {
                try {
                    StarIOPort.releasePort(port);
                } catch (StarIOPortException e) {
                }
            }
        }

        return result;
    }
}
