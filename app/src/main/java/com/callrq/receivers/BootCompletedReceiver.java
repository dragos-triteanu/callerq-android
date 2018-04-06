package com.callrq.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.callrq.services.CallReceiverService;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent callReceiverService = new Intent(context, CallReceiverService.class);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean applicationEnabled = prefs.getBoolean("pref_application_status", true);
            if (applicationEnabled) {
                context.startService(callReceiverService);
            }
        }

    }
}
