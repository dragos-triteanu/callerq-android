package com.callrq.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.callrq.R;
import com.callrq.models.Reminder;
import com.callrq.utils.CallConstants;

public class ReminderService extends IntentService {

    // constants

    private static final String TAG = "ReminderService: ";
    public static final String REMINDER = "reminderDetails";
    public static final int NOTIFICATION_ID = 3;

    public ReminderService() {
        super("ReminderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        assert intent != null;
        String action = intent.getAction();
        Bundle extras = intent.getBundleExtra("reminderBundle");
        Reminder reminder = extras.getParcelable(REMINDER);
        Uri eventUri = intent.getParcelableExtra("eventUri");
        assert action != null;
        if (action.equals("reminderNotification")) {
            showReminderNotification(this, reminder);
            if (eventUri != null) {
                getContentResolver().delete(eventUri, null, null);
            }
        }
    }

    private void showReminderNotification(Context context, Reminder reminder) {

        Intent callIntent = new Intent(context, NotificationActionService.class).setAction("callContact");
        callIntent.putExtra(REMINDER, reminder);
        callIntent.putExtra(CallConstants.NOTIFICATION_ID, NOTIFICATION_ID);

        Intent snoozeIntent = new Intent(context, NotificationActionService.class).setAction("snoozeCall");
        snoozeIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        snoozeIntent.putExtra(REMINDER, reminder);
        snoozeIntent.putExtra(CallConstants.NOTIFICATION_ID, NOTIFICATION_ID);

        PendingIntent callPendingIntent = PendingIntent.getService(context, 0, callIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent snoozePendingIntent = PendingIntent.getService(context, 0, snoozeIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder notificationBuilder = new Notification.Builder(context);

        notificationBuilder.setAutoCancel(false)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_logo)
                .setContentText(reminder.getMemoText().isEmpty() ? "No additional notes" : reminder.getMemoText())
                .setContentIntent(callPendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT);

        String reminderTitle;

        if (reminder.isMeeting()) {
            reminderTitle = getString(R.string.calendar_event_title_meeting) + " " + reminder.getContactName();
        } else {
            reminderTitle = getString(R.string.calendar_event_title_call) + " " + reminder.getContactName();
        }

        notificationBuilder.setContentTitle(reminderTitle);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("pref_alert_sound", true)) {
            notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
        if (prefs.getBoolean("pref_alert_vibrate", true)) {
            notificationBuilder.setVibrate(new long[]{1000, 1000, 1000});
        }

        notificationBuilder.addAction(0, "Snooze", snoozePendingIntent);
        notificationBuilder.addAction(0, "Call now", callPendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}
