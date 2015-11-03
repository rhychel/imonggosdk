package nueca.net.salesdashboard.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import nueca.net.salesdashboard.R;
import nueca.net.salesdashboard.adapters.ViewPagerWeeklyAdapter;
import nueca.net.salesdashboard.enums.Update;
import nueca.net.salesdashboard.enums.UpdateWeekType;
import nueca.net.salesdashboard.interfaces.OnPageChangeWeeklyListener;
import nueca.net.salesdashboard.interfaces.SalesRefreshListener;
import nueca.net.salesdashboard.viewpager.CustomViewPager;

public class SalesWeekly extends BaseSalesFragment implements SalesRefreshListener, OnPageChangeWeeklyListener, ViewPager.OnPageChangeListener {
    private String TAG = "SalesWeekly";
    private CustomViewPager viewPager;
    private ViewPagerWeeklyAdapter viewPagerAdapter;
    private CharSequence Titles[] = {"This Week", "Last Week", "Last 2 Weeks", "Last 3 Weeks"};
    private int numOfTabs = 4;
    public static SalesWeekly instance = null;

    public static SalesWeekly getInstance() {
        if (instance == null) {
            return new SalesWeekly();
        }
        return instance;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "SalesWeekly Fragment Running..");
        View view = inflater.inflate(R.layout.weekly_sales_viewpager, container, false);

        viewPagerAdapter = new ViewPagerWeeklyAdapter(getFragmentManager(), Titles, numOfTabs, this, this);
        viewPagerAdapter.setDbHelper(getHelper());

        viewPager = (CustomViewPager) view.findViewById(R.id.cv_pager);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(this);

