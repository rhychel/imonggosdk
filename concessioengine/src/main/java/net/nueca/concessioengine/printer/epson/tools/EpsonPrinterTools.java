package net.nueca.concessioengine.printer.epson.tools;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.epson.epos2.Epos2CallbackCode;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.FilterOption;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.printer.epson.enums.EPSONDeviceType;
import net.nueca.concessioengine.printer.epson.enums.EPSONFilterType;
import net.nueca.concessioengine.printer.epson.enums.EPSONLanguage;
import net.nueca.concessioengine.printer.epson.enums.EPSONModels;
import net.nueca.concessioengine.printer.epson.enums.EPSONPortType;
import net.nueca.concessioengine.printer.epson.enums.EPSONPrinterSeries;
import net.nueca.concessioengine.printer.epson.listener.DiscoveryListener;
import net.nueca.concessioengine.printer.epson.listener.DiscoverySettingsListener;
import net.nueca.concessioengine.printer.epson.listener.PrintListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jn on 19/01/16.
 */
public class EpsonPrinterTools {

    public static String TARGET_PRINTER = "_target_printer";
    public static String PRINTER_NAME = "_printer_name";
    public static String PORT_TYPE = "_port_type";
    public static String PRINTER_LANGUAGE = "_language";
    public static String FILTER_TYPE = "_filter_type";
    public static String MODEL_TYPE = "_model_type";
    public static String DEVICE_TYPE = "_device_type";
    public static String PRINTER_SERIES = "_printer_series";
    static ArrayAdapter<String> mPrinterListAdapter = null;
    static List<String> mPrinterNameList = null;
    static List<String> mTargetPrinterList = null;
    private static String TAG = "EpsonPrinterTools";

    public static String spacer(String text1, String text2, int maxChar) {
        String finalText = text1+text2;
        int combinedLength = text1.length()+text2.length();
        if(combinedLength < maxChar) {
            int spaces = maxChar-combinedLength;
            String space = "";
            for(int i = 0;i < spaces;i++)
                space += " ";

            finalText = text1+space+text2;
        }
        return finalText;
    }

    public static String addSpace(int spaces) {
        String addSpaces = "";
        for(int i = 0;i < spaces;i++)
            addSpaces += " ";
        return addSpaces;
    }

//    Salesman: Joel Estelin Lamangan
//    ----------------------an Domingo

    public static String tabber(String textTabber, String text2, int maxChar) {
        String finalText = textTabber+text2;
        int tabSpace = textTabber.length();

        if(textTabber.length()+text2.length() <= maxChar)
            return finalText;

        finalText = "";
        String[] splitted = (textTabber+text2).split(" ");
        String append = "";
        boolean next = false;
        for(String val : splitted) {
            if(next)
                append += " ";
            if((append+val+" ").length() >= 33) {
                finalText += append+"\n";
                append = "";
                append += addSpace(tabSpace);
            }
            append += val;

            next = true;
        }
        finalText += append;

        return finalText;
    }


