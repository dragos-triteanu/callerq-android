package com.callerq.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.callerq.R;
import com.callerq.fragments.SettingsFragment;
import com.callerq.fragments.settings.GeneralFragment;
import com.callerq.fragments.settings.NotificationsFragment;
import com.callerq.fragments.settings.PrivacyFragment;

public class SettingsActivity extends AppCompatActivity implements ListView.OnItemClickListener, SettingsFragment.OnFragmentCompleteListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private Fragment settingsFragment;

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
    }



    @Override
    public void onFragmentComplete() {
        assert settingsFragment.getView() != null;
        ListView settingsList = settingsFragment.getView().findViewById(R.id.settingsList);

        String[] settingsOptions = { "General", "Notifications", "Privacy" };

        ArrayAdapter arrayAdapter = new ArrayAdapter<>(this, R.layout.settings_list, R.id.settingsTitle, settingsOptions);

        settingsList.setAdapter(arrayAdapter);
        settingsList.addFooterView(new View(this));

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
                        .replace(R.id.settings_content, new GeneralFragment())
                        .commit();
                setTitle("General");
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
