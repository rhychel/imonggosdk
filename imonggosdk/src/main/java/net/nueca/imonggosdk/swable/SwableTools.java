package net.nueca.imonggosdk.swable;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.RequestQueue;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.OfflineDataType;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.operations.http.HTTPRequests;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.List;

public class SwableTools {
	public static Intent startSwable(Activity activity) {
		Log.e("SwableTools", "startSwable in " + activity.getClass().getSimpleName());
		Intent service = new Intent(activity,ImonggoSwable.class);
		activity.startService(service);
		return service;
	}
	
	public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}

    public static OfflineData addOfflineData(ImonggoDBHelper helper, User user, Object o, OfflineDataType type)
            throws SQLException, JSONException {
        return addOfflineData(helper, user, o, type, "");
    }
    public static OfflineData addOfflineData(ImonggoDBHelper helper, User user, Object o, OfflineDataType type, String parameters)
            throws SQLException, JSONException {
        return addOfflineData(helper, user.getHome_branch_id(), o, type, parameters);
    }

    public static OfflineData addOfflineData(ImonggoDBHelper helper, Session session, Object o, OfflineDataType type)
            throws SQLException, JSONException {
        return addOfflineData(helper, session, o, type, "");
    }
    public static OfflineData addOfflineData(ImonggoDBHelper helper, Session session, Object o, OfflineDataType type, String parameters)
            throws SQLException, JSONException {
        if(helper.getUsers().queryForAll().size() <= 0) {
            Log.e("SwableTools", "addOfflineData : no users in table, Users");
            return null;
        }
        User user = helper.getUsers().queryBuilder().where().eq("email", session.getEmail()).query().get(0);
        return addOfflineData(helper, user.getHome_branch_id(), o, type, parameters);
    }

    public static OfflineData addOfflineData(ImonggoDBHelper helper, int branchId, Object o, OfflineDataType type)
            throws SQLException, JSONException {
        return addOfflineData(helper, branchId, o, type, "");
    }
    public static OfflineData addOfflineData(ImonggoDBHelper helper, int branchId, Object o, OfflineDataType type, String parameters)
            throws SQLException, JSONException {
        String data;
        if(o instanceof String)
            data = (String)o;
        else if(o instanceof Order)
            data = ((Order) o).refresh(helper).toJSONString();
        else if(o instanceof Invoice)
            data = ((Invoice) o).refresh(helper).toJSONString();
        else {
            Log.e("SwableTools", "addOfflineData : Class '" + o.getClass().getSimpleName() + "' is not supported for OfflineData");
            return null;
        }

        OfflineData offlineData = new OfflineData(data, type);
        offlineData.setBranch_id(branchId);
        offlineData.setParameters(parameters);

        if(o instanceof Order) {
            Order order = (Order) o;
            offlineData.setReference_no(order.getReference());
        }
        else if(o instanceof Invoice) {
            Invoice invoice = (Invoice) o;
            offlineData.setReference_no(invoice.getReference());
        }

        offlineData.insertTo(helper);
        return offlineData;
    }

    public static void voidTransaction(ImonggoDBHelper helper, int returnId, OfflineDataType type, String reason)
            throws SQLException {
        List<OfflineData> transactions = helper.getOfflineData().queryBuilder().where().eq("returnId", returnId).query();
        if(transactions == null || transactions.size() <= 0) {
            Log.e("SwableTools", "voidTransaction : offlinedata with return id '" + returnId + "' not found");
            return;
        }
        OfflineData forVoid = transactions.get(0);
        forVoid.setType(type.getNumericValue());
        forVoid.setSynced(false);
        forVoid.setDocumentReason(reason);
        forVoid.updateTo(helper);
    }

    public static void voidTransaction(ImonggoDBHelper helper, OfflineData offlineData, OfflineDataType type, String reason)
            throws SQLException {
        if(offlineData.getReturnId() == null || offlineData.getReturnId().length() <= 0) {
            Log.e("SwableTools", "voidTransaction : offlinedata has no return id");
            return;
        }
        offlineData.setType(type.getNumericValue());
        offlineData.setSynced(false);
        offlineData.setDocumentReason(reason);
        offlineData.updateTo(helper);
    }
}