    public static void print(String target, final PrintListener printListener,
                             Context context) {

        final PrinterStatusInfo status;
        ReceiveListener receiveListener = new ReceiveListener() {
            @Override
            public void onPtrReceive(Printer printer, int i, PrinterStatusInfo info, String s) {
                printListener.onPrinterReceive(printer, i, info, s);
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
                    printer.sendData(30000);
                    printListener.onPrintSuccess();
                } catch (Epos2Exception e) {
                    onPrintError(printListener, "cannot send data to printer. ", e);
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
            if (printer != null) {
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
        if(Discovery.isRunning())
            Discovery.stop();
    }

    public static void updatePortType(Context context, int portType) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(pinfo.packageName + PORT_TYPE, portType);
            editor.apply();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void updateDeviceModel(Context context, int model) {
        Log.e(TAG, "Updating Device Model: " + model);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putInt(pinfo.packageName + MODEL_TYPE, model);
            editor.apply();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void updateFilterType(Context context, int filterType) {
        Log.e(TAG, "Updating Filter Type: " + filterType);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(pinfo.packageName + FILTER_TYPE, filterType);
            editor.apply();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void updateDeviceType(Context context, int deviceType) {
        Log.e(TAG, "Updating Device Type: " + deviceType);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putInt(pinfo.packageName + DEVICE_TYPE, deviceType);
            editor.apply();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void updatePrinterSeries(Context context, int series) {
        Log.e(TAG, "Update Printer Series: " + series);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putInt(pinfo.packageName + PRINTER_SERIES, series);
            editor.apply();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void updateLanguage(Context context, int language) {
        Log.e(TAG, "Update Language: " + language);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putInt(pinfo.packageName + PRINTER_LANGUAGE, language);
            editor.apply();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * Use this to get the setting of the following
     * using its Corresponding KEY:
     *         NAME     :   KEY
     *    Port Type Key : PORT_TYPE
     *     Device Model : MODEL_TYPE
     *      Device Type : DEVICE_TYPE
     *      Filter Type : FILTER_TYPE
     *   Printer Series : PRINTER_SERIES
     *         Language : LANGUAGE
     *
     * @param context a context
     * @param KEY a Key see instructions above
     * @return integer value
     */
    public static int getPrinterProperties(Context context, String KEY) {
        Log.e(TAG, "Getting Properties with KEY: " + KEY);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return preferences.getInt(KEY, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Target printer not found");
            return -1;
        }
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

            Log.e(TAG, "Printer Name: " + printer.get(EpsonPrinterTools.PRINTER_NAME));
            Log.e(TAG, "Printer Target: " + printer.get(EpsonPrinterTools.TARGET_PRINTER));

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
     * @return target printer name
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
     * @return FilterOption
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
        filterOption.setDeviceType(deviceType);
        return filterOption;
    }

    public static List<String> getPortTypes() {
        List<String> portTypes = new ArrayList<>();
        for (EPSONPortType e : EPSONPortType.values()) {
            if (e.getPortType() != -1) {
                portTypes.add(e.getName());
            }
        }
        return portTypes;
    }

    public static List<String> getModelTypes() {
        List<String> models = new ArrayList<>();

        for (EPSONModels e : EPSONModels.values()) {
            if (e.getModel() != -1)
                models.add(e.getName());
        }

        return models;
    }

    public static List<String> getPrinterSeries() {
        List<String> series = new ArrayList<>();

        for (EPSONPrinterSeries e : EPSONPrinterSeries.values()) {
            if (!e.getName().equals("none Series"))
                series.add(e.getName());
        }

        return series;
    }

    public static List<String> getDeviceTypes() {
        List<String> devices = new ArrayList<>();

        for (EPSONDeviceType e : EPSONDeviceType.values()) {
            if (e.getDevice_type() != -1) {
                devices.add(e.getName());
            }
        }
        return devices;
    }

    public static List<String> getLanguages() {
        List<String> language = new ArrayList<>();

        for (EPSONLanguage e : EPSONLanguage.values()) {
            if (e.getLanguage() != -1) {
                language.add(e.getName());
            }
        }
        return language;
    }

    public static List<String> getFilterTypes() {
        List<String> filter = new ArrayList<>();

        for (EPSONFilterType e : EPSONFilterType.values()) {
            filter.add(e.getName());
        }

        return filter;
    }


    /**
     * @param context  you may want to use 'ACTIVITY_NAME.this' because $#!t(miracle) happens.
     * @param listener
     */
    public static void showDiscoveryDialog(final Context context, final DiscoverySettingsListener listener) {

        View viewSettings;
        Spinner spPortType;
        Spinner spDeviceType;
        Spinner spModelType;
        Spinner spPrinterSeries;
        Spinner spLanguage;
        Spinner spFilterType;

        List<String> portTypes = getPortTypes();
        List<String> models = getModelTypes();
        List<String> series = getPrinterSeries();
        List<String> devices = getDeviceTypes();
        List<String> language = getLanguages();
        List<String> filter = getFilterTypes();

        ArrayAdapter<String> portTypeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, portTypes);
        portTypeAdapter.setDropDownViewResource(R.layout.textview_printername);
        ArrayAdapter<String> modelTypeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, models);
        modelTypeAdapter.setDropDownViewResource(R.layout.textview_printername);
        ArrayAdapter<String> seriesTypeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, series);
        seriesTypeAdapter.setDropDownViewResource(R.layout.textview_printername);
        ArrayAdapter<String> devicesTypeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, devices);
        devicesTypeAdapter.setDropDownViewResource(R.layout.textview_printername);
        ArrayAdapter<String> languageTypeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, language);
        languageTypeAdapter.setDropDownViewResource(R.layout.textview_printername);
        ArrayAdapter<String> filterTypeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, filter);
        filterTypeAdapter.setDropDownViewResource(R.layout.textview_printername);

        MaterialDialog discoveryDialog = new MaterialDialog.Builder(context)
                .customView(R.layout.discovery_customview, true)
                .title(R.string.discoverPrinterTitle)
                .positiveColor(ColorStateList.valueOf(Color.GRAY))
                .positiveText(R.string.discoverPrinterOk)
                .negativeColor(ColorStateList.valueOf(Color.GRAY))
                .negativeText(R.string.discoverPrinterCancel)
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dlog, @NonNull DialogAction which) {

                        View viewDiscoveredPrinter;
                        mPrinterNameList = new ArrayList<>();
                        mPrinterListAdapter = new ArrayAdapter<>(context, R.layout.textview_printername, mPrinterNameList);
                        mPrinterListAdapter.setDropDownViewResource(R.layout.textview_printername);
                        mTargetPrinterList = new ArrayList<>();

                        final MaterialDialog discoverDialog = new MaterialDialog.Builder(context)
                                .customView(R.layout.discovered_printer_customview, false)
                                .title("Printer Discovery")
                                .negativeText("Cancel")
                                .negativeColor(ColorStateList.valueOf(Color.GRAY))
                                .cancelable(false)
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        try {
                                            stopDiscovery();
                                        } catch (Epos2Exception e) {
                                            Log.e(TAG, getWarningCodeMessage(e.getErrorStatus()));
                                        }
                                    }
                                })
                                .build();

