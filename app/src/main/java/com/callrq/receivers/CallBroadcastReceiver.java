package com.callrq.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.callrq.helpers.AddressBookHelper;
import com.callrq.helpers.PreferencesHelper;
import com.callrq.models.CallDetails;
import com.callrq.services.ScheduleService;
import com.callrq.utils.CallConstants;

import java.util.Calendar;

public class CallBroadcastReceiver extends BroadcastReceiver implements AddressBookHelper.AddressBookListener {

    private static final String TAG = "CallBroadcastReceiver";

    private static String previousCallState = "";
    private static String contactName;
    private static String phoneNumber;
    private static Calendar callStartedTime;
    private static Calendar callStopTime;
    private static String getContactRequestId;

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        AddressBookHelper.getInstance().addListener(this);

        String intentAction = intent.getAction();

        assert intentAction != null;
        assert intent.getExtras() != null;

        if (intentAction.equals(CallConstants.OUTGOING_CALL_ACTION)) {
            phoneNumber = intent.getExtras().getString(CallConstants.INTENT_PHONE_NUMBER);

            Log.i(TAG, "new outgoing call to number:" + phoneNumber);
            Log.i(TAG, intent.getExtras().toString());
        } else if (intentAction.equals(CallConstants.PHONE_STATE_ACTION)) {
            String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            Log.i(TAG, "calling state changed to :" + phoneState);
            Log.i(TAG, intent.getExtras().toString());

            // phone state changed to idle (after a call)
            if (phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                if (previousCallState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    callStopTime = Calendar.getInstance();

                    // check if the phone number is not on the ignore list
                    if (!PreferencesHelper.isPhoneNumberIgnored(context, phoneNumber)) {
                        AddressBookHelper addressBookHelper = AddressBookHelper.getInstance();
                        getContactRequestId = addressBookHelper.getContact(context, phoneNumber);
                    }
                }
            }
            // phone state changed to ringing (when receiving a call)
            else if (phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                phoneNumber = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            } else if (phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                callStartedTime = Calendar.getInstance();
            }

            previousCallState = phoneState;
        }

    }

    @Override
    public void contactRetrieved(String requestId, AddressBookHelper.Contact contact) {
        if (getContactRequestId.equals(requestId)) {
            if (contact != null) {
                contactName = contact.name;
            } else {
                contactName = "";
            }
            getContactRequestId = "";

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            final boolean isApplicationOn = prefs.getBoolean("pref_application_status", true);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isApplicationOn) {
                        Intent scheduleIntent = new Intent(context, ScheduleService.class).setAction("scheduleNotification");

                        Bundle bundle = new Bundle();
                        bundle.putSerializable(CallConstants.CALL_DETAILS_EXTRA, new CallDetails(contactName, phoneNumber, callStartedTime, callStopTime));

                        scheduleIntent.putExtra("callDetailsBundle", bundle);

                        context.startService(scheduleIntent);
                    }
                }
            }, 500);
        }

    }
}
