package com.callerq.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import com.callerq.models.Reminder;
import com.callerq.services.DatabaseService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseHelper {

    public static final String TAG = "DatabaseHelper:";

    private static DatabaseHelper instance;

    private static List<DatabaseListener> listeners = new ArrayList<>();

    private Context activityContext;

    // class for event listeners for DatabaseHelper events
    public static interface DatabaseListener {

        public void savedReminder(String requestId);
        public void gotReminders(ArrayList<Reminder> reminders);

    }

    protected DatabaseHelper() {
    }

    public static synchronized DatabaseHelper getInstance() {
        if (instance == null)
            instance = new DatabaseHelper();
        return instance;
    }

    public synchronized void addListener(DatabaseListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(DatabaseListener listener) {
        listeners.remove(listener);
    }

    public String getReminders(Context context) {
        activityContext = context;

        String requestId = UUID.randomUUID().toString();

        registerReceiver(context, DatabaseService.ACTION_GOT_REMINDERS);

        Intent remindersIntent = new Intent(context, DatabaseService.class);
        remindersIntent.setAction(DatabaseService.ACTION_GET_REMINDERS);
        remindersIntent.setData(Uri.fromParts(DatabaseService.URI_SCHEME, requestId, null));
        context.startService(remindersIntent);

        // TODO: Try to return the reminders to the RemindersFragment
        return requestId;
    }

    public String saveReminder(Context context, Reminder reminder) {
        activityContext = context;

        String requestId = UUID.randomUUID().toString();

        registerReceiver(context, DatabaseService.ACTION_SAVED_REMINDER);

        Intent msgIntent = new Intent(context, DatabaseService.class);
        msgIntent.setAction(DatabaseService.ACTION_SAVE_REMINDER);
        msgIntent.putExtra(DatabaseService.PARAM_REMINDER, reminder);
        msgIntent.setData(Uri.fromParts(DatabaseService.URI_SCHEME, requestId, null));
        context.startService(msgIntent);
        return requestId;
    }

    public class DatabaseServiceReceiver extends BroadcastReceiver {

        private Context broadcastContext;

        public DatabaseServiceReceiver(Context context) {
            broadcastContext = context;
        }

        @Override
        public void onReceive(Context receiverContext, Intent intent) {
            String requestId = intent.getData().getSchemeSpecificPart();
            Log.d("DatabaseServiceReceiver",
                    "Received event from database service: "
                            + intent.toString());
            if (intent.getAction().equals(DatabaseService.ACTION_SAVED_REMINDER)) {
                for (DatabaseListener listener : listeners) {
                    listener.savedReminder(requestId);
                }
            } else if (intent.getAction().equals(DatabaseService.ACTION_GOT_REMINDERS)) {
                ArrayList<Reminder> reminders = intent.getParcelableArrayListExtra("remindersToDisplay");
                if (activityContext instanceof DatabaseListener) {
                    DatabaseListener mListener = (DatabaseListener) activityContext;
                    mListener.gotReminders(reminders);
                }
            }

            // if the activity context still exists, unregister the receiver
            try {
                broadcastContext.unregisterReceiver(this);
            } catch (Exception e) {
                ;
            }
        }

    }

    private void registerReceiver(Context context, String action) {
        Context appContext = context.getApplicationContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction(action);
        filter.addDataScheme(DatabaseService.URI_SCHEME);
        DatabaseServiceReceiver receiver = new DatabaseServiceReceiver(appContext);
        appContext.registerReceiver(receiver, filter);
    }

}
