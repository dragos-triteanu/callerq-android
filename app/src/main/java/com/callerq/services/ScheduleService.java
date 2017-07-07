package com.callerq.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneNumberUtils;
import com.callerq.R;
import com.callerq.activities.RescheduleActivity;
import com.callerq.models.CallDetails;
import com.callerq.utils.CallConstants;

public class ScheduleService extends IntentService {

    private static final String TAG = "ScheduleService: ";

    public static final int NOTIFICATION_ID = 1;

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

        if (action.equals("scheduleNotification")) {
            sendNotificationAfterCall(this, callDetails);
        }
    }


    public void sendNotificationAfterCall(Context ctx, CallDetails callDetails) {
        Intent rescheduleActivityIntent = new Intent();
        rescheduleActivityIntent.setClass(ctx, RescheduleActivity.class);
        rescheduleActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        rescheduleActivityIntent.putExtra(CallConstants.CALL_DETAILS_EXTRA, callDetails);

        Intent snoozeIntent = new Intent(ctx, NotificationActionService.class).setAction("snooze");
        snoozeIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        snoozeIntent.putExtra(CallConstants.CALL_DETAILS_EXTRA, callDetails);
        snoozeIntent.putExtra(CallConstants.NOTIFICATION_ID, NOTIFICATION_ID);

        PendingIntent reschedulePendingIntent = PendingIntent.getActivity(ctx, 0, rescheduleActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent cancelPendingIntent = PendingIntent.getService(ctx, 0, snoozeIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx);

        String contentText = "Schedule a call for " + (callDetails.getContactName().isEmpty() ? PhoneNumberUtils.formatNumber(callDetails.getPhoneNumber()) : callDetails.getContactName());

        notificationBuilder.setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_logo)
                .setContentTitle("Add a reminder")
                .setContentText(contentText)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT)
                .setContentIntent(reschedulePendingIntent)
                .setContentInfo("Info");

        notificationBuilder.addAction(new NotificationCompat.Action(0, "Snooze", cancelPendingIntent));
        notificationBuilder.addAction(0, "Schedule", reschedulePendingIntent);

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}
