package com.test.tube.baby.streetpaper.app.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by prem on 2/17/14.
 */
public class Config {

    public static final String API_KEY = "AIzaSyBm5p73wtKFS_v5Mg8l19e0wob1CmIUq_0";

    /**
     * Determines if the WIFI is connected
     *
     * @param context the needed Context
     * @return true if connected
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }


}
