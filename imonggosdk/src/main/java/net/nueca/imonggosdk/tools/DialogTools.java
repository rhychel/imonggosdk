package net.nueca.imonggosdk.tools;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

/**
 * Created by rhymart on 8/22/15.
 * imonggosdk2 (c)2015
 */
public class DialogTools<T> {

    public interface OnItemSelected<T> {
        void itemChosen(T object);
    }

    public static final int NO_THEME = -1;
    private static int selectedIndex = 0;

    /**
     * Easy error dialog.
     * @param context
     * @param title
     * @param message
     */
    public static void showDialog(Context context, String title, String message) {
        showDialog(context, title, message, NO_THEME);
    }
    public static void showDialog(Context context, String title, String message, int theme) {
        AlertDialog.Builder dialog = (theme == NO_THEME) ? new AlertDialog.Builder(context) : new AlertDialog.Builder(context, theme);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { }
        });
        dialog.show();
    }
    public static AlertDialog showDialog(Context context, String title, String message, String btnText, DialogInterface.OnClickListener callback, int theme) {
        AlertDialog.Builder dialog = (theme == NO_THEME) ? new AlertDialog.Builder(context) : new AlertDialog.Builder(context, theme);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton(btnText, callback);
        return dialog.show();
    }

    public static void showConfirmationDialog(Context context, String title, String message,
                                              String positiveText, DialogInterface.OnClickListener positiveCallback,
                                              String negativeText) {
        showConfirmationDialog(context, title, message, positiveText, positiveCallback, negativeText, NO_THEME);
    }
    public static void showConfirmationDialog(Context context, String title, String message,
                                              String positiveText, DialogInterface.OnClickListener positiveCallback,
                                              String negativeText, int theme) {
        showConfirmationDialog(context, title, message, positiveText, positiveCallback, negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { }
        }, theme);
    }
    public static void showConfirmationDialog(Context context, String title, String message,
                                              String positiveText, DialogInterface.OnClickListener positiveCallback,
                                              String negativeText, DialogInterface.OnClickListener negativeCallback) {
        showConfirmationDialog(context, title, message, positiveText, positiveCallback, negativeText, negativeCallback, NO_THEME);
    }
    public static void showConfirmationDialog(Context context, String title, String message,
                                              String positiveText, DialogInterface.OnClickListener positiveCallback,
                                              String negativeText, DialogInterface.OnClickListener negativeCallback,
                                              int theme) {
        AlertDialog.Builder dialog = (theme == NO_THEME) ? new AlertDialog.Builder(context) : new AlertDialog.Builder(context, theme);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton(positiveText, positiveCallback);
        dialog.setNegativeButton(negativeText, negativeCallback);
        dialog.show();
    }

    public static void showSelectionDialog(Context context, final ArrayAdapter<?> objects, String positiveText,
                                           final OnItemSelected onItemSelected, String negativeText) {
        showSelectionDialog(context, objects, positiveText, onItemSelected, negativeText, NO_THEME);
    }

    public static void showSelectionDialog(Context context, final ArrayAdapter<?> objects, String positiveText,
                                           final OnItemSelected onItemSelected, String negativeText, int theme) {
        selectedIndex = 0;
        AlertDialog.Builder dialog = theme == NO_THEME ? new AlertDialog.Builder(context) : new AlertDialog.Builder(context, theme);
        dialog.setTitle("Choose branch");
        int index = 0;
        dialog.setSingleChoiceItems(objects, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int selected) {
                selectedIndex = selected;
            }
        });
        dialog.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int selected) {
                if(onItemSelected != null)
                    onItemSelected.itemChosen(objects.getItem(selectedIndex));
            }
        });
        dialog.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int selected) { }
        });
        dialog.show();
    }
}
