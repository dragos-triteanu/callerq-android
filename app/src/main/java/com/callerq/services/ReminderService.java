package com.callerq.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import com.callerq.R;
import com.callerq.models.Reminder;

public class ReminderService extends IntentService {

    public static final int NOTIFICATION_ID = 2;
    private Reminder reminder;

    public ReminderService() {
        super("ReminderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//        if (intent != null) {
//
//        }
    }

    private void onShowNotification() {

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.fromParts("tel", reminder.getContactPhones().get(0), null));

        Intent snoozeIntent = new Intent();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

        notificationBuilder.setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.callerq_icon)
                .setContentTitle("Call " + reminder.getContactName())
                .setContentText(reminder.getMemoText().isEmpty() ? "No additional notes" : reminder.getMemoText())
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT);

        PendingIntent callPendingIntent = PendingIntent.getActivity(this, 0, callIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent cancelPendingIntent = PendingIntent.getService(this, 0, snoozeIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        notificationBuilder.addAction(0, "Call now", callPendingIntent);
        notificationBuilder.addAction(new NotificationCompat.Action(0, "Snooze", cancelPendingIntent));

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}
