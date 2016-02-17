package net.nueca.concessioengine.printer;

import android.content.Context;
import android.util.Log;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.discovery.DeviceInfo;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.dialogs.CustomDialog;
import net.nueca.concessioengine.dialogs.DiscoverPrinterFL;
import net.nueca.concessioengine.fragments.interfaces.PrinterDiscoveryListener;
import net.nueca.concessioengine.printer.enums.EpsonPrinterSeries;
import net.nueca.concessioengine.printer.enums.PrinterInterfaceType;
import net.nueca.concessioengine.printer.enums.EpsonSelectionType;
import net.nueca.concessioengine.printer.enums.PrinterManufacturer;
import net.nueca.concessioengine.printer.listener.PrinterSeriesListener;
import net.nueca.concessioengine.printer.listener.PrinterTypeListener;
import net.nueca.concessioengine.printer.tools.PrinterTools;
import net.nueca.imonggosdk.tools.LoggingTools;

public class TestPrinter {

    private static String TAG = "TestPrinter";

    private PrinterManufacturer mManufacturer;
    private PrinterInterfaceType mEpsonPrinterType;
    private EpsonPrinterSeries mEpsonPrinterSeries;
    private static Context mContext;
    private String printData = "";

    public PrinterManufacturer getManufacturer() {
        return mManufacturer;
    }

    public void setManufacturer(PrinterManufacturer mf) {
        this.mManufacturer = mf;
    }

    public PrinterInterfaceType getEpsonPrinterType() {
        return mEpsonPrinterType;
    }

    public void setEpsonPrinterType(PrinterInterfaceType mEpsonPrinterType) {
        this.mEpsonPrinterType = mEpsonPrinterType;
    }

    public void execute(Context c) {
        mContext = c;

        //select connected printer


        selectEpsonPrinterType();
    }

    public void selectEpsonPrinterType() {
        if (mManufacturer == PrinterManufacturer.EPSON) {
            PrinterTools.showSelectEpsonPrinterTypeDialog(mContext, EpsonSelectionType.TYPE, new PrinterTypeListener() {
                @Override
                public void onPrinterTypeSelected(PrinterInterfaceType pType) {
                    if (pType != null) {
                        mEpsonPrinterType = pType;
                        selectEpsonPrinterSeries(pType);
                    } else {
                        PrinterTools.printErrorLog(1, mContext);
                    }
                }
            }, null);

        } else {
            PrinterTools.printErrorLog(0, mContext);
        }
    }

    public void selectEpsonPrinterSeries(PrinterInterfaceType printerType) {

        if (mManufacturer != null) {

            // EPSON
            if (mManufacturer == PrinterManufacturer.EPSON) {

                if (printerType != null) {
                    Log.e(TAG, "Printer Selected: " + printerType.getName());

                    if (printerType == PrinterInterfaceType.BLUETOOTH) {
                        // BLUETOOTH
                        PrinterTools.showSelectEpsonPrinterTypeDialog(mContext, EpsonSelectionType.MODEL, null, new PrinterSeriesListener() {
                            @Override
                            public void onPrinterModelSelected(EpsonPrinterSeries series) {
                                mEpsonPrinterSeries = series;

                                Log.e(TAG, "Epson Printer Type: " + mEpsonPrinterType);
                                Log.e(TAG, "Epson Printer Series: " + mEpsonPrinterSeries);

                                // start Discovery
                                startDiscoveringEpsonPrinters();
                            }
                        });


                    } else if (printerType == PrinterInterfaceType.WIFI) {
                        // WIFI
                        LoggingTools.showToast(mContext, "Epson Wifi Printer Not Yet Supported");
                    } else if (printerType == PrinterInterfaceType.USB) {
                        LoggingTools.showToast(mContext, "Epson USB Printer Not Yet Supported");
                    } else {
                        PrinterTools.printErrorLog(3, mContext);
                    }

                } else {
                    if (mContext != null) {
                        PrinterTools.printErrorLog(1, mContext);
                    }
                }
            }
            // OTHERS
            PrinterTools.printErrorLog(3, mContext);

        } else {
            if (mContext != null) {
                PrinterTools.printErrorLog(2, mContext);
            }
        }
    }

    public void startDiscoveringEpsonPrinters() {
        DiscoverPrinterFL dPrinter = new DiscoverPrinterFL(mContext);
        dPrinter.setPrinterDiscoveryListener(new PrinterDiscoveryListener() {
            @Override
            public void onPrinterSelected(DeviceInfo deviceInfo) {

            }

            @Override
            public void onPrinterDiscovered(DeviceInfo deviceInfo) {

            }
        });

        CustomDialog customDialog = new CustomDialog(mContext, R.style.DiscoverPrinter_DialogFrameLayout);
        customDialog.setTitle("Searching for Printer");
        customDialog.setContentView(dPrinter);
        customDialog.setCancelable(true);
        try {
            dPrinter.startTheDisco();
        } catch (Epos2Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            Log.e(TAG, e.getErrorStatus() + "");
        }
        customDialog.show();
    }

}
