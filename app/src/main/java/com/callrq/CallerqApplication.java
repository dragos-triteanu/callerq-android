package com.callrq;

import android.app.Application;
import android.content.Context;
import com.callrq.injection.component.ApplicationComponent;
import com.callrq.injection.component.DaggerApplicationComponent;
import com.callrq.injection.module.ServiceModule;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class CallerqApplication extends Application {
    private static final String TAG = "CallerqApplication: ";
    public static ApplicationComponent APP;
    private Tracker mTracker;

    public static CallerqApplication get(Context context) {
        return (CallerqApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        APP = DaggerApplicationComponent.builder()
                .serviceModule(new ServiceModule())
                .build();

    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(ACCESSIBILITY_SERVICE);
        }
        return mTracker;
    }

    public Tracker getmTracker() {
        return mTracker;
    }

    public void setmTracker(Tracker mTracker) {
        this.mTracker = mTracker;
    }
}
