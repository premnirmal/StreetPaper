package com.test.tube.baby.streetpaper.app.services;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.test.tube.baby.streetpaper.app.request.UrlBuilder;

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
    private static final int ROTATE_TIME_MILLIS = 3 * 60 * 60 * 1000; // rotate every 3 hours
    private static final int FAILED_TIME = 1000; // every second

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

    @Override
    protected void onTryUpdate(int i) throws RetryException {
        if (mCurrentLocation != null) {
            Location currentLocation = mCurrentLocation;
            String url = UrlBuilder.buildUrl(currentLocation, getResources());
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.US);
            List<Address> addresses = new ArrayList<Address>();
            try {
                addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            } catch (IOException e) {
            }
            String name = "";
            String desc = "";
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                name = address.getAdminArea();
                desc = address.getThoroughfare() + "," + address.getSubLocality() + ", " + address.getAdminArea()
                        + ", " + address.getCountryName();
            }
            publishArtwork(new Artwork.Builder()
                    .title(name)
                    .byline(desc)
                    .imageUri(Uri.parse(url))
                    .build());
            scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
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
