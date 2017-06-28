package com.callerq;

import android.app.Application;
import android.content.Context;
import com.callerq.injection.component.ApplicationComponent;
import com.callerq.injection.component.DaggerApplicationComponent;
import com.callerq.injection.module.ServiceModule;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class CallerqApplication extends Application {
    private static final String TAG = "CallerqApplication: ";

    private Tracker mTracker;
    public static ApplicationComponent APP;

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

    public static CallerqApplication get(Context context) {
        return (CallerqApplication) context.getApplicationContext();
    }

    public Tracker getmTracker() {
        return mTracker;
    }

    public void setmTracker(Tracker mTracker) {
        this.mTracker = mTracker;
    }
}