                        viewDiscoveredPrinter = discoverDialog.getCustomView();
                        assert viewDiscoveredPrinter != null;
                        final ListView list = (ListView) viewDiscoveredPrinter.findViewById(R.id.lsReceiveData);
                        list.setAdapter(mPrinterListAdapter);

                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Log.e(TAG, "OnItemClick.. ");

                                if (mPrinterNameList != null && mTargetPrinterList != null) {
                                    final String name = mPrinterNameList.get(position);
                                    final String target_printer = mTargetPrinterList.get(position);
                                    Log.e(TAG, "Selecting.. " + name + " " + target_printer);


                                    new MaterialDialog.Builder(context)
                                            .title("Are You Sure")
                                            .content("You want to use " + name)
                                            .positiveText("Yes")
                                            .negativeText("No")
                                            .positiveColor(Color.GRAY)
                                            .negativeColor(Color.GRAY)
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                                    HashMap<String, String> selectedPrinter = new HashMap<>();
                                                    selectedPrinter.put(PRINTER_NAME, name);
                                                    selectedPrinter.put(TARGET_PRINTER, target_printer);
                                                    updateTargetPrinter(context, selectedPrinter);

                                                    listener.onTargetPrinterSelected(target_printer);
                                                    try {
                                                        stopDiscovery();
                                                    } catch (Epos2Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    dialog.dismiss();
                                                    discoverDialog.dismiss();
                                                }
                                            })
                                            .show();
                                } else {
                                    Log.e(TAG, "Printer Name List is null");
                                    listener.onDiscoveryError(new Exception("Error List is null"));
                                    dlog.dismiss();
                                }

                            }
                        });


                        int portType = getPrinterProperties(context, PORT_TYPE);
                        int model = getPrinterProperties(context, MODEL_TYPE);
                        int filterType = getPrinterProperties(context, FILTER_TYPE);
                        int deviceType = getPrinterProperties(context, DEVICE_TYPE);

                        startDiscovery(context, getFilterOptions(portType, null, model, filterType, deviceType), new DiscoveryListener() {
                            @Override
                            public void onDiscovered(final HashMap<String, String> printer) {
                                Log.e(TAG, "discovered printer >>>>>");
                                Log.e(TAG, "Printer Found!");
                                Log.e(TAG, "Printer Name: " + printer.get(PRINTER_NAME));
                                Log.e(TAG, "Printer Target: " + printer.get(TARGET_PRINTER));

                                String name = printer.get(PRINTER_NAME);
                                String target_printer = printer.get(TARGET_PRINTER);

                                mPrinterNameList.add(name);
                                mTargetPrinterList.add(target_printer);

                                ((Activity)context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mPrinterListAdapter.notifyDataSetChanged();
                                    }
                                });

                                listener.onPrinterDiscovered(printer);
                            }

                            @Override
                            public void onDiscoveryError(Exception e) {
                                Log.e(TAG, "Error: " + e.toString());
                                listener.onDiscoveryError(e);
                            }
                        });
