package nueca.net.salesdashboard.fragments;

import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.nueca.imonggosdk.objects.DailySales;
import net.nueca.imonggosdk.tools.SettingTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import nueca.net.salesdashboard.R;
import nueca.net.salesdashboard.enums.FormatValue;
import nueca.net.salesdashboard.tools.AnimTools;
import nueca.net.salesdashboard.tools.DateTools;
import nueca.net.salesdashboard.tools.ImonggoSettingsTools;
import nueca.net.salesdashboard.tools.LineChartValueTools;

import static nueca.net.salesdashboard.tools.NumberTools.RoundValue;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public abstract class SimpleWeekSalesFragment extends BaseSalesFragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swipeContainer = null;
    private String TAG = "SimpleWeekSalesFragment";
    private LineChartView mLineChartView;
    private LineChartData data;
    private List<String> dateComparing1;
    private List<String> dateComparing2;
    private int mViewPortLeft = 0, mViewPortRight = 6, mViewPortBottom = 0;
    private long maxViewPortTop;
    private TextView tvWeeklyTitle;
    private TextView tvSummationWeek1;
    private TextView tvSummationWeek2;
    private TextView tvProgressMsg;
    private LinearLayout vProgressHud;
    private ImageView spinnerImage;
    private AnimationDrawable spinner;
    private View boxWeek1;
    private View boxWeek2;

    private Double week1Summation = 0.0;
    private Double week2Summation = 0.0;
    protected View mWeeklySalesView;
    private FrameLayout flNotification;
    private TextView tvNotification;
    private Boolean haveData = false;

    public abstract String getWeekTitle();

    public abstract void updateWeeklySalesLineChart() throws SQLException;

    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        try {
            updateWeeklySalesLineChart();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mWeeklySalesView = inflater.inflate(R.layout.weekly_sales_for_all, container, false);

        initData(getWeekTitle());

        return mWeeklySalesView;
    }

    private void initData(String title) {

        Typeface AvenirNextMedium = Typeface.createFromAsset(mWeeklySalesView.getContext().getAssets(), "fonts/AvenirNext-Medium.ttf");
        Typeface AvenirNextBold = Typeface.createFromAsset(mWeeklySalesView.getContext().getAssets(), "fonts/AvenirNext-Bold.ttf");

        RelativeLayout relativeLayout = (RelativeLayout) mWeeklySalesView.findViewById(R.id.lcWeeklyChart);
        mLineChartView = new LineChartView(getActivity().getApplicationContext());
        mLineChartView.setVisibility(View.INVISIBLE);
        relativeLayout.addView(mLineChartView);

        tvWeeklyTitle = (TextView) mWeeklySalesView.findViewById(R.id.tVWeekly);
        tvWeeklyTitle.setText(title);
        tvWeeklyTitle.setTypeface(AvenirNextMedium);
        tvWeeklyTitle.setTextSize(24);

        flNotification = (FrameLayout) mWeeklySalesView.findViewById(R.id.flNotification);
        flNotification.setVisibility(View.GONE);
        tvNotification = (TextView) mWeeklySalesView.findViewById(R.id.tvNotification);

        tvSummationWeek1 = (TextView) mWeeklySalesView.findViewById(R.id.tvThisWeekSummation);
        tvSummationWeek2 = (TextView) mWeeklySalesView.findViewById(R.id.tvLastWeekSummation);
        tvSummationWeek1.setVisibility(View.INVISIBLE);
        tvSummationWeek1.setTypeface(AvenirNextBold);
        tvSummationWeek1.setTextSize(12);
        tvSummationWeek2.setVisibility(View.INVISIBLE);
        tvSummationWeek2.setTypeface(AvenirNextBold);
        tvSummationWeek2.setTextSize(12);

        tvProgressMsg = (TextView) mWeeklySalesView.findViewById(R.id.message);
        tvProgressMsg.setText("Please Wait...");

        vProgressHud = (LinearLayout) mWeeklySalesView.findViewById(R.id.progress_hud_all);
        vProgressHud.setVisibility(View.GONE);

        boxWeek1 = mWeeklySalesView.findViewById(R.id.boxLastWeek);
        boxWeek2 = mWeeklySalesView.findViewById(R.id.boxThisWeek);
        boxWeek1.setVisibility(View.INVISIBLE);
        boxWeek2.setVisibility(View.INVISIBLE);

        mLineChartView.setViewportCalculationEnabled(false);
        mLineChartView.setScrollEnabled(false);
        mLineChartView.setZoomEnabled(false);
        mLineChartView.setInteractive(false);
        mLineChartView.setPadding(30, 250, 10, 30);

        setUpSwipeRefreshView(mWeeklySalesView);

    }

    private void startProgressHudAnimation() {
        spinnerImage = (ImageView) mWeeklySalesView.findViewById(R.id.spinnerImageView);
        spinner = (AnimationDrawable) spinnerImage.getBackground();
        spinner.start();
    }

    public void updateSalesSummation() throws SQLException {

        boxWeek1.setVisibility(View.VISIBLE);
        boxWeek2.setVisibility(View.VISIBLE);
        tvSummationWeek1.setVisibility(View.VISIBLE);
        tvSummationWeek2.setVisibility(View.VISIBLE);

        String thisWeekSummationMessage = DateTools.getTextViewDate(dateComparing1.get(0),
                dateComparing1.get(dateComparing1.size() - 1)) + " " + ImonggoSettingsTools.getFormatSettings(getHelper(), FormatValue.FORMAT_UNIT)
                + RoundValue(week1Summation, 2);

        String lastWeekSummationMessage =
                DateTools.getMonth(dateComparing2.get(0)) + " " +
                        DateTools.getDay(dateComparing2.get(0)) + "-" +
                        DateTools.getDay(dateComparing2.get(dateComparing2.size() - 1)) + ", " +
                        DateTools.getYear(dateComparing2.get(0)) + " - " + ImonggoSettingsTools.getFormatSettings(getHelper(), FormatValue.FORMAT_UNIT) +
                        RoundValue(week2Summation, 2);

        tvSummationWeek1.setText(thisWeekSummationMessage);
        tvSummationWeek2.setText(lastWeekSummationMessage);
        Log.e(TAG, thisWeekSummationMessage);
        Log.e(TAG, lastWeekSummationMessage);
    }

    public void generateData() {
        maxViewPortTop = 0;
        week1Summation = 0.0;
        week2Summation = 0.0;
        int branch_id = Integer.parseInt(SettingTools.currentBranchId(getActivity().getApplicationContext()));
        Log.e(TAG, "Updating Weekly Sales Line Chart... Branch ID:" + branch_id);

        haveData = true;

        List<PointValue> pointValueListThisWeek = new ArrayList<>();
        List<PointValue> pointValueListLastWeek = new ArrayList<>();

        if (dateComparing1 != null) {
            Log.e(TAG, "Querying This Week");
            int i = 0;
            for (String day : dateComparing1) {
                try {
                    DailySales dailySales = getDailySales(branch_id, day);
                    if (dailySales != null) {
                        week1Summation += Double.parseDouble(dailySales.getAmount());
                        Long value = (long) Math.ceil(Double.parseDouble(dailySales.getAmount()));
                        PointValue pointValueThisWeek = new PointValue(i, value);
                        pointValueListThisWeek.add(pointValueThisWeek);

                        if (value > maxViewPortTop) {
                            maxViewPortTop = value;
                        }
                    } else {
                        Log.e(TAG, "Daily Sales of " + day + " is empty.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                i++;
            }
        } else {
            Log.e(TAG, "Date This Week is empty. initialize it first.");
        }

        if (dateComparing2 != null) {
            Log.e(TAG, "Querying Last Week");
            int i = 0;
            for (String day : dateComparing2) {
                try {
                    DailySales dailySales = getDailySales(branch_id, day);
                    if (dailySales != null) {
                        week2Summation += Double.parseDouble(dailySales.getAmount());
                        long value = (long) Math.ceil(Double.parseDouble(dailySales.getAmount()));
                        PointValue pointValueLastWeek = new PointValue(i, value);
                        pointValueListLastWeek.add(pointValueLastWeek);

                        if (value > maxViewPortTop) {
                            maxViewPortTop = value;
                        }
                    } else {
                        haveData = false;
                        Log.e(TAG, "Daily Sales of " + day + " is empty.");
                        return;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                i++;
            }
        } else {
            Log.e(TAG, "Date Last Week is empty. initialize it first");
        }


        // Create List of Line
        List<Line> lineList = new ArrayList<>();

        // Add the PointValues to Line
        Line lineThisWeek = new Line(pointValueListThisWeek);
        lineThisWeek.setColor(getResources().getColor(R.color.d_line_chart_color_1));
        lineThisWeek.setPointRadius(6);

        Line lineLastWeek = new Line(pointValueListLastWeek);
        lineLastWeek.setColor(getResources().getColor(R.color.d_line_chart_color_2));
        lineLastWeek.setPointRadius(6);

        // Add the Line with Point Values
        lineList.add(lineLastWeek);
        lineList.add(lineThisWeek);

        // Create Line Chart Data and Point the Line
        data = new LineChartData(lineList);

        List<AxisValue> axisLabelsX = new ArrayList<>();
        List<AxisValue> axisLabelsY = new ArrayList<>();

        AxisValue axisValueSun = new AxisValue(0).setLabel("Sun");
        AxisValue axisValueMon = new AxisValue(1).setLabel("Mon");
        AxisValue axisValueTue = new AxisValue(2).setLabel("Tue");
        AxisValue axisValueWed = new AxisValue(3).setLabel("Wed");
        AxisValue axisValueThu = new AxisValue(4).setLabel("Thu");
        AxisValue axisValueFri = new AxisValue(5).setLabel("Fri");
        AxisValue axisValueSat = new AxisValue(6).setLabel("Sat");

        axisLabelsX.add(axisValueSun);
        axisLabelsX.add(axisValueMon);
        axisLabelsX.add(axisValueTue);
        axisLabelsX.add(axisValueWed);
        axisLabelsX.add(axisValueThu);
        axisLabelsX.add(axisValueFri);
        axisLabelsX.add(axisValueSat);

        Log.e(TAG, "Highest Value: " + maxViewPortTop);
        Long maxAdd10Percent = LineChartValueTools.add10percentLong(maxViewPortTop);
        Log.e(TAG, "Add 10%: " + maxAdd10Percent);
        Long maxAlgo = Long.valueOf(LineChartValueTools.computedMaxValue(maxAdd10Percent));
        Log.e(TAG, "Max Algo: " + maxAlgo);

        Long yValue4 = maxAlgo;
        Long yValue2 = yValue4 / 2;
        Long yValue1 = yValue2 / 2;
        Long yValue3 = yValue2 + yValue1;

        String yValue4Label = LineChartValueTools.getShortenedCurrency(yValue4);
        String yValue3Label = LineChartValueTools.getShortenedCurrency(yValue3);
        String yValue2Label = LineChartValueTools.getShortenedCurrency(yValue2);
        String yValue1Label = LineChartValueTools.getShortenedCurrency(yValue1);

        Log.e(TAG, "Value: " + maxViewPortTop + " The Max Value is: " + yValue4Label + "");
        maxViewPortTop = yValue4;

        AxisValue axisValue1 = new AxisValue(0).setLabel("$0");
        AxisValue axisValue2 = new AxisValue(yValue1).setLabel(yValue1Label);
        AxisValue axisValue3 = new AxisValue(yValue2).setLabel(yValue2Label);
        AxisValue axisValue4 = new AxisValue(yValue3).setLabel(yValue3Label);
        AxisValue axisValue5 = new AxisValue(yValue4).setLabel(yValue4Label);


        axisLabelsY.add(axisValue1);
        axisLabelsY.add(axisValue2);
        axisLabelsY.add(axisValue3);
        axisLabelsY.add(axisValue4);
        axisLabelsY.add(axisValue5);

        Axis axisY = new Axis(axisLabelsY);
        axisY.setHasLines(true);
        axisY.setHasSeparationLine(false);
        axisY.setAutoGenerated(false);
        axisY.setMaxLabelChars(5);

        Axis axisX = new Axis(axisLabelsX);
        axisX.setHasLines(false);
        axisX.setHasTiltedLabels(false);

        data.setAxisYRight(axisY);
        data.setAxisXBottom(axisX);

        data.setBaseValue(Float.NEGATIVE_INFINITY);
        mLineChartView.setLineChartData(data);

    }

    public void animateLineChart() {

    }


    public Boolean isProgressHudVisible() {
        return vProgressHud.getVisibility() == View.VISIBLE;
    }

    public void showProgressHUD() {
        progressHudOptions(true);
    }

    public void hideProgressHUD() {
        progressHudOptions(false);
    }

    private void progressHudOptions(boolean show) {
        Log.e(TAG, "progressHudOptions: " + show);
        if (show) {
            if ((vProgressHud.getVisibility() == View.INVISIBLE || vProgressHud.getVisibility() == View.GONE)) {
                vProgressHud.setVisibility(View.VISIBLE);
                AnimTools.fadeInAndShowImage(vProgressHud, 500);
                startProgressHudAnimation();
            }
        } else {
            if (vProgressHud.getVisibility() == View.VISIBLE) {
                vProgressHud.setVisibility(View.INVISIBLE);
                AnimTools.fadeOutAndHideImage(vProgressHud, 250);
            }
        }
    }

    public void showLineChart() {
        lineChartViewOptions(true);
    }

    public void hideLineChart() {
        lineChartViewOptions(false);
    }

    private void lineChartViewOptions(Boolean toHide) {
        if (!toHide) {
            tvSummationWeek1.setText("");
            tvSummationWeek2.setText("");

            boxWeek1.setVisibility(View.INVISIBLE);
            boxWeek2.setVisibility(View.INVISIBLE);
            resetViewPort(0, 6, 0, 0);
            mLineChartView.setVisibility(View.INVISIBLE);
        }

        if (toHide) {
            tvSummationWeek1.setVisibility(View.VISIBLE);
            tvSummationWeek2.setVisibility(View.VISIBLE);

            boxWeek1.setVisibility(View.VISIBLE);
            boxWeek2.setVisibility(View.VISIBLE);
            resetViewPort(mViewPortLeft, 6, maxViewPortTop, mViewPortBottom);
            mLineChartView.setVisibility(View.VISIBLE);
        }
    }

    public void resetViewPort(int left, int right, long top, int bottom) {
        final Viewport v = new Viewport(mLineChartView.getMaximumViewport());
        v.left = left;
        v.right = right;
        v.top = top;
        v.bottom = bottom;

        mLineChartView.setMaximumViewport(v);
        mLineChartView.setCurrentViewport(v);

        mLineChartView.setVisibility(View.VISIBLE);
    }

    private void setUpSwipeRefreshView(View view) {
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.scWeekly);
        swipeContainer.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        if (swipeContainer != null)
            isRefreshing(swipeContainer);
    }

    @Override
    public void isRefreshing(SwipeRefreshLayout swipeRefreshLayout) {

    }

    public List<String> getDateComparing1() {
        return dateComparing1;
    }

    public void setDateComparing1(List<String> dateComparing1) {
        this.dateComparing1 = dateComparing1;
    }

    public List<String> getDateComparing2() {
        return dateComparing2;
    }

    public void setDateComparing2(List<String> dateComparing2) {
        this.dateComparing2 = dateComparing2;
    }

    public SwipeRefreshLayout getSwipeContainer() {
        return swipeContainer;
    }

    public void setSwipeContainer(SwipeRefreshLayout swipeContainer) {
        this.swipeContainer = swipeContainer;
    }

    public LineChartView getmLineChartView() {
        return mLineChartView;
    }

    public void setmLineChartView(LineChartView mLineChartView) {
        this.mLineChartView = mLineChartView;
    }

    public String getTAG() {
        return TAG;
    }

    public void setTAG(String TAG) {
        this.TAG = TAG;
    }

    public LineChartData getData() {
        return data;
    }

    public void setData(LineChartData data) {
        this.data = data;
    }

    public int getmViewPortLeft() {
        return mViewPortLeft;
    }

    public void setmViewPortLeft(int mViewPortLeft) {
        this.mViewPortLeft = mViewPortLeft;
    }

    public int getmViewPortRight() {
        return mViewPortRight;
    }

    public void setmViewPortRight(int mViewPortRight) {
        this.mViewPortRight = mViewPortRight;
    }

    public int getmViewPortBottom() {
        return mViewPortBottom;
    }

    public void setmViewPortBottom(int mViewPortBottom) {
        this.mViewPortBottom = mViewPortBottom;
    }

    public Long getMaxViewPortTop() {
        return maxViewPortTop;
    }

    public void setMaxViewPortTop(Long maxViewPortTop) {
        this.maxViewPortTop = maxViewPortTop;
    }

    public TextView getTvWeeklyTitle() {
        return tvWeeklyTitle;
    }

    public void setTvWeeklyTitle(TextView tvWeeklyTitle) {
        this.tvWeeklyTitle = tvWeeklyTitle;
    }

    public TextView getTvSummationWeek1() {
        return tvSummationWeek1;
    }

    public void setTvSummationWeek1(TextView tvSummationWeek1) {
        this.tvSummationWeek1 = tvSummationWeek1;
    }

    public TextView getTvSummationWeek2() {
        return tvSummationWeek2;
    }

    public void setTvSummationWeek2(TextView tvSummationWeek2) {
        this.tvSummationWeek2 = tvSummationWeek2;
    }

    public TextView getTvProgressMsg() {
        return tvProgressMsg;
    }

    public void setTvProgressMsg(TextView tvProgressMsg) {
        this.tvProgressMsg = tvProgressMsg;
    }

    public LinearLayout getvProgressHud() {
        return vProgressHud;
    }

    public void setvProgressHud(LinearLayout vProgressHud) {
        this.vProgressHud = vProgressHud;
    }

    public ImageView getSpinnerImage() {
        return spinnerImage;
    }

    public void setSpinnerImage(ImageView spinnerImage) {
        this.spinnerImage = spinnerImage;
    }

    public AnimationDrawable getSpinner() {
        return spinner;
    }

    public void setSpinner(AnimationDrawable spinner) {
        this.spinner = spinner;
    }

    public Double getWeek1Summation() {
        return week1Summation;
    }

    public void setWeek1Summation(Double week1Summation) {
        this.week1Summation = week1Summation;
    }

    public Double getWeek2Summation() {
        return week2Summation;
    }

    public void setWeek2Summation(Double week2Summation) {
        this.week2Summation = week2Summation;
    }

    public View getmWeeklySalesView() {
        return mWeeklySalesView;
    }

    public void setmWeeklySalesView(View mWeeklySalesView) {
        this.mWeeklySalesView = mWeeklySalesView;
    }

    public View getBoxWeek2() {
        return boxWeek2;
    }

    public void setBoxWeek2(View boxWeek2) {
        this.boxWeek2 = boxWeek2;
    }

    public View getBoxWeek1() {
        return boxWeek1;
    }

    public void setBoxWeek1(View boxWeek1) {
        this.boxWeek1 = boxWeek1;
    }

    public Boolean HaveData() {
        return haveData;
    }

    public void setHaveData(Boolean haveData) {
        this.haveData = haveData;
    }

    public void showNotification(String message, Boolean isRefreshing){
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
