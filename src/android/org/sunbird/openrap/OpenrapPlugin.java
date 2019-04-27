package org.sunbird.openrap;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sunbird.openrap.nsd.OpenrapDiscoveryHelper;
import org.sunbird.openrap.nsd.OpenrapDiscoveryListener;

import java.util.ArrayList;


public class OpenrapPlugin extends CordovaPlugin implements OpenrapDiscoveryListener {

    private static final String KEY_OPEN_RAP_HOST = "open_rap_host";
    private static final String KEY_OPEN_RAP_PORT = "open_rap_port";
    private static String openRapHost;
    private static final String SHARED_PREF_NAME = "openrap";

    public static JSONObject jsonObject = new JSONObject();
    private ArrayList<CallbackContext> mHandler = new ArrayList<>();
    private JSONObject mLastEvent;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (args.get(0).equals("startDiscovery")) {
            this.startDiscovery();
            mHandler.add(callbackContext);
        }
        return true;
    }

    private void startDiscovery() {
        new OpenrapDiscoveryHelper(this.cordova.getActivity(), this).startDiscovery("_openrap._tcp",
                "Open Resource Access Point");
    }

    @Override
    public void onNsdServiceFound(NsdServiceInfo foundServiceInfo) {

    }

    @Override
    public void onNsdDiscoveryFinished() {

    }

    @Override
    public void onNsdServiceResolved(NsdServiceInfo resolvedNsdServiceInfo) {

    }

    @Override
    public void onConnectedToService(NsdServiceInfo connectedServiceInfo) {
        getSharedPreferences(cordova.getContext()).edit().putString(KEY_OPEN_RAP_HOST, connectedServiceInfo.getHost().toString()).apply();
        getSharedPreferences(cordova.getContext()).edit().putInt(KEY_OPEN_RAP_PORT, connectedServiceInfo.getPort()).apply();
        setParams();

       try {
            jsonObject.put("actionType","connected");
            jsonObject.put("ip",openRapHost);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mLastEvent = jsonObject;
        consumeEvents();

    }

    @Override
    public void onNsdServiceLost(NsdServiceInfo nsdServiceInfo) {
        try {
            jsonObject.put("actionType","disconnected");
            jsonObject.put("ip",openRapHost);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mLastEvent = jsonObject;
        consumeEvents();

    }

    public void setParams() {

        openRapHost = getSharedPreferences(cordova.getContext()).getString(KEY_OPEN_RAP_HOST, null);

        // int port = PreferenceUtil.getPreferenceWrapper().getInt(KEY_OPEN_RAP_PORT,0);
        openRapHost = openRapHost.replace("/", "http://");

    }

    public SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    private void consumeEvents() {
        if (this.mHandler.size() == 0 || mLastEvent == null) {
            return;
        }

        for (CallbackContext callback : this.mHandler) {
            final PluginResult result = new PluginResult(PluginResult.Status.OK, mLastEvent);
            result.setKeepCallback(true);
            callback.sendPluginResult(result);
        }

        mLastEvent = null;
    }

}
