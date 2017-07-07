package com.callerq.activities;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.callerq.R;
import com.callerq.helpers.AddressBookHelper;
import com.callerq.helpers.AddressBookHelper.AddressBookListener;
import com.callerq.helpers.AddressBookHelper.Contact;
import com.callerq.helpers.DatabaseHelper;
import com.callerq.helpers.PreferencesHelper;
import com.callerq.models.CallDetails;
import com.callerq.models.Reminder;
import com.callerq.services.ScheduleService;
import com.callerq.utils.CallConstants;
import com.callerq.utils.RequestCodes;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;

public class RescheduleActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, AddressBookListener {

    public static final String MEMO_PREFS_NAME = "memoPreferences";
    public static final String REMINDER_URI_SCHEME = "callerq";

    private static final int CALENDAR_EVENT_CALL_LENGTH_MINUTES = 15;
    private static final int CALENDAR_EVENT_MEETING_LENGTH_MINUTES = 60;

    // start and end time for the call (retrieved from the broadcast receiver)
    private static Calendar callStartTime;
    private static Calendar callStopTime;

    // the client's phone number
    private static String phoneNumber;

    @BindView(R.id.nameInput)
    EditText nameInput;

    @BindView(R.id.companyInput)
    EditText companyInput;

    @BindView(R.id.notesInput)
    EditText notesInput;

    @BindView(R.id.meetingCheckbox)
    CheckBox meetingCheckbox;

    @BindView(R.id.buttonSubmit)
    ImageButton buttonSubmit;

    @BindView(R.id.buttonSubmitDisabled)
    ImageButton buttonSubmitDisabled;

    @BindView(R.id.buttonClose)
    ImageButton buttonClose;

    @BindView(R.id.dateInput)
    BetterSpinner dateInput;

    @BindView(R.id.timeInput)
    BetterSpinner timeInput;

    @BindView(R.id.quickContactBadge)
    QuickContactBadge quickContactBadge;

    @Nullable
    @BindView(R.id.savedTitle)
    TextView savedTitle;

    @Nullable
    @BindView(R.id.savedDateAndTime)
    TextView savedDateAndTime;

    @Inject
    ScheduleService scheduleService;

    private ViewGroup mContainerView;
    private ViewGroup formView;

    private String[] dateArray = {"Today", "Tomorrow", "Pick a date..."};
    private String[] timeArray = {"Morning", "Afternoon", "Evening", "Set the time..."};
    private ArrayAdapter<String> dateArrayAdapter;

    private Calendar setCalendar;

    // the client's contact object
    private Contact contact;
    private String getContactRequestId;

