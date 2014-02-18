package com.test.tube.baby.streetpaper.app.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.test.tube.baby.streetpaper.app.activities.SettingsActivity;
import com.test.tube.baby.streetpaper.app.request.UrlBuilder;
import com.test.tube.baby.streetpaper.app.utils.Config;
import com.test.tube.baby.streetpaper.app.utils.PreferenceKeys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by prem on 2/17/14.
 */
public class StreetPaperService extends RemoteMuzeiArtSource implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    public static final String APP_NAME = "StreetPaper";
    private static final int FAILED_TIME = 1000; // every second
    private final IBinder mBinder = new LocalBinder();
    private LocationClient mLocationClient;
    private Location mCurrentLocation;

    public StreetPaperService() {
        super(APP_NAME);
    }

    public StreetPaperService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationClient = setupLocationClient();
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
    }

    public class LocalBinder extends Binder {
        public StreetPaperService getService() {
            return StreetPaperService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private LocationClient setupLocationClient() {
        LocationClient locationClient = new LocationClient(getApplicationContext(), this, this);
        locationClient.connect();
        return locationClient;
    }

    @Override
    public void onDestroy() {
        disconnectClient();
        super.onDestroy();
    }

    public void buildImage(int mode, int zoom) {
        if (mCurrentLocation != null) {
            Location currentLocation = mCurrentLocation;
            String url = UrlBuilder.buildUrl(currentLocation, getResources(), mode, zoom);
            Geocoder geocoder = new Geocoder(this, Locale.US);
            List<Address> addresses = new ArrayList<Address>();
            try {
                addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            } catch (IOException e) {
            }
            String name = "";
            String desc = "";
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                name = address.getSubLocality();
                desc = address.getAdminArea() + " " + address.getCountryName();
            }
            publishArtwork(new Artwork.Builder()
                    .title(name)
                    .byline(desc)
                    .imageUri(Uri.parse(url))
                    .build());
        }
    }

    @Override
    protected void onTryUpdate(int i) throws RetryException {
        final SharedPreferences settings = getSharedPreferences(SettingsActivity.PREFS_NAME, 0);
        // Check if we cancel the update due to WIFI connection
        if (settings.getBoolean(PreferenceKeys.WIFI_ONLY, false) && !Config.isWifiConnected(this)) {
            scheduleUpdate(System.currentTimeMillis() + SettingsActivity.BASE_REFRESH_RATE * (1 + settings.getInt(PreferenceKeys.REFRESH_TIME, 0)));
            return;
        }

        if (mCurrentLocation != null) {
            buildImage(settings.getInt(PreferenceKeys.MODE, 0), settings.getInt(PreferenceKeys.ZOOM, 12));
            scheduleUpdate(System.currentTimeMillis() + SettingsActivity.BASE_REFRESH_RATE * (1 + settings.getInt(PreferenceKeys.REFRESH_TIME, 0)));
        } else {
            scheduleUpdate(System.currentTimeMillis() + FAILED_TIME);
        }

    }

    private void disconnectClient() {
        if (mLocationClient != null) {
            if (mLocationClient.isConnected()) {
                mLocationClient.disconnect();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = mLocationClient.getLastLocation();
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}
