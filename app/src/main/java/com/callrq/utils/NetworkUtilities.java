package com.callrq.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.callrq.helpers.PreferencesHelper;
import com.callrq.models.Reminder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Provides utility methods for communicating with the server.
 */
final public class NetworkUtilities {

    /**
     * Timeout (in ms) we specify for each http request
     */
    private static final int HTTP_REQUEST_TIMEOUT_MS = 90 * 1000;
    private static final String URL_BASE = "https://api.callerq.com:8001/mobile";
    private static final String POST_REMINDERS_SUFFIX = "/reminders";
    private static final String REGISTER_SUFFIX = "/register";
    /**
     * The tag used to log to adb console.
     */
    private static final String TAG = "NetworkUtilities";

    private NetworkUtilities() {
    }

    /**
     * Configures the httpClient to connect to the URL provided.
     */
    private static HttpClient getHttpClient() {
        HttpClient httpClient;
        try {

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            httpClient = new DefaultHttpClient(params);
        } catch (Exception e) {
            httpClient = new DefaultHttpClient();
        }
        final HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
        HttpConnectionParams.setSoTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
        ConnManagerParams.setTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
        return httpClient;
    }

    public static String register(String token) {
        HttpResponse resp = null;
        HttpEntity entity = null;
        String response = null;

        JSONObject jsonContent = new JSONObject();
        try {
            jsonContent.put("token", token);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON content: " + e.getLocalizedMessage());
        }

        try {
            entity = new StringEntity(jsonContent.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Error encoding JSON content: " + e.getLocalizedMessage());
        }

        final HttpPost post = new HttpPost(URL_BASE + REGISTER_SUFFIX);
        post.setEntity(entity);
        post.addHeader("Content-Type", "application/json");

        try {
            resp = getHttpClient().execute(post);
        } catch (Exception e) {
            Log.e(TAG, "Error executing post: " + e.getLocalizedMessage());
        }

        int responseStatusCode = 0;
        try {
            assert resp != null;
            responseStatusCode = resp.getStatusLine().getStatusCode();
        } catch (Exception e) {
            Log.e(TAG, "Error reading response code: " + e.getLocalizedMessage());
        }

        if (responseStatusCode == HttpStatus.SC_OK) {
            try {
                InputStream istream = (resp.getEntity() != null) ? resp.getEntity().getContent()
                        : null;
                if (istream != null) {
                    BufferedReader ireader = new BufferedReader(new InputStreamReader(istream));
                    response = ireader.readLine().trim();
                    ireader.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading response: " + e.getLocalizedMessage());
            }
        }

        return response;
    }

    public static String postReminders(Context context, List<Reminder> reminders) {
        HttpResponse resp = null;
        HttpEntity entity = null;
        String response = null;

        JSONObject jsonContent = new JSONObject();
        try {
            String loginToken = PreferencesHelper.getLoginToken(context);
            if (loginToken == null)
                return null;

            jsonContent.put("token", loginToken);

            JSONArray jsonReminders = new JSONArray();
            for (Reminder r : reminders) {
                JSONObject jsonReminder = r.toJSONObject();
                if (jsonReminder != null) {
                    jsonReminders.put(jsonReminder);
                }
            }
            jsonContent.put("reminders", jsonReminders);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON content: " + e.getLocalizedMessage());
        }

        try {
            entity = new StringEntity(jsonContent.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Error encoding JSON content: " + e.getLocalizedMessage());
        }

        final HttpPost post = new HttpPost(URL_BASE + POST_REMINDERS_SUFFIX);
        post.setEntity(entity);
        post.addHeader("Content-Type", "application/json");

        try {
            resp = getHttpClient().execute(post);
        } catch (Exception e) {
            Log.e(TAG, "Error executing post: " + e.getLocalizedMessage());
        }

        assert resp != null;
        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                InputStream istream = (resp.getEntity() != null) ? resp.getEntity().getContent()
                        : null;
                if (istream != null) {
                    BufferedReader ireader = new BufferedReader(new InputStreamReader(istream));
                    response = ireader.readLine().trim();
                    ireader.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading response: " + e.getLocalizedMessage());
            }
        }

        return response;
    }

    public static boolean hasDataConnectivity(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

}