    private String eventTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reschedule);
        setFinishOnTouchOutside(false);
        mContainerView = (ViewGroup) findViewById(R.id.rescheduleContainer);

        formView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.reschedule_form, mContainerView, false);
        mContainerView.addView(formView);
        ButterKnife.bind(this);

        setCalendar = Calendar.getInstance();
        initiateDateInput();
        initiateTimeInput();

        nameInput.addTextChangedListener(new MyTextWatcher());
        dateInput.addTextChangedListener(new MyTextWatcher());
        timeInput.addTextChangedListener(new MyTextWatcher());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RescheduleActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    RequestCodes.MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            AddressBookHelper.getInstance().addListener(this);
            handleIntent();
        }
    }

    public void onSubmit(View view) {

        validateDateAndTime();

        if (!validateName() || !validateTime() || !validateDate()) {
            return;
        }

        if (contact == null) {
            // perform validation
            contact = new Contact();
            contact.name = nameInput.getText().toString();
            contact.company = companyInput.getText().toString();
            ArrayList<String> phonesList = new ArrayList<>();
            phonesList.add(phoneNumber);
            contact.phoneNumbers = phonesList;
            contact.email = "";

            AddressBookHelper.getInstance().addContact(this, contact);
        }

        Reminder reminder = new Reminder();
        reminder.setMemoText(notesInput.getText().toString());
        reminder.setMeeting(meetingCheckbox.isChecked());
        reminder.setCreatedDatetime(System.currentTimeMillis());
        reminder.setScheduleDatetime(setCalendar.getTimeInMillis());
        reminder.setCallStartDatetime(callStartTime.getTimeInMillis());
        reminder.setCallDuration((int) ((callStopTime.getTimeInMillis() - callStartTime
                .getTimeInMillis()) / 1000));
        reminder.setContactName(contact.name);
        reminder.setContactCompany(contact.company);
        reminder.setContactPhones(contact.phoneNumbers);
        reminder.setContactEmail(contact.email);

        setCalendarEvent(reminder);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        setAlarm(reminder);

        // save the reminder to the local database and attempt upload
        DatabaseHelper.getInstance().saveReminder(this, reminder);

        // save the last memo in order to display it next time
        SharedPreferences memos = getSharedPreferences(MEMO_PREFS_NAME, 0);
        SharedPreferences.Editor editor = memos.edit();
        editor.putString(phoneNumber + contact.name, reminder.getMemoText()).apply();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(ScheduleService.NOTIFICATION_ID);

        ViewGroup savedView = (ViewGroup) LayoutInflater.from(RescheduleActivity.this).inflate(
                R.layout.reschedule_saved, mContainerView, false);
        formView.setVisibility(View.GONE);
        mContainerView.addView(savedView);

        setFinishOnTouchOutside(true);
        ButterKnife.bind(RescheduleActivity.this);

        assert savedTitle != null;
        savedTitle.setText(eventTitle);
        assert savedDateAndTime != null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d/M/yy hh:mm", Locale.US);
        savedDateAndTime.setText(simpleDateFormat.format(setCalendar.getTime()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCodes.MY_PERMISSIONS_REQUEST_READ_CONTACTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AddressBookHelper.getInstance().addListener(this);
                    handleIntent();
                } else {
                    finish();
                }
                break;
            case RequestCodes.MY_PERMISSIONS_REQUEST_WRITE_CALENDAR:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onSubmit(null);
                } else {
                    finish();
                }
                break;
        }
    }

    public void onClose(View view) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(ScheduleService.NOTIFICATION_ID);

        ViewGroup canceledView = (ViewGroup) LayoutInflater.from(RescheduleActivity.this).inflate(
                R.layout.reschedule_canceled, mContainerView, false);
        formView.setVisibility(View.GONE);
        mContainerView.addView(canceledView);

        setFinishOnTouchOutside(true);
        ButterKnife.bind(RescheduleActivity.this);
    }

    public void onIgnoreNumber(View view) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle(R.string.ignore_dialog_title);

        String numberToIgnore = contact != null ? contact.name : PhoneNumberUtils.formatNumber(phoneNumber);

        alertDialog.setMessage("We will not show you any more notifications for " + numberToIgnore + "\n\nAre you sure?");
        alertDialog.setPositiveButton(R.string.ignore_dialog_positive,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PreferencesHelper.ignorePhoneNumber(RescheduleActivity.this, phoneNumber);
                        Toast.makeText(RescheduleActivity.this, "Number added to ignore list", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 500);
                    }
                })
                .setNegativeButton(R.string.ignore_dialog_negative,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

        alertDialog.show();
    }

    public void onViewReminders(View view) {
        if (MainActivity.isRunning) {
            MainActivity.displayReminders = true;
        } else {
            Intent intent = new Intent(this, StartActivity.class);
            intent.putExtra("displayReminders", true);
            startActivity(intent);
        }
        finish();
    }

    private void handleIntent() {

        // get the phone number out of the intent
        CallDetails callDetails = (CallDetails) getIntent().getSerializableExtra(CallConstants.CALL_DETAILS_EXTRA);

        phoneNumber = callDetails.getPhoneNumber();

        // get the call start time and call end time
        callStartTime = callDetails.getCallStartedTime();
        callStopTime = callDetails.getCallStopTime();

        // set the contact badge photo
        quickContactBadge.assignContactFromPhone(phoneNumber, true);
        quickContactBadge.setImageResource(R.drawable.account_box_icon);
        quickContactBadge.setMode(ContactsContract.QuickContact.MODE_SMALL);

        // retrieve contact data based on phone number
        AddressBookHelper addressBookHelper = AddressBookHelper.getInstance();
        getContactRequestId = addressBookHelper.getContact(this, phoneNumber);
    }

    @Override
    public void contactRetrieved(String requestId, Contact contact) {
        if (getContactRequestId.equals(requestId)) {
            this.contact = contact;
            if (contact != null) {
                nameInput.setText(contact.name);
                if (!contact.company.isEmpty()) {
                    companyInput.setText(contact.company);
                } else {
                    companyInput.setText("N/A");
                }

                nameInput.setEnabled(false);
                companyInput.setEnabled(false);

                // set the old memo
                SharedPreferences memos = getSharedPreferences(MEMO_PREFS_NAME, 0);
                String oldMemo = memos.getString(phoneNumber + contact.name, null);
                if (oldMemo != null) {
                    notesInput.setText(oldMemo);
                }
                notesInput.setSelectAllOnFocus(true);

                showSubmitButton();
            }

        }

    }

    private void setAlarm(Reminder reminder) {

        Intent intent = new Intent(RescheduleActivity.this, ReminderActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelable(ReminderActivity.REMINDER, reminder);

        intent.putExtra("reminderBundle", bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // get the main contact phone
        List<String> contactPhones = reminder.getContactPhones();
        if (contactPhones == null || contactPhones.size() < 1)
            return;
        String mainPhoneNumber = contactPhones.get(0);

        // set a unique element to identify the pending intent
        // in this case, the contact's phone number
        intent.setData(Uri.fromParts(REMINDER_URI_SCHEME, mainPhoneNumber, ""));

        PendingIntent sender = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the AlarmManager service
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, reminder.getScheduleDatetime(), sender);
    }

    private void setCalendarEvent(Reminder reminder) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RescheduleActivity.this,
                    new String[]{Manifest.permission.WRITE_CALENDAR},
                    RequestCodes.MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
            return;
        }

        Account googleAccount = getFirstGoogleAccount();
        if (googleAccount == null) {
            Toast.makeText(this, "There is no Google account set up on the device.", Toast.LENGTH_LONG).show();
            finish();
        }

        Long calendarId = getFirstCalendarId(googleAccount);
        if (calendarId == null) {
            Toast.makeText(this, "No calendar was found on the Google account.", Toast.LENGTH_LONG).show();
            finish();
        }

        // calculate the start end end time
        long startMillis = reminder.getScheduleDatetime();
        Calendar endTime = Calendar.getInstance();
        endTime.setTimeInMillis(reminder.getScheduleDatetime());
        if (reminder.isMeeting()) {
            endTime.add(Calendar.MINUTE, CALENDAR_EVENT_MEETING_LENGTH_MINUTES);
        } else {
            endTime.add(Calendar.MINUTE, CALENDAR_EVENT_CALL_LENGTH_MINUTES);
        }
        long endMillis = endTime.getTimeInMillis();

        // set up the event title
        if (reminder.isMeeting()) {
            eventTitle = getString(R.string.calendar_event_title_meeting);
        } else {
            eventTitle = getString(R.string.calendar_event_title_call);
        }
        eventTitle += " " + reminder.getContactName();

        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, eventTitle);
        values.put(CalendarContract.Events.DESCRIPTION, reminder.getMemoText());
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        cr.insert(CalendarContract.Events.CONTENT_URI, values);

    }

    private Account getFirstGoogleAccount() {

        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);

        if (accounts.length > 0) {
            return accounts[0];
        }

        return null;
    }

    private Long getFirstCalendarId(Account account) {

        // try to find the main calendar
        Cursor cur;
        ContentResolver cr = getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND (" + CalendarContract.Calendars.ACCOUNT_TYPE
                + " = ?) AND (" + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[]{account.name, account.type, account.name};

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RescheduleActivity.this,
                    new String[]{Manifest.permission.WRITE_CALENDAR},
                    RequestCodes.MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
            return null;
        } else {
            cur = cr.query(uri, new String[]{CalendarContract.Calendars._ID}, selection, selectionArgs, null);

            assert cur != null;
            if (!cur.moveToFirst()) {
                return null;
            }

            long calendarId = cur.getLong(cur.getColumnIndex(CalendarContract.Calendars._ID));
            cur.close();

            return calendarId;
        }
    }

    private void initiateDateInput() {
        dateArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, dateArray);

        dateInput.setAdapter(dateArrayAdapter);
        dateInput.setText(dateArray[1]);
        setCalendar.set(Calendar.DAY_OF_YEAR, Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
        setCalendar.add(Calendar.DAY_OF_YEAR, 1);

        dateInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemClicked, long l) {
                Calendar currentTime = Calendar.getInstance();

                if (itemClicked != 2) {
                    setCalendar.set(Calendar.YEAR, currentTime.get(Calendar.YEAR));
                    setCalendar.set(Calendar.DAY_OF_YEAR, currentTime.get(Calendar.DAY_OF_YEAR));
                }

                switch (itemClicked) {
                    case 0:
                        break;
                    case 1:
                        setCalendar.add(Calendar.DAY_OF_YEAR, 1);
                        break;
                    case 2:
                        DatePickerDialog datePickerDialog = new DatePickerDialog(RescheduleActivity.this, RescheduleActivity.this,
                                setCalendar.get(Calendar.YEAR), setCalendar.get(Calendar.MONTH), setCalendar.get(Calendar.DAY_OF_MONTH));
                        datePickerDialog.show();
                        dateInput.setTextColor(Color.BLACK);
                        break;
                }

                if (itemClicked != 2) {
                    validateDateAndTime();
                    dateInput.requestFocus();
                }
            }
        });
    }

    private void initiateTimeInput() {
        final ArrayAdapter<String> timeArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, timeArray);

        timeInput.setAdapter(timeArrayAdapter);
        timeInput.setText(timeArray[0]);
        setCalendar.set(Calendar.HOUR_OF_DAY, 8);
        setCalendar.set(Calendar.MINUTE, 0);

        timeInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemClicked, long l) {

                setCalendar.set(Calendar.SECOND, 0);
                if (itemClicked != 3) {
                    setCalendar.set(Calendar.MINUTE, 0);
                }

                switch (itemClicked) {
                    case 0:
                        setCalendar.set(Calendar.HOUR_OF_DAY, 8);
                        break;
                    case 1:
                        setCalendar.set(Calendar.HOUR_OF_DAY, 14);
                        break;
                    case 2:
                        setCalendar.set(Calendar.HOUR_OF_DAY, 19);
                        break;
                    case 3:
                        TimePickerDialog timePickerDialog = new TimePickerDialog(RescheduleActivity.this, RescheduleActivity.this,
                                setCalendar.get(Calendar.HOUR_OF_DAY), setCalendar.get(Calendar.MINUTE), true);
                        timePickerDialog.show();
                        timeInput.setTextColor(Color.BLACK);
                        break;
                }

                if (itemClicked != 3) {
                    validateDateAndTime();
                    timeInput.requestFocus();
                }
            }
        });
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        setCalendar.set(i, i1, i2);

        if (dateInput != null) {
            Calendar currentCalendar = Calendar.getInstance();
            int today = currentCalendar.get(Calendar.DAY_OF_YEAR);

            currentCalendar.add(Calendar.DAY_OF_YEAR, 1);
            int tomorrow = currentCalendar.get(Calendar.DAY_OF_YEAR);

            if (setCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)) {
                if (setCalendar.get(Calendar.DAY_OF_YEAR) == today) {
                    dateInput.setText(dateArrayAdapter.getItem(0));
                } else if (setCalendar.get(Calendar.DAY_OF_YEAR) == tomorrow) {
                    dateInput.setText(dateArrayAdapter.getItem(1));
                } else {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, MMMM dd", Locale.US);
                    dateInput.setText(simpleDateFormat.format(setCalendar.getTime()));
                }
            } else {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, MMMM dd, yyyy", Locale.US);
                dateInput.setText(simpleDateFormat.format(setCalendar.getTime()));
            }

            validateDateAndTime();
            dateInput.requestFocus();

        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        setCalendar.set(Calendar.HOUR_OF_DAY, i);
        setCalendar.set(Calendar.MINUTE, i1);

        if (timeInput != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.US);
            timeInput.setText(simpleDateFormat.format(setCalendar.getTime()));

            validateDateAndTime();
            timeInput.requestFocus();
        }
    }

    private boolean validateName() {
        String nameText = nameInput.getText().toString();
        if (nameText.isEmpty()) {
            disableSubmitButton();
            return false;
        }
        return true;
    }

    private boolean validateDate() {
        String dateText = dateInput.getText().toString();

        if (dateText.equals(dateArray[2]) || dateText.contains("is in the past")) {
            disableSubmitButton();
            return false;
        }
        return true;
    }

    private boolean validateTime() {
        String timeText = timeInput.getText().toString();
        if (timeText.equals(timeArray[3]) || timeText.contains("is in the past")) {
            disableSubmitButton();
            return false;
        }
        return true;
    }

    private class MyTextWatcher implements TextWatcher {

        private MyTextWatcher() {

        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            showSubmitButton();
            validateName();
            validateDate();
            validateTime();
        }
    }

    private void validateDateAndTime() {
        Calendar currentTime = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        String dateText = dateInput.getText().toString().replace(" is in the past", "");
        String dateError = dateText + " is in the past";
        String timeText = timeInput.getText().toString().replace(" is in the past", "");
        String timeError = timeText + " is in the past";

        if (setCalendar.before(yesterday)) {
            if (!dateText.contains("is in the past") && !dateText.equals(dateArray[2])) {
                dateInput.setText(dateError);
                timeInput.setText(timeText);
                dateInput.setTextColor(Color.RED);
                timeInput.setTextColor(Color.BLACK);
            }
        } else if (setCalendar.before(currentTime)) {
            if (!timeText.contains("is in the past") && !timeText.equals(timeArray[3])) {
                timeInput.setText(timeError);
                dateInput.setText(dateText);
                timeInput.setTextColor(Color.RED);
                dateInput.setTextColor(Color.BLACK);
            }
        } else {
            dateInput.setTextColor(Color.BLACK);
            timeInput.setTextColor(Color.BLACK);
            dateInput.setText(dateText);
            timeInput.setText(timeText);
        }

        mContainerView.requestFocus();
    }

    private void showSubmitButton() {
        buttonSubmit.setVisibility(View.VISIBLE);
        buttonSubmitDisabled.setVisibility(View.INVISIBLE);
    }

    private void disableSubmitButton() {
        buttonSubmitDisabled.setVisibility(View.VISIBLE);
        buttonSubmit.setVisibility(View.INVISIBLE);
        Toast.makeText(scheduleService, "SUBMIT BUTTON DISABLED", Toast.LENGTH_LONG).show();
    }
}
