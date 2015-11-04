package nueca.net.salesdashboard.fragments;

import android.support.v4.widget.SwipeRefreshLayout;

import nueca.net.salesdashboard.enums.Update;
import nueca.net.salesdashboard.enums.UpdateDayType;
import nueca.net.salesdashboard.operations.SyncDailySales;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public class SalesDaily extends SimpleDailySalesFragment {
    public static String TAG = "SalesDaily";
    public static SalesDaily instance = null;

    public static SalesDaily getInstance() {
        if(instance == null) {
            return new SalesDaily();
        }
        return instance;
    }

    @Override
    public void isRefreshing(SwipeRefreshLayout swipeRefreshLayout) {
        super.isRefreshing(swipeRefreshLayout);

        getSalesRefreshListener().onRefresh(Update.DAY, swipeRefreshLayout);
    }

    public void updateCardDate(UpdateDayType update, String date) {

        switch (update) {
            case TODAY:
                getTodayDate().setText(date);
                break;
            case YESTERDAY:
                getTodayDate().setText(date);
                break;
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        getSalesRefreshListener().onPageStopped(SyncDailySales.REQUEST_TAG_DAILY);
    }
}
