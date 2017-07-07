package com.callerq.services;

import android.Manifest;
import android.app.*;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import com.callerq.R;
import com.callerq.activities.RescheduleActivity;
import com.callerq.models.Reminder;
import com.callerq.utils.CallConstants;
import com.callerq.utils.RequestCodes;

public class ReminderService extends IntentService {

    // constants

    private static final String TAG = "ReminderService: ";
    public static final int NOTIFICATION_ID = 2;
    public static final String REMINDER = "reminderDetails";

    private static final long SNOOZE_INTERVAL_MILLIS = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

    public ReminderService() {
        super("ReminderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        assert intent != null;
        String action = intent.getAction();
        Bundle extras = intent.getBundleExtra("reminderBundle");
        Reminder reminder = extras.getParcelable(REMINDER);
        if (action.equals("reminderNotification")) {
            showReminderNotification(this, reminder);
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

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);

        notificationBuilder.setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_logo)
                .setContentTitle("Call " + reminder.getContactName())
                .setContentText(reminder.getMemoText().isEmpty() ? "No additional notes" : reminder.getMemoText())
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT);

        PendingIntent callPendingIntent = PendingIntent.getService(context, 0, callIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent cancelPendingIntent = PendingIntent.getService(context, 0, snoozeIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        notificationBuilder.addAction(new NotificationCompat.Action(0, "Snooze", cancelPendingIntent));
        notificationBuilder.addAction(new NotificationCompat.Action(0, "Call now", callPendingIntent));

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}
