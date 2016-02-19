package net.nueca.concessioengine.printer.epson.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.epson.epos2.Epos2CallbackCode;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.FilterOption;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;

import net.nueca.concessioengine.printer.epson.listener.DiscoveryListener;
import net.nueca.concessioengine.printer.epson.listener.PrintListener;

import java.util.HashMap;

/**
 * Created by Jn on 19/01/16.
 */
public class EPSONPrinterTools {

    public static String TAG = "EPSONPrinterTools";
    public static String TARGET_PRINTER = "_target_printer";
    public static String PRINTER_NAME = "_printer_name";


    public static void print(String target, final PrintListener printListener,
                             Context context) {

        final PrinterStatusInfo status;
        ReceiveListener receiveListener = new ReceiveListener() {
            @Override
            public void onPtrReceive(Printer printer, int i, PrinterStatusInfo info, String s) {
                printListener.onPrinterReceive(printer,i,info, s);
            }
        };

        Printer printer = printListener.initializePrinter();

        boolean isBeginTransaction = false;

        // initialize object
        if (printer != null) {

            // receive listener
            printer.setReceiveEventListener(receiveListener);

            // create receipt data
            printer = printListener.onBuildPrintData(printer);

            if (printer != null) {

                // connectPrinter
                try {
                    printer.connect(target, Printer.PARAM_DEFAULT);
                    printer.beginTransaction();
                    isBeginTransaction = true;
                } catch (Epos2Exception e) {
                    onPrintError(printListener, "cannot connect to printer. ", e);
                }

                // if connecting successful
                if (!isBeginTransaction) {
                    disconnectPrinter(printer);
                }

                // print
                String warningMessage = "";
                status = printer.getStatus();

                if (status.getPaper() == Printer.PAPER_NEAR_END) {
                    warningMessage += "Roll Paper is nearly end";
                }

                if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
                    warningMessage += "Battery level of printer is low";
                }

                // display warning
                printListener.onPrinterWarning(warningMessage);

                // check if printer can printer

                if (status.getConnection() == Printer.FALSE) {
                    onPrintError(printListener, "printer connection is not available");
                    disconnectPrinter(printer);
                    return;
                } else if (status.getOnline() == Printer.FALSE) {
                    onPrintError(printListener, "printer is offline");
                    disconnectPrinter(printer);
                    return;
                } else {
                    Log.e(TAG, "Printer is Available. Print should work now.");
                }


                // send data
                try {
                    printer.sendData(Printer.PARAM_DEFAULT);
                } catch (Epos2Exception e) {
                    onPrintError(printListener, "", e);
                    disconnectPrinter(printer);
                }


            } else {
                onPrintError(printListener, "printer received from onbuildPrintData is null");
                disconnectPrinter(printer);
            }
        } else {
            onPrintError(printListener, "printer not initialized");
            disconnectPrinter(printer);
        }

    }

    public static void disconnectPrinter(Printer printer) {
        try {
            if(printer != null) {
                printer.disconnect();
                printer.clearCommandBuffer();
                printer.setReceiveEventListener(null);
                printer.endTransaction();
                printer = null;
            }
        } catch (Epos2Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private static void onPrintError(PrintListener printListener, String message) {
        onPrintError(printListener, message, null);
    }

    private static void onPrintError(PrintListener printListener, String message, Epos2Exception e) {
        if (e != null) {
            printListener.onPrintError(message + getWarningCodeMessage(e.getErrorStatus()));
        } else {
            printListener.onPrintError(message);
        }
    }

    /**
     * starts Epson Discovering Printer
     *
     * @param context                a Context
     * @param filterOption           a FilterOption
     * @param epsonDiscoveryListener the Interface
     */
    public static void startDiscovery(Context context,
                                      FilterOption filterOption,
                                      final DiscoveryListener epsonDiscoveryListener) {
        Log.e(TAG, "Start Discovery...");
        com.epson.epos2.discovery.DiscoveryListener mDiscoveryListener = new com.epson.epos2.discovery.DiscoveryListener() {
            @Override
            public void onDiscovery(final DeviceInfo deviceInfo) {
                HashMap<String, String> item = new HashMap<>();
                item.put(PRINTER_NAME, deviceInfo.getDeviceName());
                item.put(TARGET_PRINTER, deviceInfo.getTarget());
                if (epsonDiscoveryListener != null) {
                    epsonDiscoveryListener.onDiscovered(item);
                }
            }
        };
        try {
            Discovery.start(context, filterOption, mDiscoveryListener);
        } catch (Exception e) {
            epsonDiscoveryListener.onDiscoveryError(e);
        }
    }

    /**
     * Stops printer discovery
     *
     * @throws Epos2Exception
     */
    public static void stopDiscovery() throws Epos2Exception {
        Discovery.stop();
    }

    /**
     * Updates the target printer in sharedPreferences
     *
     * @param context
     * @param printer
     */
    public static void updateTargetPrinter(Context context, HashMap<String, String> printer) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            SharedPreferences.Editor editor = preferences.edit();

            Log.e(TAG, "Printer Name: " + printer.get(EPSONPrinterTools.PRINTER_NAME));
            Log.e(TAG, "Printer Target: " + printer.get(EPSONPrinterTools.TARGET_PRINTER));

            editor.putString(pinfo.packageName + TARGET_PRINTER, printer.get(TARGET_PRINTER));
            editor.putString(pinfo.packageName + PRINTER_NAME, printer.get(PRINTER_NAME));
            editor.apply();
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Target printer not found.");
            e.printStackTrace();

        }
    }

    /**
     * Printer that saved in sharedPreferences
     *
     * @param context a Context
     * @return returns targetPrinter saved in sharedPreferences
     */
    public static String targetPrinter(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            Log.e(TAG, "Getting Target Printer.. Key is " + pinfo.packageName + TARGET_PRINTER);
            return preferences.getString(pinfo.packageName + TARGET_PRINTER, "");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Target printer not found");
            return "";
        }
    }

    public static String targetPrinterName(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            Log.e(TAG, "Getting Target Printer Name.. Key is " + pinfo.packageName + PRINTER_NAME);
            return preferences.getString(pinfo.packageName + PRINTER_NAME, "");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Target printer name not found");
            return "";
        }
    }

    /**
     * Creates FilterOptions based on your Input;
     *
     * @param portType     Discovery.PORTTYPE_ALL, Discovery.PORTTYPE_BLUETOOTH, Discovery.PORTTYPE_USB, Discovery.PORTTYPE_TCP
     * @param broadcast    default is "255.255.255.255"
     * @param deviceModel  Discovery.MODEL_ALL, Discovery.MODEL_TM_PRINTER, Discovery.MODEL_TM_INTELLIGENT
     * @param deviceFilter Discovery.FILTER_NONE or Discovery.FILTER_NONE
     * @param deviceType   choose from Discovery.TYPE_
     * @return FilterOption based on options
     */
    public static FilterOption getFilterOptions(@NonNull int portType,
                                                String broadcast,
                                                @NonNull int deviceModel,
                                                @NonNull int deviceFilter,
                                                @NonNull int deviceType) {
        FilterOption filterOption = new FilterOption();
        filterOption.setPortType(portType);

        if (broadcast == null) {
            String bcast = "255.255.255.255";
            filterOption.setBroadcast(bcast);
        } else {
            filterOption.setBroadcast(broadcast);
        }

        filterOption.setDeviceModel(deviceModel);
        filterOption.setEpsonFilter(deviceFilter);
        filterOption.setDeviceModel(deviceType);
        return filterOption;
    }


    private static String getEposExceptionText(int state) {
        String return_text = "";
        switch (state) {
            case Epos2Exception.ERR_PARAM:
                return_text = "ERR_PARAM";
                break;
            case Epos2Exception.ERR_CONNECT:
                return_text = "ERR_CONNECT";
                break;
            case Epos2Exception.ERR_TIMEOUT:
                return_text = "ERR_TIMEOUT";
                break;
            case Epos2Exception.ERR_MEMORY:
                return_text = "ERR_MEMORY";
                break;
            case Epos2Exception.ERR_ILLEGAL:
                return_text = "ERR_ILLEGAL";
                break;
            case Epos2Exception.ERR_PROCESSING:
                return_text = "ERR_PROCESSING";
                break;
            case Epos2Exception.ERR_NOT_FOUND:
                return_text = "ERR_NOT_FOUND";
                break;
            case Epos2Exception.ERR_IN_USE:
                return_text = "ERR_IN_USE";
                break;
            case Epos2Exception.ERR_TYPE_INVALID:
                return_text = "ERR_TYPE_INVALID";
                break;
            case Epos2Exception.ERR_DISCONNECT:
                return_text = "ERR_DISCONNECT";
                break;
            case Epos2Exception.ERR_ALREADY_OPENED:
                return_text = "ERR_ALREADY_OPENED";
                break;
            case Epos2Exception.ERR_ALREADY_USED:
                return_text = "ERR_ALREADY_USED";
                break;
            case Epos2Exception.ERR_BOX_COUNT_OVER:
                return_text = "ERR_BOX_COUNT_OVER";
                break;
            case Epos2Exception.ERR_BOX_CLIENT_OVER:
                return_text = "ERR_BOX_CLIENT_OVER";
                break;
            case Epos2Exception.ERR_UNSUPPORTED:
                return_text = "ERR_UNSUPPORTED";
                break;
            case Epos2Exception.ERR_FAILURE:
                return_text = "ERR_FAILURE";
                break;
            default:
                return_text = String.format("%d", state);
                break;
        }
        return return_text;
    }

    public static String getWarningCodeMessage(int state) {
        String return_text = "";
        switch (state) {
            case Epos2CallbackCode.CODE_SUCCESS:
                return_text = "PRINT_SUCCESS";
                break;
            case Epos2CallbackCode.CODE_PRINTING:
                return_text = "PRINTING";
                break;
            case Epos2CallbackCode.CODE_ERR_AUTORECOVER:
                return_text = "ERR_AUTORECOVER";
                break;
            case Epos2CallbackCode.CODE_ERR_COVER_OPEN:
                return_text = "ERR_COVER_OPEN";
                break;
            case Epos2CallbackCode.CODE_ERR_CUTTER:
                return_text = "ERR_CUTTER";
                break;
            case Epos2CallbackCode.CODE_ERR_MECHANICAL:
                return_text = "ERR_MECHANICAL";
                break;
            case Epos2CallbackCode.CODE_ERR_EMPTY:
                return_text = "ERR_EMPTY";
                break;
            case Epos2CallbackCode.CODE_ERR_UNRECOVERABLE:
                return_text = "ERR_UNRECOVERABLE";
                break;
            case Epos2CallbackCode.CODE_ERR_FAILURE:
                return_text = "ERR_FAILURE";
                break;
            case Epos2CallbackCode.CODE_ERR_NOT_FOUND:
                return_text = "ERR_NOT_FOUND";
                break;
            case Epos2CallbackCode.CODE_ERR_SYSTEM:
                return_text = "ERR_SYSTEM";
                break;
            case Epos2CallbackCode.CODE_ERR_PORT:
                return_text = "ERR_PORT";
                break;
            case Epos2CallbackCode.CODE_ERR_TIMEOUT:
                return_text = "ERR_TIMEOUT";
                break;
            case Epos2CallbackCode.CODE_ERR_JOB_NOT_FOUND:
                return_text = "ERR_JOB_NOT_FOUND";
                break;
            case Epos2CallbackCode.CODE_ERR_SPOOLER:
                return_text = "ERR_SPOOLER";
                break;
            case Epos2CallbackCode.CODE_ERR_BATTERY_LOW:
                return_text = "ERR_BATTERY_LOW";
                break;
            default:
                return_text = String.format("%d", state);
                break;
        }
        return return_text;
    }


    public static String getErrorMessage(PrinterStatusInfo status) {
        String msg = "";

        if (status.getOnline() == Printer.FALSE) {
            msg += "Printer is offline. ";
        }
        if (status.getConnection() == Printer.FALSE) {
            msg += "Please check the connection of the printer and the mobile terminal.\\nConnection get lost.\\n";
        }
        if (status.getCoverOpen() == Printer.TRUE) {
            msg += "Please close roll paper cover.\\n";
        }
        if (status.getPaper() == Printer.PAPER_EMPTY) {
            msg += "Please check roll paper.\\n";
        }
        if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
            msg += "Please release a paper feed switch.\\n";
        }
        if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
            msg += "Please remove jammed paper and close roll paper cover.\\nRemove any jammed paper or foreign substances in the printer, and then turn the printer off and turn the printer on again.\\n";
            msg += "Then, If the printer doesn\\'t recover from error, please cycle the power switch.\\n";
        }
        if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
            msg += "Please cycle the power switch of the printer.\\nIf same errors occurred even power cycled, the printer may out of order.";
        }
        if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
                msg += "Please wait until error LED of the printer turns off. \\n";
                msg += "Print head of printer is hot.\\n";
            }
            if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
                msg += "Motor Driver IC of printer is hot.\\n";
                msg += "Battery of printer is hot.\\n";
            }
            if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
                msg += "Motor Driver IC of printer is hot.\\n";
                msg += "Battery of printer is hot.\\n";
            }
            if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
                msg += "Please set correct roll paper.\\n";
            }
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
            msg += "Please connect AC adapter or change the battery.\\nBattery of printer is almost empty.\\n";
        }

        return msg;
    }

}
