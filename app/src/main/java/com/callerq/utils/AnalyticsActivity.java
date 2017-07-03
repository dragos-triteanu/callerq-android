package com.callerq.utils;

import com.callerq.helpers.PreferencesHelper;

import android.support.v4.app.FragmentActivity;

public class AnalyticsActivity extends FragmentActivity {
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
