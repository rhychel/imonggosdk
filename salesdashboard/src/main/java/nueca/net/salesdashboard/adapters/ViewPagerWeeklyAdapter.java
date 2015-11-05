package nueca.net.salesdashboard.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import net.nueca.imonggosdk.database.ImonggoDBHelper;

import java.sql.SQLException;

import nueca.net.salesdashboard.fragments.SalesWeek1;
import nueca.net.salesdashboard.fragments.SalesWeek2;
import nueca.net.salesdashboard.fragments.SalesWeek3;
import nueca.net.salesdashboard.fragments.SalesWeek4;
import nueca.net.salesdashboard.interfaces.OnPageChangeWeeklyListener;
import nueca.net.salesdashboard.interfaces.SalesRefreshListener;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public class ViewPagerWeeklyAdapter extends FragmentStatePagerAdapter {
    private String TAG = "ViewPagerWeeklyAdapter";
    private SalesWeek1 thisWeekFragment = null;
    private SalesWeek2 lastWeekFragment = null;
    private SalesWeek3 lastlastWeekFragment = null;
    private SalesWeek4 lastlastlastWeekFragment = null;

    private SalesRefreshListener salesRefreshListener = null;
    private OnPageChangeWeeklyListener onPageChangeWeeklyListener= null;
    private ImonggoDBHelper dbHelper;

    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created

    public ViewPagerWeeklyAdapter(FragmentManager fm,  CharSequence mTitles[], int mNumbOfTabsumb,
                                  SalesRefreshListener salesRefreshListener, OnPageChangeWeeklyListener onPageChangeWeeklyListener) {
        super(fm);
        this.salesRefreshListener = salesRefreshListener;
        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;
        this.onPageChangeWeeklyListener = onPageChangeWeeklyListener;
    }

    public void setDbHelper(ImonggoDBHelper dbHelper) {
        this.dbHelper = dbHelper;
        Log.e(TAG, "dbHelper is now set");
    }

    public ViewPagerWeeklyAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        Log.e(TAG, "Fragment position: " +position);

        if(position == 0) {
            lastlastlastWeekFragment = SalesWeek4.getInstance();
            Log.e(TAG, "Setting Sales Refresh Listener 3 weeks ago");
            lastlastlastWeekFragment.setSalesRefreshListener(salesRefreshListener);
            lastlastlastWeekFragment.setHelper(dbHelper);
            fragment = lastlastlastWeekFragment;
        }

        if(position == 1) {
            lastlastWeekFragment = SalesWeek3.getInstance();
            Log.e(TAG, "Setting Sales Refresh Listener 2 weeks ago");
            lastlastWeekFragment.setSalesRefreshListener(salesRefreshListener);
            lastlastWeekFragment.setHelper(dbHelper);
            fragment = lastlastWeekFragment;
        }

        if(position == 2){
            lastWeekFragment = SalesWeek2.getInstance();
            Log.e(TAG, "Setting Sales Refresh Listener last week");
            lastWeekFragment.setSalesRefreshListener(salesRefreshListener);
            lastWeekFragment.setHelper(dbHelper);
            fragment = lastWeekFragment;
        }

        if(position == 3) {
            thisWeekFragment = SalesWeek1.getInstance();
            Log.e(TAG, "Setting Sales Refresh Listener this week");
            thisWeekFragment.setSalesRefreshListener(salesRefreshListener);
            thisWeekFragment.setHelper(dbHelper);
            fragment = thisWeekFragment;
        }

        return fragment;
    }

    public Boolean isThisWeekProgressHudVisible() {
        return thisWeekFragment.isProgressHudVisible();
    }

    public Boolean isLastWeekProgressHudVisible() {
        return lastWeekFragment.isProgressHudVisible();
    }

    public Boolean isLast2WeeksProgressHudVisible() {
        return lastlastWeekFragment.isProgressHudVisible();
    }

    public Boolean isLast3WeeksProgressHudVisible() {
        return lastlastlastWeekFragment.isProgressHudVisible();
    }

    public void showThisWeekProgressHUD() {
        thisWeekFragment.showProgressHUD();
    }

    public void hideThisWeekProgressHUD() {
        thisWeekFragment.hideProgressHUD();
    }

    public void showLastWeekProgressHUD() {
        lastWeekFragment.showProgressHUD();
    }

    public void hideLastWeekProgressHUD() {
        lastWeekFragment.hideProgressHUD();
    }

    public void showLast2WeeksProgressHUD() {
        lastlastWeekFragment.showProgressHUD();
    }

    public void hideLast2WeeksProgressHUD() {
        lastlastWeekFragment.hideProgressHUD();
    }

    public void showLast3WeeksProgressHUD() {
        lastlastlastWeekFragment.showProgressHUD();
    }

    public void hideLast3WeeksProgressHUD() {
        lastlastlastWeekFragment.hideProgressHUD();
    }

    public void showThisWeekLineChart() {
        thisWeekFragment.showLineChart();
    }

    public void hideThisWeekLineChart() {
        thisWeekFragment.hideLineChart();
    }

    public void showLastWeekLineChart() {
        lastWeekFragment.showLineChart();
    }

    public void hideLastWeekLineChart() {
        lastWeekFragment.hideLineChart();
    }

    public void showLast2WeeksLineChart() {
        lastlastWeekFragment.showLineChart();
    }

    public void hideLast2WeeksLineChart() {
        lastlastWeekFragment.hideLineChart();
    }

    public void showLast3WeeksLineChart() {
        lastlastlastWeekFragment.showLineChart();
    }

    public void hideLast3WeeksLineChart() {
        lastlastlastWeekFragment.hideLineChart();
    }

    public void updateSalesThisWeek() {
        Log.e(TAG, "Updating Sales This Week");
        try {
            thisWeekFragment.updateWeeklySalesLineChart();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSalesLastWeeks() {
        Log.e(TAG, "Updating Sales Last Week");
        try {
            lastWeekFragment.updateWeeklySalesLineChart();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSalesLast2Weeks() {
        Log.e(TAG, "Updating Sales Last 2 Week");
        try {
            lastlastWeekFragment.updateWeeklySalesLineChart();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSalesLast3Weeks() {
        Log.e(TAG, "Updating Sales This Week");
        try {
            lastlastlastWeekFragment.updateWeeklySalesLineChart();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return NumbOfTabs;
    }



    public void showNotificationMessage(String message, Boolean isRefreshing, int page) {
        switch(page) {
            case 0:
                if(lastlastlastWeekFragment != null ) {
                    Log.e(TAG, "showing notification");
                    lastlastlastWeekFragment.showNotification(message, isRefreshing);
                } else {
                    Log.e(TAG, "Can't hide notification message, last last last week ragment is null");
                }
                break;
            case 1:
                if(lastlastWeekFragment != null ) {
                    Log.e(TAG, "showing notification");
                    lastlastWeekFragment.showNotification(message, isRefreshing);
                } else {
                    Log.e(TAG, "Can't hide notification message, last last week ragment is null");
                }
                break;
            case 2:
                if(lastWeekFragment != null) {
                    lastWeekFragment.showNotification(message, isRefreshing);
                }  else {
                    Log.e(TAG, "Can't hide notification message, last week fragment is null");
                }
                break;
            case 3:
                if(thisWeekFragment != null ) {
                    Log.e(TAG, "showing notification");
                    thisWeekFragment.showNotification(message, isRefreshing);
                } else {
                    Log.e(TAG, "Can't hide notification message, this week fragment is null");
                }
                break;
        }
    }

    public void hideNotificationMessage(Boolean isRefreshing, int page) {
        switch (page) {
            case 0:
                if(lastlastlastWeekFragment != null) {
                    lastlastlastWeekFragment.hideNotification(isRefreshing);
                } else {
                    Log.e(TAG, "Can't hide notification message, last last last week fragment is null");
                }
                break;
            case 1:
                if(lastlastWeekFragment != null) {
                    lastlastWeekFragment.hideNotification(isRefreshing);
                } else {
                    Log.e(TAG, "Can't hide notification message, last last week fragment is null");
                }
                break;
            case 2:
                if(lastWeekFragment != null) {
                    lastWeekFragment.hideNotification(isRefreshing);
                } else {
                    Log.e(TAG, "Can't hide notification message, last week fragment is null");
                }
                break;
            case 3:
                if(thisWeekFragment != null) {
                    thisWeekFragment.hideNotification(isRefreshing);
                } else {
                    Log.e(TAG, "Can't hide notification message, this week fragment is null");
                }
                break;
        }
    }
}
