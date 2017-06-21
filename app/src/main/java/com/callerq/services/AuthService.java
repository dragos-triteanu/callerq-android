package com.callerq.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthService extends Service {

    private Boolean userLoggedIn;

    public AuthService() {
        userLoggedIn = false;

    }

    public Boolean isUserLoggedIn() {
        return userLoggedIn;
    }

    public void onLogin() {
        userLoggedIn = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        throw new UnsupportedOperationException("Not yet implemented");
    }
}
