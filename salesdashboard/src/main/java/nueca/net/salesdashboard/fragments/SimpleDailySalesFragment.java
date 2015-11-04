package nueca.net.salesdashboard.fragments;

import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nueca.imonggosdk.objects.DailySales;
import net.nueca.imonggosdk.tools.DateTimeTools;
import net.nueca.imonggosdk.tools.SettingTools;

import java.sql.SQLException;

import me.grantland.widget.AutofitHelper;
import nueca.net.salesdashboard.R;
import nueca.net.salesdashboard.enums.FormatValue;
import nueca.net.salesdashboard.tools.AnimTools;
import nueca.net.salesdashboard.tools.ImonggoSettingsTools;

import static nueca.net.salesdashboard.tools.NumberTools.RoundValue;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public class SimpleDailySalesFragment extends BaseSalesFragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swipeContainer;
    private String TAG = "SimpleDailySalesFragment";
    private TextView tvTodayDate, tvTodayTotalAmount, tvTodayQtySold, tvTodayNoOfInvoice, tvTodayAverageAmount;
    private TextView tvYesterdayDate, tvYesterdayTotalAmount, tvYesterdayQtySold, tvYesterdayNoOfInvoice, tvYesterdayAverageAmount;
    private TextView tvYesterdayDateTitle, tvTodayDateTitle;
    private TextView tvTodayQtySoldTitle, tvTodayNoOfInvoiceTitle, tvTodayAverageAmountTitle;
    private TextView tvYesterdayQtySoldTitle, tvYesterdayNoOfInvoiceTitle, tvYesterdayAverageAmountTitle;
    private FrameLayout flNotification;
    private TextView tvNotification;

    public DailySales todaySales, yesterdaySales;

    private String todayTotalAmount, todayTotalQtySold, todayTotalNoOfInvoice, todayAverageAmount;
    private String yesterdayTotalAmount, yesterdayTotalQtySold, yesterdayTotalNoOfInvoice, yesterdayAverageAmount;

    private TextView tvProgressMsg;
    private LinearLayout vProgressHud;
    private ImageView spinnerImage;
    private AnimationDrawable spinner;
    protected View dailySalesView;
    private FrameLayout frameLayout;
    private CardView cvToday;
    private CardView cvYesterday;

    private String format_unit;
    private String format_thousands_sep;
    private String format_decimal_sep;
    private String format_no_of_decimals;
    private String format_round_value;

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        updateDailySalesData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "DailySales Fragment Running..");
        dailySalesView = inflater.inflate(R.layout.daily_sales, container, false);

        setUpTextViews(dailySalesView);
        setUpSwipeRefreshView(dailySalesView);
        updateDailySalesData();

        getFormatData();

        return dailySalesView;
    }



    private void getFormatData() {
        try {
            format_unit = ImonggoSettingsTools.getFormatSettings(getHelper(), FormatValue.FORMAT_UNIT);
            format_thousands_sep = ImonggoSettingsTools.getFormatSettings(getHelper(), FormatValue.FORMAT_THOUSANDS_SEPARATOR);
            format_decimal_sep = ImonggoSettingsTools.getFormatSettings(getHelper(), FormatValue.FORMAT_DECIMAL_SEPARATOR);
            format_no_of_decimals = ImonggoSettingsTools.getFormatSettings(getHelper(), FormatValue.FORMAT_NO_OF_DECIMALS);
            format_round_value = ImonggoSettingsTools.getFormatSettings(getHelper(), FormatValue.FORMAT_ROUND_VALUE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setUpSwipeRefreshView(View view) {
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.scDaily);
        swipeContainer.setOnRefreshListener(this);
    }

    public void updateDailySalesData() {

        if (format_unit == "") {
            getFormatData();
        }

        int branch_id = Integer.parseInt(SettingTools.currentBranchId(dailySalesView.getContext()));
        String current_date = DateTimeTools.getCurrentDateTimeWithFormat("yyyy-MM-dd");
        String yesterday_date = DateTimeTools.getYesterdayDateTimeWithFormat("yyyy-MM-dd");

        String current_date_and_time = DateTimeTools.getCurrentDateTimeWithFormat("EEEE, MMM d, yyyy");
        String yesterday_date2 = DateTimeTools.getYesterdayDateTimeWithFormat("EEEE, MMM d, yyyy");

        try {
            DailySales dailySales = getDailySales(branch_id, current_date);
            String time = "";
            if (dailySales != null) {
                String temp = dailySales.getDate_requested_at();
                String[] tokens = temp.split(" ");
                time = "as of " + DateTimeTools.convertTo12HourFormat(tokens[1]);
            }
            tvTodayDate.setText(current_date_and_time + " " + time);
            tvYesterdayDate.setText(yesterday_date2);
            todaySales = dailySales;
            yesterdaySales = getDailySales(branch_id, yesterday_date);
        } catch (SQLException e) {
            e.printStackTrace();
        }


        if (todaySales != null && todaySales.getAmount() != null) {

            Log.e(TAG, "TODAY SALES: " + todaySales.getAmount());

            todayTotalAmount = String.valueOf(format_unit + RoundValue(Double.parseDouble(todaySales.getAmount()), 0));
            todayTotalQtySold = String.valueOf(RoundValue(todaySales.getQuantity(), 1));
            todayTotalNoOfInvoice = String.valueOf(todaySales.getTransaction_count());
            todayAverageAmount = String.valueOf(format_unit + RoundValue(todaySales.getAverage_amount_per_invoice(), 0));
        } else {
            todayTotalAmount = format_unit + "0";
            todayTotalQtySold = format_unit + "0";
            todayTotalNoOfInvoice = format_unit + "0";
            todayAverageAmount = format_unit + "0";
        }

        if (yesterdaySales != null && yesterdaySales.getAmount() != null) {
            Log.e(TAG, "YESTERDAY SALES: " + yesterdaySales.getAmount());
            yesterdayTotalAmount = String.valueOf(format_unit + RoundValue(Double.parseDouble(yesterdaySales.getAmount()), 0));
            yesterdayTotalQtySold = String.valueOf(RoundValue(yesterdaySales.getQuantity(), 1));
            yesterdayTotalNoOfInvoice = String.valueOf(yesterdaySales.getTransaction_count());
            yesterdayAverageAmount = String.valueOf(format_unit + RoundValue(yesterdaySales.getAverage_amount_per_invoice(), 0));
        } else {
            yesterdayTotalAmount = format_unit + "0";
            yesterdayTotalQtySold = "0";
            yesterdayTotalNoOfInvoice = "0";
            yesterdayAverageAmount = format_unit + "0";
        }

        tvTodayTotalAmount.setText(todayTotalAmount);
        tvTodayQtySold.setText(todayTotalQtySold);
        tvTodayNoOfInvoice.setText(todayTotalNoOfInvoice);
        tvTodayAverageAmount.setText(todayAverageAmount);

        tvYesterdayTotalAmount.setText(yesterdayTotalAmount);
        tvYesterdayQtySold.setText(yesterdayTotalQtySold);
        tvYesterdayNoOfInvoice.setText(yesterdayTotalNoOfInvoice);
        tvYesterdayAverageAmount.setText(yesterdayAverageAmount);

        cvToday.setVisibility(View.VISIBLE);
        cvYesterday.setVisibility(View.VISIBLE);
    }

    public void showCardViewChart() {
        lineCardViewOptions(false);
    }
    public void hideCardViewChart() {
        lineCardViewOptions(true);
    }


    private void lineCardViewOptions(Boolean toHide) {
        if (frameLayout != null) {
            if (toHide) {
                cvToday.setVisibility(View.INVISIBLE);
                cvYesterday.setVisibility(View.INVISIBLE);
            } else {
                cvToday.setVisibility(View.VISIBLE);
                cvYesterday.setVisibility(View.VISIBLE);
            }
        } else {
            Log.e(TAG, "Error! FrameLayout cannot be null");
        }
    }

    private void setUpTextViews(View view) {

        Typeface AvenirNextMedium = Typeface.createFromAsset(view.getContext().getAssets(), "fonts/AvenirNext-Medium.ttf");
        Typeface AvenirNextRegular = Typeface.createFromAsset(view.getContext().getAssets(), "fonts/AvenirNext-Regular.ttf");
        Typeface AvenirNextBold = Typeface.createFromAsset(view.getContext().getAssets(), "fonts/AvenirNext-Bold.ttf");

        frameLayout = (FrameLayout) view.findViewById(R.id.fl_dailySales);
        flNotification = (FrameLayout) view.findViewById(R.id.flNotification);
        flNotification.setVisibility(View.GONE);
        tvNotification = (TextView) view.findViewById(R.id.tvNotification);


        tvTodayDate = (TextView) view.findViewById(R.id.tvTodayDate);
        tvTodayDateTitle= (TextView) view.findViewById(R.id.tvTodayDateTitle);
        tvTodayTotalAmount = (TextView) view.findViewById(R.id.tvTodayDailySalesAmount);
        tvTodayQtySold = (TextView) view.findViewById(R.id.tvTodayQtySold);
        tvTodayQtySoldTitle = (TextView) view.findViewById(R.id.tvTodayQtySoldTitle);
        tvTodayNoOfInvoice = (TextView) view.findViewById(R.id.tvTodayNoOfInvoice);
        tvTodayNoOfInvoiceTitle = (TextView) view.findViewById(R.id.tvTodayNoOfInvoiceTitle);
        tvTodayAverageAmount = (TextView) view.findViewById(R.id.tvTodayAverageAmount);
        tvTodayAverageAmountTitle = (TextView) view.findViewById(R.id.tvTodayAverageAmountTitle);
        tvYesterdayDate = (TextView) view.findViewById(R.id.tvYesterdayDate);
        tvYesterdayDateTitle = (TextView) view.findViewById(R.id.tvYesterdayDateTitle);
        tvYesterdayTotalAmount = (TextView) view.findViewById(R.id.tvYesterdayDailySalesAmount);
        tvYesterdayQtySold = (TextView) view.findViewById(R.id.tvYesterdayQtySold);
        tvYesterdayQtySoldTitle = (TextView) view.findViewById(R.id.tvYesterdayQtySoldTitle);
        tvYesterdayNoOfInvoice = (TextView) view.findViewById(R.id.tvYesterdayNoOfInvoice);
        tvYesterdayNoOfInvoiceTitle = (TextView) view.findViewById(R.id.tvYesterdayNoOfInvoiceTitle);
        tvYesterdayAverageAmount = (TextView) view.findViewById(R.id.tvYesterdayAverageAmount);
        tvYesterdayAverageAmountTitle = (TextView) view.findViewById(R.id.tvYesterdayAverageAmountTitle);

        tvProgressMsg = (TextView) view.findViewById(R.id.message);
        tvProgressMsg.setText("Please Wait...");

        vProgressHud = (LinearLayout) view.findViewById(R.id.progress_hud_all);
        vProgressHud.setVisibility(View.INVISIBLE);

        cvToday = (CardView) view.findViewById(R.id.cvToday);
        cvYesterday = (CardView) view.findViewById(R.id.cvYesterday);

        cvToday.setVisibility(View.INVISIBLE);
        cvYesterday.setVisibility(View.INVISIBLE);

        AutofitHelper.create(tvTodayTotalAmount);
        AutofitHelper.create(tvYesterdayTotalAmount);
        AutofitHelper.create(tvTodayAverageAmount);
        AutofitHelper.create(tvYesterdayAverageAmount);

        tvTodayTotalAmount.setText("$0");
        tvYesterdayTotalAmount.setText("$0");

        tvYesterdayDateTitle.setTypeface(AvenirNextMedium);
        tvTodayDateTitle.setTypeface(AvenirNextMedium);
        tvYesterdayDateTitle.setTextSize(24);
        tvTodayDateTitle.setTextSize(24);

        tvTodayDate.setTypeface(AvenirNextRegular);
        tvTodayDate.setTextSize(14);
        tvYesterdayDate.setTypeface(AvenirNextRegular);
        tvYesterdayDate.setTextSize(14);

        tvTodayTotalAmount.setTypeface(AvenirNextBold);
        tvYesterdayTotalAmount.setTypeface(AvenirNextBold);
        tvTodayTotalAmount.setTextSize(48);
        tvYesterdayTotalAmount.setTextSize(48);

        tvTodayQtySold.setTypeface(AvenirNextBold);
        tvTodayQtySold.setTextSize(27);
        tvTodayNoOfInvoice.setTypeface(AvenirNextBold);
        tvTodayNoOfInvoice.setTextSize(27);
        tvTodayAverageAmount.setTypeface(AvenirNextBold);
        tvTodayAverageAmount.setTextSize(27);

        tvTodayQtySoldTitle.setTypeface(AvenirNextRegular);
        tvTodayQtySoldTitle.setTextSize(10);
        tvTodayNoOfInvoiceTitle.setTypeface(AvenirNextRegular);
        tvTodayNoOfInvoiceTitle.setTextSize(10);
        tvTodayAverageAmountTitle.setTypeface(AvenirNextRegular);
        tvTodayAverageAmountTitle.setTextSize(10);

        tvYesterdayQtySold.setTypeface(AvenirNextBold);
        tvYesterdayQtySold.setTextSize(27);
        tvYesterdayNoOfInvoice.setTypeface(AvenirNextBold);
        tvYesterdayNoOfInvoice.setTextSize(27);
        tvYesterdayAverageAmount.setTypeface(AvenirNextBold);
        tvYesterdayAverageAmount.setTextSize(27);

        tvYesterdayQtySoldTitle.setTypeface(AvenirNextRegular);
        tvYesterdayQtySoldTitle.setTextSize(10);
        tvYesterdayNoOfInvoiceTitle.setTypeface(AvenirNextRegular);
        tvYesterdayNoOfInvoiceTitle.setTextSize(10);
        tvYesterdayAverageAmountTitle.setTypeface(AvenirNextRegular);
        tvYesterdayAverageAmountTitle.setTextSize(10);

        //getSalesRefreshListener().getCardViewTodayTextViewDate(tvTodayDate);
       // getSalesRefreshListener().getCardViewYesterdayTextViewDate(tvYesterdayDate);
    }

    public Boolean isProgressHudVisible() {
        return vProgressHud.getVisibility() == View.VISIBLE;
    }

    private void showProgressHud(boolean show) {
        Log.e(TAG, "progressHudOptions: " + show);
        if (show) {
            if((vProgressHud.getVisibility() == View.INVISIBLE || vProgressHud.getVisibility() == View.GONE)) {
                vProgressHud.setVisibility(View.VISIBLE);
                AnimTools.fadeInAndShowImage(vProgressHud,500);
                startProgressHudAnimation();
            }

        } else {
            if (vProgressHud.getVisibility() == View.VISIBLE) {
                vProgressHud.setVisibility(View.INVISIBLE);
                AnimTools.fadeOutAndHideImage(vProgressHud, 250);
            }
        }
    }

    private void startProgressHudAnimation() {
        {
            if (dailySalesView != null) {
                spinnerImage = (ImageView) dailySalesView.findViewById(R.id.spinnerImageView);
                spinner = (AnimationDrawable) spinnerImage.getBackground();
                spinner.start();
            } else {
                Log.e(TAG, "Cannot stop progress hud animation, view is empty");
            }
        }
    }

    private void stopProgressHudAnimation() {
        if (spinner != null) {
            spinner.stop();
        }
    }


    public void showProgressHUD() {
        Log.e(TAG, "show progress hud");
        showProgressHud(true);
    }

    public void hideProgressHUD() {
        Log.e(TAG, "hide progress hud");
        showProgressHud(false);
    }

    @Override
    public void onRefresh() {
        isRefreshing(swipeContainer);
    }

    @Override
    public void isRefreshing(SwipeRefreshLayout swipeRefreshLayout) {

    }

    public TextView getTodayDate() {
        return tvTodayDate;
    }

    public void setTodayDate(TextView tvTodayDate) {
        this.tvTodayDate = tvTodayDate;
    }

    public TextView getYesterdayDate() {
        return tvYesterdayDate;
    }

    public void setYesterdayDate(TextView tvYesterdayDate) {
        this.tvYesterdayDate = tvYesterdayDate;
    }

    public void showNotificationMessage(String message, Boolean isRefreshing){
        if(isRefreshing) {
            if ((flNotification.getVisibility() == View.INVISIBLE || flNotification.getVisibility() == View.GONE)) {
                flNotification.setVisibility(View.VISIBLE);
                tvNotification.setText(message);
                AnimTools.fadeInAndShowImage(flNotification, 1000);
            }
        }
    }

    public void hideNotification(Boolean isRefreshing){

        Log.e(TAG, "hide notification");
        if(isRefreshing) {
            if (flNotification.getVisibility() == View.VISIBLE) {
                flNotification.setVisibility(View.INVISIBLE);
                AnimTools.fadeOutAndHideImage(flNotification, 2000);
            }
        }
    }
}
