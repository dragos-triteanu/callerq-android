package com.callerq.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.callerq.R;
import com.callerq.helpers.PreferencesHelper;
import com.callerq.utils.RequestCodes;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class StartActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "StartActivity";
    private static final int RC_SIGN_IN = 9001;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInResult result;
    private Button signInButton;
    private Boolean doneLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                if (getIntent().getBooleanExtra("requestLogout", false)) {
                    onLogout();
                }

                final OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
                if (opr.isDone()) {
                    // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                    // and the GoogleSignInResult will be available instantly.
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Got cached sign-in");
                            result = opr.get();
                            handleSignInResult();
                        }
                    }, 1000);
                } else {
                    // If the user has not previously signed in on this device or the sign-in has expired,
                    // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                    // single sign-on will occur in this branch.
                    opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                        @Override
                        public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                            result = googleSignInResult;
                            handleSignInResult();
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        });

        mContentView = findViewById(R.id.fullscreen_content);

        signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLogin();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        signInButton.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        delayedHide(100);
    }

    @Override
    public void onBackPressed() {
        if (doneLoading) {
            super.onBackPressed();
        }
    }

    private void onLogin() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void onLogout() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Log.e(TAG, "User sign out status: " + status);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult();
        }
    }

    private void handleSignInResult() {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, ask for terms agreement
            if (PreferencesHelper.getTermsAccepted(this)) {
                onProceed();
            } else {
                onShowTermsDialog();
            }
        } else {
            // Signed out, show unauthenticated UI.
            signInButton.setVisibility(View.VISIBLE);
            doneLoading = true;
        }
    }

    private void onShowTermsDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        final ViewGroup nullParent = null;
        View view = inflater.inflate(R.layout.dialog_terms, nullParent);

        alertDialog.setTitle(R.string.title_dialog_terms);
        alertDialog.setView(view);

        alertDialog.setPositiveButton(R.string.button_accept,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PreferencesHelper.setTermsAccepted(StartActivity.this, true);
                        ActivityCompat.requestPermissions(StartActivity.this,
                                new String[] { Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE },
                                RequestCodes.MY_PERMISSIONS_MULTIPLE_REQUEST);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.button_decline,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                PreferencesHelper.setTermsAccepted(StartActivity.this, false);
                                onLogout();
                                dialog.dismiss();
                            }
                        });

        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                PreferencesHelper.setTermsAccepted(StartActivity.this, false);
                onLogout();
            }
        });

        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onProceed();
    }

    private void onProceed() {
        GoogleSignInAccount account = result.getSignInAccount();
        Intent intent = new Intent(StartActivity.this, MainActivity.class);
        intent.putExtra("accountDetails", account);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    // Runnable for hiding the status and navigation bar
    private final Runnable mHidePartRunnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            // Hide UI first
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }

            // Schedule a runnable to remove the status and navigation bar after a delay
            mHideHandler.postDelayed(mHidePartRunnable, UI_ANIMATION_DELAY);
        }
    };

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Connection failed with error code " + connectionResult);
        Toast.makeText(this, getString(R.string.play_services_connection_error), Toast.LENGTH_SHORT).show();
    }
}
