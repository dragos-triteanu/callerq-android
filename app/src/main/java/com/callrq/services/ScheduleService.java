package com.callrq.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.telephony.PhoneNumberUtils;
import com.callrq.R;
import com.callrq.activities.RescheduleActivity;
import com.callrq.models.CallDetails;
import com.callrq.utils.CallConstants;

public class ScheduleService extends IntentService {

    private static final String TAG = "ScheduleService: ";
    public static final int NOTIFICATION_ID = 2;

    public ScheduleService() {
        super(ScheduleService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        assert intent != null;
        String action = intent.getAction();
        Bundle bundle = intent.getBundleExtra("callDetailsBundle");
        CallDetails callDetails = (CallDetails) bundle.getSerializable(CallConstants.CALL_DETAILS_EXTRA);

        assert action != null;
        if (action.equals("scheduleNotification")) {
            sendNotificationAfterCall(this, callDetails);
        }
    }


    public void sendNotificationAfterCall(Context context, CallDetails callDetails) {
        Intent rescheduleActivityIntent = new Intent();
        rescheduleActivityIntent.setClass(context, RescheduleActivity.class);
        rescheduleActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        rescheduleActivityIntent.putExtra(CallConstants.CALL_DETAILS_EXTRA, callDetails);

        Intent snoozeIntent = new Intent(context, NotificationActionService.class).setAction("snoozeReminder");
        snoozeIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        snoozeIntent.putExtra(CallConstants.CALL_DETAILS_EXTRA, callDetails);
        snoozeIntent.putExtra(CallConstants.NOTIFICATION_ID, NOTIFICATION_ID);

        PendingIntent reschedulePendingIntent = PendingIntent.getActivity(context, 0, rescheduleActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent cancelPendingIntent = PendingIntent.getService(context, 0, snoozeIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder notificationBuilder = new Notification.Builder(this);

        String contentText = "Schedule a call for " + (callDetails.getContactName().isEmpty() ? PhoneNumberUtils.formatNumber(callDetails.getPhoneNumber()) : callDetails.getContactName());

        notificationBuilder.setAutoCancel(false)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_logo)
                .setContentTitle("Add a reminder")
                .setContentText(contentText)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT)
                .setContentIntent(reschedulePendingIntent)
                .setContentInfo("Info");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("pref_alert_sound", true)) {
            notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
        if (prefs.getBoolean("pref_alert_vibrate", true)) {
            notificationBuilder.setVibrate(new long[]{1000, 1000});
        } else {
            notificationBuilder.setVibrate(null);
        }

        notificationBuilder.addAction(0, "Snooze", cancelPendingIntent);
        notificationBuilder.addAction(0, "Schedule", reschedulePendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}
