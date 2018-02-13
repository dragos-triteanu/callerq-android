package com.callrq.utils;

import android.support.v4.app.FragmentActivity;
import com.callrq.helpers.PreferencesHelper;

public class AnalyticsActivity extends FragmentActivity {

    // TODO: maybe implement this

    @Override
    protected void onStart() {
        super.onStart();
        if (PreferencesHelper.getAnalyiticsEnabled(this)) {
//            EasyTracker.getInstance().activityStart(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (PreferencesHelper.getAnalyiticsEnabled(this)) {
//            EasyTracker.getInstance().activityStop(this);
        }
    }
}
