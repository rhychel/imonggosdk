package nueca.net.salesdashboard.adapters;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import net.nueca.imonggosdk.database.ImonggoDBHelper;

import nueca.net.salesdashboard.enums.Update;
import nueca.net.salesdashboard.enums.UpdateWeekType;
import nueca.net.salesdashboard.fragments.SalesDaily;
import nueca.net.salesdashboard.fragments.SalesMonthly;
import nueca.net.salesdashboard.fragments.SalesWeekly;
import nueca.net.salesdashboard.interfaces.OnPageChangeWeeklyListener;
import nueca.net.salesdashboard.interfaces.SalesRefreshListener;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private String TAG = "ViewPagerAdapter";
    private SalesDaily dailySalesFragment = null;
    private SalesWeekly weeklySalesFragment = null;

    private SalesMonthly monthlySalesFragment = null;
    private SalesRefreshListener salesRefreshListener;
    private OnPageChangeWeeklyListener onPageChangeWeeklyListener = null;
    private ImonggoDBHelper dbHelper;

    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created

    public ViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabsumb,
                            SalesRefreshListener salesRefreshListener, OnPageChangeWeeklyListener onPageChangeWeeklyListener) {
        super(fm);

        this.salesRefreshListener = salesRefreshListener;
        this.onPageChangeWeeklyListener = onPageChangeWeeklyListener;
        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;

    }


    public void setDbHelper(ImonggoDBHelper dbHelper) {
        this.dbHelper = dbHelper;
        Log.e(TAG, "dbHelper is now set");
    }

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        if (position == 0) {
            dailySalesFragment = SalesDaily.getInstance();
            dailySalesFragment.setSalesRefreshListener(salesRefreshListener);
            dailySalesFragment.setHelper(dbHelper);
            fragment = dailySalesFragment;
        }
        if (position == 1) {

            weeklySalesFragment = SalesWeekly.getInstance();
            weeklySalesFragment.setSalesRefreshListener(salesRefreshListener);
            weeklySalesFragment.setOnPageChangeWeeklyListener(onPageChangeWeeklyListener);
            weeklySalesFragment.setHelper(dbHelper);
            fragment = weeklySalesFragment;
        }
        if (position == 2) {
            monthlySalesFragment = SalesMonthly.getInstance();
            monthlySalesFragment.setSalesRefreshListener(salesRefreshListener);
            monthlySalesFragment.setHelper(dbHelper);
            fragment = SalesMonthly.getInstance();
        }

        return fragment;

    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    @Override
    public int getCount() {
        return NumbOfTabs;
    }

    public void selectThisWeekInWeeklyFragment() {
        if (weeklySalesFragment != null) {
            weeklySalesFragment.selectThisWeekFragment();
        } else {
            Log.e(TAG, "Weekly Sales Fragment is null, please instantiate it first");
        }
    }

    public void updateWeeklySalesLineChart() {
        if (weeklySalesFragment != null) {
            Log.e(TAG, "Updating Weekly Sales Line Chart please wait...");
            weeklySalesFragment.updateWeeklyLineChart();
        }
    }

    public UpdateWeekType getUpdateWeekType() {
        if (weeklySalesFragment != null) {
            return weeklySalesFragment.getCurrentWeekView();
        }
        return null;
    }

    public void updateDailySalesCardValues() {
        if (dailySalesFragment != null) {
            Log.e(TAG, "Updating Card View please wait...");
            dailySalesFragment.updateDailySalesData();
        }
    }

    public void updateMonthlySalesLineChart() {
        if (monthlySalesFragment != null) {
            Log.e(TAG, "Updating Monthly Sales Line Chart please wait...");
            monthlySalesFragment.updateMonthlySalesLineChart();
        }
    }

    public void hideViewDataOptions(Update update, Boolean toHide) {

        if (update == Update.DAY) {
            if (dailySalesFragment != null) {
                if (toHide) {
                    Log.e(TAG, "hiding card view for day");
                    dailySalesFragment.hideCardViewChart();
                } else {
                    Log.e(TAG, "showing card view for day");
                    dailySalesFragment.showCardViewChart();
                }
            }
        }

        if (update == Update.WEEK) {
            if (weeklySalesFragment != null) {
                if (toHide) {
                    Log.e(TAG, "hiding Line Chart for week");
                    weeklySalesFragment.hideLineChart();
                } else {
                    Log.e(TAG, "showing Line Chart for week");
                    weeklySalesFragment.showLineChart();
                }
            }
        }

        if (update == Update.MONTH) {
            if (monthlySalesFragment != null) {
                Log.e(TAG, "hiding Line Chart for month");
                if (toHide) {
                    monthlySalesFragment.hideLineChart();
                } else {
                    monthlySalesFragment.showLineChart();
                }
            }
        }
    }

    public Boolean isDailySalesProgressHUDVisible() {
        return dailySalesFragment.isProgressHudVisible();
    }

    public Boolean isMonthlySalesProgressHUDVisible() {
        return monthlySalesFragment.isProgressHudVisible();
    }


    public void showDailyProgressHUD(boolean show) {
        if (dailySalesFragment != null) {
            if (show) {
                Log.e(TAG, "show progress hud");
                dailySalesFragment.showProgressHUD();
            } else {
                Log.e(TAG, "hide progress hud");
                dailySalesFragment.hideProgressHUD();
            }
        }
    }

    public void showWeeklyProgressHUD(boolean show) {
        if (weeklySalesFragment != null) {
            if (show) {
                weeklySalesFragment.progressHudOptions(getUpdateWeekType(), true);
            } else {
                weeklySalesFragment.progressHudOptions(getUpdateWeekType(), false);
            }
        } else {
            Log.e(TAG, "Your WeekFragment is null");
        }
    }

    public void showMonthlyProgressHUD(boolean show) {
        if (monthlySalesFragment != null) {
            if (show) {
                monthlySalesFragment.showProgressHUD();
            } else {
                monthlySalesFragment.hideProgressHUD();
            }
        }
    }

    public void showNotificationMessage(String message, Boolean isRefreshing, int update) {
        switch (update) {
            case 0:
                if (dailySalesFragment != null) {
                    Log.e(TAG, "showing notification");
                    dailySalesFragment.showNotificationMessage(message, isRefreshing);
                } else {
                    Log.e(TAG, "Can't hide notification message, day ragment is null");
                }
                break;
            case 1:

                if (weeklySalesFragment != null) {

                    weeklySalesFragment.showNotificationMessage(message, isRefreshing);
                } else {
                    Log.e(TAG, "Can't hide notification message, monthly fragment is null");
                }
                break;
            case 2:
                if (monthlySalesFragment != null) {
                    Log.e(TAG, "showing notification");
                    monthlySalesFragment.showNotificationMessage(message, isRefreshing);
                } else {
                    Log.e(TAG, "Can't hide notification message, day ragment is null");
                }
                break;
        }
    }

    public void hideDailyNotificationMessage(Boolean isRefreshing, int update) {
        switch (update) {
            case 0:
                if (dailySalesFragment != null) {
                    dailySalesFragment.hideNotification(isRefreshing);
                } else {
                    Log.e(TAG, "Can't hide notification message, day fragment is null");
                }
                break;
            case 1:
                if (weeklySalesFragment != null) {
                    weeklySalesFragment.hideNotificationMessage(isRefreshing);
                } else {
                    Log.e(TAG, "Can't hide notification message, month fragment is null");
                }
                break;
            case 2:
                if (monthlySalesFragment != null) {
                    monthlySalesFragment.hideNotification(isRefreshing);
                } else {
                    Log.e(TAG, "Can't hide notification message, month fragment is null");
                }
                break;
        }


    }
}