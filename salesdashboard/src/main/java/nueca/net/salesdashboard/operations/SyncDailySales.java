package nueca.net.salesdashboard.operations;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DailySalesEnums;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Parameter;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchTag;
import net.nueca.imonggosdk.objects.DailySales;
import net.nueca.imonggosdk.objects.LastUpdatedAt;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;
import net.nueca.imonggosdk.objects.base.BatchList;
import net.nueca.imonggosdk.operations.ImonggoTools;
import net.nueca.imonggosdk.operations.http.ImonggoOperations;
import net.nueca.imonggosdk.tools.DateTimeTools;
import net.nueca.imonggosdk.tools.LastUpdateAtTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import nueca.net.salesdashboard.enums.Update;
import nueca.net.salesdashboard.enums.UpdateDayType;
import nueca.net.salesdashboard.enums.UpdateType;
import nueca.net.salesdashboard.enums.UpdateWeekType;
import nueca.net.salesdashboard.interfaces.OnReloadBranches;
import nueca.net.salesdashboard.interfaces.SyncDailySalesListener;
import nueca.net.salesdashboard.tools.DateTools;
import nueca.net.salesdashboard.tools.HUDTools;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public class SyncDailySales implements VolleyRequestListener {

    private SyncDailySalesListener mSyncDailySalesListener;
    private Context mContext;
    private String mCurrentBranchId;
    private Session mSession;
    private RequestQueue mRequestQueue;
    private ImonggoDBHelper mDBHelper;
    private String TAG = "SyncDailySales";
    private Gson gson = new GsonBuilder().serializeNulls().create();
    private List<String> dateThisWeek;
    private List<String> dateLastWeek;
    private List<String> dateLast2Weeks;
    private List<String> dateLast3Weeks;
    private List<String> dateLast4Weeks;
    private List<String> dateThisMonth;
    private int thisWeekIndex = 0;
    private int thisMonthIndex = 0;
    private int lastWeekIndex = 0;
    private int last2WeeksIndex = 0;
    private int last3WeeksIndex = 0;
    private int last4WeeksIndex = 0;
    private UpdateWeekType updateWeekType = UpdateWeekType.THIS_WEEK;
    private UpdateWeekType startUpdateWeekType = UpdateWeekType.THIS_WEEK;
    private int page = 1;
    private int count = 0;
    private int numberOfPages = 1;
    private OnReloadBranches onReloadBranches;

    public static final String REQUEST_TAG_DAILY = "DAILY";
    public static final String REQUEST_TAG_WEEKLY1 = "THIS_WEEK";
    public static final String REQUEST_TAG_WEEKLY2 = "LAST_WEEK";
    public static final String REQUEST_TAG_WEEKLY3 = "2_WEEKS_AGO";
    public static final String REQUEST_TAG_WEEKLY4 = "3_WEEKS_AGO";
    public static final String REQUEST_TAG_MONTHLY = "MONTHLY";
    private String TAG_FOR_WEEK = "";

    protected LastUpdatedAt lastUpdatedAt = null;
    protected LastUpdatedAt newLastUpdatedAt = null;

    public SyncDailySales() {

    }

    public SyncDailySales(Context context, SyncDailySalesListener SyncDailySalesListener,
                          String CurrentBranchId, Session Session, RequestQueue RequestQueue,
                          ImonggoDBHelper DBHelper) {
        this.mSyncDailySalesListener = SyncDailySalesListener;
        this.mCurrentBranchId = CurrentBranchId;
        this.mSession = Session;
        this.mRequestQueue = RequestQueue;
        this.mDBHelper = DBHelper;
        this.mContext = context;

    }

    private void fetchBranchUser() {

    }

    private void fetchYesterday(RequestType requestType, UpdateType updateType, Update update) throws SQLException {
        DailySales dailySales = null;

        dailySales = getDailySales(Integer.parseInt(mCurrentBranchId),
                DateTimeTools.getYesterdayDateTimeWithFormat("yyyy-MM-dd"));

        if (dailySales == null) {
            Log.e(TAG, "Yesterday is not existing downloading..");
            startSyncModuleContents(requestType);
        } else {
            Log.e(TAG, "Yesterday is existing... checking time requested ");
            try {
                if (checkYesterdayRequestedAt(dailySales)) {
                    Log.e(TAG, "not latest, redownloading");
                    startSyncModuleContents(requestType);
                } else {
                    Log.e(TAG, "all is well, next.. download today!");
                    // TODO: check if the requested time in the database is not exceeded 1 hour
                    startFetchingDailySales(updateType, update,
                            RequestType.DAILY_SALES_TODAY);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void fetchToday(RequestType requestType) throws SQLException {
        fetchToday(requestType, UpdateType.DEFAULT_UPDATE);
    }

    private void fetchToday(RequestType requestType, UpdateType updateType) throws SQLException {
        if (updateType == UpdateType.FORCED_UPDATE) {
            Log.e(TAG, "Forcing Update Today's Data..");
            startSyncModuleContents(requestType);
        } else {
            DailySales dailySales = getDailySales(Integer.parseInt(mCurrentBranchId),
                    DateTimeTools.getCurrentDateTimeWithFormat("yyyy-MM-dd"));
            if (dailySales == null) {
                Log.e(TAG, "today's data is missing,  downloading it now..");
                startSyncModuleContents(requestType);
            } else {

                Log.e(TAG, "checking time if outdated.. ");
                try {
                    if (checkTodayRequestedAt(dailySales)) {
                        Log.e(TAG, "not latest, redownloading");
                        startSyncModuleContents(requestType);
                    } else {
                        Log.e(TAG, "all is well! you have the latest today's data requested at " + dailySales.getDate_requested_at());
                        mSyncDailySalesListener.onEndDownload(requestType);
                        mSyncDailySalesListener.onFinishDownload();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void fetchLast3Weeks(RequestType requestType) {
        startUpdateWeekType = UpdateWeekType.LAST_3_WEEKS;
        String downloadThisDate = "";

        if (updateWeekType == UpdateWeekType.LAST_3_WEEKS) {
            Log.e(TAG, "Type: LAST 3 Weeks. Updating Date...");
            if (dateThisWeek == null) {
                Log.e(TAG, "List of LAST 3 Weeks Dates is empty. fill it first");
                return;
            }
            downloadThisDate = dateLast2Weeks.get(last3WeeksIndex);
        }

        if (updateWeekType == UpdateWeekType.LAST_4_WEEKS) {
            Log.e(TAG, "Type: Last 4 Weeks. Updating Date...");
            if (dateLastWeek == null) {
                Log.e(TAG, "List of Last 4 Weeks Dates is empty. fill it first");
                return;
            }
            downloadThisDate = dateLast4Weeks.get(last4WeeksIndex);
        }

        try {
            Log.e(TAG, "Downloading " + downloadThisDate);
            // get the data in the database
            DailySales ds = getHelper().getDailySales().queryBuilder().where().eq("date_of_sales",
                    downloadThisDate).and().eq("branch_id", mCurrentBranchId).queryForFirst();

            // check if it is existing, if false then fetch the data
            if (!isExisting(ds, DailySalesEnums.DATE_OF_DAILY_SALES, Integer.parseInt(mCurrentBranchId))) {
                Log.e(TAG, "Start Downloading " + downloadThisDate);
                TAG_FOR_WEEK = REQUEST_TAG_WEEKLY4;
                startSyncModuleContents(requestType, downloadThisDate);
            } else {
                Log.e(TAG, "daily sale of " + downloadThisDate + " is existing.");
                Log.e(TAG, "database data is up to date.. skipping download");

                if (updateWeekType == UpdateWeekType.LAST_3_WEEKS) {
                    last3WeeksIndex++;
                    Log.e(TAG, "Last 3 Week's Current Downloaded Index: " + last3WeeksIndex + " Max Size: " +
                            dateLast3Weeks.size());

                    if (last3WeeksIndex >= dateLast3Weeks.size()) {
                        Log.e(TAG, "Downloading Last 3 Weeks is finished, Downloading Last 4 Weeks");
                        last3WeeksIndex = 0;
                        updateWeekType = UpdateWeekType.LAST_4_WEEKS;
                    } else {
                        updateWeekType = UpdateWeekType.LAST_3_WEEKS;
                    }

                    fetchLast3Weeks(requestType);
                    return;
                }

                if (updateWeekType == UpdateWeekType.LAST_4_WEEKS) {
                    last4WeeksIndex++;

                    if (last4WeeksIndex >= dateLast4Weeks.size()) {
                        Log.e(TAG, "Downloading Last 4 Week is finished");
                        last4WeeksIndex = 0;
                        last3WeeksIndex = 0;
                        mSyncDailySalesListener.onEndDownload(requestType);
                        mSyncDailySalesListener.onFinishDownload();
                        return;
                    } else {
                        Log.e(TAG, "Last 4 Week's Current Downloaded Index: " + last4WeeksIndex + " Max Size: " + dateLast4Weeks.size());
                        fetchLast3Weeks(requestType);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchLast2Weeks(RequestType requestType) {
        startUpdateWeekType = UpdateWeekType.LAST_2_WEEKS;
        String downloadThisDate = "";

        if (updateWeekType == UpdateWeekType.LAST_2_WEEKS) {
            Log.e(TAG, "Type: LAST 2 Weeks. Updating Date...");
            if (dateThisWeek == null) {
                Log.e(TAG, "List of LAST 2 Weeks Dates is empty. fill it first");
                return;
            }
            downloadThisDate = dateLast2Weeks.get(last2WeeksIndex);
        }

        if (updateWeekType == UpdateWeekType.LAST_3_WEEKS) {
            Log.e(TAG, "Type: Last 3 Weeks. Updating Date...");
            if (dateLastWeek == null) {
                Log.e(TAG, "List of Last 3 Weeks Dates is empty. fill it first");
                return;
            }
            downloadThisDate = dateLast3Weeks.get(last3WeeksIndex);
        }

        try {
            Log.e(TAG, "Downloading " + downloadThisDate);
            // get the data in the database
            DailySales ds = getHelper().getDailySales().queryBuilder().where().eq("date_of_sales",
                    downloadThisDate).and().eq("branch_id", mCurrentBranchId).queryForFirst();

            // check if it is existing, if false then fetch the data
            if (!isExisting(ds, DailySalesEnums.DATE_OF_DAILY_SALES, Integer.parseInt(mCurrentBranchId))) {
                Log.e(TAG, "Start Downloading " + downloadThisDate);
                TAG_FOR_WEEK = REQUEST_TAG_WEEKLY3;
                startSyncModuleContents(requestType, downloadThisDate);
            } else {
                Log.e(TAG, "daily sale of " + downloadThisDate + " is existing.");
                Log.e(TAG, "database data is up to date.. skipping download");

                if (updateWeekType == UpdateWeekType.LAST_2_WEEKS) {
                    last2WeeksIndex++;
                    Log.e(TAG, "Last 2 Week's Current Downloaded Index: " + last2WeeksIndex + " Max Size: " +
                            dateLast2Weeks.size());

                    if (last2WeeksIndex >= dateLast2Weeks.size()) {
                        Log.e(TAG, "Downloading Last 2 Weeks is finished, Downloading Last 3 Weeks");
                        last2WeeksIndex = 0;
                        updateWeekType = UpdateWeekType.LAST_3_WEEKS;
                    } else {
                        updateWeekType = UpdateWeekType.LAST_2_WEEKS;
                    }

                    fetchLast2Weeks(requestType);
                    return;
                }

                if (updateWeekType == UpdateWeekType.LAST_3_WEEKS) {
                    last3WeeksIndex++;

                    if (last3WeeksIndex >= dateLast3Weeks.size()) {
                        Log.e(TAG, "Downloading Last 3 Week is finished");
                        last3WeeksIndex = 0;
                        last2WeeksIndex = 0;
                        mSyncDailySalesListener.onEndDownload(requestType);
                        mSyncDailySalesListener.onFinishDownload();
                        return;
                    } else {
                        Log.e(TAG, "Last 3 Week's Current Downloaded Index: " + last3WeeksIndex + " Max Size: " + dateLast3Weeks.size());
                        fetchLast3Weeks(requestType);
                    }
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void fetchLastWeek(RequestType requestType) {
        startUpdateWeekType = UpdateWeekType.LAST_WEEK;
        String downloadThisDate = "";

        if (updateWeekType == UpdateWeekType.LAST_WEEK) {
            Log.e(TAG, "Type: LAST Week. Updating Date...");
            if (dateLastWeek == null) {
                Log.e(TAG, "List of LAST Week Dates is empty. fill it first");
                return;
            }
            downloadThisDate = dateLastWeek.get(lastWeekIndex);
        }

        if (updateWeekType == UpdateWeekType.LAST_2_WEEKS) {
            Log.e(TAG, "Type: Last 2 Weeks. Updating Date...");
            if (dateLast2Weeks == null) {
                Log.e(TAG, "List of Last 2 Weeks Dates is empty. fill it first");
                return;
            }
            downloadThisDate = dateLast2Weeks.get(last2WeeksIndex);
        }

        try {
            Log.e(TAG, "Downloading " + downloadThisDate);
            // get the data in the database
            DailySales ds = getHelper().getDailySales().queryBuilder().where().eq("date_of_sales",
                    downloadThisDate).and().eq("branch_id", mCurrentBranchId).queryForFirst();

            // check if it is existing, if false then fetch the data
            if (!isExisting(ds, DailySalesEnums.DATE_OF_DAILY_SALES, Integer.parseInt(mCurrentBranchId))) {
                Log.e(TAG, "Start Downloading " + downloadThisDate);
                TAG_FOR_WEEK = REQUEST_TAG_WEEKLY2;
                startSyncModuleContents(requestType, downloadThisDate);
            } else {
                Log.e(TAG, "daily sale of " + downloadThisDate + " is existing.");
                Log.e(TAG, "database data is up to date.. skipping download");

                if (updateWeekType == UpdateWeekType.LAST_WEEK) {
                    lastWeekIndex++;
                    Log.e(TAG, "Last Week's Current Downloaded Index: " + lastWeekIndex + " Max Size: " +
                            dateLastWeek.size());

                    if (lastWeekIndex >= dateLastWeek.size()) {
                        Log.e(TAG, "Downloading Last Week is finished, Downloading Last 2 Weeks");
                        lastWeekIndex = 0;
                        updateWeekType = UpdateWeekType.LAST_2_WEEKS;
                    } else {
                        updateWeekType = UpdateWeekType.LAST_WEEK;
                    }

                    fetchLastWeek(requestType);
                    return;
                }

                if (updateWeekType == UpdateWeekType.LAST_2_WEEKS) {
                    last2WeeksIndex++;

                    if (last2WeeksIndex >= dateLast2Weeks.size()) {
                        Log.e(TAG, "Downloading Last Week is finished");
                        last2WeeksIndex = 0;
                        lastWeekIndex = 0;
                        mSyncDailySalesListener.onEndDownload(requestType);
                        mSyncDailySalesListener.onFinishDownload();
                        return;
                    } else {
                        Log.e(TAG, "Last Week's Current Downloaded Index: " + last2WeeksIndex + " Max Size: " + dateLast2Weeks.size());
                        fetchLastWeek(requestType);
                    }
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void fetchWeek(RequestType requestType) {
        startUpdateWeekType = UpdateWeekType.THIS_WEEK;
        String downloadThisDate = "";
        if (updateWeekType == UpdateWeekType.THIS_WEEK) {
            Log.e(TAG, "Type: This Week. Updating Date...");
            if (dateThisWeek == null) {
                Log.e(TAG, "List of This Week Dates is empty. fill it first");
                return;
            }
            downloadThisDate = dateThisWeek.get(thisWeekIndex);
        }

        if (updateWeekType == UpdateWeekType.LAST_WEEK) {
            Log.e(TAG, "Type: Last Week. Updating Date...");
            if (dateLastWeek == null) {
                Log.e(TAG, "List of Last Week Dates is empty. fill it first");
                return;
            }
            downloadThisDate = dateLastWeek.get(lastWeekIndex);
        }

        try {
            Log.e(TAG, "Downloading " + downloadThisDate);
            // get the data in the database
            DailySales ds = getHelper().getDailySales().queryBuilder().where().eq("date_of_sales",
                    downloadThisDate).and().eq("branch_id", mCurrentBranchId).queryForFirst();

            // check if it is existing, if false then fetch the data
            if (!isExisting(ds, DailySalesEnums.DATE_OF_DAILY_SALES, Integer.parseInt(mCurrentBranchId))) {
                Log.e(TAG, "Start Downloading " + downloadThisDate);
                TAG_FOR_WEEK = REQUEST_TAG_WEEKLY1;
                startSyncModuleContents(requestType, downloadThisDate);
            } else {
                Log.e(TAG, "daily sale of " + downloadThisDate + " is existing.");
                Log.e(TAG, "database data is up to date.. skipping download");

                if (updateWeekType == UpdateWeekType.THIS_WEEK) {
                    thisWeekIndex++;
                    Log.e(TAG, "Week's Current Downloaded Index: " + thisWeekIndex + " Max Size: " +
                            dateThisWeek.size());

                    if (thisWeekIndex >= dateThisWeek.size()) {
                        Log.e(TAG, "Downloading This Week is finished, Downloading Last Week");
                        thisWeekIndex = 0;
                        updateWeekType = UpdateWeekType.LAST_WEEK;
                    } else {
                        updateWeekType = UpdateWeekType.THIS_WEEK;
                    }

                    fetchWeek(requestType);
                    return;
                }

                if (updateWeekType == UpdateWeekType.LAST_WEEK) {
                    lastWeekIndex++;

                    if (lastWeekIndex >= dateLastWeek.size()) {
                        Log.e(TAG, "Downloading Last Week is finished, Downloading Last last Week");
                        lastWeekIndex = 0;
                        thisWeekIndex = 0;
                        mSyncDailySalesListener.onEndDownload(requestType);
                        mSyncDailySalesListener.onFinishDownload();
                    } else {
                        Log.e(TAG, "Last Week's Current Downloaded Index: " + lastWeekIndex + " Max Size: " + dateLastWeek.size());
                        fetchWeek(requestType);
                    }
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchMonth(RequestType requestType) throws SQLException {
        String downloadThisDate = "";


        if (dateThisMonth == null) {
            Log.e(TAG, "This Months Date is empty. initialize it first..");
            return;
        }

        downloadThisDate = dateThisMonth.get(thisMonthIndex);

        Log.e(TAG, "Downloading " + downloadThisDate);

        DailySales ds = getHelper().getDailySales().queryBuilder().where().eq("date_of_sales", downloadThisDate).and().eq("branch_id", mCurrentBranchId).queryForFirst();

        if (!isExisting(ds, DailySalesEnums.DATE_OF_DAILY_SALES, Integer.parseInt(mCurrentBranchId))) {
            startSyncModuleContents(requestType, downloadThisDate);
        } else {
            Log.e(TAG, "daily sale of " + downloadThisDate + " is existing.");
            Log.e(TAG, "database data is up to date.. skipping download");

            thisMonthIndex++;

            if (thisMonthIndex >= dateThisMonth.size()) {
                thisMonthIndex = 0;
                Log.e(TAG, "Downloading This Month is finished..");
                mSyncDailySalesListener.onEndDownload(requestType);
                mSyncDailySalesListener.onFinishDownload();
            } else {
                Log.e(TAG, "This Month's Current Downloaded Index: " + thisMonthIndex + " Max Size: " + dateThisMonth.size());
                fetchMonth(requestType);
            }
        }
    }

    public void startFetchingDailySales(UpdateType updateType, Update update,
                                        RequestType requestType) throws SQLException {
        startFetchingDailySales(updateType, update, requestType, null);
    }

    public void startFetchingBranchUser(OnReloadBranches onReloadBranches) {
        this.onReloadBranches = onReloadBranches;
        newLastUpdatedAt = null;
        lastUpdatedAt = null;

        try {
            QueryBuilder<LastUpdatedAt, Integer> queryBuilder = getHelper().getLastUpdatedAts().queryBuilder();
            queryBuilder.where().eq("tableName", LastUpdateAtTools.getTableToSync(Table.BRANCH_USERS));

            lastUpdatedAt = getHelper().getLastUpdatedAts().queryForFirst(queryBuilder.prepare());

            if (lastUpdatedAt != null) {
                Log.e(TAG, "TABLE NAME: " + lastUpdatedAt.getTableName() + " lastUpdateAt: " + lastUpdatedAt.getLast_updated_at());
            } else {
                Log.e(TAG, "lastUpdatedAt is null");
            }
            ImonggoOperations.getAPIModule(mContext, getQueue(), getSession(),
                    this, Table.BRANCH_USERS, getSession().getServer(), RequestType.COUNT, "?q=last_updated_at");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void startFetchingDailySales(UpdateType updateType, Update update,
                                        RequestType requestType, UpdateWeekType updateWeekTypex) throws SQLException {
        // DEFAULT UPDATE
        if (updateType == UpdateType.DEFAULT_UPDATE) {

            if (update == Update.DAY) { // DAY
                if (requestType == RequestType.DAILY_SALES_YESTERDAY) {
                    fetchYesterday(requestType, updateType, update);
                }
                if (requestType == RequestType.DAILY_SALES_TODAY) {
                    fetchToday(requestType);
                }
            }

            if (update == Update.WEEK) { // WEEK
                getDatesOfWeeks();
                thisWeekIndex = 0;
                lastWeekIndex = 0;
                last2WeeksIndex = 0;
                last3WeeksIndex = 0;
                last4WeeksIndex = 0;

                updateWeekType = updateWeekTypex;

                if (updateWeekTypex == UpdateWeekType.THIS_WEEK) {
                    Log.e(TAG, "startFetchingDailySales This Week");
                    fetchWeek(requestType);
                }

                if (updateWeekTypex == UpdateWeekType.LAST_WEEK) {
                    Log.e(TAG, "startFetchingDailySales Last Week");
                    fetchLastWeek(requestType);
                }

                if (updateWeekTypex == UpdateWeekType.LAST_2_WEEKS) {
                    Log.e(TAG, "startFetchingDailySales Last 2 Weeks");
                    fetchLast2Weeks(requestType);
                }

                if (updateWeekTypex == UpdateWeekType.LAST_3_WEEKS) {
                    Log.e(TAG, "startFetchingDailySales Last 4 Weeks");
                    fetchLast3Weeks(requestType);
                }

            }

            if (update == Update.MONTH) { // MONTH
                getDatesOfThisMonth();
                thisMonthIndex = 0;
                fetchMonth(requestType);
            }
        }
        // FORCED UPDATE
        else if (updateType == UpdateType.FORCED_UPDATE) {
            Log.e(TAG, "Forcing Update " + update);

            if (requestType == RequestType.DAILY_SALES_TODAY) {
                fetchToday(requestType, updateType);
            } else {
                // execute this even if it is for week and month
                fetchYesterday(RequestType.DAILY_SALES_YESTERDAY, UpdateType.FORCED_UPDATE, Update.DAY);
            }
        }
    }

    private void startSyncModuleContents(RequestType requestType) throws SQLException {
        startSyncModuleContents(requestType, "");
    }

    private void startSyncModuleContents(RequestType requestType, String date) throws SQLException {
        if (getHelper() == null) {
            Log.e(TAG, "helper is null");
        }

        // DAY WEEK & MONTH
        if (requestType == RequestType.DAILY_SALES_TODAY) {
            Log.e(TAG, "startSyncModuleContents TODAY");
            ImonggoOperations.getAPIModule(mContext, getQueue(), getSession(),
                    this, Table.DAILY_SALES, getSession().getServer(), requestType,
                    getParameters(Update.DAY, UpdateDayType.TODAY), REQUEST_TAG_DAILY);
        }

        if (requestType == RequestType.DAILY_SALES_YESTERDAY) {
            Log.e(TAG, "startSyncModuleContents YESTERDAY");
            ImonggoOperations.getAPIModule(mContext, getQueue(), getSession(), this,
                    Table.DAILY_SALES, getSession().getServer(), requestType,
                    getParameters(Update.DAY, UpdateDayType.YESTERDAY), REQUEST_TAG_DAILY);
        }

        if (requestType == RequestType.DAILY_SALES_WEEK) {
            Log.e(TAG, "startSyncModuleContents WEEK " + "downloading: " + date);

            ImonggoOperations.getAPIModule(mContext, getQueue(), getSession(), this, Table.DAILY_SALES,
                    getSession().getServer(), requestType, getParameters(Update.WEEK, UpdateDayType.TODAY, date), TAG_FOR_WEEK);

        }

        if (requestType == RequestType.DAILY_SALES_MONTH) {
            Log.e(TAG, "startSyncModuleContents MONTH");
            ImonggoOperations.getAPIModule(mContext, getQueue(), getSession(), this, Table.DAILY_SALES,
                    getSession().getServer(), requestType, getParameters(Update.MONTH, UpdateDayType.TODAY, date), REQUEST_TAG_MONTHLY);
        }
    }

    private boolean checklastUpdatedAt(LastUpdatedAt lastUpdatedAt, LastUpdatedAt newLastUpdatedAt) {

        if (lastUpdatedAt != null && newLastUpdatedAt != null) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss", Locale.ENGLISH);

                GregorianCalendar calendar1 = (GregorianCalendar) Calendar.getInstance();
                GregorianCalendar calendar2 = (GregorianCalendar) Calendar.getInstance();


                String dateparse1 = lastUpdatedAt.getLast_updated_at();
                String dateparse2 = newLastUpdatedAt.getLast_updated_at();

                java.util.Date date1 = dateFormat.parse(dateparse1);
                java.util.Date date2 = dateFormat.parse(dateparse2);

                calendar1.setTime(date1);
                calendar2.setTime(date2);


                int comparison_result = calendar2.compareTo(calendar1);
                Log.e(TAG, dateparse1 + " < " + dateparse2);

                Log.e(TAG, "Compare Result : " + comparison_result);
                return comparison_result == 0;
            } catch (ParseException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        }

        return false;
    }

    private boolean checkYesterdayRequestedAt(DailySales dailySales)
            throws SQLException, ParseException {
        if (dailySales != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            GregorianCalendar calendar1 = (GregorianCalendar) Calendar.getInstance();
            GregorianCalendar calendar2 = (GregorianCalendar) Calendar.getInstance();

            String dateparse1 = dailySales.getDate_requested_at();
            String dateparse2 = dailySales.getDate_of_sales() + " 23:59:59";
            Log.e(TAG, dateparse2);

            java.util.Date date1 = dateFormat.parse(dateparse1);
            java.util.Date date2 = dateFormat.parse(dateparse2);

            calendar1.setTime(date1);
            calendar2.setTime(date2);

            int comparison_result = calendar2.compareTo(calendar1);
            Log.e(TAG, "Compare Result : " + comparison_result);
            Log.e(TAG, dateparse1 + " < " + dateparse2);

            if (comparison_result == 1) {
                return true;
            } else if (comparison_result == 0 || comparison_result == -1) {
                return false;
            }
        }
        return false;
    }


    private boolean checkTodayRequestedAt(DailySales dailySales)
            throws SQLException, ParseException {
        if (dailySales != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            GregorianCalendar calendar1 = (GregorianCalendar) Calendar.getInstance();
            GregorianCalendar calendar2 = (GregorianCalendar) Calendar.getInstance();


            String dateparse1 = dailySales.getDate_requested_at();

            java.util.Date date1 = dateFormat.parse(dateparse1);
            java.util.Date date2 = dateFormat.parse(DateTimeTools.getCurrentDateTimeWithFormat("yyyy-MM-dd hh:mm:ss"));

            calendar1.setTime(date1);
            calendar2.setTime(date2);

/*
            if(calendar1.get(Calendar.HOUR) == 0) {
                Log.e(TAG, "Hour is Zero1");
                calendar1.set(Calendar.HOUR, 12);
            }

            if(calendar2.get(Calendar.HOUR) == 0) {
                Log.e(TAG, "Hour is Zero2");
                calendar2.set(Calendar.HOUR, 12);
            }
*/

            Log.e(TAG, calendar1.getTime().toString() + " < " + calendar2.getTime().toString());
            if (calendar1.get(Calendar.DAY_OF_MONTH) > calendar2.get(Calendar.DAY_OF_MONTH)) {
                return true;
            } else {
                // int comparison_result = calendar2.compareTo(calendar1);
                Log.e(TAG, "Compare Result : " + "Hour from db: " + calendar1.get(Calendar.HOUR) + " == " +
                        "Current Hour: " + calendar2.get(Calendar.HOUR));

                if (calendar1.get(Calendar.HOUR) == calendar2.get(Calendar.HOUR)) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }


    private String getParameters(Update update, UpdateDayType updateDayType) {
        try {
            return getParameters(update, updateDayType, "");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String getParameters(Update update, UpdateDayType updateDayType, String date)
            throws SQLException {

        switch (update) {
            case DAY:
                if (updateDayType == UpdateDayType.TODAY) {
                    return String.format(ImonggoTools.generateParameter(Parameter.CURRENT_DATE, Parameter.BRANCH_ID),
                            DateTimeTools.getCurrentDateTimeWithFormat("yyyy-MM-dd"), mCurrentBranchId);
                } else if (updateDayType == UpdateDayType.YESTERDAY) {
                    return String.format(ImonggoTools.generateParameter(Parameter.CURRENT_DATE, Parameter.BRANCH_ID),
                            DateTimeTools.getYesterdayDateTimeWithFormat("yyyy-MM-dd"), mCurrentBranchId);
                }
            case WEEK:
                return String.format(ImonggoTools.generateParameter(Parameter.CURRENT_DATE,
                                Parameter.BRANCH_ID),
                        date, mCurrentBranchId);

            case MONTH:
                return String.format(ImonggoTools.generateParameter(Parameter.CURRENT_DATE,
                                Parameter.BRANCH_ID),
                        date, mCurrentBranchId);
        }


        return "";
    }

    private DailySales getDailySales(int branch_id, String date) throws SQLException {

        List<DailySales> listDailySales =
                mDBHelper.getDailySales().queryBuilder().where().eq("branch_id", branch_id).query();
        DailySales dailySales = null;

        if (listDailySales.size() != 0) {
            for (DailySales ds : listDailySales) {
                if (ds.getDate_of_sales().equals(date)) {
                    dailySales = ds;
                }
            }
        } else {
            Log.e(TAG, "DailySales is empty");
        }
        return dailySales;
    }

    public boolean isExisting(Object o, Table table) throws SQLException {
        Branch branch = (Branch) o;
        return getHelper().getBranches().queryBuilder().where().eq("id", branch.getId()).queryForFirst() != null;
    }

    private boolean isExisting(Object o, DailySalesEnums dailySalesEnums, int branch_id) throws SQLException {

        DailySales dailySales = (DailySales) o;

        if (dailySales != null && dailySalesEnums == DailySalesEnums.DATE_OF_DAILY_SALES) {
            return getHelper().getDailySales().queryBuilder().where().eq("date_of_sales", dailySales.getDate_of_sales()).and().eq("branch_id", branch_id).queryForFirst() != null;
        } else if (dailySalesEnums == DailySalesEnums.DATE_REQUESTED) {

            DailySales dailySalesDB = getHelper().getDailySales().queryBuilder().where().eq("date_of_sales", dailySales.getDate_of_sales()).and().eq("branch_id", branch_id).queryForFirst();

            if (dailySalesDB != null) {
                Log.e(TAG, "daily sales db: " + dailySalesDB.toString());

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                GregorianCalendar calendar1 = (GregorianCalendar) Calendar.getInstance();
                GregorianCalendar calendar2 = (GregorianCalendar) Calendar.getInstance();

                try {
                    String dateparse1 = dailySales.getDate_requested_at();
                    String dateparse2 = dailySalesDB.getDate_requested_at();

                    java.util.Date date1 = dateFormat.parse(dateparse1);
                    java.util.Date date2 = dateFormat.parse(dateparse2);

                    calendar1.setTime(date1);
                    calendar2.setTime(date2);

                    int comparison_result = calendar1.compareTo(calendar2);
                    Log.e(TAG, "Compare Result : " + comparison_result);
                    Log.e(TAG, dateparse2 + " < " + dateparse1);

                    if (comparison_result == 1) {
                        return true;
                    } else if (comparison_result == 0 || comparison_result == -1) {
                        return false;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "dailysalesDB is null");
                return false;
            }
        }
        return false;
    }

    private void getDatesOfThisMonth() {
        dateThisMonth = DateTools.getDatesThisMonth();
    }

    private void getDatesOfWeeks() {
        dateThisWeek = DateTools.getDatesOfThisWeek();
        dateLastWeek = DateTools.getDatesOfLastWeek();
        dateLast2Weeks = DateTools.getDatesOfLast2Weeks();
        dateLast3Weeks = DateTools.getDatesOfLast3Weeks();
        dateLast4Weeks = DateTools.getDatesOfLast4Weeks();
    }

    private void getDatesOfLastWeek() {
        dateLastWeek = DateTools.getDatesOfLastWeek();
    }

    public SyncDailySalesListener getSyncDailySalesListener() {
        return mSyncDailySalesListener;
    }

    public void setSyncDailySalesListener(SyncDailySalesListener mSyncDailySalesListener) {
        this.mSyncDailySalesListener = mSyncDailySalesListener;
    }

    public RequestQueue getQueue() {
        return mRequestQueue;
    }

    public void setQueue(RequestQueue mRequestQueue) {
        this.mRequestQueue = mRequestQueue;
    }

    public String getCurrentBranchId() {
        return mCurrentBranchId;
    }

    public void setCurrentBranchId(String mCurrentBranchId) {
        this.mCurrentBranchId = mCurrentBranchId;
    }

    public Session getSession() {
        return mSession;
    }

    public void setSession(Session mSession) {
        this.mSession = mSession;
    }

    public ImonggoDBHelper getHelper() {
        return mDBHelper;
    }

    public void setHelper(ImonggoDBHelper mDBHelper) {
        this.mDBHelper = mDBHelper;
    }

    @Override
    public void onStart(Table table, RequestType requestType) {
        mSyncDailySalesListener.onStartDownload(requestType);
    }

    @Override
    public void onSuccess(Table table, RequestType requestType, Object response) {

        Log.e(TAG, "Request Type: " + requestType);
        Log.e(TAG, "Update Week Type: " + updateWeekType);
        Log.e(TAG, "Success: " + response.toString());
        Log.e(TAG, "startUpdateType: " + startUpdateWeekType);
        //Log.e(TAG, "Current Date: " + date)

        if (response instanceof JSONObject) {
            if (requestType == RequestType.LAST_UPDATED_AT) {

                JSONObject jsonObject = (JSONObject) response;

                newLastUpdatedAt = gson.fromJson(jsonObject.toString(), LastUpdatedAt.class);
                newLastUpdatedAt.setTableName(LastUpdateAtTools.getTableToSync(table));

                if (lastUpdatedAt != null) {
                    Log.e(TAG, "Updating lastUpdateAt");
                    newLastUpdatedAt.updateTo(getHelper());
                    Log.e(TAG, "LastUpdatedAt from database" + lastUpdatedAt.getLast_updated_at());
                    Log.e(TAG, "LastUpdatedAt from imonggo " + newLastUpdatedAt.getLast_updated_at());
                } else {
                    newLastUpdatedAt.insertTo(getHelper());
                    Log.e(TAG, "New Last Updated At: " + jsonObject.toString());
                }

                count = 0;
                page = 1;

                if (!checklastUpdatedAt(lastUpdatedAt, newLastUpdatedAt)) {
                    Log.e(TAG, "They are not the same downloading branch users");
                    HUDTools.hideIndeterminateProgressDialog();
                    onReloadBranches.showProgressHudReloadBranches("Checking your branch...");
                    fetchBranchUsersCount();
                    return;
                } else {
                    Log.e(TAG, "All is well.");
                    HUDTools.hideIndeterminateProgressDialog();
                    onReloadBranches.showBasicDialogMessageReloadBranches("Your branch list is up to date.", "We're good!");
                }

                return;
            }

            if (requestType == RequestType.COUNT) {
                JSONObject jsonObject = (JSONObject) response;
                try {
                    count = jsonObject.getInt("count");
                    Log.e(TAG, "Count....... is " + count);

                    if (count != 0) {
                        numberOfPages = ((int) Math.ceil(count / 50.0));

                    } else {
                        Log.e(TAG, "Branch Users is empty");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                ImonggoOperations.getAPIModule(mContext, getQueue(), getSession(), this,
                        Table.BRANCH_USERS, getSession().getServer(), RequestType.API_CONTENT,
                        String.format(ImonggoTools.generateParameter(Parameter.PAGE, Parameter.USER_ID, Parameter.AFTER),
                                String.valueOf(page), String.valueOf(getSession().getUser().getId()), DateTimeTools.convertDateForUrl(lastUpdatedAt.getLast_updated_at())));
                return;
            }


            String date_today_updated_at = DateTimeTools.getCurrentDateTimeWithFormat("yyyy-MM-dd");
            String date_today_requested_at = DateTimeTools.getCurrentDateTimeWithFormat("yyyy-MM-dd HH:mm:ss");
            String date_yesterday_updated_at = DateTimeTools.getYesterdayDateTimeWithFormat("yyyy-MM-dd");

            try {
                JSONObject jsonObject = (JSONObject) response;
                DailySales dailySales = gson.fromJson(jsonObject.toString(), DailySales.class);

                if (requestType == RequestType.DAILY_SALES_YESTERDAY) {
                    Log.e(TAG, "Successfully downloaded yesterday, downloading today");
                    dailySales.setDate_of_sales(date_yesterday_updated_at);
                    dailySales.setDate_requested_at(date_today_requested_at);
                    dailySales.setBranch_id(Integer.parseInt(mCurrentBranchId));


                    if (!isExisting(dailySales, DailySalesEnums.DATE_OF_DAILY_SALES, Integer.parseInt(mCurrentBranchId))) {
                        dailySales.dbOperation(getHelper(), DatabaseOperation.INSERT);
                    } else {
                        //  Log.e(TAG, "DailySale is existing checking for time");
                        if (isExisting(dailySales, DailySalesEnums.DATE_REQUESTED, Integer.parseInt(mCurrentBranchId))) {
                            //   Log.e(TAG, "--Fetched date time is the most recent... updating database");
                            getHelper().getDailySales().deleteBuilder().where().eq("date_requested_at", date_today_requested_at);
                            dailySales.dbOperation(getHelper(), DatabaseOperation.INSERT);
                        } else {
                            Log.e(TAG, "database yesterday's data is up to date");
                        }
                    }

                    try { // after Successfully downloaded weekly data.wnloaded yesterday's data, start downloading today's data
                        startSyncModuleContents(RequestType.DAILY_SALES_TODAY);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else if (requestType == RequestType.DAILY_SALES_TODAY) {
                    Log.e(TAG, "Successfully downloaded today");

                    dailySales.setDate_of_sales(date_today_updated_at);
                    dailySales.setDate_requested_at(date_today_requested_at);
                    dailySales.setBranch_id(Integer.parseInt(mCurrentBranchId));


                    getHelper().getDailySales().deleteBuilder().where().eq("date_requested_at", date_today_requested_at);
                    dailySales.dbOperation(getHelper(), DatabaseOperation.INSERT);

                    mSyncDailySalesListener.onEndDownload(requestType);
                    mSyncDailySalesListener.onFinishDownload();

                } else if (requestType == RequestType.DAILY_SALES_WEEK) {
                    Log.e(TAG, "Successfully downloaded weekly data. " + dailySales.getAmount());

                    if (updateWeekType == UpdateWeekType.THIS_WEEK) {
                        dailySales.setDate_of_sales(dateThisWeek.get(thisWeekIndex));
                        dailySales.setDate_requested_at(date_today_requested_at);
                        dailySales.setBranch_id(Integer.parseInt(mCurrentBranchId));
                        thisWeekIndex++;
                        Log.e(TAG, "Week's Current Downloaded Index: " + thisWeekIndex + " Max Size: " + dateThisWeek.size());
                        if (!isExisting(dailySales, DailySalesEnums.DATE_OF_DAILY_SALES, Integer.parseInt(mCurrentBranchId))) {
                            dailySales.dbOperation(getHelper(), DatabaseOperation.INSERT);
                        } else {
                            //  Log.e(TAG, "DailySale is existing checking for time");
                            if (isExisting(dailySales, DailySalesEnums.DATE_REQUESTED, Integer.parseInt(mCurrentBranchId))) {
                                //   Log.e(TAG, "Fetched date time is the most recent... updating database");
                                getHelper().getDailySales().deleteBuilder().where().eq("date_requested_at", date_today_requested_at);
                                dailySales.dbOperation(getHelper(), DatabaseOperation.INSERT);
                            } else {
                                Log.e(TAG, "database yesterday's data is up to date");
                            }
                        }
                        if (thisWeekIndex >= dateThisWeek.size()) {
                            lastWeekIndex = 0;
                            thisWeekIndex = 0;
                            Log.e(TAG, "Downloading This Week is finished, Downloading Last Week");
                            updateWeekType = UpdateWeekType.LAST_WEEK;
                        } else {
                            Log.e(TAG, "This Week's Current Downloaded Index: " + thisWeekIndex + " Max Size: " + dateThisWeek.size());
                            fetchWeek(requestType);
                            return;
                        }
                    }

                    if (updateWeekType == UpdateWeekType.LAST_WEEK) {

                        dailySales.setDate_of_sales(dateLastWeek.get(lastWeekIndex));
                        dailySales.setDate_requested_at(date_today_requested_at);
                        dailySales.setBranch_id(Integer.parseInt(mCurrentBranchId));

                        lastWeekIndex++;

                        if (!isExisting(dailySales, DailySalesEnums.DATE_OF_DAILY_SALES, Integer.parseInt(mCurrentBranchId))) {
                            dailySales.dbOperation(getHelper(), DatabaseOperation.INSERT);
                        } else {
                            //  Log.e(TAG, "DailySale is existing checking for time");
                            if (isExisting(dailySales, DailySalesEnums.DATE_REQUESTED, Integer.parseInt(mCurrentBranchId))) {
                                //   Log.e(TAG, "Fetched date time is the most recent... updating database");
                                getHelper().getDailySales().deleteBuilder().where().eq("date_requested_at", date_today_requested_at);
                                dailySales.dbOperation(getHelper(), DatabaseOperation.INSERT);
                            } else {
                                Log.e(TAG, "database last week data is up to date");
                            }
                        }

                        if (lastWeekIndex >= dateLastWeek.size()) {
                            Log.e(TAG, "Downloading Last Week is finished");
                            lastWeekIndex = 0;
                            thisWeekIndex = 0;
                            if (startUpdateWeekType == UpdateWeekType.THIS_WEEK) {
                                mSyncDailySalesListener.onEndDownload(requestType);
                                mSyncDailySalesListener.onFinishDownload();
                            } else {
                                Log.e(TAG, "Downloading Last 2 Weeks");
                                updateWeekType = UpdateWeekType.LAST_2_WEEKS;
                            }
                        } else {
                            Log.e(TAG, "Last Week's Current Downloaded Index: " + lastWeekIndex + " Max Size: " + dateLastWeek.size());
                            if (startUpdateWeekType == UpdateWeekType.THIS_WEEK) {
                                fetchWeek(requestType);
                            } else if (startUpdateWeekType == UpdateWeekType.LAST_WEEK) {
                                fetchLastWeek(requestType);
                            }
                            return;
                        }
                    }

                    if (updateWeekType == UpdateWeekType.LAST_2_WEEKS) {

                        dailySales.setDate_of_sales(dateLast2Weeks.get(last2WeeksIndex));
                        dailySales.setDate_requested_at(date_today_requested_at);
                        dailySales.setBranch_id(Integer.parseInt(mCurrentBranchId));

                        last2WeeksIndex++;

                        if (!isExisting(dailySales, DailySalesEnums.DATE_OF_DAILY_SALES, Integer.parseInt(mCurrentBranchId))) {
                            dailySales.dbOperation(getHelper(), DatabaseOperation.INSERT);
                        } else {
                            //  Log.e(TAG, "DailySale is existing checking for time");
                            if (isExisting(dailySales, DailySalesEnums.DATE_REQUESTED, Integer.parseInt(mCurrentBranchId))) {
                                //   Log.e(TAG, "Fetched date time is the most recent... updating database");
                                getHelper().getDailySales().deleteBuilder().where().eq("date_requested_at", date_today_requested_at);
                                dailySales.dbOperation(getHelper(), DatabaseOperation.INSERT);
                            } else {
                                Log.e(TAG, "database last 2 weeks data is up to date");
                            }
                        }

                        if (last2WeeksIndex >= dateLast2Weeks.size()) {
                            Log.e(TAG, "Downloading Last 2 Weeks is finished");
                            last2WeeksIndex = 0;
                            lastWeekIndex = 0;
                            if (startUpdateWeekType == UpdateWeekType.LAST_WEEK) {
                                mSyncDailySalesListener.onEndDownload(requestType);
                                mSyncDailySalesListener.onFinishDownload();
                            } else {
                                Log.e(TAG, "Downloading Last 3 Weeks");
                                updateWeekType = UpdateWeekType.LAST_3_WEEKS;
                            }
                        } else {
                            Log.e(TAG, "Last 2 Week's Current Downloaded Index: " + last2WeeksIndex + " Max Size: " + dateLast2Weeks.size());
                            if (startUpdateWeekType == UpdateWeekType.LAST_WEEK) {
                                fetchLastWeek(requestType);
                            }

                            if (startUpdateWeekType == UpdateWeekType.LAST_2_WEEKS) {
                                fetchLast2Weeks(requestType);
                            }
                            return;
                        }
                    }

                    if (updateWeekType == UpdateWeekType.LAST_3_WEEKS) {

                        dailySales.setDate_of_sales(dateLast3Weeks.get(last3WeeksIndex));
                        dailySales.setDate_requested_at(date_today_requested_at);
                        dailySales.setBranch_id(Integer.parseInt(mCurrentBranchId));

                        last3WeeksIndex++;

                        if (!isExisting(dailySales, DailySalesEnums.DATE_OF_DAILY_SALES, Integer.parseInt(mCurrentBranchId))) {
                            dailySales.dbOperation(getHelper(), DatabaseOperation.INSERT);
                        } else {
                            //  Log.e(TAG, "DailySale is existing checking for time");
                            if (isExisting(dailySales, DailySalesEnums.DATE_REQUESTED, Integer.parseInt(mCurrentBranchId))) {
                                //   Log.e(TAG, "Fetched date time is the most recent... updating database");
                                getHelper().getDailySales().deleteBuilder().where().eq("date_requested_at", date_today_requested_at);
                                dailySales.dbOperation(getHelper(), DatabaseOperation.INSERT);
                            } else {
                                Log.e(TAG, "database last 3 weeks data is up to date");
                            }
                        }

                        if (last3WeeksIndex >= dateLast3Weeks.size()) {
                            Log.e(TAG, "Downloading Last 3 Weeks is finished");
                            last3WeeksIndex = 0;
                            last2WeeksIndex = 0;
                            if (startUpdateWeekType == UpdateWeekType.LAST_2_WEEKS) {
                                mSyncDailySalesListener.onEndDownload(requestType);
                                mSyncDailySalesListener.onFinishDownload();
                            } else {
                                Log.e(TAG, "Downloading Last 4 Weeks");
                                updateWeekType = UpdateWeekType.LAST_4_WEEKS;
                            }
                        } else {
                            Log.e(TAG, "Last 3 Week's Current Downloaded Index: " + last3WeeksIndex + " Max Size: " + dateLast3Weeks.size());
                            if (startUpdateWeekType == UpdateWeekType.LAST_2_WEEKS) {
                                fetchLast2Weeks(requestType);
                            }

                            if (startUpdateWeekType == UpdateWeekType.LAST_3_WEEKS) {
                                fetchLast3Weeks(requestType);
                            }
                            return;
                        }
                    }

                    if (updateWeekType == UpdateWeekType.LAST_4_WEEKS) {

                        dailySales.setDate_of_sales(dateLast4Weeks.get(last4WeeksIndex));
                        dailySales.setDate_requested_at(date_today_requested_at);
                        dailySales.setBranch_id(Integer.parseInt(mCurrentBranchId));

                        last4WeeksIndex++;

                        if (!isExisting(dailySales, DailySalesEnums.DATE_OF_DAILY_SALES, Integer.parseInt(mCurrentBranchId))) {
                            dailySales.dbOperation(getHelper(), DatabaseOperation.INSERT);
                        } else {
                            //  Log.e(TAG, "DailySale is existing checking for time");
                            if (isExisting(dailySales, DailySalesEnums.DATE_REQUESTED, Integer.parseInt(mCurrentBranchId))) {
                                //   Log.e(TAG, "Fetched date time is the most recent... updating database");
                                getHelper().getDailySales().deleteBuilder().where().eq("date_requested_at", date_today_requested_at);
                                dailySales.dbOperation(getHelper(), DatabaseOperation.INSERT);
                            } else {
                                Log.e(TAG, "database last 4 weeks data is up to date");
                            }
                        }

                        if (last4WeeksIndex >= dateLast4Weeks.size()) {
                            Log.e(TAG, "Downloading Last 4 Weeks is finished end all");
                            last3WeeksIndex = 0;
                            last4WeeksIndex = 0;

                            mSyncDailySalesListener.onEndDownload(requestType);
                            mSyncDailySalesListener.onFinishDownload();
                        } else {
                            Log.e(TAG, "Last 4 Week's Current Downloaded Index: " + last4WeeksIndex + " Max Size: " + dateLast4Weeks.size());
                            fetchLast3Weeks(requestType);
                        }
                    }

                } else if (requestType == RequestType.DAILY_SALES_MONTH) {

                    if (thisMonthIndex < dateThisMonth.size() - 1) {
                        dailySales.setDate_of_sales(dateThisMonth.get(thisMonthIndex));
                        dailySales.setDate_requested_at(date_today_requested_at);
                        dailySales.setBranch_id(Integer.parseInt(mCurrentBranchId));
                    } else {
                        thisMonthIndex = 0;
                    }

                    thisMonthIndex++;
                    Log.e(TAG, "This Month's Current Downloaded Index: " + thisMonthIndex + " Max Size: " + dateThisMonth.size());

                    if (!isExisting(dailySales, DailySalesEnums.DATE_OF_DAILY_SALES, Integer.parseInt(mCurrentBranchId))) {
                        dailySales.dbOperation(getHelper(), DatabaseOperation.INSERT);
                    } else {
                        //  Log.e(TAG, "DailySale is existing checking for time");
                        if (isExisting(dailySales, DailySalesEnums.DATE_REQUESTED, Integer.parseInt(mCurrentBranchId))) {
                            //   Log.e(TAG, "Fetched date time is the most recent... updating database");
                            getHelper().getDailySales().deleteBuilder().where().eq("date_requested_at", date_today_requested_at);
                            dailySales.dbOperation(getHelper(), DatabaseOperation.INSERT);
                        } else {
                            Log.e(TAG, "database this month data is up to date");
                        }
                    }

                    if (thisMonthIndex < dateThisMonth.size() - 1) {
                        fetchMonth(requestType);
                    } else {
                        thisMonthIndex = 0;
                        Log.e(TAG, "Downloading This Month is finished..");
                        mSyncDailySalesListener.onEndDownload(requestType);
                        mSyncDailySalesListener.onFinishDownload();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else if (response instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) response;
            int size = jsonArray.length();
            Log.e(TAG, "content size: " + size);

            if (size == 0) {
                if (page == 1) {
                    HUDTools.hideIndeterminateProgressDialog();
                    onReloadBranches.showBasicDialogMessageReloadBranches("Your branch list is up to date.", "We're good!");
                    return;
                } else {
                    syncNext();
                }
            } else {

                BatchList<Branch> newBranches = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                BatchList<Branch> updateBranches = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                BatchList<BranchTag> newBranchTags = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                BatchList<BranchTag> updateBranchTags = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

                BatchList<BranchUserAssoc> newBranchUserAssocs = new BatchList<>(DatabaseOperation.INSERT, getHelper());
                BatchList<BranchUserAssoc> updateBranchUserAssocs = new BatchList<>(DatabaseOperation.UPDATE, getHelper());

/*
                try {
                    getHelper().getBranches().deleteBuilder().delete();
                    getHelper().getBranchTags().deleteBuilder().delete();
                    getHelper().getBranchUserAssocs().deleteBuilder().delete();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
*/

                for (int i = 0; i < size; i++) {

                    try {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        Branch branch = gson.fromJson(jsonObject.toString(), Branch.class);
                        BranchUserAssoc branchUserAssoc = new BranchUserAssoc(branch, getSession().getUser());

                        //  if (jsonArray.getJSONObject(i).getString("site_type").equals("null")) {
                        Log.e(TAG, jsonArray.getJSONObject(i).toString());


                            if (isExisting(branch, Table.BRANCHES)) {
                                updateBranches.add(branch);
                                updateBranchUserAssocs.add(branchUserAssoc);
                            } else {
                                newBranches.add(branch);
                                newBranchUserAssocs.add(branchUserAssoc);
                            }

                        // }

                        if (jsonObject.has("tag_list")) {
                            JSONArray tagsListArray = jsonObject.getJSONArray("tag_list");
                            int tagsSize = tagsListArray.length();

                            for (int tagsI = 0; tagsI < tagsSize; tagsI++) {
                                BranchTag branchTag = new BranchTag(tagsListArray.getString(tagsI), branch);

                                    if (isExisting(branchTag, Table.BRANCH_TAGS)) {
                                        updateBranchTags.add(branchTag);
                                    } else {
                                        newBranchTags.add(branchTag);
                                    }

                            }
                        }
                    } catch (JSONException | SQLException e) {
                        e.printStackTrace();
                    }
                }

                newBranches.doOperation();
                updateBranches.doOperation();

                newBranchUserAssocs.doOperation();
                updateBranchUserAssocs.doOperation();

                newBranchTags.doOperation();
                updateBranchTags.doOperation();

                // TODO SUPPORT MULTI PAGE
                onReloadBranches.finishedReloading();
            }

            return;
        }

    }

    @Override
    public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
        Log.e(TAG, "Response code:" + String.valueOf(responseCode));

        if(!hasInternet) {
            mSyncDailySalesListener.onErrorDownload("no internet");
        } else {

            if(responseCode == 401) {
                mSyncDailySalesListener.onErrorDownload("401");
            } else if (response != null) {
                mSyncDailySalesListener.onErrorDownload(response.toString() + "");
                Log.e(TAG, "Response: " + response.toString());
            } else {
                mSyncDailySalesListener.onErrorDownload("Error! di ko alam kung ano");
            }
        }

    }

    @Override
    public void onRequestError() {
        HUDTools.hideIndeterminateProgressDialog();
        mSyncDailySalesListener.onErrorDownload("Request Error");
    }


    public DailySales getDailySalesForToday(int branch_id, String date) throws SQLException {

        List<DailySales> listDailySales = getHelper().getDailySales().queryBuilder().where().eq("branch_id", branch_id).query();
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

    public void cancelRequest(String TAG) {
        if (getQueue() != null) {
            getQueue().cancelAll(TAG);
        }
    }

    private void syncNext() {

    }

    public OnReloadBranches getOnReloadBranches() {
        return onReloadBranches;
    }

    public void setOnReloadBranches(OnReloadBranches onReloadBranches) {
        this.onReloadBranches = onReloadBranches;
    }

    private void fetchBranchUsersCount() {
        ImonggoOperations.getAPIModule(mContext, getQueue(), getSession(), this,
                Table.BRANCH_USERS, getSession().getServer(), RequestType.COUNT,
                String.format(ImonggoTools.generateParameter(Parameter.COUNT, Parameter.USER_ID),
                        String.valueOf(getSession().getUser().getId())));
    }
}