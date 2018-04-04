package com.callrq.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.callrq.R;
import com.callrq.fragments.SettingsFragment;
import com.callrq.fragments.settings.GeneralFragment;
import com.callrq.fragments.settings.NotificationsFragment;
import com.callrq.fragments.settings.PrivacyFragment;
import com.callrq.services.CallReceiverService;

public class SettingsActivity extends AppCompatActivity implements ListView.OnItemClickListener, SettingsFragment.OnFragmentCompleteListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private Fragment settingsFragment;

    private GeneralFragment generalFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settingsFragment = new SettingsFragment();

        getFragmentManager().beginTransaction()
                .replace(R.id.settings_content, settingsFragment)
                .commit();

        generalFragment = new GeneralFragment();
    }

    // complicated totally unnecessary method for listening for preference changes and confirming app status
    private void generalFragmentInit() {
        final String statusSummaryTrue = getString(R.string.application_status_summary_true);
        final String statusSummaryFalse = getString(R.string.application_status_summary_false);

        generalFragment.sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, Object object) {
                if (preference.getKey().equals("pref_application_status")) {
                    final SwitchPreference applicationStatus = (SwitchPreference) preference;
                    final boolean isChecked = (Boolean) object;

                    if (isChecked == applicationStatus.isChecked()) {
                        applicationStatus.setSummary(isChecked ? statusSummaryTrue : statusSummaryFalse);
                    } else {
                        final Intent callReceiverService = new Intent(SettingsActivity.this, CallReceiverService.class);
                        if (isChecked) {
                            applicationStatus.setSummary(statusSummaryTrue);
                            startService(callReceiverService);
                        } else {
                            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsActivity.this);

                            alertDialog.setTitle(R.string.title_confirmation_dialog)
                                    .setMessage("Are you sure you want to entirely disable notifications?")
                                    .setPositiveButton(R.string.ignore_dialog_positive,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    preference.getEditor().putBoolean(preference.getKey(), false);
                                                    applicationStatus.setChecked(false);
                                                    applicationStatus.setSummary(statusSummaryFalse);
                                                    stopService(callReceiverService);
                                                    dialog.dismiss();
                                                }
                                            })
                                    .setNegativeButton(R.string.ignore_dialog_negative,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                }
                                            })
                                    .setCancelable(false)
                                    .show();

                            return false;
                        }
                    }
                }
                return true;
            }
        };
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onFragmentComplete() {
        assert settingsFragment.getView() != null;
        ListView settingsList = settingsFragment.getView().findViewById(R.id.settingsList);

        String[] settingsOptions = {"General", "Notifications", "Privacy"};

        ArrayAdapter arrayAdapter = new ArrayAdapter<>(this, R.layout.settings_list, R.id.settingsTitle, settingsOptions);

        settingsList.setAdapter(arrayAdapter);

        settingsList.setOnItemClickListener(this);

        settingsList.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0:
                getFragmentManager().beginTransaction()
                        .replace(R.id.settings_content, generalFragment)
                        .commit();
                setTitle("General");
                generalFragmentInit();
                break;
            case 1:
                getFragmentManager().beginTransaction()
                        .replace(R.id.settings_content, new NotificationsFragment())
                        .commit();
                setTitle("Notifications");
                break;
            case 2:
                getFragmentManager().beginTransaction()
                        .replace(R.id.settings_content, new PrivacyFragment())
                        .commit();
                setTitle("Privacy");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (getTitle().equals("Settings")) {
            super.onBackPressed();
        } else {
            getFragmentManager().beginTransaction()
                    .replace(R.id.settings_content, settingsFragment)
                    .commit();
            setTitle("Settings");
        }
    }
}
