package com.callerq.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.callerq.CallerqApplication;
import com.callerq.R;
import com.callerq.fragments.HomeFragment;
import com.callerq.fragments.RemindersFragment;
import com.callerq.services.ScheduleService;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import javax.inject.Inject;

public class MainActivity extends CallerqActivity implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    GoogleSignInAccount account;
    @Inject
    ScheduleService scheduleService;
    private Boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        account = getIntent().getParcelableExtra("accountDetails");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RescheduleActivity.class));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setFragment(HomeFragment.class);

        View header = navigationView.getHeaderView(0);

        ImageView userPhoto = header.findViewById(R.id.userPhoto);
        TextView userDisplayName = header.findViewById(R.id.userDisplayName);
        TextView userEmail = header.findViewById(R.id.userEmail);

        Glide.with(this).load(account.getPhotoUrl()).apply(RequestOptions.circleCropTransform()).into(userPhoto);
        userDisplayName.setText(account.getDisplayName());
        userEmail.setText(account.getEmail());
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Create a new fragment and specify the fragment to show based on nav item clicked

        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                setFragment(HomeFragment.class);
                item.setChecked(true);
                setTitle("CallerQ - " + item.getTitle());
                break;
            case R.id.nav_reminders:
                setFragment(RemindersFragment.class);
                item.setChecked(true);
                setTitle("CallerQ - " + item.getTitle());
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

    private void setFragment(@NonNull Class fragmentClass) {
        Fragment fragment = null;

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_content, fragment).commit();
    }

    @Override
    void injectDependencies() {
        CallerqApplication.APP.inject(this);
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
