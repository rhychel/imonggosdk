package nueca.net.salesdashboard.fragments;

import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
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
import nueca.net.salesdashboard.tools.AnimTools;
import nueca.net.salesdashboard.tools.DateTools;
import nueca.net.salesdashboard.tools.LineChartValueTools;

import static nueca.net.salesdashboard.tools.NumberTools.RoundValue;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public class SimpleMonthlySalesFragment extends BaseSalesFragment implements OnRefreshListener {
    private SwipeRefreshLayout swipeContainer = null;
    private String TAG = "SimpleMonthlySalesFragment";
    private LineChartView mLineChartView = null;
    private List<String> dateThisMonth;
    private LineChartData data;
    private long maxViewPortTop = 0;
    private int mViewPortLeft = 0, mViewPortRight = 31, mViewPortBottom = 0;
    private TextView tvMonthTitle;
    private TextView tvSummationThisMonth;
    private TextView tvProgressMsg;
    private LinearLayout vProgressHud;
    private ImageView spinnerImage;
    private AnimationDrawable spinner;
    private View boxThisMonth;
    private String thisMonthSummationMessage;
    private Double thisMonthSummation = 0.0;
    private View monthlySalesView;
    private FrameLayout flNotification;
    private TextView tvNotification;
    private Boolean haveData = false;
    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        updateMonthlySalesLineChart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "SimpleMonthlySalesFragment Fragment Running..");
        initData(inflater, container);
        try {
            generateDummyData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return monthlySalesView;
    }

    private void initData(LayoutInflater inflater, ViewGroup container) {
        monthlySalesView = inflater.inflate(R.layout.monthly_sales, container, false);
        RelativeLayout relativeLayout = (RelativeLayout) monthlySalesView.findViewById(R.id.lcMonthlyChart);

        flNotification = (FrameLayout) monthlySalesView.findViewById(R.id.flNotification);
        flNotification.setVisibility(View.GONE);
        tvNotification = (TextView) monthlySalesView.findViewById(R.id.tvNotification);

        mLineChartView = new LineChartView(getActivity().getApplicationContext());
        mLineChartView.setVisibility(View.INVISIBLE);
        relativeLayout.addView(mLineChartView);

        Typeface AvenirNextMedium = Typeface.createFromAsset(monthlySalesView.getContext().getAssets(), "fonts/AvenirNext-Medium.ttf");
        Typeface AvenirNextBold = Typeface.createFromAsset(monthlySalesView.getContext().getAssets(), "fonts/AvenirNext-Bold.ttf");

        tvSummationThisMonth = (TextView) monthlySalesView.findViewById(R.id.tvThisMonthSummation);
        tvMonthTitle = (TextView) monthlySalesView.findViewById(R.id.tVMonthly);
        tvMonthTitle.setTypeface(AvenirNextMedium);
        tvMonthTitle.setTextSize(24);

        tvSummationThisMonth.setTypeface(AvenirNextBold);
        tvSummationThisMonth.setTextSize(12);

        boxThisMonth = monthlySalesView.findViewById(R.id.boxThisMonth);

        tvProgressMsg = (TextView) monthlySalesView.findViewById(R.id.message);
        tvProgressMsg.setText("Please Wait...");

        vProgressHud = (LinearLayout) monthlySalesView.findViewById(R.id.progress_hud_all);
        vProgressHud.setVisibility(View.GONE);

        mLineChartView.setViewportCalculationEnabled(false);
        mLineChartView.setScrollEnabled(false);
        mLineChartView.setZoomEnabled(false);
        mLineChartView.setInteractive(false);
        mLineChartView.setPadding(30, 250, 10, 30);

        setUpSwipeRefreshView(monthlySalesView);
    }

    public Boolean isProgressHudVisible() {
        return vProgressHud.getVisibility() == View.VISIBLE;
    }

    private void showProgressHud(boolean show) {
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

    private void startProgressHudAnimation() {
        spinnerImage = (ImageView) monthlySalesView.findViewById(R.id.spinnerImageView);
        spinner = (AnimationDrawable) spinnerImage.getBackground();
        spinner.start();
    }

    private void stopProgressHudAnimation() {
        if (spinner != null) {
            spinner.stop();
        }
    }

    public void showProgressHUD() {
        showProgressHud(true);
    }

    public void hideProgressHUD() {
        showProgressHud(false);
    }

    public void showLineChart() {
        lineChartViewOptions(false);
    }

    public void hideLineChart() {
        lineChartViewOptions(true);
    }

    private void lineChartViewOptions(Boolean toHide) {
        // hide line chart
        if (toHide) {
            tvSummationThisMonth.setText("");
            boxThisMonth.setVisibility(View.INVISIBLE);
            resetViewPort(0, 31, 0, 0);
            mLineChartView.setVisibility(View.INVISIBLE);
        }

        // show line chart
        if (!toHide) {
            tvSummationThisMonth.setText("");
            boxThisMonth.setVisibility(View.VISIBLE);
            resetViewPort(mViewPortLeft, 31, maxViewPortTop, mViewPortBottom);
            mLineChartView.setVisibility(View.VISIBLE);
        }
    }

    private void updateSalesSummation() {
        mLineChartView.setVisibility(View.VISIBLE);
        boxThisMonth.setVisibility(View.VISIBLE);
        tvSummationThisMonth.setVisibility(View.VISIBLE);
        thisMonthSummationMessage = DateTools.getTextViewDate(dateThisMonth.get(0),
                dateThisMonth.get(dateThisMonth.size() - 1)) + " $" + RoundValue(thisMonthSummation, 0);
        tvSummationThisMonth.setText(thisMonthSummationMessage);
    }

    private void generateValues() {
        dateThisMonth = DateTools.getDatesThisMonth();
    }

    List<PointValue> pointValueListThisMonth;

    private void  generateDummyData() throws SQLException {

        generateValues();

        int branch_id = Integer.parseInt(SettingTools.currentBranchId(getActivity().getApplicationContext()));
        DailySales d = getDailySales(branch_id, "");

        if (d == null) {
            maxViewPortTop = 0;
            List<PointValue> pointValueListThisMonth = new ArrayList<>();
            PointValue pointValueThisWeek = new PointValue(0, 0);
            pointValueListThisMonth.add(pointValueThisWeek);

            maxViewPortTop = 8;
            // Create List of Line
            List<Line> lineList = new ArrayList<>();

            // Add the PointValues to Line
            Line lineThisMonth = new Line(pointValueListThisMonth);
            lineThisMonth.setColor(getResources().getColor(R.color.d_line_chart_color_1));
            lineThisMonth.setPointRadius(4);

            // Add the Line with Point Values
            lineList.add(lineThisMonth);

            // Create Line Chart Data and Point the Line
            LineChartData data = new LineChartData(lineList);

            List<AxisValue> axisLabelsX = new ArrayList<>();
            List<AxisValue> axisLabelsY = new ArrayList<>();
            AxisValue axisValue;

            for (int x = 0; x < 31; x++) {
                if (x == 0 || x == 4 || x == 9 || x == 14 || x == 19 || x == 24 || x == 29) {
                    //Log.e(TAG, "labeling " + (x + 1));
                    axisValue = new AxisValue(x).setLabel(String.valueOf(x + 1));
                    axisLabelsX.add(axisValue);
                }
            }

            int yValue4 = LineChartValueTools.computedMaxValue(4);

            int yValue2 = yValue4 / 2;
            int yValue1 = yValue2 / 2;
            int yValue3 = yValue2 + yValue1;

            String yValue4Label = LineChartValueTools.getShortenedCurrency(yValue4 + "");
            String yValue3Label = LineChartValueTools.getShortenedCurrency(yValue3 + "");
            String yValue2Label = LineChartValueTools.getShortenedCurrency(yValue2 + "");
            String yValue1Label = LineChartValueTools.getShortenedCurrency(yValue1 + "");

            Log.e(TAG, "yvalue1:" + yValue1Label);

            maxViewPortTop = 80;

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

            data.setAxisYRight(axisY);
            data.setAxisXBottom(axisX);

            data.setBaseValue(Float.NEGATIVE_INFINITY);
            mLineChartView.setLineChartData(data);
            mLineChartView.setVisibility(View.VISIBLE );

            boxThisMonth.setVisibility(View.INVISIBLE);
            tvSummationThisMonth.setVisibility(View.INVISIBLE);
            resetViewPort(mViewPortLeft, 31, 8, mViewPortBottom);
        } else {
            updateMonthlySalesLineChart();
        }


    }

    private void generateData() {
        maxViewPortTop = 0;
        thisMonthSummation = 0.0;
        int branch_id = Integer.parseInt(SettingTools.currentBranchId(getActivity().getApplicationContext()));
        Log.e(TAG, "Updating Month Sales Line Chart... Branch ID: " + branch_id);

        haveData = true;

        pointValueListThisMonth = new ArrayList<>();

        if (dateThisMonth != null) {
            Log.e(TAG, "Querying This Month");
            int i = 0;
            for (String day : dateThisMonth) {
                try {
                    DailySales dailySales = getDailySales(branch_id, day);

                    if (dailySales != null) {
                        thisMonthSummation += Double.parseDouble(dailySales.getAmount());
                        Long value = (long) Math.ceil(Double.parseDouble(dailySales.getAmount()));
                        //Log.e(TAG, day + " Index: " + i + " Data: " + value);
                        PointValue pointValueThisWeek = new PointValue(i, value);
                        pointValueListThisMonth.add(pointValueThisWeek);

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
            Log.e(TAG, "Date This Month is empty. initialize it first");
        }

        // Create List of Line
        List<Line> lineList = new ArrayList<>();

        // Add the PointValues to Line
        Line lineThisMonth = new Line(pointValueListThisMonth);
        lineThisMonth.setColor(getResources().getColor(R.color.d_line_chart_color_1));
        lineThisMonth.setPointRadius(4);

        // Add the Line with Point Values
        lineList.add(lineThisMonth);

        // Create Line Chart Data and Point the Line
        data = new LineChartData(lineList);

        List<AxisValue> axisLabelsX = new ArrayList<>();
        List<AxisValue> axisLabelsY = new ArrayList<>();
        AxisValue axisValue;

        for (int x = 0; x < 31; x++) {
            if (x == 0 || x == 4 || x == 9 || x == 14 || x == 19 || x == 24 || x == 29) {
                //Log.e(TAG, "labeling " + (x + 1));
                axisValue = new AxisValue(x).setLabel(String.valueOf(x + 1));
                axisLabelsX.add(axisValue);
            }
        }

        Long maxAdd10Percent = LineChartValueTools.add10percentLong(maxViewPortTop);
        Long maxAlgo = Long.valueOf(LineChartValueTools.computedMaxValue(maxAdd10Percent));
        Log.e(TAG, "Highest Value: " + maxViewPortTop);
        Log.e(TAG, "Add 10%: " + maxAdd10Percent);
        Log.e(TAG, "Max Algo: " + maxAlgo);

        Long yValue4 = maxAlgo;
        Long yValue2 = yValue4 / 2;
        Long yValue1 = yValue2 / 2;
        Long yValue3 = yValue2 + yValue1;

        String yValue4Label = LineChartValueTools.getShortenedCurrency(yValue4);
        String yValue3Label = LineChartValueTools.getShortenedCurrency(yValue3);
        String yValue2Label = LineChartValueTools.getShortenedCurrency(yValue2);
        String yValue1Label = LineChartValueTools.getShortenedCurrency(yValue1);

        Log.e(TAG, "Value: " + maxViewPortTop + "The Max Value is: " + yValue4Label + "");
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

        data.setAxisYRight(axisY);
        data.setAxisXBottom(axisX);

        data.setBaseValue(Float.NEGATIVE_INFINITY);
        mLineChartView.setLineChartData(data);
    }

    public void updateMonthlySalesLineChart() {
        generateValues();
        generateData();

        if(haveData) {
            resetViewPort(0, 31, maxViewPortTop, 0);
            updateSalesSummation();
        } else {
            hideLineChart();
        }
/*

        //Prepare chart's data, and other things
        //Change target values
        for (Line line : data.getLines()) {
            for (PointValue value : line.getValues()) {
                value.setTarget(0,value.getY());//some random target value
            }
        }

        mLineChartView.startDataAnimation();*/
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
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.scMonthly);
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
