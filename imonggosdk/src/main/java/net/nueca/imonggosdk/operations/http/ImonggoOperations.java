package net.nueca.imonggosdk.operations.http;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.operations.ImonggoTools;
import net.nueca.imonggosdk.tools.Configurations;

import org.json.JSONObject;

/**
 * Created by rhymart on 5/13/15.
 * Modified by Jn on 6/9/15
 * <p/>
 * imonggosdk (c)2015
 */
public class ImonggoOperations {

    public static final String IMONGGO_OPERATIONS_TAG = "imonggo_operations_tag";

    /**
     * Returns URL for requesting a single row in the API.
     *
     * @param context
     * @param session
     * @param table
     * @param id
     * @param parameter
     * @param server
     * @return String URL for the id
     */
    public static String getAPIModuleIDURL(Context context, Session session, Table table, Server server, String id, String parameter) {
        String URL = "";
        switch (server) {
            case IMONGGO:
                URL = ImonggoTools.buildAPIModuleIDURL(context, session.getApiToken(), session.getAcctUrlWithoutProtocol(), table, id, parameter, true);
                break;
            case IRETAILCLOUD_COM:
            case IRETAILCLOUD_NET:
            case PLDTRETAILCLOUD:
            case PETRONDIS_COM:
            case PETRONDIS_NET:
            case REBISCO_DEV:
            case REBISCO_LIVE:
            case REBISCO_LIVE_NET:
                URL = ImonggoTools.buildAPIModuleIDURL(context, session.getApiToken(), session.getAcctUrlWithoutProtocol(), table, id, parameter, false);
                break;
            default:
                return "";
        }
        return (parameter == null || parameter.equals("")) ? URL.replace("?", "") : URL;
    }

    /**
     * Returns URL for requesting a all in the API.
     *
     * @param context
     * @param session
     * @param table
     * @param server
     * @param parameter
     * @return
     */
    public static String getAPIModuleURL(Context context, Session session, Table table, Server server, String parameter) {
        switch (server) {
            case IMONGGO:
                return ImonggoTools.buildAPIModuleURL(context, session.getApiToken(), session.getAcctUrlWithoutProtocol(), table, parameter, true);
            case IRETAILCLOUD_COM:
            case IRETAILCLOUD_NET:
            case PLDTRETAILCLOUD:
            case IMONGGO_NET:
            case PETRONDIS_NET:
            case PETRONDIS_COM:
            case REBISCO_DEV:
            case REBISCO_LIVE:
            case REBISCO_LIVE_NET:
                return ImonggoTools.buildAPIModuleURL(context, session.getApiToken(), session.getAcctUrlWithoutProtocol(), table, parameter, false);
            default:
                return "";
        }
    }

    /**
     * Returns URL for requesting a single row by reference in the API.
     *
     * @param context
     * @param session
     * @param table
     * @param reference
     * @param server
     * @return String URL for the reference
     */
    public static String getAPIModuleReferenceURL(Context context, Session session, Table table, Server server, String reference) {
        String URL = "";
        switch (server) {
            case IMONGGO:
                URL = ImonggoTools.buildAPIModuleReferenceURL(context, session.getApiToken(), session.getAcctUrlWithoutProtocol(), table, reference, true);
                break;
            case IRETAILCLOUD_COM:
            case IRETAILCLOUD_NET:
            case PLDTRETAILCLOUD:
            case PETRONDIS_COM:
            case PETRONDIS_NET:
            case REBISCO_DEV:
            case REBISCO_LIVE:
            case REBISCO_LIVE_NET:
                URL = ImonggoTools.buildAPIModuleReferenceURL(context, session.getApiToken(), session.getAcctUrlWithoutProtocol(), table, reference, false);
                break;
            default:
                return "";
        }
        return URL;
    }

    public static void getAPIModule(Context context, RequestQueue queue, Session session,
                                    VolleyRequestListener volleyRequestListener, Table table,
                                    Server server, RequestType requestType) {
        getAPIModule(context, queue, session, volleyRequestListener, table, server, requestType, "");
    }

