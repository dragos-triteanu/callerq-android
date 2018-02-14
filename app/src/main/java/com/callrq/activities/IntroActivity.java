package com.callrq.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.callrq.R;
import com.callrq.helpers.PreferencesHelper;
import com.callrq.utils.RequestCodes;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IntroActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final String TAG = "IntroActivity";
    private static final int RC_SIGN_IN = 9001;

    @BindView(R.id.fullscreen_content)
    View mContentView;

    @BindView(R.id.googleSignInButton)
    SignInButton mGoogleSignInButton;

    @BindView(R.id.facebookSignInButton)
    LoginButton mFacebookSignInButton;

    @BindView(R.id.authContainer)
    View authContainer;

    @BindView(R.id.loadingIndicator)
    View loadingIndicator;


    private FirebaseAuth mFirebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;
    private Boolean isLoading = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);

        Intent logout = getIntent();

        mFirebaseAuth = FirebaseAuth.getInstance();
        authContainer.setVisibility(View.INVISIBLE);
        loadingIndicator.setVisibility(View.INVISIBLE);
        initGoogleFirebaseAuth();
        initFacebookFirebaseAuth();

        boolean logoutRequested = logout.getBooleanExtra("requestLogout", false);
        if (logoutRequested) {
            onLogout();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();

        if (null != currentUser) {
            onProceed(currentUser);
        } else {
            authContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (isLoading) {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        Log.i(TAG, "Clicked on " + view.getId());
        switch (view.getId()) {
            case R.id.googleSignInButton:
                googleSignIn();
                break;
        }
    }

    private void initGoogleFirebaseAuth() {
        mGoogleSignInButton.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initFacebookFirebaseAuth() {
        mCallbackManager = CallbackManager.Factory.create();
        mFacebookSignInButton.setReadPermissions("email", "public_profile");
        mFacebookSignInButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                firebaseAuthWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                showAuthButtons();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                showAuthButtons();
                Snackbar.make(mContentView, getString(R.string.authentication_failed), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        loadingIndicator.setVisibility(View.VISIBLE);
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            handleSignInResult(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.fullscreen_content), getString(R.string.authentication_failed), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void firebaseAuthWithFacebook(AccessToken token) {
        loadingIndicator.setVisibility(View.VISIBLE);
        Log.d(TAG, "firebaseAuthWithFacebook:" + token);

        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            handleSignInResult(user);
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                Log.e(TAG, "firebaseAuthWithFacebook:: Account login colission", e);
                                if (e.getErrorCode().equals("ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL")) {

                                }
                            } catch (Exception e) {
                                Log.e(TAG, "firebaseAuthWithFacebook:: Error while facebook login", e);
                            }


                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.fullscreen_content), getString(R.string.authentication_failed), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Method that opens up the default google sign in screen
     */
    private void googleSignIn() {
        Log.i(TAG, "googleSignIn:: Handling sign  using google");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void onLogout() {
        // Firebase sign out
        mFirebaseAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });

        LoginManager.getInstance().logOut();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        authContainer.setVisibility(View.INVISIBLE);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                authContainer.setVisibility(View.VISIBLE);
                Log.w(TAG, "Google sign in failed", e);
            }
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RequestCodes.MY_PERMISSIONS_REQUEST_MAKE_PHONE_CALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onProceed(mFirebaseAuth.getCurrentUser());
                } else {
                    finish();
                }
                break;
            default:
                onProceed(mFirebaseAuth.getCurrentUser());
                break;
        }
    }

    private void onProceed(final FirebaseUser firebaseUser) {

        assert firebaseUser != null;
        Task<GetTokenResult> idToken = firebaseUser.getIdToken(true);

        idToken.addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                if (getIntent().getBooleanExtra("displayReminders", false)) {
                    intent.putExtra("displayReminders", true);
                }
                startActivity(intent);
                loadingIndicator.setVisibility(View.INVISIBLE);
                finish();
            }
        });


    }


    private void handleSignInResult(FirebaseUser firebaseUser) {
        Log.d(TAG, "handleSignInResult:");
        if (PreferencesHelper.getTermsAccepted(this)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(IntroActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        RequestCodes.MY_PERMISSIONS_REQUEST_MAKE_PHONE_CALL);
            } else {
                onProceed(firebaseUser);
            }
        } else {
            onShowTermsDialog();
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
                        PreferencesHelper.setTermsAccepted(IntroActivity.this, true);
                        ActivityCompat.requestPermissions(IntroActivity.this,
                                new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CALENDAR},
                                RequestCodes.MY_PERMISSIONS_MULTIPLE_REQUEST);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.button_decline,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                PreferencesHelper.setTermsAccepted(IntroActivity.this, false);
                                onLogout();
                                dialog.dismiss();
                                showAuthButtons();
                            }
                        });

        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                PreferencesHelper.setTermsAccepted(IntroActivity.this, false);
                onLogout();
                showAuthButtons();
            }
        });

        alertDialog.show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Connection failed with error code " + connectionResult);
        Toast.makeText(this, getString(R.string.play_services_connection_error), Toast.LENGTH_SHORT).show();
    }

    private void showAuthButtons() {
        authContainer.setVisibility(View.VISIBLE);
    }

    private void hideAuthButtons() {
        authContainer.setVisibility(View.INVISIBLE);
    }

}
