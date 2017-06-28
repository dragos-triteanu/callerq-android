package com.callerq.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import com.callerq.R;


/**
 * Due to an android bug: http://stackoverflow.com/questions/17911883/cannot-get-the-notificationlistenerservice-class-to-work
 * This class' name should be changed before each debug use, because if not, onReceive will never get called.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CalendarNotificationListenerService extends NotificationListenerService {

    private String TAG = this.getClass().getSimpleName();
    private CalendarNotificationBroadcastReceiver receiver;

    @Override
    public void onCreate() {
        Log.i(TAG,"********** Service is created");
        super.onCreate();
        receiver = new CalendarNotificationBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.EVENT_REMINDER");
        intentFilter.addDataScheme("content");
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"**********  Service is destroyed");
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public IBinder onBind(Intent mIntent) {
        IBinder mIBinder = super.onBind(mIntent);
        Log.i(TAG, "**********  onBind");
        return mIBinder;
    }

    @Override
    public boolean onUnbind(Intent mIntent) {
        boolean mOnUnbind = super.onUnbind(mIntent);
        Log.i(TAG, "**********  onUnbind");
        return mOnUnbind;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG,"**********  onNotificationPosted");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
        Intent i = new  Intent("com.enginizer.NOTIFICATION_LISTENER_EXAMPLE");
        i.putExtra("notification_event","onNotificationPosted :" + sbn.getPackageName() + "\n");
        sendBroadcast(i);
        if(sbn.getKey().contains("calendar")){
            CalendarNotificationListenerService.this.cancelNotification(sbn.getKey());

            Intent snoozeIntent = new Intent(getApplicationContext(), NotificationActionService.class).setAction("snooze");

            PendingIntent cancelPendingIntent = PendingIntent.getService(getApplicationContext(), 0, snoozeIntent, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());

            notificationBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.callerq_icon)
                    .setTicker("Hearty365")
                    .setContentTitle("Add a reminder")
                    .setContentText("For your callDetails to: " + "x")
                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT)
                    .setContentInfo("Info");

            notificationBuilder.addAction(new NotificationCompat.Action(0,"Snooze",cancelPendingIntent));

            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, notificationBuilder.build());
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG,"********** onNotificationRemoved");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText +"\t" + sbn.getPackageName());
        Intent i = new  Intent("com.enginizer.NOTIFICATION_LISTENER_EXAMPLE");
        i.putExtra("notification_event","onNotificationRemoved :" + sbn.getPackageName() + "\n");

        sendBroadcast(i);
    }

//

    class CalendarNotificationBroadcastReceiver extends BroadcastReceiver{

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "********** Notification received");
            String intentAction = intent.getAction();
            Log.i(TAG, "********** " + intentAction);

        }
    }

    @Override
    public void onListenerConnected() {
        Log.i(TAG,"********** Listener connected");
        super.onListenerConnected();
    }
}
