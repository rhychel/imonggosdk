package net.nueca.imonggosdk.operations.http;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Session;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rhymart on 5/19/15.
 * imonggosdk (c)2015
 * <p/>
 * =====================================================
 * ==---------------RESTFul HTTP Methods--------------==
 * =====================================================
 */
public class HTTPRequests {

    public static String TAG = "HTTPRequests";

    public static JsonObjectRequest sendGETJsonObjectRequest(Context context, final Session session,
                                                             final VolleyRequestListener volleyRequestListener, Server server,
                                                             final Table table, final RequestType requestType, String id, String parameter) {
        if (volleyRequestListener != null)
            volleyRequestListener.onStart(table, requestType);

        Log.e(TAG,  ImonggoOperations.getAPIModuleURL(context, session, table, server, parameter));

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.GET,
                ImonggoOperations.getAPIModuleIDURL(context, session, table, server, id, parameter),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (volleyRequestListener != null)
                            volleyRequestListener.onSuccess(table, requestType, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getLocalizedMessage());
                        if (volleyRequestListener != null) {
                            if (error.networkResponse != null)
                                volleyRequestListener.onError(table, true, new String(error.networkResponse.data), error.networkResponse.statusCode);
                            else
                                volleyRequestListener.onError(table, false, null, 0);
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                String auth = "Basic " + session.getApiAuthentication();
                params.put("Authorization", auth);
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(15000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        jsonObjectRequest.setTag(ImonggoOperations.IMONGGO_OPERATIONS_TAG);
        return jsonObjectRequest;
    }

    public static JsonObjectRequest sendGETJsonObjectRequest(Context context, final Session session,
                                                             final VolleyRequestListener volleyRequestListener, Server server,
                                                             final Table table, final RequestType requestType, String parameter) {
        if (volleyRequestListener != null)
            volleyRequestListener.onStart(table, requestType);

        Log.e(TAG,  ImonggoOperations.getAPIModuleURL(context, session, table, server, parameter));

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.GET,
                ImonggoOperations.getAPIModuleURL(context, session, table, server, parameter),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (volleyRequestListener != null)
                            volleyRequestListener.onSuccess(table, requestType, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (volleyRequestListener != null) {
                            if (error.networkResponse != null)
                                volleyRequestListener.onError(table, true, new String(error.networkResponse.data), error.networkResponse.statusCode);
                            else
                                volleyRequestListener.onError(table, false, null, 0);
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                String auth = "Basic " + session.getApiAuthentication();
                params.put("Authorization", auth);
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(15000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        jsonObjectRequest.setTag(ImonggoOperations.IMONGGO_OPERATIONS_TAG);
        return jsonObjectRequest;
    }

    public static JsonArrayRequest sendGETJsonArrayRequest(Context context, final Session session,
                                                           final VolleyRequestListener volleyRequestListener, Server server,
                                                           final Table table, final RequestType requestType, String parameter) {
        if (volleyRequestListener != null)
            volleyRequestListener.onStart(table, requestType);

        Log.e(TAG,  ImonggoOperations.getAPIModuleURL(context, session, table, server, parameter));

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(ImonggoOperations.getAPIModuleURL(context, session, table, server, parameter),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (volleyRequestListener != null)
                            volleyRequestListener.onSuccess(table, requestType, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (volleyRequestListener != null) {
                            if (error.networkResponse != null)
                                volleyRequestListener.onError(table, true, new String(error.networkResponse.data), error.networkResponse.statusCode);
                            else
                                volleyRequestListener.onError(table, false, null, 0);
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                String auth = "Basic " + session.getApiAuthentication();
                params.put("Authorization", auth);
                return params;
            }
        };
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(15000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        jsonArrayRequest.setTag(ImonggoOperations.IMONGGO_OPERATIONS_TAG);
        return jsonArrayRequest;
    }

    public static JsonObjectRequest sendGETRequest(Context context, final Session session,
                                                   final VolleyRequestListener volleyRequestListener, Server server,
                                                   final Table table, String id, String parameter) {
        if (volleyRequestListener != null)
            volleyRequestListener.onStart(table, RequestType.GET);

        Log.e("Request-URL", ImonggoOperations.getAPIModuleIDURL(context, session, table, server, id, parameter));

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.GET,
                ImonggoOperations.getAPIModuleIDURL(context, session, table, server, id, parameter),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (volleyRequestListener != null)
                            volleyRequestListener.onSuccess(table, RequestType.GET, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (volleyRequestListener != null) {
                            if (error.networkResponse != null) {
                                volleyRequestListener.onError(table, true, new String(error.networkResponse.data), error.networkResponse.statusCode);
                            } else
                                volleyRequestListener.onError(table, false, null, 0);
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                String auth = "Basic " + session.getApiAuthentication();
                params.put("Authorization", auth);
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(15000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        jsonObjectRequest.setTag(ImonggoOperations.IMONGGO_OPERATIONS_TAG);
        return jsonObjectRequest;
    }

    public static JsonArrayRequest sendGETJsonArrayRequest(Context context, final Session session,
                                                           final VolleyRequestListener volleyRequestListener, Server server,
                                                           final Table table, String id, String parameter) {
        if (volleyRequestListener != null)
            volleyRequestListener.onStart(table, RequestType.GET);

        Log.e("Request-URL", ImonggoOperations.getAPIModuleIDURL(context, session, table, server, id, parameter));

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(ImonggoOperations.getAPIModuleIDURL(context, session, table, server, id, parameter),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (volleyRequestListener != null)
                            volleyRequestListener.onSuccess(table, RequestType.GET, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (volleyRequestListener != null) {
                            if (error.networkResponse != null)
                                volleyRequestListener.onError(table, true, new String(error.networkResponse.data), error.networkResponse.statusCode);
                            else
                                volleyRequestListener.onError(table, false, null, 0);
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                String auth = "Basic " + session.getApiAuthentication();
                params.put("Authorization", auth);
                return params;
            }
        };
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(15000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        jsonArrayRequest.setTag(ImonggoOperations.IMONGGO_OPERATIONS_TAG);
        return jsonArrayRequest;
    }

    public static JsonObjectRequest sendPOSTRequest(Context context, final Session session,
                                                    final VolleyRequestListener volleyRequestListener, Server server,
                                                    final Table table, final JSONObject jsonObject, String parameter) {
        if (volleyRequestListener != null)
            volleyRequestListener.onStart(table, RequestType.POST);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                ImonggoOperations.getAPIModuleURL(context, session, table, server, parameter), jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (volleyRequestListener != null)
                            volleyRequestListener.onSuccess(table, RequestType.POST, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (volleyRequestListener != null) {
                            if (error.networkResponse != null)
                                volleyRequestListener.onError(table, true, new String(error.networkResponse.data), error.networkResponse.statusCode);
                            else
                                volleyRequestListener.onError(table, false, null, 0);
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                String auth = "Basic " + session.getApiAuthentication();
                params.put("Authorization", auth);
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(15000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        jsonObjectRequest.setTag(ImonggoOperations.IMONGGO_OPERATIONS_TAG);
        return jsonObjectRequest;
    }

    public static JsonObjectRequest sendPUTRequest(Context context, final Session session,
                                                   final VolleyRequestListener volleyRequestListener, Server server,
                                                   final Table table, final JSONObject jsonObject, String id, String parameter) {
        if (volleyRequestListener != null)
            volleyRequestListener.onStart(table, RequestType.PUT);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.PUT,
                ImonggoOperations.getAPIModuleIDURL(context, session, table, server, id, parameter), jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (volleyRequestListener != null)
                            volleyRequestListener.onSuccess(table, RequestType.PUT, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (volleyRequestListener != null) {
                            if (error.networkResponse != null)
                                volleyRequestListener.onError(table, true, new String(error.networkResponse.data), error.networkResponse.statusCode);
                            else
                                volleyRequestListener.onError(table, false, null, 0);
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                String auth = "Basic " + session.getApiAuthentication();
                params.put("Authorization", auth);
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(15000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        jsonObjectRequest.setTag(ImonggoOperations.IMONGGO_OPERATIONS_TAG);
        return jsonObjectRequest;
    }

    public static JsonObjectRequest sendDELETERequest(Context context, final Session session,
                                                      final VolleyRequestListener volleyRequestListener, Server server,
                                                      final Table table, String id, String parameter) {
        if (volleyRequestListener != null)
            volleyRequestListener.onStart(table, RequestType.DELETE);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.DELETE,
                ImonggoOperations.getAPIModuleIDURL(context, session, table, server, id, parameter),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (volleyRequestListener != null)
                            volleyRequestListener.onSuccess(table, RequestType.DELETE, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (volleyRequestListener != null) {
                            if (error.networkResponse != null)
                                volleyRequestListener.onError(table, true, new String(error.networkResponse.data), error.networkResponse.statusCode);
                            else
                                volleyRequestListener.onError(table, false, null, 0);
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                String auth = "Basic " + session.getApiAuthentication();
                params.put("Authorization", auth);
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(15000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        jsonObjectRequest.setTag(ImonggoOperations.IMONGGO_OPERATIONS_TAG);
        return jsonObjectRequest;
    }

    /** New page send implementation **/

    public static JsonObjectRequest sendPOSTRequest2(Context context, final Session session,
                                                    final VolleyRequestListener volleyRequestListener, Server server,
                                                    final Table table, final JSONObject jsonObject, int branch_id,
                                                     String parameter) {
        if (volleyRequestListener != null)
            volleyRequestListener.onStart(table, RequestType.POST);

        if (parameter != null && parameter.length() > 1 && parameter.charAt(0) != '&')
            parameter = "?post=0&branch_id=" + branch_id +
                    (parameter.charAt(0) == '?'? parameter.substring(1) : parameter);
        else
            parameter = "?post=0&branch_id=" + branch_id;

        Log.e("HTTPRequests","sendPOSTRequest2 : " + ImonggoOperations.getAPIModuleURL(context, session, table, server, parameter));

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                ImonggoOperations.getAPIModuleURL(context, session, table, server,
                        parameter),
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (volleyRequestListener != null)
                            volleyRequestListener.onSuccess(table, RequestType.POST, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (volleyRequestListener != null) {
                            if (error.networkResponse != null)
                                volleyRequestListener.onError(table, true, new String(error.networkResponse.data), error.networkResponse.statusCode);
                            else
                                volleyRequestListener.onError(table, false, null, 0);
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                String auth = "Basic " + session.getApiAuthentication();
                params.put("Authorization", auth);
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(15000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        jsonObjectRequest.setTag(ImonggoOperations.IMONGGO_OPERATIONS_TAG);
        return jsonObjectRequest;
    }

    public static JsonObjectRequest sendPUTRequest2(Context context, final Session session,
                                                   final VolleyRequestListener volleyRequestListener, Server server,
                                                   final Table table, final JSONObject jsonObject, String id,
                                                    int branch_id, boolean isLastPage, String parameter) {
        if (volleyRequestListener != null)
            volleyRequestListener.onStart(table, RequestType.PUT);

        if (parameter != null && parameter.length() > 1 && parameter.charAt(0) != '&')
            parameter = "post=" + (isLastPage? "1" : "0") + "&branch_id=" + branch_id +
                    (parameter.charAt(0) == '?'? parameter.substring(1) : parameter);
        else
            parameter = "post=" + (isLastPage? "1" : "0") + "&branch_id="+ branch_id;

        Log.e("HTTPRequests","sendPUTRequest2 : " + ImonggoOperations.getAPIModuleIDURL(context, session, table, server, id, parameter));

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.PUT,
                ImonggoOperations.getAPIModuleIDURL(context, session, table, server, id, parameter), jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (volleyRequestListener != null)
                            volleyRequestListener.onSuccess(table, RequestType.PUT, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (volleyRequestListener != null) {
                            if (error.networkResponse != null)
                                volleyRequestListener.onError(table, true, new String(error.networkResponse.data), error.networkResponse.statusCode);
                            else
                                volleyRequestListener.onError(table, false, null, 0);
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                String auth = "Basic " + session.getApiAuthentication();
                params.put("Authorization", auth);
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(15000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        jsonObjectRequest.setTag(ImonggoOperations.IMONGGO_OPERATIONS_TAG);
        return jsonObjectRequest;
    }
}
