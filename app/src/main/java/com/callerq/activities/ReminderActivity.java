package com.callerq.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.callerq.CallerqApplication;
import com.callerq.R;
import com.callerq.models.Reminder;
import com.callerq.utils.RequestCodes;

import java.util.Stack;

public class ReminderActivity extends AppCompatActivity {

	public static final String TAG = "ReminderActivity:";

	// constants
    public static final String REMINDER = "reminderDetails";

	private static final long SNOOZE_INTERVAL_MILLIS = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

	// reminder data
    private Reminder reminder;

	// form fields
	private EditText nameField;
	private EditText memoField;
	private EditText companyField;

	// snooze flag
	// TODO: fix the problem if the user taps home, he loses the reminder
	// private boolean snoozed;
	// private boolean callInitiated;

	// will hold the reminders that pop-up while a pop-up is already displayed
    private Stack<Intent> intentStack = new Stack<>();

	// Sets an ID for the notification
	private int mNotificationId = 1;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reminder);

        nameField = (EditText) findViewById(R.id.nameField);
        memoField = (EditText) findViewById(R.id.memoField);
        companyField = (EditText) findViewById(R.id.companyField);

        handleIntent();
        showNotification();

    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (!intent.equals(getIntent())) {
			// push the old intent on the stack
			intentStack.push(getIntent());
			setIntent(intent);
			handleIntent();
			showNotification();
		}

	}

	public void onSnoozeButtonClick(View v) {
		// snoozed = true;
		snoozeReminder(reminder);
		finishProcessingIntent();
	}

	public void onCallButtonClick(View v) {
		// callInitiated = true;

		try {
			// perform the call
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.fromParts("tel", reminder.getContactPhones().get(0), null));
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(ReminderActivity.this,
						new String[]{Manifest.permission.CALL_PHONE},
						RequestCodes.MY_PERMISSIONS_REQUEST_MAKE_PHONE_CALL);
				return;
			}
			startActivity(callIntent);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "Call failed", e);
		}

		finishProcessingIntent();
	}

	private void snoozeReminder(Reminder reminder) {
		Intent intent = new Intent(this, ReminderActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(ReminderActivity.REMINDER, reminder);

		intent.setData(Uri.fromParts(RescheduleActivity.REMINDER_URI_SCHEME,
				Long.toString(reminder.getId()), ""));

		PendingIntent sender = PendingIntent.getActivity(this, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + SNOOZE_INTERVAL_MILLIS, sender);
	}

	@Override
	protected void onStop() {
		super.onStop();

		// for (Intent intent : intentStack) {
		// reminder = (Reminder) intent.getExtras().get(REMINDER);
		// snoozeReminder(reminder);
		// }
		//
		// // in case the user exits by other means snooze the current reminder
		// too
		// if (!snoozed && !callInitiated) {
		// snoozeReminder(reminder);
		// }

		NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotifyMgr.cancel(mNotificationId);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void finishProcessingIntent() {
		if (intentStack.isEmpty()) {
			finish();
		} else {
			setIntent(intentStack.pop());
			handleIntent();
		}
	}

	private void handleIntent() {

        Bundle extras = getIntent().getBundleExtra("reminderBundle");
        reminder = extras.getParcelable(REMINDER);

		// populate the fields
        assert reminder != null;
        nameField.setText(reminder.getContactName());
		memoField.setText(reminder.getMemoText());
		companyField.setText(reminder.getContactCompany());

		// snoozed = false;
		// callInitiated = false;

	}

	private void showNotification() {
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, getIntent(),
				PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.callerq_logo)
				.setContentTitle("Call " + reminder.getContactName())
				.setContentText("Reminder: " + reminder.getMemoText())
				.setContentIntent(contentIntent).setPriority(NotificationCompat.PRIORITY_HIGH);

		Notification notification = mBuilder.build();
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.ledARGB = 0xffffffff; // color, in this case, white
		notification.ledOnMS = 1000; // light on in milliseconds
		notification.ledOffMS = 4000; // light off in milliseconds
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;

		// Gets an instance of the NotificationManager service
		NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Builds the notification and issues it.
		mNotifyMgr.notify(mNotificationId, notification);
	}

}
