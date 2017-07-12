package com.callerq.fragments.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.callerq.R;

/************************************************
 *                                              *
 *   Copyright (C) 2016-2017 Glimpse Team       *
 *                                              *
 *   Created by adipopa on 7/11/2017.          *
 *                                              *
 ************************************************/


public class PrivacyFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_privacy);
    }
}