/*
                        // TODO: DELETE
                        testDiscovery(new testDisco() {
                            @Override
                            public void onDisco(HashMap<String, String> printer) {
                                String name = printer.get(PRINTER_NAME);
                                String target_printer = printer.get(TARGET_PRINTER);

                                Log.e(TAG, ">> name: " + name);

                                mPrinterNameList.add(name);
                                mTargetPrinterList.add(target_printer);
                                mPrinterListAdapter.notifyDataSetChanged();

                                Log.e(TAG, "Size: " + mPrinterNameList.size());
                            }
                        });
                        // TODO: DELETE*/

                        discoverDialog.show();

                    }
                }).build();

        viewSettings = discoveryDialog.getCustomView();

        assert viewSettings != null;
        spPortType = (Spinner) viewSettings.findViewById(R.id.spPortType);
        spPortType.setAdapter(portTypeAdapter);

        spDeviceType = (Spinner) viewSettings.findViewById(R.id.spDeviceType);
        spDeviceType.setAdapter(devicesTypeAdapter);

        spModelType = (Spinner) viewSettings.findViewById(R.id.spModel);
        spModelType.setAdapter(modelTypeAdapter);

        spPrinterSeries = (Spinner) viewSettings.findViewById(R.id.spPrinterSeries);
        spPrinterSeries.setAdapter(seriesTypeAdapter);

        spLanguage = (Spinner) viewSettings.findViewById(R.id.spLanguage);
        spLanguage.setAdapter(languageTypeAdapter);

        spFilterType = (Spinner) viewSettings.findViewById(R.id.spFilterType);
        spFilterType.setAdapter(filterTypeAdapter);


        spPortType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = parent.getItemAtPosition(position).toString();
                int portType = EPSONPortType.getPrinterTypeByName(name).getPortType();
                updatePortType(context, portType);
                listener.onPortTypeSelected(portType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // something appears out of nothing
            }
        });

        spDeviceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = parent.getItemAtPosition(position).toString();
                int deviceType = EPSONDeviceType.getDeviceTypeByName(name).getDevice_type();
                updateDeviceType(context, deviceType);
                listener.onDeviceTypeSelected(deviceType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // something appears out of nothing
            }
        });

        spModelType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = parent.getItemAtPosition(position).toString();
                int deviceModel = EPSONModels.getModelByName(name).getModel();
                updateDeviceModel(context, deviceModel);
                listener.onModelTypeSelected(deviceModel);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // something appears out nothing

            }
        });

        spPrinterSeries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = parent.getItemAtPosition(position).toString();
                int printerSeries = EPSONPrinterSeries.getPrinterSeriesByName(name).getSeries();
                updatePrinterSeries(context, printerSeries);
                listener.onPrinterSeriesSelected(printerSeries);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = parent.getItemAtPosition(position).toString();
                int language = EPSONLanguage.getLanguageTypeByName(name).getLanguage();
                updateLanguage(context, language);
                listener.onLanguageSelected(language);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spFilterType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = parent.getItemAtPosition(position).toString();
                int deviceFilter = EPSONFilterType.getFilterByName(name).getFilter();
                updateFilterType(context, deviceFilter);
                listener.onFilterTypeSelected(deviceFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // something appears out of nothing
            }
        });


        discoveryDialog.show();
    }


    // TODO: DELETE THIS
    private static void testDiscovery(final testDisco t) {


        class TimeDelay extends AsyncTask<String, String, String> {

            protected String doInBackground(String... urls) {

                for (int i = 0; i < 10000; i++) {
                    Log.e(TAG, "time: " + i);
                }
                return urls[0];
            }

            protected void onProgressUpdate(String... progress) {
                Log.e(TAG, "onProgressUpdate");
            }

            protected void onPostExecute(String result) {
                HashMap<String, String> printer = new HashMap<>();
                printer.put(TARGET_PRINTER, "BT:00:01:90:C2:AE:51");
                printer.put(PRINTER_NAME, result);
                t.onDisco(printer);
            }
        }

        new TimeDelay().execute("Printer 1");
        new TimeDelay().execute("Printer 2");
        new TimeDelay().execute("Printer 3");
        new TimeDelay().execute("Printer 4");
        new TimeDelay().execute("Printer 5");
        new TimeDelay().execute("Printer 6");
        new TimeDelay().execute("Printer 7");
        new TimeDelay().execute("Printer 8");
        new TimeDelay().execute("Printer 9");
        new TimeDelay().execute("Printer 10");
        new TimeDelay().execute("Printer 11");
        new TimeDelay().execute("Printer 12");
        new TimeDelay().execute("Printer 13");
        new TimeDelay().execute("Printer 14");
        new TimeDelay().execute("Printer 15");
    }

    /**
     * Converts EPOSException Code to Message
     *
     * @param state Error Code
     * @return exception text
     */
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

    // TODO: DELETE THIS

    /**
     * Converts EPOSException Code to Warning Message
     *
     * @param state return code
     * @return warning message
     */
    public static String getWarningCodeMessage(int state) {
        String return_text;
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


    interface testDisco {
        void onDisco(HashMap<String, String> printer);
    }
}
