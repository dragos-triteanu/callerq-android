package com.callrq.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.callrq.services.CallReceiverService;
import com.google.firebase.auth.FirebaseAuth;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

            if (mFirebaseAuth.getCurrentUser() != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean applicationEnabled = prefs.getBoolean("pref_application_status", true);

                if (applicationEnabled) {
                    Intent callReceiverService = new Intent(context, CallReceiverService.class);
                    context.startService(callReceiverService);
                }
            }
        }

    }
}
