package net.nueca.imonggosdk.operations.http;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.operations.ImonggoTools;

import org.json.JSONObject;

/**
 * Created by rhymart on 5/13/15.
 * Modified by Jn on 6/9/15
 *
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
        switch (server) {
            case IMONGGO:
                return ImonggoTools.buildAPIModuleIDURL(context, session.getApiToken(), session.getAcctUrlWithoutProtocol(), table, id, parameter, true);
            case IRETAILCLOUD_COM:
                return ImonggoTools.buildAPIModuleIDURL(context, session.getApiToken(), session.getAcctUrlWithoutProtocol(), table, id, parameter, false);
            case IRETAILCLOUD_NET:
                return ImonggoTools.buildAPIModuleIDURL(context, session.getApiToken(), session.getAcctUrlWithoutProtocol(), table, id, parameter, false);
            case PLDTRETAILCLOUD:
                return ImonggoTools.buildAPIModuleIDURL(context, session.getApiToken(), session.getAcctUrlWithoutProtocol(), table, id, parameter, false);
            default:
                return "";
        }
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
                return ImonggoTools.buildAPIModuleURL(context, session.getApiToken(), session.getAcctUrlWithoutProtocol(), table, parameter, false);
            case IRETAILCLOUD_NET:
                return ImonggoTools.buildAPIModuleURL(context, session.getApiToken(), session.getAcctUrlWithoutProtocol(), table, parameter, false);
            case PLDTRETAILCLOUD:
                return ImonggoTools.buildAPIModuleURL(context, session.getApiToken(), session.getAcctUrlWithoutProtocol(), table, parameter, false);
            default:
                return "";
        }
    }

    public static void getAPIModule(Context context, RequestQueue queue, Session session,
                                    VolleyRequestListener volleyRequestListener, Table table,
                                    Server server, RequestType requestType) {
        getAPIModule(context, queue, session, volleyRequestListener, table, server, requestType, "");
    }

    public static void getAPIModule(Context context, RequestQueue queue, Session session,
                                    VolleyRequestListener volleyRequestListener, Table table,
                                    Server server, RequestType requestType, String parameter) {
        if (requestType == RequestType.LAST_UPDATED_AT || requestType == RequestType.COUNT)
            queue.add(HTTPRequests.sendGETJsonObjectRequest(context, session, volleyRequestListener, server, table, requestType, parameter));
        else if (requestType == RequestType.API_CONTENT)
            queue.add(HTTPRequests.sendGETJsonArrayRequest(context, session, volleyRequestListener, server, table, requestType, parameter));
    }

    /**
     * **********************
     * ** Special Requests **
     * **********************
     */

    /**
     * Checkin Customer --- for CityMall
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
        if(autoStart)
            queue.start();
    }

    /**
     * GET THE CONCESSIO.JSON APPLICATION SETTINGS.
     */

    public static void getConcesioAppSettings(Context context, RequestQueue queue, Session session,
                                              VolleyRequestListener volleyRequestListener, Server server) {
        getConcesioAppSettings(context, queue, session, volleyRequestListener, server, false);
    }

    public static void getConcesioAppSettings(Context context, RequestQueue queue, Session session,
                                                           VolleyRequestListener volleyRequestListener, Server server, boolean autoStart) {
        queue.add(HTTPRequests.sendGETRequest(context, session, volleyRequestListener, server, Table.APPLICATION_SETTINGS, "concesio", ""));
        if(autoStart)
            queue.start();
    }

    public static void sendPOSDevice(Context context, RequestQueue queue, Session session,
                                                  VolleyRequestListener volleyRequestListener, Server server) {
        sendPOSDevice(context,queue, session, volleyRequestListener, server, null, "");
    }

    public static void sendPOSDevice(Context context, RequestQueue queue, Session session,
                                                  VolleyRequestListener volleyRequestListener, Server server, JSONObject jsonObject, String parameter) {
        queue.add( HTTPRequests.sendPOSTRequest(context,session,volleyRequestListener,server,Table.POS_DEVICES, jsonObject, parameter));
    }

}
