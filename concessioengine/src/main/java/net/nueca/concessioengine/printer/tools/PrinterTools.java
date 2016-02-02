package net.nueca.concessioengine.printer.tools;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.printer.enums.EpsonPrinterSeries;
import net.nueca.concessioengine.printer.enums.EpsonSelectionType;
import net.nueca.concessioengine.printer.enums.EpsonPrinterType;
import net.nueca.concessioengine.printer.listener.PrinterSeriesListener;
import net.nueca.concessioengine.printer.listener.PrinterTypeListener;
import net.nueca.imonggosdk.enums.Table;

/**
 * Created by Jn on 01/02/16.
 */
public class PrinterTools {
    public static String TAG = "PrinterTools";
    public static String PRINTER_TYPE = "PRINTER_TYPE";
    public static String PRINTER_NAME = "PRINTER_NAME";

    public static void setPrinterType(Context context, EpsonPrinterType epsonPrinterType) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(PRINTER_TYPE, epsonPrinterType == null ? 0 : epsonPrinterType.ordinal());
        editor.apply();

    }

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

        if(selectionType == EpsonSelectionType.TYPE) {
            dialog_title = "Select Printer Type";

            for (EpsonPrinterType p : EpsonPrinterType.values()) {
                if (p != EpsonPrinterType.NOT_SUPPORTED) {
                    RadioButton rb = new RadioButton(context);
                    rb.setText(p.getName());
                    rgPrinterType.addView(rb);
                }
            }
        }

        if(selectionType == EpsonSelectionType.MODEL) {
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
                if(selectionType == EpsonSelectionType.TYPE) {
                    if (pTypeListener != null) {
                        pTypeListener.onPrinterTypeSelected(EpsonPrinterType.getPrinterTypeByName(name));
                    }
                }

                if(selectionType == EpsonSelectionType.MODEL) {
                    if(pSeriesListener != null) {
                        pSeriesListener.onPrinterModelSelected(EpsonPrinterSeries.getPrinterSeriesByName(name));
                    }
                }

                dialog.dismiss();
            }
        });

        dialog.show();

    }
}
