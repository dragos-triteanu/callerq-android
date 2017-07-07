package com.callerq.services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import com.callerq.CallerqApplication;
import com.callerq.activities.RescheduleActivity;
import com.callerq.models.CallDetails;
import com.callerq.utils.CallConstants;
import com.callerq.utils.RequestCodes;

import javax.inject.Inject;
import java.util.List;

public class NotificationActionService extends IntentService {
    private static final String TAG = "NotificationActionService: ";

    @Inject
    CalendarService calendarService;

    public NotificationActionService() {
        super(NotificationActionService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CallerqApplication.APP.inject(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        assert intent != null;
        String action = intent.getAction();
        CallDetails callDetails = (CallDetails) intent.getSerializableExtra(CallConstants.CALL_DETAILS_EXTRA);
        int intExtra = intent.getIntExtra(CallConstants.NOTIFICATION_ID, 0);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        switch (action) {
            case "snooze":
//                calendarService.addEvent(this, callDetails);
                snoozeReminder(callDetails);
                notificationManager.cancel(intExtra);
                break;
            case "schedule":

                break;
        }
    }

    private void snoozeReminder(CallDetails callDetails) {

        int snoozeReminderDuration = 1;

        int snoozeInMillies = snoozeReminderDuration * 5000;

        Intent scheduleIntent = new Intent(this, ScheduleService.class).setAction("scheduleNotification");

        Bundle bundle = new Bundle();
        bundle.putSerializable(CallConstants.CALL_DETAILS_EXTRA, callDetails);

        scheduleIntent.putExtra("callDetailsBundle", bundle);

        PendingIntent sender = PendingIntent.getService(this, 0, scheduleIntent, PendingIntent.FLAG_ONE_SHOT);

        // Get the AlarmManager service
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, snoozeInMillies, sender);
    }
}


