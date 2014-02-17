package com.test.tube.baby.streetpaper.app.request;

import android.content.res.Resources;
import android.location.Location;

import com.test.tube.baby.streetpaper.app.utils.Config;

/**
 * Created by prem on 2/17/14.
 */
public class UrlBuilder {

    public static final String SATELLITE = "satellite";
    public static final String HYBRID = "HYBRID";

    private static final String ZOOM = "12";

    private UrlBuilder() {
    }

    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/staticmap?center=";

    public static String buildUrl(Location location, Resources res) {
        return BASE_URL + location.getLatitude() + "," + location.getLongitude() +
                "&zoom=" + ZOOM + "&size=" + getDimensions(res) +// "&maptype=" + SATELLITE+
                "&sensor=false&key=" + Config.API_KEY;
    }

    private String getMapType() {
        return "";
    }

    private static String getDimensions(Resources res) {
        String width = String.valueOf(res.getDisplayMetrics().widthPixels);
        String height = String.valueOf(res.getDisplayMetrics().heightPixels);
        return width + "x" + height;
    }
}

