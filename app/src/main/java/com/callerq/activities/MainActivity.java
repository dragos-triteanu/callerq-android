package com.callerq.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.callerq.CallerqApplication;
import com.callerq.R;
import com.callerq.fragments.HomeFragment;
import com.callerq.fragments.RemindersFragment;
import com.callerq.services.ScheduleService;
import com.callerq.utils.RequestCodes;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import javax.inject.Inject;

public class MainActivity extends CallerqActivity implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    public static boolean isRunning = false;
    private static final int PICK_CONTACT = 0;
    public static boolean displayReminders = false;
    private Intent callIntent;

    GoogleSignInAccount account;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @Inject
    ScheduleService scheduleService;

    private Boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        account = getIntent().getParcelableExtra("accountDetails");

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

        if (getIntent().getBooleanExtra("displayReminders", false)) {
            setFragment(RemindersFragment.class, remindersItem);
        } else {
            setFragment(HomeFragment.class, homeItem);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Create a new fragment and specify the fragment to show based on nav item clicked

        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                setFragment(HomeFragment.class, item);
                break;
            case R.id.nav_reminders:
                setFragment(RemindersFragment.class, item);
                break;
            case R.id.nav_about:

                break;
            case R.id.nav_settings:

                break;
            case R.id.nav_logout:
                startActivity(new Intent(MainActivity.this, StartActivity.class).putExtra("requestLogout", true));
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
            setFragment(RemindersFragment.class, remindersItem);
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

    private void setFragment(@NonNull Class fragmentClass, @NonNull MenuItem item) {
        Fragment fragment = null;

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_content, fragment).commit();

        item.setChecked(true);
        setTitle(item.getTitle());
    }

    @Override
    void injectDependencies() {
        CallerqApplication.APP.inject(this);
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
}
