package nueca.net.salesdashboard.activities;

import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.dialogs.DialogTools;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.tools.NetworkTools;

import java.sql.SQLException;

import nueca.net.salesdashboard.R;
import nueca.net.salesdashboard.enums.Update;
import nueca.net.salesdashboard.enums.UpdateType;
import nueca.net.salesdashboard.enums.UpdateWeekType;
import nueca.net.salesdashboard.interfaces.OnReloadBranches;
import nueca.net.salesdashboard.interfaces.SyncDailySalesListener;
import nueca.net.salesdashboard.operations.SyncDailySales;
import nueca.net.salesdashboard.tools.HUDTools;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public abstract class BaseDailySalesActivity extends ImonggoAppCompatActivity {
    public String TAG = "BaseDailySalesActivity";
    private ImonggoDBHelper dbHelper = null;
    private RequestQueue queue = null;
    private SwipeRefreshLayout swipeRefreshLayout;
    public SyncDailySales syncDailySales = null;

    @Override
    public void onDestroy() {
        if (dbHelper != null) {
            OpenHelperManager.releaseHelper();
            dbHelper = null;
        }
        super.onDestroy();
    }

    protected RequestQueue getQueue() {
        if (queue == null)
            queue = Volley.newRequestQueue(this);
        return queue;
    }/**/

    public void startSyncData(SyncDailySalesListener syncDailySalesListener, String branch_id,
                              UpdateType updateType, Update update, RequestType requestType) throws SQLException {
        startSyncData(syncDailySalesListener, branch_id, updateType, update, requestType, null);
    }

    public void startSyncData(SyncDailySalesListener syncDailySalesListener, String branch_id,
                              UpdateType updateType, Update update, RequestType requestType, UpdateWeekType updateWeekType) throws SQLException {
        if (syncDailySales == null) {
            syncDailySales = new SyncDailySales(BaseDailySalesActivity.this, syncDailySalesListener, branch_id,
                    getSession(), getQueue(), getHelper());
        }

        Log.e(TAG, "Start Sync Data: " + update + " Update Week Type: " + updateWeekType);
        if (update == Update.WEEK) {
            syncDailySales.startFetchingDailySales(updateType, update, requestType, updateWeekType);
        } else {
            syncDailySales.startFetchingDailySales(updateType, update, requestType);
        }
    }

    public void cancelRequest(String TAG) {
        if (syncDailySales != null) {
            syncDailySales.cancelRequest(TAG);
        }
    }

    public void reloadBranchUsers(OnReloadBranches onReloadBranches) {
        if (NetworkTools.isInternetAvailable(this)) {
            HUDTools.hideIndeterminateProgressDialog();
            HUDTools.showIndeterminateProgressHUD(this, "Please Wait...", false);
            if (syncDailySales != null) {
                syncDailySales.startFetchingBranchUser(onReloadBranches);
            } else {
                Log.e(TAG, "Sync Daily Sales not working...");
            }
        } else {
            DialogTools.showBasicWithTitle(this, getResources().getString(R.string.LOGIN_FAILED_OFFLINE),
                    getResources().getString(R.string.LOGIN_NETWORK_ERROR), getResources().getString(R.string.LOGIN_DIALOG_POSITIVE_BUTTON), "", false, new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            dialog.dismiss();
                        }
                    });
        }
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
    }
}
