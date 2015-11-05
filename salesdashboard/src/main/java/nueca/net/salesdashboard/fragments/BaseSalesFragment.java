package nueca.net.salesdashboard.fragments;

import android.support.v4.widget.SwipeRefreshLayout;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
import net.nueca.imonggosdk.objects.DailySales;

import java.sql.SQLException;
import java.util.List;

import lecho.lib.hellocharts.view.LineChartView;
import nueca.net.salesdashboard.exceptions.DailySalesFragmentException;
import nueca.net.salesdashboard.interfaces.OnPageChangeWeeklyListener;
import nueca.net.salesdashboard.interfaces.SalesRefreshListener;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public abstract class BaseSalesFragment extends ImonggoFragment {

    public static String TAG = "BaseSalesFragment";
    private SalesRefreshListener salesRefreshListener;
    private OnPageChangeWeeklyListener onPageChangeWeeklyListener;

    private LineChartView mLineChartView = null;
    private int mViewPortLeft = 0, mViewPortRight = 0, mViewPortTop = 0, mViewPortBottom = 0;


    public abstract void isRefreshing(SwipeRefreshLayout swipeRefreshLayout);

    public void setSalesRefreshListener(SalesRefreshListener salesRefreshListener) {
        this.salesRefreshListener = salesRefreshListener;
    }

    public void setOnPageChangeWeeklyListener(OnPageChangeWeeklyListener onPageChangeWeeklyListener) {
        this.onPageChangeWeeklyListener = onPageChangeWeeklyListener;
    }

    public OnPageChangeWeeklyListener getOnPageChangeWeeklyListener() {
        return onPageChangeWeeklyListener;
    }

    public SalesRefreshListener getSalesRefreshListener() {
        return this.salesRefreshListener;
    }

    @Override
    public ImonggoDBHelper getHelper() {
        if (super.getHelper() == null)
            throw new DailySalesFragmentException("dbHelper is null. Use " + this.getClass().getSimpleName() + ".setHelper().");
        return super.getHelper();
    }

    public DailySales getDailySales(int branch_id, String date) throws SQLException {
        List<DailySales> listDailySales = super.getHelper().getDailySales().queryBuilder().where().eq("branch_id", branch_id).query();
        DailySales dailySales = null;

        if (listDailySales.size() != 0) {
            for (DailySales ds : listDailySales) {
                if (ds.getDate_of_sales().equals(date)) {
                    dailySales = ds;
                }
            }
        }
        return dailySales;
    }
}