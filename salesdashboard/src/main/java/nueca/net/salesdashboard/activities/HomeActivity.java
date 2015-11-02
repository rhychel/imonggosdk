package nueca.net.salesdashboard.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.afollestad.materialdialogs.MaterialDialog;

import net.nueca.imonggosdk.dialogs.DialogTools;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchTag;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;
import net.nueca.imonggosdk.objects.base.BatchList;
import net.nueca.imonggosdk.operations.http.ImonggoOperations;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.NetworkTools;
import net.nueca.imonggosdk.tools.SettingTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nueca.net.salesdashboard.R;
import nueca.net.salesdashboard.adapters.UserBranchesSpinnerAdapter;
import nueca.net.salesdashboard.adapters.ViewPagerAdapter;
import nueca.net.salesdashboard.enums.ShareType;
import nueca.net.salesdashboard.enums.Update;
import nueca.net.salesdashboard.enums.UpdateType;
import nueca.net.salesdashboard.enums.UpdateWeekType;
import nueca.net.salesdashboard.interfaces.OnPageChangeWeeklyListener;
import nueca.net.salesdashboard.interfaces.OnReloadBranches;
import nueca.net.salesdashboard.interfaces.SalesRefreshListener;
import nueca.net.salesdashboard.interfaces.SyncDailySalesListener;
import nueca.net.salesdashboard.operations.SyncDailySales;
import nueca.net.salesdashboard.slidingtab.SlidingTabLayout;
import nueca.net.salesdashboard.tools.HUDTools;
import nueca.net.salesdashboard.tools.IntentTools;
import nueca.net.salesdashboard.viewpager.CustomViewPager;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public class HomeActivity extends BaseDailySalesActivity implements ViewPager.OnPageChangeListener, OnPageChangeWeeklyListener, SalesRefreshListener, SyncDailySalesListener, VolleyRequestListener, OnReloadBranches {
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView nvDrawer;
    private Intent appIntent;
    private ActionBar toolbar;
    private CustomViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private SlidingTabLayout tabs;
    private CharSequence Titles[] = {"Day", "Week", "Month"};
    private int NumbOfTabs = 3;
    private View spinnerContainer;
    private Spinner spinner;
    private UserBranchesSpinnerAdapter userBranchesSpinnerAdapter;
    private static String TAG = "HomeActivity";
    private Update UpdateThis = Update.DAY;
    private UpdateType UpdateTypeThis = UpdateType.DEFAULT_UPDATE;
    private LinearLayout mTabsLinearLayout;
    private Branch currentBranch;
    private Boolean downloadStarted = false;
    private OnReloadBranches onReloadBranches;
    private Boolean isRefreshing = false;

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        //TODO Update Dashboard
    }

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.home);
        setUpSyncDailySales();
        setUpComponents();
        setUpTabs();


    }

    private void setUpSyncDailySales() {
        if (syncDailySales == null) {
            try {
                syncDailySales = new SyncDailySales(getApplicationContext(), this, getSession().getCurrent_branch_id() + "",
                        getSession(), getQueue(), getHelper());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void setUpComponents() {
        try {
            SettingTools.updateSettings(HomeActivity.this, SettingsName.CURRENT_BRANCH,
                    String.valueOf(getSession().getCurrent_branch_id()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        mToolbar = (Toolbar) findViewById(R.id.tbActionBar);

        if (Build.VERSION.SDK_INT >= 21) {
            mToolbar.setElevation(0);
        }

        assert getSupportActionBar() != null;
        setSupportActionBar(mToolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });
        toolbar = getSupportActionBar();
        toolbar.setHomeAsUpIndicator(R.drawable.ic_action_more);
        toolbar.setDisplayHomeAsUpEnabled(true);
        toolbar.setDisplayShowTitleEnabled(false);

        // Find our drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        setupDrawerContent(nvDrawer);

        spinnerContainer = LayoutInflater.from(this).inflate(R.layout.app_bar_spinner, mToolbar, false);
        mToolbar.addView(spinnerContainer);

        userBranchesSpinnerAdapter = new UserBranchesSpinnerAdapter(getLayoutInflater(), getBranchUsers());
        spinner = (Spinner) findViewById(R.id.toolbar_spinner);

        spinner.setAdapter(userBranchesSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                offsetSpinnerBelowv21(spinner);
                currentBranch = userBranchesSpinnerAdapter.getBranch(position);
                SettingTools.updateSettings(HomeActivity.this, SettingsName.CURRENT_BRANCH, String.valueOf(currentBranch.getId()));
                if (syncDailySales != null) {
                    syncDailySales.setCurrentBranchId(String.valueOf(currentBranch.getId()));
                } else {
                    Log.e(TAG, "SyncDailySales is null");
                }


                UpdateTypeThis = UpdateType.DEFAULT_UPDATE;
                try {
                    updateDashboardData(UpdateTypeThis, UpdateThis, viewPagerAdapter.getUpdateWeekType());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setUpTabs() {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), Titles, NumbOfTabs, this, this);
        viewPagerAdapter.setDbHelper(getHelper());

        viewPager = (CustomViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setPagingEnabled(false);
        viewPager.setOffscreenPageLimit(0);

        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true);

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(viewPager);

        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.d_secondary_text);    //define any color in xml resources and set it here, I have used white
            }

        });

        tabs.setOnPageChangeListener(this);
        mTabsLinearLayout = ((LinearLayout) tabs.getChildAt(0));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void offsetSpinnerBelowv21(Spinner spinner) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if (Build.VERSION.SDK_INT < 21)
            spinner.setDropDownVerticalOffset((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, -(spinner.getHeight() + 14), metrics));
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                selectDrawerItem(menuItem);
                return false;
            }
        });
    }

    private List<BranchUserAssoc> getBranchUsers() {
        List<BranchUserAssoc> temp;
        List<BranchUserAssoc> branchUserAssoc = new ArrayList<>();
        try {
            temp = getHelper().getBranchUserAssocs().queryForEq("user_id", getUser());

            for (BranchUserAssoc branch : temp) {
                if (branch.getBranch().getSite_type() != null) {
                    Log.e(TAG, "Dont add this branch");
                } else {
                    Log.e(TAG, "adding this " + branch.getBranch().getName());
                    branchUserAssoc.add(branch);
                }
            }
            return branchUserAssoc;
        } catch (SQLException e) {
            return branchUserAssoc;
        }
    }


    private void updateDashboardData(UpdateType updateType, Update update) throws SQLException {
        updateDashboardData(updateType, update, null);
    }

    /**
     * instance of update
     * 1. slide to refresh                - forced refresh
     * 2. selection of branch in dropdown - check for time
     */
    private void updateDashboardData(UpdateType updateType, Update update, UpdateWeekType updateWeekType) throws SQLException {
        // TODO: Implement Dialogs Something

        Log.e(TAG, updateType + " Updating " + update);

        if (currentBranch != null) {
            Log.e(TAG, "Branch: " + currentBranch.getName());

            if (update == Update.DAY) {

                if (!NetworkTools.isInternetAvailable(this)) {
                    isRefreshing = true;
                    showNotificationNoInternet();
                }

                startSyncData(this, String.valueOf(currentBranch.getId()), updateType, Update.DAY, RequestType.DAILY_SALES_YESTERDAY);
                viewPagerAdapter.updateDailySalesCardValues();
            }
            if (update == Update.WEEK) {
                if (!NetworkTools.isInternetAvailable(this)) {
                    isRefreshing = true;
                    showNotificationNoInternet();
                }
                Log.e(TAG, "UPDATING WEEK " + updateType);
                startSyncData(this, String.valueOf(currentBranch.getId()), updateType, Update.WEEK, RequestType.DAILY_SALES_WEEK, updateWeekType);
            }
            if (update == Update.MONTH) {
                if (!NetworkTools.isInternetAvailable(this)) {
                    isRefreshing = true;
                    showNotificationNoInternet();
                }
                startSyncData(this, String.valueOf(currentBranch.getId()), updateType, Update.MONTH, RequestType.DAILY_SALES_MONTH);

            }
        } else {
            Log.e(TAG, "Current Branch is null");
        }

        Log.e(TAG, "Branch ID: " + SettingTools.currentBranchId(HomeActivity.this));
    }

    @Override
    public void onRefresh(Update update, UpdateWeekType updateWeekType, SwipeRefreshLayout swipeRefreshLayout) {
        isRefreshing = true;
        Log.e(TAG, "Refreshing... " + update + " Week... " + updateWeekType);

        try {
            ImonggoOperations.getAPIModule(this, getQueue(), getSession(),
                    this, Table.DAILY_SALES, getSession().getServer(), RequestType.DAILY_SALES_TODAY, "2015-10-20.json?branch_id=19302");
        } catch (SQLException e) {
            e.printStackTrace();
        }


        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        UpdateTypeThis = UpdateType.FORCED_UPDATE;

        if (update == Update.WEEK) {
            try {
                updateDashboardData(UpdateTypeThis, Update.WEEK, updateWeekType);
                viewPagerAdapter.showWeeklyProgressHUD(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRefresh(Update update, SwipeRefreshLayout swipeRefreshLayout) {
        isRefreshing = true;
        Log.e(TAG, "Refreshing... " + update);
        setSwipeRefreshLayout(swipeRefreshLayout);
        UpdateTypeThis = UpdateType.FORCED_UPDATE;

        if (getSwipeRefreshLayout() != null) {
            getSwipeRefreshLayout().setRefreshing(false);
        }

        if (update == Update.DAY) {
            try {
                updateDashboardData(UpdateTypeThis, Update.DAY);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (update == Update.MONTH) {
            try {
                updateDashboardData(UpdateTypeThis, Update.MONTH);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPageStopped(String TAG) {
        Log.e(HomeActivity.TAG, "Fragment Stopped. Message: " + TAG);
        cancelRequest(TAG);
    }

    public void selectDrawerItem(MenuItem menuItem) {
        mDrawerLayout.setSelected(false);
        mDrawerLayout.closeDrawers();

        switch (menuItem.getItemId()) {
            case R.id.nav_reload_branches:
                reloadBranchUsers(this);
                break;
            case R.id.nav_facebook:
                checkUsOnFacebook();
                break;
            case R.id.nav_twitter:
                followUsOnTwitter();
                break;
            case R.id.nav_instagram:
                seeUsOnInstagram();
                break;
            case R.id.nav_report_a_problem:
                reportAProblem();
                break;
            case R.id.nav_unlink:
                DialogTools.showBasicWithTitle(HomeActivity.this, "Confirm Unlink",
                        "Are  you sure you want to unlink this device to your Imonggo account?",
                        "Unlink Device", "Cancel", false, new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                unlinkDevice();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                                dialog.dismiss();
                            }
                        });
                return;
        }

        Log.e(TAG, menuItem.toString());

        if (menuItem.getItemId() == R.id.nav_reload_branches) {
            return;
        } else if (menuItem.getItemId() == R.id.nav_report_a_problem) {
            startActivity(Intent.createChooser(appIntent, "Send mail"));
        } else {
            startActivity(appIntent);
        }
    }

    private void showMoreOptions() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    private void checkUsOnFacebook() {
        Log.e(TAG, "Check Us On Facebook!");
        appIntent = IntentTools.getIntentByType(HomeActivity.this, ShareType.FACEBOOK);

    }

    private void followUsOnTwitter() {
        Log.e(TAG, "Follow Us On Twitter");
        appIntent = IntentTools.getIntentByType(HomeActivity.this, ShareType.TWITTER);
    }

    private void seeUsOnInstagram() {
        Log.e(TAG, "See Us On Instagram");
        appIntent = IntentTools.getIntentByType(HomeActivity.this, ShareType.INSTAGRAM);
    }

    private void reportAProblem() {
        Log.e(TAG, "Report A Problem");
        appIntent = IntentTools.getIntentByType(HomeActivity.this, ShareType.REPORT);
    }

    private void unlinkDevice() {
        Log.e(TAG, "Unlink Device");
        try {
            AccountTools.unlinkAccount(HomeActivity.this, getHelper());
            finish();
            startActivity(new Intent(HomeActivity.this, DashboardLoginActivity.class));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        try {
            Log.e(TAG, "position: " + position);
            UpdateTypeThis = UpdateType.DEFAULT_UPDATE;

            switch (viewPager.getCurrentItem()) {
                case 0:
                    UpdateThis = Update.DAY;
                    updateDashboardData(UpdateTypeThis, UpdateThis);
                    break;
                case 1:
                    viewPagerAdapter.selectThisWeekInWeeklyFragment();
                    UpdateThis = Update.WEEK;
                    break;
                case 2:
                    UpdateThis = Update.MONTH;
                    updateDashboardData(UpdateTypeThis, UpdateThis);
                    break;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPageSelected(UpdateWeekType updateWeekType) {
        hideNotification();
        Log.e(TAG, "OnPageSelected HOMEACTIVITY week: " + updateWeekType);
        try {
            updateDashboardData(UpdateType.DEFAULT_UPDATE, Update.WEEK, updateWeekType);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onStartDownload(RequestType requestType) {
        downloadStarted = true;
        Log.e(TAG, "onStartDownload RequestType" + requestType);

        if (requestType == RequestType.DAILY_SALES_TODAY || requestType == RequestType.DAILY_SALES_YESTERDAY ||
                requestType == RequestType.DAILY_SALES_WEEK || requestType == RequestType.DAILY_SALES_MONTH) {
            if (NetworkTools.isInternetAvailable(this)) {
                if (viewPager.getCurrentItem() == 0) {
                    if (!viewPagerAdapter.isDailySalesProgressHUDVisible()) {
                        viewPagerAdapter.showDailyProgressHUD(true);
                    }
                }

                if (viewPager.getCurrentItem() == 1) {

                    viewPagerAdapter.showWeeklyProgressHUD(true);

                }

                if (viewPager.getCurrentItem() == 2) {
                    if (!viewPagerAdapter.isMonthlySalesProgressHUDVisible()) {
                        viewPagerAdapter.showMonthlyProgressHUD(true);
                    }
                }
            }
        }
    }

    @Override
    public void onDownloadProgress(RequestType requestType, int page, int max) {

    }

    RequestType requestTypeX;

    @Override
    public void onEndDownload(RequestType requestType) {
        Log.e(TAG, "onEndDownload");

        requestTypeX = requestType;

        Log.e(TAG, "Request: " + requestType);
        if (downloadStarted) {
            (new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(550);
                        runOnUiThread(new Runnable() // start actions in UI thread
                        {
                            @Override
                            public void run() {
                                showDailySalesView();
                                downloadStarted = false;

                            }
                        });
                    } catch (InterruptedException e) {
                        Log.e(TAG, "InteruptedException: " + e.toString());
                    }
                }
            })).start();
        } else {
            showDailySalesView();
        }

    }

    private void showDailySalesView() {
        if (viewPager.getCurrentItem() == 0) {
            viewPagerAdapter.hideViewDataOptions(Update.DAY, false);
            viewPagerAdapter.updateDailySalesCardValues();
            viewPagerAdapter.showDailyProgressHUD(false);
        }

        if (viewPager.getCurrentItem() == 1) {
            Log.e(TAG, "OnEndDownloadWeek...");
            viewPagerAdapter.hideViewDataOptions(Update.WEEK, false);
            viewPagerAdapter.showWeeklyProgressHUD(false);
            viewPagerAdapter.updateWeeklySalesLineChart();
        }

        if (viewPager.getCurrentItem() == 2) {
            viewPagerAdapter.hideViewDataOptions(Update.MONTH, false);
            viewPagerAdapter.updateMonthlySalesLineChart();
            viewPagerAdapter.showMonthlyProgressHUD(false);
        }
    }

    @Override
    public void onFinishDownload() {

    }

    public void showNotificationNoInternet() {
        showNotification(getResources().getString(R.string.notification_no_internet));
        (new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                    runOnUiThread(new Runnable() // start actions in UI thread
                    {
                        @Override
                        public void run() {
                            hideNotification();
                        }
                    });
                } catch (InterruptedException e) {
                    Log.e(TAG, "InteruptedException: " + e.toString());
                }
            }
        })).start();
    }

    public void updateBranches(boolean deleteThisBranch) {
        BatchList<BranchTag> branchTags = new BatchList<>(DatabaseOperation.DELETE, getHelper());
        BatchList<BranchUserAssoc> branchUsers = new BatchList<>(DatabaseOperation.DELETE, getHelper());

        try {
            List<BranchTag> tag = getHelper().getBranchTags().queryBuilder().where().eq("branch_id", currentBranch).query();
            List<BranchUserAssoc> branchUserAssocs = getBranchUsers();

            for (BranchTag t : tag) {
                branchTags.add(t);
                Log.e(TAG, t.getBranch().getId() + "xxxx");
            }

            for(BranchUserAssoc branchUserAssoc : branchUserAssocs) {
                if(branchUserAssoc.getBranch().getId() == currentBranch.getId()) {
                    branchUsers.add(branchUserAssoc);
                    Log.e(TAG, "Adding " + currentBranch.getName() + " to be deleted");
                    userBranchesSpinnerAdapter.updateDeleteBranch(branchUserAssoc);
                    userBranchesSpinnerAdapter.notifyDataSetChanged();

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }

        if (deleteThisBranch) {
            //                getHelper().getBranchUserAssocs().queryBuilder().where().eq("branch_id", currentBranch).queryForFirst().deleteTo(getHelper());
//                getHelper().getBranchTags().queryBuilder().where().eq("branch_id", currentBranch).queryForFirst().deleteTo(getHelper());

            getHelper().batchCreateOrUpdateBranchTags(branchTags, DatabaseOperation.DELETE);
            getHelper().batchCreateOrUpdateBranchAssocs(branchUsers, DatabaseOperation.DELETE);
            currentBranch.deleteTo(getHelper());

            userBranchesSpinnerAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onErrorDownload(String message) {
        HUDTools.hideIndeterminateProgressDialog();

        Log.e(TAG, "onErrorDownload " + message);

        String no_internet = getResources().getString(R.string.error_response_no_internet);
        String invalid_branch = getResources().getString(R.string.error_response_invalid_branch);

        String branch_is_not_acessible = getResources().getString(R.string.error_response_branch_is_not_accessible);
        String function_is_restricted = getResources().getString(R.string.error_response_function_is_restricted);
        String http_basic = "401";

        final OnReloadBranches onReloadBranches = this;


        if (message.equals(no_internet)) {
            showNotificationNoInternet();
        } else if (message.equals(invalid_branch)) {
            DialogTools.showBasicWithTitle(HomeActivity.this,
                     getResources().getString(R.string.error_dialog_title_invalid_branch),
                     getResources().getString(R.string.error_dialog_message_invalid_branch),
                     "OK", null, false, new MaterialDialog.ButtonCallback() {
                         @Override
                         public void onPositive(MaterialDialog dialog) {
                             super.onPositive(dialog);
                             updateBranches(true);
                             reloadBranchUsers(onReloadBranches);
                             dialog.dismiss();
                         }
                     });
        } else if (message.equals(branch_is_not_acessible)) {
            DialogTools.showBasicWithTitle(HomeActivity.this,
                    getResources().getString(R.string.error_dialog_title_branch_is_not_accessible),
                    getResources().getString(R.string.error_dialog_message_branch_is_not_accessible),
                    "OK", null, false, new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);

                            updateBranches(true);
                            dialog.dismiss();
                        }
                    });
        } else if (message.equals(function_is_restricted)) {
            DialogTools.showBasicWithTitle(HomeActivity.this,
                    getResources().getString(R.string.error_dialog_title_function_is_restricted),
                    getResources().getString(R.string.error_dialog_message_function_is_restricted),
                    "OK", null, false, new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);

                            // delete all data

                            dialog.dismiss();
                        }
                    });
        } else if (message.equals(http_basic)) {
            DialogTools.showBasicWithTitle(HomeActivity.this,
                    getResources().getString(R.string.error_dialog_title_http_basic),
                    getResources().getString(R.string.error_dialog_message_http_basic),
                    "OK", null, false, new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);

                            // delete all data

                            dialog.dismiss();
                        }
                    });
        } else {
            DialogTools.showBasicWithTitle(HomeActivity.this, getResources().getString(R.string.error_response_title),
                    "Please contact admin. " + message,
                    "Okay", null, false, new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            dialog.dismiss();
                        }
                    });
        }

        if (viewPager.getCurrentItem() == 0) {
            viewPagerAdapter.showDailyProgressHUD(false);
            viewPagerAdapter.hideViewDataOptions(Update.DAY, true);
            viewPagerAdapter.updateDailySalesCardValues();
        }

        if (viewPager.getCurrentItem() == 1) {
            viewPagerAdapter.showWeeklyProgressHUD(false);
            viewPagerAdapter.hideViewDataOptions(Update.WEEK, true);
            viewPagerAdapter.updateWeeklySalesLineChart();
        }

        if (viewPager.getCurrentItem() == 2) {
            viewPagerAdapter.showMonthlyProgressHUD(false);
            viewPagerAdapter.hideViewDataOptions(Update.MONTH, true);
            viewPagerAdapter.updateMonthlySalesLineChart();
        }
    }

    @Override
    public void onStart(Table table, RequestType requestType) {
        Log.e(TAG, "starting downloading today");
    }

    @Override
    public void onSuccess(Table table, RequestType requestType, Object response) {
        Log.e(TAG, "response: " + response.toString());
    }

    @Override
    public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
        Log.e(TAG, "response code: " + responseCode);
        if (hasInternet) {
            Log.e(TAG, "no internet");
        } else {
            if (response != null) {
                Log.e(TAG, "response: " + response.toString());
            } else {
                Log.e(TAG, "Response is null");

            }
        }
    }

    @Override
    public void onRequestError() {
        Log.e(TAG, "Request Error: ");
    }

    @Override
    public void finishedReloading() {
        HUDTools.hideIndeterminateProgressDialog();
        if (spinner != null) {
            userBranchesSpinnerAdapter = new UserBranchesSpinnerAdapter(getLayoutInflater(), getBranchUsers());
            spinner.setAdapter(userBranchesSpinnerAdapter);
        }
    }

    @Override
    public void showBasicDialogMessageReloadBranches(String message, String title) {
        DialogTools.showBasicWithTitle(HomeActivity.this, title, message, "Ok", null, true, new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                dialog.dismiss();
            }
        });
    }

    @Override
    public void showProgressHudReloadBranches(String message) {
        HUDTools.showIndeterminateProgressHUD(HomeActivity.this, message, false);
    }

    public void showNotification(String message) {
        if (isRefreshing || downloadStarted) {
            Log.e(TAG, "Showing notification");
            viewPagerAdapter.showNotificationMessage(message, isRefreshing, viewPager.getCurrentItem());
        } else {
            Log.e(TAG, "Can't Show Notification");
        }
    }

    public void hideNotification() {
        Log.e(TAG, "hide notification");
        if (isRefreshing) {
            viewPagerAdapter.hideDailyNotificationMessage(isRefreshing, viewPager.getCurrentItem());
        }
        isRefreshing = false;
        downloadStarted = false;
    }
}