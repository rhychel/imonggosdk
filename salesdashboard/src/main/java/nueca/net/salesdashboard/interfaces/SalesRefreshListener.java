package nueca.net.salesdashboard.interfaces;

import android.support.v4.widget.SwipeRefreshLayout;

import nueca.net.salesdashboard.enums.Update;
import nueca.net.salesdashboard.enums.UpdateWeekType;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public interface SalesRefreshListener {
    void onRefresh(Update update, UpdateWeekType updateWeekType, SwipeRefreshLayout swipeRefreshLayout);
    void onRefresh(Update update, SwipeRefreshLayout swipeRefreshLayout);
    void onPageStopped(String message);
}
