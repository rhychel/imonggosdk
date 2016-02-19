package net.nueca.concessioengine.printer.tools;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.printer.enums.EpsonPrinterSeries;
import net.nueca.concessioengine.printer.enums.EpsonSelectionType;
import net.nueca.concessioengine.printer.enums.PrinterInterfaceType;
import net.nueca.concessioengine.printer.listener.PrinterSeriesListener;
import net.nueca.concessioengine.printer.listener.PrinterTypeListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Jn on 01/02/16.
 */
public class PrinterTools {
    public static String TAG = "PrinterTools";
    public static String PRINTER = "_printer";

    public static void updatePrinter(Context context, HashMap<String,String> inputMap) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            SharedPreferences.Editor editor = preferences.edit();

            JSONObject jsonObject = new JSONObject(inputMap);
            String jsonString = jsonObject.toString();
            editor.putString(pinfo.packageName + PRINTER, jsonString);
            editor.apply();
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Key[updateIsUnlinked]", "Not Found");
            e.printStackTrace();
        }
    }

    public void getPrintersName(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            //return preferences.getBoolean(pinfo.packageName + PRINTER, true);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Key[isUnlinked]", "Not Found");
            //return true;
        }
    }


    // For Star Micronics Printers
    public static List<PortInfo> getDiscoveredPorts(PrinterInterfaceType interfaceType) {
        List<PortInfo> BTPorts;
        List<PortInfo> USBPorts;

        List<PortInfo> discoveredPortInfo = new ArrayList<>();
        //List<String> discoveredPortNames = new ArrayList<>();

        try {
            if (interfaceType == PrinterInterfaceType.BLUETOOTH || interfaceType == PrinterInterfaceType.ALL) {
                BTPorts = StarIOPort.searchPrinter("BT:");
                for (PortInfo port : BTPorts) {
                    discoveredPortInfo.add(port);
                }
            }
            if (interfaceType == PrinterInterfaceType.USB || interfaceType == PrinterInterfaceType.ALL) {
                USBPorts = StarIOPort.searchPrinter("USB:");
                for (PortInfo port : USBPorts) {
                    discoveredPortInfo.add(port);
                }
            }
            return discoveredPortInfo;
        } catch (StarIOPortException e) {
            e.printStackTrace();
        }
        return null;
    }


    // TODO: DELETE THIS UNDER

    public static void printErrorLog(int errorNumber, Context c) {
        switch (errorNumber) {
            case 0:
                Log.e(TAG, c.getString(R.string.printer_not_supported));
                break;
            case 1:
                Log.e(TAG, c.getString(R.string.printer_type_is_null));
                break;
            case 2:
                Log.e(TAG, c.getString(R.string.printer_manufacturer_is_null));
                break;
            case 3:
                Log.e(TAG, c.getString(R.string.printer_not_yet_supported));
                break;
            case 4:

                break;
            default:
                Log.e(TAG, "Error Number not recognized.");
                break;
        }
    }

    public static void showSelectEpsonPrinterModel(Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.setTitle("Select PrinterModel");
    }

    public static void showSelectEpsonPrinterTypeDialog(Context context, final EpsonSelectionType selectionType, final PrinterTypeListener pTypeListener, final PrinterSeriesListener pSeriesListener) {
        final Dialog dialog = new Dialog(context);
        String dialog_title = "Select Printer";

        dialog.setContentView(R.layout.simple_select_epson_printer);

        final RadioGroup rgPrinterType = (RadioGroup) dialog.findViewById(R.id.rgEpsonPrinter);

        if (selectionType == EpsonSelectionType.TYPE) {
            dialog_title = "Select Printer Type";

            for (PrinterInterfaceType p : PrinterInterfaceType.values()) {
                if (p != PrinterInterfaceType.NOT_SUPPORTED) {
                    RadioButton rb = new RadioButton(context);
                    rb.setText(p.getName());
                    rgPrinterType.addView(rb);
                }
            }
        }

        if (selectionType == EpsonSelectionType.MODEL) {
            dialog_title = "Select Printer Model";
            for (EpsonPrinterSeries p : EpsonPrinterSeries.values()) {
                if (p != EpsonPrinterSeries.none) {
                    RadioButton rb = new RadioButton(context);
                    rb.setText(p.getName());
                    rgPrinterType.addView(rb);
                }
            }
        }

        dialog.setTitle(dialog_title);
        rgPrinterType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) rgPrinterType.findViewById(checkedId);
                String name = rb.getText().toString();
                if (selectionType == EpsonSelectionType.TYPE) {
                    if (pTypeListener != null) {
                        pTypeListener.onPrinterTypeSelected(PrinterInterfaceType.getPrinterTypeByName(name));
                    }
                }
                if (selectionType == EpsonSelectionType.MODEL) {
                    if (pSeriesListener != null) {
                        pSeriesListener.onPrinterModelSelected(EpsonPrinterSeries.getPrinterSeriesByName(name));
                    }
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
