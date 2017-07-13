package com.callerq.services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import com.callerq.CallerqApplication;
import com.callerq.activities.RescheduleActivity;
import com.callerq.models.CallDetails;
import com.callerq.models.Reminder;
import com.callerq.utils.CallConstants;
import com.callerq.utils.RequestCodes;

public class NotificationActionService extends IntentService {
    private static final String TAG = "NotifActionService: ";

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
        Reminder reminder = intent.getParcelableExtra(ReminderService.REMINDER);
        int intExtra = intent.getIntExtra(CallConstants.NOTIFICATION_ID, 0);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        switch (action) {
            case "snoozeReminder":
                snoozeReminder(callDetails);
                notificationManager.cancel(intExtra);
                break;
            case "callContact":
                onCall(reminder);
                notificationManager.cancel(intExtra);
                break;
            case "snoozeCall":
                snoozeCall(reminder);
                notificationManager.cancel(intExtra);
        }
    }

    private void snoozeReminder(CallDetails callDetails) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int snoozeReminderDuration = prefs.getInt("pref_snooze_duration", 5);

        // TODO: check why the pending intent doesn't snooze for the right amount of time
        // TODO: change to 60000 after that
        int snoozeInMillies = snoozeReminderDuration * 1000;

        Intent scheduleIntent = new Intent(this, ScheduleService.class).setAction("scheduleNotification");

        Bundle bundle = new Bundle();
        bundle.putSerializable(CallConstants.CALL_DETAILS_EXTRA, callDetails);

        scheduleIntent.putExtra("callDetailsBundle", bundle);

        PendingIntent sender = PendingIntent.getService(this, 0, scheduleIntent, PendingIntent.FLAG_ONE_SHOT);

        // Get the AlarmManager service
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, snoozeInMillies, sender);
    }

    private void onCall(Reminder reminder) {
        try {
            // perform the call
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.fromParts("tel", reminder.getContactPhones().get(0), null));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(new RescheduleActivity(),
                        new String[]{Manifest.permission.CALL_PHONE},
                        RequestCodes.MY_PERMISSIONS_REQUEST_MAKE_PHONE_CALL);
                return;
            }
            startActivity(callIntent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Call failed", e);
        }
    }

    private void snoozeCall(Reminder reminder) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int snoozeCallDuration = prefs.getInt("pref_snooze_duration", 5);

        int snoozeInMillies = snoozeCallDuration * 1000;

        Intent reminderIntent = new Intent(this, ReminderService.class).setAction("reminderNotification");

        Bundle bundle = new Bundle();
        bundle.putParcelable(ReminderService.REMINDER, reminder);

        reminderIntent.putExtra("reminderBundle", bundle);

        PendingIntent sender = PendingIntent.getService(this, 0, reminderIntent, PendingIntent.FLAG_ONE_SHOT);

        // Get the AlarmManager service
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, snoozeInMillies, sender);
    }
}


