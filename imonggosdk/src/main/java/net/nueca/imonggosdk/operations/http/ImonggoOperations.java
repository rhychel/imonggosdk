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
import net.nueca.imonggosdk.operations.http.HTTPRequests;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
public class ImonggoOperations {

    public static final String IMONGGO_OPERATIONS_TAG = "imonggo_operations_tag";

    /**
     *
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
            case CUSTOM_URL_SECURED:
                return ImonggoTools.buildAPIModuleIDURL(context, session.getApiToken(), session.getAcctUrlWithoutProtocol(), table, id, parameter, true);
            case IRETAILCLOUD_COM:
            case IRETAILCLOUD_NET:
            case CUSTOM_URL:
            case PLDTRETAILCLOUD:
                return ImonggoTools.buildAPIModuleIDURL(context, session.getApiToken(), session.getAcctUrlWithoutProtocol(), table, id, parameter, false);
        }
        return "";
    }

    /**
     *
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
            case CUSTOM_URL_SECURED:
                return ImonggoTools.buildAPIModuleURL(context, session.getApiToken(), session.getAcctUrlWithoutProtocol(), table, parameter, true);
            case IRETAILCLOUD_COM:
            case IRETAILCLOUD_NET:
            case CUSTOM_URL:
            case PLDTRETAILCLOUD:
                return ImonggoTools.buildAPIModuleURL(context, session.getApiToken(), session.getAcctUrlWithoutProtocol(), table, parameter, false);
        }
        return "";
    }

    public static void getAPIModule(Context context, RequestQueue queue, Session session,
                                    VolleyRequestListener volleyRequestListener, Table table,
                                    Server server, RequestType requestType) {
        getAPIModule(context, queue, session, volleyRequestListener, table, server, requestType, "");
    }

    public static void getAPIModule(Context context, RequestQueue queue, Session session,
                                    VolleyRequestListener volleyRequestListener, Table table,
                                    Server server, RequestType requestType, String parameter) {
        if(requestType == RequestType.LAST_UPDATED_AT || requestType == RequestType.COUNT)
            queue.add(HTTPRequests.sendGETJsonObjectRequest(context, session, volleyRequestListener, server, table, requestType, parameter));
        else if (requestType == RequestType.API_CONTENT)
            queue.add(HTTPRequests.sendGETJsonArrayRequest(context, session, volleyRequestListener, server, table, requestType, parameter));
    }

    /**
     *  **********************
     *  ** Special Requests **
     *  **********************
     */
    public static JsonObjectRequest checkinCustomer(Context context, RequestQueue queue,
                                                    Session session, VolleyRequestListener volleyRequestListener,
                                                    Server server, String id, String parameter) {
        return HTTPRequests.sendGETRequest(context, session, volleyRequestListener, server, Table.CUSTOMERS, id+"/checkin", parameter);
    }

    /**
     * GET THE CONCESIO.JSON APPLICATION SETTINGS.
     */
    public static JsonObjectRequest getConcesioAppSettings(Context context, RequestQueue queue, Session session,
                                              VolleyRequestListener volleyRequestListener, Server server) {
         return HTTPRequests.sendGETRequest(context, session, volleyRequestListener, server, Table.APPLICATION_SETTINGS, "concesio", "");
    }
}
