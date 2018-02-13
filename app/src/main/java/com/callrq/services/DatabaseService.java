package com.callrq.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.callrq.helpers.PreferencesHelper;
import com.callrq.helpers.SQLiteHelper;
import com.callrq.models.Reminder;
import com.callrq.utils.NetworkUtilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatabaseService extends IntentService {

    public static final String TAG = "DatabaseService";

    public static final String URI_SCHEME = "callerqdatabase";

    public static final String PARAM_REMINDER = "com.callerq.DatabaseService.reminder";
    public static final String ACTION_SAVE_REMINDER = "com.callerq.DatabaseService.saveReminder";
    public static final String ACTION_ATTEMPT_UPLOAD = "com.callerq.DatabaseService.attemptUpload";
    public static final String ACTION_SAVED_REMINDER = "com.callerq.DatabaseService.savedReminder";
    public static final String ACTION_GET_REMINDERS = "com.callerq.DatabaseService.getReminders";
    public static final String ACTION_GOT_REMINDERS = "com.callerq.DatabaseService.gotReminders";
    private static final long UPLOAD_DEFAULT_RETRY_INTERVAL = AlarmManager.INTERVAL_HALF_HOUR; // 30
    // minutes
    private static final long UPLOAD_MAX_RETRY_INTERVAL = 2 * AlarmManager.INTERVAL_HOUR; // 2
    private SQLiteDatabase database;
    // hours

    public DatabaseService() {
        super("DatabaseService");

    }

    @Override
    public void onCreate() {
        super.onCreate();
        PreferencesHelper.setRetryInterval(this, UPLOAD_DEFAULT_RETRY_INTERVAL);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received new intent to handle; action = " + action);
        switch (action) {
            case ACTION_SAVE_REMINDER: {
                Reminder reminder = intent.getParcelableExtra(PARAM_REMINDER);
                saveReminder(reminder);
                Intent responseIntent = new Intent(ACTION_SAVED_REMINDER);
                responseIntent.setData(intent.getData());
                sendBroadcast(responseIntent);
                break;
            }
            case ACTION_ATTEMPT_UPLOAD:
                // implement exponential backoff
                long currentRetryInterval = PreferencesHelper
                        .getRetryInterval(this);
                if (currentRetryInterval * 2 <= UPLOAD_MAX_RETRY_INTERVAL) {
                    PreferencesHelper.setRetryInterval(this,
                            currentRetryInterval * 2);
                } else {
                    PreferencesHelper.setRetryInterval(this,
                            UPLOAD_MAX_RETRY_INTERVAL);
                }

                uploadReminders();
                break;
            case ACTION_GET_REMINDERS: {
                ArrayList<Reminder> reminders = getRemindersToDisplay();

                Intent responseIntent = new Intent(ACTION_GOT_REMINDERS);
                responseIntent.putParcelableArrayListExtra("remindersToDisplay", reminders);
                responseIntent.setData(intent.getData());
                sendBroadcast(responseIntent);
                break;
            }
        }
    }

    private void saveReminder(Reminder reminder) {
        writeReminderToLocalDatabase(reminder);
        uploadReminders();
    }

    private void writeReminderToLocalDatabase(Reminder reminder) {
        openWritableDatabase();
        database.insert("Reminder", null, reminder.toContentValues());
        Log.d(TAG, "inserting: " + reminder.toContentValues().toString());
        closeDatabase();
    }

    private Reminder getReminderById(long id) {
        openReadableDatabase();
        Reminder result = null;
        String[] selectArgs = {Long.toString(id)};
        Cursor results = database.query(SQLiteHelper.REMINDER_TABLE_NAME, null, "_id = ?",
                selectArgs, null, null, null, "1");
        if (results.moveToFirst()) {
            result = cursorToReminder(results);
        }

        closeDatabase();
        return result;
    }

    private void updateReminder(Reminder reminder) {
        openWritableDatabase();
        database.update("Reminder", reminder.toContentValues(),
                String.format(Locale.ENGLISH, "_id = '%d'", reminder.getId()), null);
        closeDatabase();
    }

    private void deleteReminder(Reminder reminder) {
        openWritableDatabase();
        database.delete("Reminder",
                String.format(Locale.ENGLISH, "_id = '%d'", reminder.getId()), null);
        closeDatabase();
    }

    private ArrayList<Reminder> getRemindersToDisplay() {
        openReadableDatabase();
        ArrayList<Reminder> remindersToDisplay = new ArrayList<>();

        Cursor results = database.query(SQLiteHelper.REMINDER_TABLE_NAME, null, null, null,
                null, null, null);
        results.moveToFirst();
        while (!results.isAfterLast()) {
            Reminder reminder = cursorToReminder(results);
            remindersToDisplay.add(reminder);
            results.moveToNext();
        }
        closeDatabase();
        return remindersToDisplay;
    }

    private List<Reminder> getRemindersToUpload() {
        openReadableDatabase();
        List<Reminder> remindersToUpload = new ArrayList<>();

        Cursor results = database.query(SQLiteHelper.REMINDER_TABLE_NAME, null, "uploaded = 0", null,
                null, null, null);
        results.moveToFirst();
        while (!results.isAfterLast()) {
            Reminder reminder = cursorToReminder(results);
            remindersToUpload.add(reminder);
            results.moveToNext();
        }
        closeDatabase();
        return remindersToUpload;
    }

    private Reminder cursorToReminder(Cursor cursor) {
        Reminder reminder = new Reminder();

        reminder.setId(cursor.getLong(cursor.getColumnIndex(SQLiteHelper.REMINDER_ID)));
        reminder.setCallDuration(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.REMINDER_CALL_DURATION)));
        reminder.setCallStartDatetime(cursor.getLong(cursor.getColumnIndex(SQLiteHelper.REMINDER_CALL_START_TIME)));
        reminder.setCreatedDatetime(cursor.getLong(cursor.getColumnIndex(SQLiteHelper.REMINDER_CREATED_DATETIME)));
        reminder.setMeeting(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.REMINDER_IS_MEETING)) > 0);
        reminder.setMemoText(cursor.getString(cursor.getColumnIndex(SQLiteHelper.REMINDER_MEMO_TEXT)));
        reminder.setScheduleDatetime(cursor.getLong(cursor.getColumnIndex(SQLiteHelper.REMINDER_SCHEDULE_DATETIME)));
        reminder.setContactName(cursor.getString(cursor.getColumnIndex(SQLiteHelper.REMINDER_CONTACT_NAME)));
        reminder.setContactCompany(cursor.getString(cursor.getColumnIndex(SQLiteHelper.REMINDER_CONTACT_COMPANY)));
        reminder.setContactEmail(cursor.getString(cursor.getColumnIndex(SQLiteHelper.REMINDER_CONTACT_EMAIL)));
        ArrayList<String> contactPhones = new ArrayList<>();
        contactPhones.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.REMINDER_CONTACT_PHONE_1)));
        contactPhones.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.REMINDER_CONTACT_PHONE_2)));
        contactPhones.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.REMINDER_CONTACT_PHONE_3)));
        reminder.setContactPhones(contactPhones);
        reminder.setUploaded(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.REMINDER_UPLOADED)) > 0);

        return reminder;
    }

    private void uploadReminders() {
        List<Reminder> remindersToUpload = getRemindersToUpload();

        // if there are reminders in the database that need to be uploaded
        if (!remindersToUpload.isEmpty()) {

            boolean shouldRetry = false;

            // if we have Internet connection
            if (NetworkUtilities.hasDataConnectivity(this)) {
                try {
                    String jsonResult = NetworkUtilities
                            .postReminders(this, remindersToUpload);
                    JSONObject result = new JSONObject(jsonResult);

                    boolean success = result.getBoolean("success");
                    if (success) {
                        // upon successful upload, reset the RetryScheduled flag
                        PreferencesHelper.setRetryScheduled(this, false);

                        // mark all the reminders as uploaded
                        for (Reminder r : remindersToUpload) {
                            r.setUploaded(true);
                            updateReminder(r);
                        }
                    } else {

                        Log.d(TAG, "Failed to upload reminders, reason: "
                                + result.getString("reason"));

                        // mark the successfully uploaded reminders
                        JSONArray failedIds = result
                                .getJSONArray("failedReminders");
                        for (Reminder r : remindersToUpload) {
                            boolean found = false;
                            int i = 0;
                            while (!found && i < failedIds.length()) {
                                Reminder failedReminder = getReminderById(Long
                                        .parseLong(failedIds.getString(i)));
                                if (failedReminder.getId() == r.getId()) {
                                    found = true;
                                }
                                i++;
                            }
                            if (!found) {
                                // r.setUploaded(true);
                                // updateReminder(r);
                                deleteReminder(r);
                            }
                        }

                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error saving reminder. ", e);
                    shouldRetry = true;
                }
            } else {
                Log.d(TAG, "No internet conectivity. Will try again later");
                shouldRetry = true;
            }

            boolean retryScheduled = PreferencesHelper.isRetryScheduled(this);
            if (shouldRetry && !retryScheduled) {

                PreferencesHelper.setRetryScheduled(this, true);
                Intent retryIntent = new Intent(this, DatabaseService.class);
                retryIntent.setAction(ACTION_ATTEMPT_UPLOAD);

                PendingIntent sender = PendingIntent.getService(this, 0,
                        retryIntent, PendingIntent.FLAG_ONE_SHOT);

                // wakeup the service again after a while
                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                long retryInterval = PreferencesHelper.getRetryInterval(this);
                am.set(AlarmManager.RTC, System.currentTimeMillis()
                        + retryInterval, sender);

            }
        }
    }

    private SQLiteDatabase openWritableDatabase() {
        SQLiteHelper helper = new SQLiteHelper(this);
        database = helper.getWritableDatabase();
        return database;
    }

    private SQLiteDatabase openReadableDatabase() {
        SQLiteHelper helper = new SQLiteHelper(this);
        database = helper.getReadableDatabase();
        return database;
    }

    private void closeDatabase() {
        database.close();
    }

}
