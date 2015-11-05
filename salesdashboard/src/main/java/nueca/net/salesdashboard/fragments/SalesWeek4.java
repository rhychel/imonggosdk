package nueca.net.salesdashboard.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nueca.imonggosdk.objects.DailySales;
import net.nueca.imonggosdk.tools.SettingTools;

import java.sql.SQLException;

import nueca.net.salesdashboard.R;
import nueca.net.salesdashboard.enums.Update;
import nueca.net.salesdashboard.enums.UpdateWeekType;
import nueca.net.salesdashboard.operations.SyncDailySales;
import nueca.net.salesdashboard.tools.DateTools;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public class SalesWeek4 extends SimpleWeekSalesFragment {
    public static SalesWeek4 instance = null;
    private String TAG = "SalesWeek4";

    public static SalesWeek4 getInstance() {
        if (instance == null) {
            return new SalesWeek4();
        }
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        try {
            generateValues();
            int branch_id = Integer.parseInt(SettingTools.currentBranchId(getActivity().getApplicationContext()));
            DailySales d = getDailySales(branch_id, getDateComparing2().get(0));
            if(d != null) {
                updateWeeklySalesLineChart();
            } else {
                hideLineChart();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return mWeeklySalesView;
    }

    @Override
    public String getWeekTitle() {
        return getResources().getString(R.string.weekly_title_4);
    }

    @Override
    public void updateWeeklySalesLineChart() throws SQLException {
        generateValues();
        generateData();
        if(HaveData()) {
            resetViewPort(0, 6, getMaxViewPortTop(), 0);
            updateSalesSummation();
            animateLineChart();
        } else {
            hideLineChart();
        }
    }

    private void generateValues() {
        setDateComparing1(DateTools.getDatesOfLast3Weeks());
        setDateComparing2(DateTools.getDatesOfLast4Weeks());
    }

    @Override
    public void isRefreshing(SwipeRefreshLayout swipeRefreshLayout) {
        super.isRefreshing(swipeRefreshLayout);
        getSalesRefreshListener().onRefresh(Update.WEEK, UpdateWeekType.LAST_3_WEEKS, swipeRefreshLayout);
    }

    @Override
    public void onStop(){
        super.onStop();
        getSalesRefreshListener().onPageStopped(SyncDailySales.REQUEST_TAG_WEEKLY4);
    }
}
