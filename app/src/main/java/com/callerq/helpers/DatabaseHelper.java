package com.callerq.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

import com.callerq.services.DatabaseService;
import com.callerq.models.Reminder;

public class DatabaseHelper {

    public static final String TAG = "DatabaseHelper:";

    private static DatabaseHelper instance;

    private static List<DatabaseListener> listeners = new ArrayList<DatabaseListener>();

    // class for event listeners for DatabaseHelper events
    public static interface DatabaseListener {

        public void savedReminder(String requestId);

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

    public String saveReminder(Context context, Reminder reminder) {
        String requestId = UUID.randomUUID().toString();

        registerReciever(context, DatabaseService.ACTION_SAVED_REMINDER);

        Intent msgIntent = new Intent(context, DatabaseService.class);
        msgIntent.setAction(DatabaseService.ACTION_SAVE_REMINDER);
        msgIntent.putExtra(DatabaseService.PARAM_REMINDER, reminder);
        msgIntent.setData(Uri.fromParts(DatabaseService.URI_SCHEME, requestId,
                null));
        context.startService(msgIntent);
        return requestId;
    }

    public class DatabaseServiceReciever extends BroadcastReceiver {

        private Context activityContext;

        public DatabaseServiceReciever(Context context) {
            activityContext = context;
        }

        @Override
        public void onReceive(Context recieverContext, Intent intent) {
            String requestId = intent.getData().getSchemeSpecificPart();
            Log.d("DatabaseServiceReciever",
                    "Recieved event from database service: "
                            + intent.toString());
            if (intent.getAction()
                    .equals(DatabaseService.ACTION_SAVED_REMINDER)) {
                for (DatabaseListener listener : listeners) {
                    listener.savedReminder(requestId);
                }
            }

            // if the activity context still exists, unregister the receiver
            try {
                activityContext.unregisterReceiver(this);
            } catch (Exception e) {
                ;
            }
        }

    }

    private void registerReciever(Context context, String action) {
        Context appContext = context.getApplicationContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction(action);
        filter.addDataScheme(DatabaseService.URI_SCHEME);
        DatabaseServiceReciever reciever = new DatabaseServiceReciever(
                appContext);
        appContext.registerReceiver(reciever, filter);
    }

}
