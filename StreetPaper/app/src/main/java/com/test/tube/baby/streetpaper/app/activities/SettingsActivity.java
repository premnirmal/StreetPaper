package com.test.tube.baby.streetpaper.app.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import com.test.tube.baby.streetpaper.app.R;
import com.test.tube.baby.streetpaper.app.services.StreetPaperService;
import com.test.tube.baby.streetpaper.app.utils.PreferenceKeys;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by prem on 2/17/14.
 */
public class SettingsActivity extends FragmentActivity {

    public static final String PREFS_NAME = "streetpaper_prefs";
    protected StreetPaperService mService;
    protected boolean mBound;

    public static final String[] zooms = {" 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ", " 8 ", " 9 ", " 10 ", " 11 "};

    public static final int BASE_REFRESH_RATE = 3 * 60 * 60 * 1000; // 3 hours

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, StreetPaperService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault("");

        setContentView(R.layout.settings_activity);

        // simple slide in animation
        ViewGroup layout = (ViewGroup) findViewById(R.id.layout);
        Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        layout.setLayoutAnimation(new LayoutAnimationController(animation));

        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        final SharedPreferences.Editor editor = settings.edit();

        //Find views
        Button okButton = (Button) findViewById(R.id.ok_button);
        final Spinner modeChooser = (Spinner) findViewById(R.id.mode_chooser);
        final Spinner zoomChooser = (Spinner) findViewById(R.id.zoom_chooser);

        Switch wifiOnly = (Switch) findViewById(R.id.wifi_only);

        //Wifi status and setting
        wifiOnly.setChecked(settings.getBoolean(PreferenceKeys.WIFI_ONLY, false));

        wifiOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(PreferenceKeys.WIFI_ONLY, isChecked);
                editor.commit();
            }
        });

        // mode
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.modes, R.layout.spinner_layout);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeChooser.setAdapter(adapter);
        modeChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mService != null)
                    mService.buildImage(position, settings.getInt(PreferenceKeys.ZOOM, 12));
                editor.putInt(PreferenceKeys.MODE, position);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // zoom
        ArrayAdapter<CharSequence> zoomAdapter = new ArrayAdapter<CharSequence>(this, R.layout.spinner_layout, zooms);
        zoomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zoomChooser.setAdapter(zoomAdapter);
        zoomChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mService != null)
                    mService.buildImage(settings.getInt(PreferenceKeys.MODE, 0), position + 8);
                editor.putInt(PreferenceKeys.ZOOM, position + 8);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // refresh
        final Spinner refresh_spinner = (Spinner) findViewById(R.id.refresh_rate);
        ArrayAdapter<CharSequence> refreshRateAdapter = ArrayAdapter.createFromResource(this, R.array.refreshrates, R.layout.spinner_layout);
        refreshRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        refresh_spinner.setAdapter(refreshRateAdapter);
        refresh_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor.putInt(PreferenceKeys.REFRESH_TIME, position);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        int refreshRate = settings.getInt(PreferenceKeys.REFRESH_TIME, 0);
        int mode = settings.getInt(PreferenceKeys.MODE, 0);
        int zoom = settings.getInt(PreferenceKeys.ZOOM, 12);

        modeChooser.setSelection(mode);
        zoomChooser.setSelection(zoom - 8);
        refresh_spinner.setSelection(refreshRate);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt(PreferenceKeys.MODE, modeChooser.getSelectedItemPosition());
                editor.putInt(PreferenceKeys.ZOOM, zoomChooser.getSelectedItemPosition() + 8);
                editor.putInt(PreferenceKeys.REFRESH_TIME, refresh_spinner.getSelectedItemPosition());
                editor.commit();
                finish();
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            StreetPaperService.LocalBinder binder = (StreetPaperService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}