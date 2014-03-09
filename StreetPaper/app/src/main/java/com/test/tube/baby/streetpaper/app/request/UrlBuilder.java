package com.test.tube.baby.streetpaper.app.request;

import android.content.res.Resources;
import android.location.Location;

import com.test.tube.baby.streetpaper.app.utils.Config;

import java.util.Random;

/**
 * Created by prem on 2/17/14.
 */
public class UrlBuilder {

    public static final String[] modes = {"", "&maptype=satellite", "&maptype=hybrid"};

    private static Random mRand = new Random();

    private UrlBuilder() {
    }

    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/staticmap?center=";

    public static String buildUrl(Location location, Resources res, int mode, int zoom) {
        return BASE_URL + location.getLatitude() + "," + location.getLongitude()
                + "&zoom=" + getZoom(zoom) + "&size=" + getDimensions(res) + getMode(mode)
                + "&sensor=false&key=" + Config.API_KEY;
    }

    private static String getZoom(int zoom) {
        String zoomVal;
        if(zoom == 7) {
            zoomVal = String.valueOf(mRand.nextInt(18) + 7);
        } else {
            zoomVal = String.valueOf(zoom);
        }
        return zoomVal;
    }

    private static String getMode(int mode) {
        String modeString;
        if (mode < 3) {
            modeString = modes[mode];
        } else {
            modeString = modes[mRand.nextInt(4)];
        }
        return modeString;
    }

    private static String getDimensions(Resources res) {
        String width = String.valueOf(res.getDisplayMetrics().widthPixels);
        String height = String.valueOf(res.getDisplayMetrics().heightPixels);
        return width + "x" + height;
    }
}

