package jp.co.freee.bizdev.clockin;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommonLibs {
    static String TAG = CommonLibs.class.getSimpleName();

    public static boolean isInsideFreee(Context context) {
        List<ScanResult> networkList = scan(context);
        return hasFreeeWifiSSIDs(networkList);
    }

    private static List<ScanResult> scan(Context context) {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifi.getScanResults();
    }

    private static boolean hasFreeeWifiSSIDs(List<ScanResult> networkList) {
        Set<String> ssids = new HashSet<>();
        for(ScanResult network : networkList) {
            ssids.add(network.SSID);
        }
        Log.i(TAG, ssids.toString());

        return ssids.contains("0000-USER_A") && ssids.contains("0000-USER_G");
    }
}