        return view;
    }

    public void selectThisWeekFragment() {
        Log.e(TAG, "Selecting this week fragment");
        viewPager.setCurrentItem(numOfTabs - 1);
    }

    @Override
    public void isRefreshing(SwipeRefreshLayout swipeRefreshLayout) {

    }

    @Override
    public void onRefresh(Update update, UpdateWeekType updateWeekType, SwipeRefreshLayout swipeRefreshLayout) {
        Log.e(TAG, "onRefreshI");
        getSalesRefreshListener().onRefresh(update, updateWeekType, swipeRefreshLayout);
    }

    @Override
    public void onRefresh(Update update, SwipeRefreshLayout swipeRefreshLayout) {

    }

    @Override
    public void onPageStopped(String message) {
        getSalesRefreshListener().onPageStopped(message);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        UpdateWeekType updateWeekTypex;
        switch (viewPager.getCurrentItem()) {
            case 0:
                hideNotificationMessage(true);
                updateWeekTypex = UpdateWeekType.LAST_3_WEEKS;
                Log.e(TAG, "Sales 3 Week Ago Fragment Running..");
                getOnPageChangeWeeklyListener().onPageSelected(updateWeekTypex);
                //updateWeeklyLineChart(updateWeekTypex);
                break;
            case 1:
                updateWeekTypex = UpdateWeekType.LAST_2_WEEKS;
                Log.e(TAG, "Sales 2 Weeks Ago Fragment Running..");
                getOnPageChangeWeeklyListener().onPageSelected(updateWeekTypex);
                //updateWeeklyLineChart(updateWeekTypex);
                break;
            case 2:
                updateWeekTypex = UpdateWeekType.LAST_WEEK;
                Log.e(TAG, "Sales Last Week Fragment Running..");
                getOnPageChangeWeeklyListener().onPageSelected(updateWeekTypex);
                //updateWeeklyLineChart(updateWeekTypex);
                break;
            case 3:
                updateWeekTypex = UpdateWeekType.THIS_WEEK;
                Log.e(TAG, "Sales This Week Fragment Running..");
                getOnPageChangeWeeklyListener().onPageSelected(updateWeekTypex);
                //updateWeeklyLineChart(updateWeekTypex);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public ViewPagerWeeklyAdapter getWeeklySalesViewPagerAdapter() {
        return viewPagerAdapter;
    }

    public UpdateWeekType getCurrentWeekView() {
        switch (viewPager.getCurrentItem()) {
            case 0:
                return UpdateWeekType.LAST_3_WEEKS;
            case 1:
                return UpdateWeekType.LAST_2_WEEKS;
            case 2:
                return UpdateWeekType.LAST_WEEK;
            case 3:
                return UpdateWeekType.THIS_WEEK;
            default:
                return null;
        }
    }

    public void progressHudOptions(UpdateWeekType updateWeekType, Boolean show) {
        if (updateWeekType == UpdateWeekType.THIS_WEEK) {
            if (show) {
                if (!viewPagerAdapter.isThisWeekProgressHudVisible()) {
                    viewPagerAdapter.showThisWeekProgressHUD();
                }
            } else {
                viewPagerAdapter.hideThisWeekProgressHUD();
            }
        }

        if (updateWeekType == UpdateWeekType.LAST_WEEK) {
            if (show) {
                if (!viewPagerAdapter.isLastWeekProgressHudVisible()) {
                    viewPagerAdapter.showLastWeekProgressHUD();
                }
            } else {
                viewPagerAdapter.hideLastWeekProgressHUD();
            }
        }

        if (updateWeekType == UpdateWeekType.LAST_2_WEEKS) {
            if (show) {
                if (!viewPagerAdapter.isLast2WeeksProgressHudVisible()) {
                    viewPagerAdapter.showLast2WeeksProgressHUD();
                }
            } else {
                viewPagerAdapter.hideLast2WeeksProgressHUD();
            }
        }

        if (updateWeekType == UpdateWeekType.LAST_3_WEEKS) {
            if (show) {
                if (!viewPagerAdapter.isLast3WeeksProgressHudVisible()) {
                    viewPagerAdapter.showLast3WeeksProgressHUD();
                }
            } else {
                viewPagerAdapter.hideLast3WeeksProgressHUD();
            }
        }
    }

    public void showLineChart() {
        lineChartViewOptions(getCurrentWeekView(), true);
    }

    public void hideLineChart() {
        lineChartViewOptions(getCurrentWeekView(), false);
    }

    public void lineChartViewOptions(UpdateWeekType updateWeekType, Boolean show) {
        if (updateWeekType == UpdateWeekType.THIS_WEEK) {
            if (show) {
                viewPagerAdapter.showThisWeekLineChart();
            } else {
                viewPagerAdapter.hideThisWeekLineChart();
            }
        }

        if (updateWeekType == UpdateWeekType.LAST_WEEK) {
            if (show) {
                viewPagerAdapter.showLastWeekLineChart();
            } else {
                viewPagerAdapter.hideLastWeekLineChart();
            }
        }

        if (updateWeekType == UpdateWeekType.LAST_2_WEEKS) {
            if (show) {
                viewPagerAdapter.showLast2WeeksLineChart();
            } else {
                viewPagerAdapter.hideLast2WeeksLineChart();
            }
        }

        if (updateWeekType == UpdateWeekType.LAST_3_WEEKS) {
            if (show) {
                viewPagerAdapter.showLast3WeeksLineChart();
            } else {
                viewPagerAdapter.hideLast3WeeksLineChart();
            }
        }
    }

    public void updateWeeklyLineChart() {
        updateWeeklyLineChart(getCurrentWeekView());
    }

    public void updateWeeklyLineChart(UpdateWeekType updateWeekType) {
        if (updateWeekType == UpdateWeekType.THIS_WEEK) {
            viewPagerAdapter.updateSalesThisWeek();
        }

        if (updateWeekType == UpdateWeekType.LAST_WEEK) {
            viewPagerAdapter.updateSalesLastWeeks();
        }

        if (updateWeekType == UpdateWeekType.LAST_2_WEEKS) {
            viewPagerAdapter.updateSalesLast2Weeks();
        }

        if (updateWeekType == UpdateWeekType.LAST_3_WEEKS) {
            viewPagerAdapter.updateSalesLast3Weeks();
        }
    }


    @Override
    public void onPageSelected(UpdateWeekType updateWeekType) {

    }

    public void showNotificationMessage(String message, Boolean isRefreshing) {
      viewPagerAdapter.showNotificationMessage(message, isRefreshing, viewPager.getCurrentItem());
    }

    public void hideNotificationMessage(Boolean isRefreshing) {
        viewPagerAdapter.hideNotificationMessage(isRefreshing, viewPager.getCurrentItem());
    }
}
