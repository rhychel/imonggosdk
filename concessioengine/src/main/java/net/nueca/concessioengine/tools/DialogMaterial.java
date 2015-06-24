package net.nueca.concessioengine.tools;

import android.content.Context;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;

import net.nueca.imonggosdk.enums.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jn on 6/10/2015.
 * imonggosdk (c)2015
 *
 * @uses material-dialogs Library
 * by afollestad of github.com
 */
public class DialogMaterial {

    public static void showBasicNoTitle(Context context, String content, String positiveText, String negativeText, Boolean cancelable, MaterialDialog.ButtonCallback callback) {
        new MaterialDialog.Builder(context)
                .content(content)
                .positiveText(positiveText)
                .negativeText(negativeText)
                .callback(callback)
                .cancelable(cancelable)
                .show();
    }


    public static void showBasicWithTitle(Context context, String title, String content, String positiveText, String negativeText, Boolean cancelable, MaterialDialog.ButtonCallback callback) {
        new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .positiveText(positiveText)
                .negativeText(negativeText)
                .callback(callback)
                .cancelable(cancelable)
                .show();
    }

    public static MaterialDialog createProgressDialog(Context context, String title, String content, Boolean cancelable) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .progress(true, 0)
                .cancelable(cancelable)
                .build();
    }

    public static void showCustomDialog(Context context, List<Table> items, String title, Boolean cancelable) {


        List<String> modules = new ArrayList<>();

        for (Table module : items) {
            Log.i("module", module.toString());
            // TODO: support price list
            // TODO: support route plan

            if (module == Table.USERS) {
                modules.add("Users");
            } else if (module == Table.BRANCH_USERS) {
                modules.add("User Branches");
            } else if (module == Table.TAX_SETTINGS) {
                modules.add("Tax Settings");
            } else if (module == Table.PRODUCTS) {
                modules.add("Products");
            } else if (module == Table.UNITS) {
                modules.add("Units");
            } else if (module == Table.CUSTOMERS) {
                modules.add("Customers");
            } else if (module == Table.DOCUMENTS) {
                modules.add("Documents");
            }

        }

        CharSequence[] listItem = modules.toArray(new CharSequence[modules.size()]);
        Log.i("module size", modules.size()+"");
        new MaterialDialog.Builder(context)
                .title(title)
                .items(listItem)
                .cancelable(cancelable)
                .show();
    }

}
