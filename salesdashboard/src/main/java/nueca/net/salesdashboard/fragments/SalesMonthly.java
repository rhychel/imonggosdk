package nueca.net.salesdashboard.fragments;

import android.support.v4.widget.SwipeRefreshLayout;

import nueca.net.salesdashboard.enums.Update;
import nueca.net.salesdashboard.operations.SyncDailySales;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public class SalesMonthly extends SimpleMonthlySalesFragment {

    private static SalesMonthly instance = null;
    public static String TAG = "SalesMonthly";
    public static SalesMonthly getInstance() {
        if(instance == null) {
            instance = new SalesMonthly();
        }
        return instance;
    }

    @Override
    public void isRefreshing(SwipeRefreshLayout swipeRefreshLayout) {
        super.isRefreshing(swipeRefreshLayout);

        getSalesRefreshListener().onRefresh(Update.MONTH, swipeRefreshLayout);
    }


    @Override
    public void onStop(){
        super.onStop();
        getSalesRefreshListener().onPageStopped(SyncDailySales.REQUEST_TAG_MONTHLY);
    }

}
