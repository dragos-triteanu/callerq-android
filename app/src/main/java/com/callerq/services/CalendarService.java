package com.callerq.services;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.callerq.models.CallDetails;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

public class CalendarService extends AppCompatActivity {
    public static final String TAG = "CalendarService: ";
    public static final String CONTENT_COM_ANDROID_CALENDAR_CALENDARS = "content://com.android.calendar/calendars";

    public void addEvent(Context context, CallDetails callDetails) {
        Cursor cursor;
        int calIds[] = new int[0];
        String[] projection = new String[]{CalendarContract.Calendars._ID, CalendarContract.Calendars.ACCOUNT_NAME};

        ContentResolver contentResolver = context.getContentResolver();
        cursor = contentResolver.query(Uri.parse(CONTENT_COM_ANDROID_CALENDAR_CALENDARS), projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                final String[] calNames = new String[cursor.getCount()];
                calIds = new int[cursor.getCount()];
                for (int i = 0; i < calNames.length; i++) {
                    calIds[i] = cursor.getInt(0);
                    calNames[i] = cursor.getString(1);
                    cursor.moveToNext();

                }
            }
            cursor.close();
        }

        try {
            Calendar startDate = Calendar.getInstance();
            ContentValues values = new ContentValues();

            int snoozeReminderDuration = 1;

            int snoozeInMillies = snoozeReminderDuration * 60000;
            values.put(CalendarContract.Events.DTSTART, startDate.getTimeInMillis() + snoozeInMillies + 60000);
            values.put(CalendarContract.Events.DTEND, startDate.getTimeInMillis() + snoozeInMillies + 120000);
            values.put(CalendarContract.Events.TITLE, "Call " + callDetails.getPhoneNumber());
            values.put(CalendarContract.Events.DESCRIPTION, "A reminder to call the contact");
            values.put(CalendarContract.Events.CALENDAR_ID, calIds[0]);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Uri insertedEvent = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values);
            long eventID = 0;
            if (insertedEvent != null) {
                eventID = Long.parseLong(insertedEvent.getLastPathSegment());
            }

            setReminder(contentResolver, eventID, snoozeReminderDuration);


        } catch (Exception e) {
            Log.e(TAG, Arrays.toString(e.getStackTrace()));
        }
    }

    // routine to add reminders with the event
    public void setReminder(ContentResolver cr, long eventID, int timeBefore) {
        try {
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Reminders.MINUTES, timeBefore);
            values.put(CalendarContract.Reminders.EVENT_ID, eventID);
            values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Uri uri = cr.insert(CalendarContract.Reminders.CONTENT_URI, values);
            Cursor c = CalendarContract.Reminders.query(cr, eventID,
                    new String[]{CalendarContract.Reminders.MINUTES});
            if (c.moveToFirst()) {
                System.out.println("calendar"
                        + c.getInt(c.getColumnIndex(CalendarContract.Reminders.MINUTES)));
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openCalendarActivity(Context ctx) {
        Calendar beginTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();

        Intent anotherIntent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, "Yoga")
                .putExtra(CalendarContract.Events.DESCRIPTION, "Group class")
                .putExtra(CalendarContract.Events.EVENT_LOCATION, "The gym")
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                .putExtra(Intent.EXTRA_EMAIL, "rowan@example.com,trevor@example.com")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(anotherIntent);
    }

}
