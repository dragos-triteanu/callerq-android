package com.callrq.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.*;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.callrq.R;
import com.callrq.fragments.HomeFragment;
import com.callrq.fragments.RemindersFragment;
import com.callrq.helpers.DatabaseHelper;
import com.callrq.models.Reminder;
import com.callrq.utils.RequestCodes;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener, DatabaseHelper.DatabaseListener {

    private static final String TAG = "MainActivity";
    private static final int PICK_CONTACT = 0;

    public static boolean isRunning = false;
    public static boolean displayReminders = false;

    FirebaseUser account;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private Intent callIntent;
    private Boolean doubleBackToExitPressedOnce = false;

    private Fragment remindersFragment;

    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mFirebaseAuth = FirebaseAuth.getInstance();

        account = mFirebaseAuth.getCurrentUser();

        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            RequestCodes.MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                    return;
                }

                Intent contactsIntent = new Intent();
                contactsIntent.setAction(Intent.ACTION_PICK);
                contactsIntent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);

                try {
                    startActivityForResult(contactsIntent, PICK_CONTACT);
                } catch (ActivityNotFoundException e) {
                    Log.e(getClass().getSimpleName(), e.getLocalizedMessage());
                }
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        ImageView userPhoto = header.findViewById(R.id.userPhoto);
        TextView userDisplayName = header.findViewById(R.id.userDisplayName);
        TextView userEmail = header.findViewById(R.id.userEmail);

        Glide.with(this).load(account.getPhotoUrl()).apply(RequestOptions.circleCropTransform()).into(userPhoto);
        userDisplayName.setText(account.getDisplayName());
        userEmail.setText(account.getEmail());

        Menu navMenu = navigationView.getMenu();

        MenuItem homeItem = navMenu.findItem(R.id.nav_home);
        MenuItem remindersItem = navMenu.findItem(R.id.nav_reminders);

        remindersFragment = new RemindersFragment();

        if (getIntent().getBooleanExtra("displayReminders", false)) {
            setFragment(remindersFragment, remindersItem);
        } else {
            setFragment(new HomeFragment(), homeItem);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Create a new fragment and specify the fragment to show based on nav item clicked

        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                setFragment(new HomeFragment(), item);
                break;
            case R.id.nav_reminders:
                setFragment(remindersFragment, item);
                break;
            case R.id.nav_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.nav_logout:
                startActivity(new Intent(MainActivity.this, IntroActivity.class).putExtra("requestLogout", true));
                finish();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        isRunning = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Menu navMenu = navigationView.getMenu();
        MenuItem remindersItem = navMenu.findItem(R.id.nav_reminders);

        if (displayReminders) {
            setFragment(new RemindersFragment(), remindersItem);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        displayReminders = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
    }

    private void setFragment(@NonNull Fragment fragment, @NonNull MenuItem item) {

        // Insert the fragment by replacing any existing fragment
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_content, fragment).commit();

        item.setChecked(true);
        setTitle(item.getTitle());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCodes.MY_PERMISSIONS_REQUEST_MAKE_PHONE_CALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(callIntent);
                    finish();
                }
                break;
            case RequestCodes.MY_PERMISSIONS_REQUEST_READ_CONTACTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fab.callOnClick();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {

                    final Uri phoneNumberUri = data.getData();
                    getSupportLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {

                        @Override
                        public Loader<Cursor> onCreateLoader(int loaderId, Bundle extras) {
                            return new CursorLoader(MainActivity.this, phoneNumberUri, null, null,
                                    null, null);
                        }

                        @Override
                        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                            if (cursor.moveToFirst()) {
                                String phoneNumber = cursor.getString(cursor
                                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                // perform the call
                                callIntent = new Intent(Intent.ACTION_CALL);
                                callIntent.setData(Uri.fromParts("tel", phoneNumber, null));
                                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.CALL_PHONE},
                                            RequestCodes.MY_PERMISSIONS_REQUEST_MAKE_PHONE_CALL);
                                    return;
                                }

                                startActivity(callIntent);
                                finish();
                            }
                        }

                        @Override
                        public void onLoaderReset(Loader<Cursor> loader) {
                        }
                    });

                }
                break;
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.double_press_close, Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Connection failed with error code " + connectionResult);
        Toast.makeText(this, getString(R.string.play_services_connection_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void savedReminder(String requestId) {

    }

    @Override
    public void gotReminders(ArrayList<Reminder> reminders) {
        assert remindersFragment.getView() != null;

        ListView remindersList = remindersFragment.getView().findViewById(R.id.remindersList);

        ReminderAdapter reminderAdapter = new ReminderAdapter(this, reminders);

        remindersList.setAdapter(reminderAdapter);

        if (reminderAdapter.getCount() == 0) {
            remindersFragment.getView().findViewById(R.id.remindersEmpty).setVisibility(View.VISIBLE);
        }

    }

    private class ReminderAdapter extends ArrayAdapter<Reminder> {

        private ReminderAdapter(Context context, ArrayList<Reminder> users) {
            super(context, 0, users);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            Reminder reminder = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.reminders_list, parent, false);
            }
            // Lookup view for data population
            TextView reminderTitle = convertView.findViewById(R.id.reminderTitle);
            TextView reminderDateTime = convertView.findViewById(R.id.reminderDateTime);
            // Populate the data into the template view using the data object

            assert reminder != null;
            if (reminder.isMeeting()) {
                reminderTitle.setText(getString(R.string.calendar_event_title_meeting) + " " + reminder.getContactName());
            } else {
                reminderTitle.setText(getString(R.string.calendar_event_title_call) + " " + reminder.getContactName());
            }

            SimpleDateFormat simpleDateFormat;
            Calendar currentCalendar = Calendar.getInstance();
            int today = currentCalendar.get(Calendar.DATE);

            currentCalendar.add(Calendar.DATE, 1);
            int tomorrow = currentCalendar.get(Calendar.DATE);

            Calendar scheduledCalendar = Calendar.getInstance();
            scheduledCalendar.setTimeInMillis(reminder.getScheduleDatetime());

            // TODO: if there's any problems, replace DATE by DAY_OF_YEAR back how it was

            String dateTimeText = "";

            if (scheduledCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)) {
                if (scheduledCalendar.get(Calendar.DATE) == today) {
                    simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.US);
                    dateTimeText = "Today, ";
                } else if (scheduledCalendar.get(Calendar.DATE) == tomorrow) {
                    simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.US);
                    dateTimeText = "Tomorrow, ";
                } else {
                    simpleDateFormat = new SimpleDateFormat("E, MMMM dd, HH:mm", Locale.US);
                }
            } else {
                simpleDateFormat = new SimpleDateFormat("E, MMMM dd, yyyy, HH:mm", Locale.US);
            }

            dateTimeText += simpleDateFormat.format(reminder.getScheduleDatetime());
            reminderDateTime.setText(dateTimeText);
            // Return the completed view to render on screen
            return convertView;
        }
    }
}
