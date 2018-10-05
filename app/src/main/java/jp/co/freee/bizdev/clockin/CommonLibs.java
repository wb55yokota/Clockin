package jp.co.freee.bizdev.clockin;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommonLibs {
    static String TAG = CommonLibs.class.getSimpleName();

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(context.getResources().getString(R.string.app_name), Activity.MODE_PRIVATE);
    }

    public static boolean isInsideFreee(Context context) {
        List<ScanResult> networkList = scan(context);
        return hasFreeeWifiSSIDs(context, networkList);
    }

    private static List<ScanResult> scan(Context context) {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifi.getScanResults();
    }

    private static boolean hasFreeeWifiSSIDs(Context context, List<ScanResult> networkList) {
        Set<String> ssids = new HashSet<>();
        for(ScanResult network : networkList) {
            ssids.add(network.SSID);
        }
        Log.i(TAG, ssids.toString());

        String[] targetSsids = context.getResources().getStringArray(R.array.ssids);
        for(String targetSsid : targetSsids) {
            if(!ssids.contains(targetSsid)) {
                return false;
            }
        }
        return true;
    }

    public static String prettyJson(String str) {
        try {
            JSONObject json = new JSONObject(str);
            return json.toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return str;
    }
}
