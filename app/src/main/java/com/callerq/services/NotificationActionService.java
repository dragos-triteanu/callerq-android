package com.callerq.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import com.callerq.CallerqApplication;
import com.callerq.models.CallDetails;
import com.callerq.utils.CallConstants;

import javax.inject.Inject;

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
        if ("snooze".equals(action)) {
            calendarService.addEvent(this, callDetails);
            // TODO: add reminder using alarm manager
            notificationManager.cancel(intExtra);
        }


    }
}


