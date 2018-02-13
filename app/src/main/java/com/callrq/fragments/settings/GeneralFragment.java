package com.callrq.fragments.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import com.callrq.R;

public class GeneralFragment extends PreferenceFragment {

    private static String statusSummaryTrue;
    private static String statusSummaryFalse;

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object object) {
            if (preference instanceof SwitchPreference) {
                SwitchPreference applicationStatus = (SwitchPreference) preference;
                boolean isChecked = (Boolean) object;

                applicationStatus.setSummary(isChecked ? statusSummaryTrue : statusSummaryFalse);
            }
            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_general);

        statusSummaryTrue = getString(R.string.application_status_summary_true);
        statusSummaryFalse = getString(R.string.application_status_summary_false);

        bindPreferenceSummaryToValue(findPreference("pref_application_status"));
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getBoolean(preference.getKey(), false));
    }

}
