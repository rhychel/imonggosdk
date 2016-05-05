package net.nueca.concessioengine.dialogs;

import android.app.FragmentManager;
import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.Button;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.DateTimeTools;

import java.sql.SQLException;

/**
 * Created by rhymart on 7/24/15.
 * imonggosdk (c)2015
 */
public class BaseAppCompatDialog extends AppCompatDialog {

    protected boolean isButtonTapped = false;
    protected ImonggoDBHelper2 dbHelper;
    protected static int NO_THEME = -1;
    protected String date;
    protected FragmentManager fragmentManager;
    protected ConcessioModule concessioModule = ConcessioModule.NONE;

    public BaseAppCompatDialog(Context context) {
        super(context);
    }

    public BaseAppCompatDialog(Context context, int theme) {
        super(context, theme);
    }

    protected BaseAppCompatDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public void setConcessioModule(ConcessioModule concessioModule) {
        this.concessioModule = concessioModule;
    }

    /**
     * Configure the width of the dialog to acquire the desired width.
     */
    @Override
    public void show() {
        super.show();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int pxPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, metrics); // Margin
        int pxP = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, metrics); // Ideal Width

        getWindow().setLayout(pxP + pxPadding, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * Show the delivery date picker. You need to pass the button where to set the new date.
     *
     * @param button
     */
    protected void showDeliveryDatePicker(final FragmentManager fragmentManager, final Button button) {
        date = button.getText().toString();
        date = date.replaceAll("[^0-9]","-");
        String[] dateS = date.split("-");
        final DatePickerDialog deliveryDatePicker = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog datePickerDialog, int year, int monthOfYear, int dayOfMonth) {
                String date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                date = DateTimeTools.convertToDate(date, "yyyy-M-d", "yyyy-MM-dd");
                button.setText(date);
            }
        }, Integer.valueOf(dateS[0]), Integer.valueOf(dateS[1]) - 1, Integer.valueOf(dateS[2]));
        deliveryDatePicker.show(fragmentManager, "delivery_date_picker");
    }

    public Session getSession() throws SQLException {
        Session session = null;
        if(AccountTools.isLoggedIn(dbHelper))
            session = dbHelper.fetchObjectsList(Session.class).get(0);
        return session;
    }

}