    public static void getAPIModule(Context context, RequestQueue queue, Session session,
                                    VolleyRequestListener volleyRequestListener, Table table,
                                    Server server, RequestType requestType, String parameter) {
        getAPIModule(context, queue, session, volleyRequestListener, table, server, requestType, parameter, "");

    }


    public static void getAPIModule(Context context, RequestQueue queue, Session session,
                                    VolleyRequestListener volleyRequestListener, Table table,
                                    Server server, RequestType requestType, String parameter, String TAG) {

        Log.e(TAG, "RequestType: " + requestType);
        if (requestType == RequestType.LAST_UPDATED_AT
                || requestType == RequestType.COUNT
                || table == Table.TAX_SETTINGS
                || table == Table.DAILY_SALES
                || table == Table.USERS_ME
                || table == Table.PRICE_LISTS_FROM_CUSTOMERS) {
            JsonObjectRequest request = HTTPRequests.sendGETJsonObjectRequest(context, session, volleyRequestListener, server, table, requestType, parameter);
            request.setTag(TAG);
            request.setShouldCache(false);
            queue.add(request);

        } else if (requestType == RequestType.API_CONTENT) {
            JsonArrayRequest request = HTTPRequests.sendGETJsonArrayRequest(context, session, volleyRequestListener, server, table, requestType, parameter);
            request.setTag(TAG);
            request.setShouldCache(false);
            queue.add(request);
        }
    }

    /**
     * **********************
     * ** Special Requests **
     * **********************
     */

    /**
     * Checkin Customer --- for CityMall
     *
     * @param context
     * @param queue
     * @param session
     * @param volleyRequestListener
     * @param server
     * @param id
     * @param parameter
     */
    public static void checkinCustomer(Context context, RequestQueue queue,
                                       Session session, VolleyRequestListener volleyRequestListener,
                                       Server server, String id, String parameter) {
        checkinCustomer(context, queue, session, volleyRequestListener, server, id, parameter, false);
    }

    /**
     * Checkin Customer --- for CityMall
     *
     * @param context
     * @param queue
     * @param session
     * @param volleyRequestListener
     * @param server
     * @param id
     * @param parameter
     * @param autoStart
     */
    public static void checkinCustomer(Context context, RequestQueue queue,
                                       Session session, VolleyRequestListener volleyRequestListener,
                                       Server server, String id, String parameter, boolean autoStart) {
        queue.add(HTTPRequests.sendGETRequest(context, session, volleyRequestListener, server, Table.CUSTOMERS, id + "/checkin", parameter));
        if (autoStart)
            queue.start();
    }

    /**
     * GET THE CONCESSIO.JSON APPLICATION SETTINGS.
     */

    public static void getConcesioAppSettings(Context context, RequestQueue queue, Session session, VolleyRequestListener volleyRequestListener, Server server) {
        getConcesioAppSettings(context, queue, session, volleyRequestListener, server, false, false);
    }

    public static void getConcesioAppSettings(Context context, RequestQueue queue, Session session,
                                                           VolleyRequestListener volleyRequestListener, Server server, boolean autoStart, boolean useJSONObject) {
        if(useJSONObject)
            queue.add(HTTPRequests.sendGETJsonObjectRequest(context, session, volleyRequestListener, server, Table.APPLICATION_SETTINGS,
                    RequestType.APPLICATION_SETTINGS, Configurations.CONCESSIO_JSON, ""));
        else
            queue.add(HTTPRequests.sendGETJsonArrayRequest(context, session, volleyRequestListener, server, Table.APPLICATION_SETTINGS, Configurations.CONCESSIO_JSON, ""));
        if(autoStart)
            queue.start();
    }

    public static void sendPOSDevice(Context context, RequestQueue queue, Session session,
                                     VolleyRequestListener volleyRequestListener, Server server) {
        sendPOSDevice(context, queue, session, volleyRequestListener, server, null, "");
    }

    public static void sendPOSDevice(Context context, RequestQueue queue, Session session,
                                     VolleyRequestListener volleyRequestListener, Server server, JSONObject jsonObject, String parameter) {
        queue.add(HTTPRequests.sendPOSTRequest(context, session, volleyRequestListener, server, Table.POS_DEVICES, jsonObject, parameter));
    }


}
