package com.callrq.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import com.callrq.R;
import com.callrq.activities.MainActivity;
import com.callrq.receivers.CallBroadcastReceiver;
import com.callrq.utils.CallConstants;

public class CallReceiverService extends Service {

    private CallBroadcastReceiver m_CallBroadcastReceiver;

    public static final int NOTIFICATION_ID = 1;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        registerScreenOffReceiver();
        showReceiverRunningNotification();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(m_CallBroadcastReceiver);
        m_CallBroadcastReceiver = null;
        stopForeground(true);
    }

    private void registerScreenOffReceiver() {
        m_CallBroadcastReceiver = new CallBroadcastReceiver();

        IntentFilter receiverFilters = new IntentFilter();
        receiverFilters.addAction(Intent.ACTION_SCREEN_OFF);
        receiverFilters.addAction(CallConstants.PHONE_STATE_ACTION);
        receiverFilters.addAction(CallConstants.OUTGOING_CALL_ACTION);

        registerReceiver(m_CallBroadcastReceiver, receiverFilters);
    }

    private void showReceiverRunningNotification() {
        // Create intent that will bring our app to the front, as if it was tapped in the app
        // launcher
        Intent showTaskIntent = new Intent(getApplicationContext(), MainActivity.class);
        showTaskIntent.setAction(Intent.ACTION_MAIN);
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                showTaskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_logo)
                .setContentTitle("Running in the background")
                .setContentText("Tap to launch the app")
                .setPriority(Notification.PRIORITY_MIN)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(Notification.VISIBILITY_SECRET);
        }

        Notification notification = notificationBuilder.build();

        startForeground(NOTIFICATION_ID, notification);
    }
}
