package com.callerq.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.callerq.CallerqApplication;
import com.callerq.helpers.PreferencesHelper;
import com.callerq.models.CallDetails;
import com.callerq.services.ScheduleService;
import com.callerq.utils.CallConstants;

import javax.inject.Inject;
import java.util.Calendar;

public class CallBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "CallBroadcastReceiver";

    private static String previousCallState;
    private static String phoneNumber;
    private static Calendar callStartedTime;
    private static Calendar callStopTime;

    @Inject
    ScheduleService schedulingService;

    @Override
    public void onReceive(Context context, Intent intent) {

        CallerqApplication.APP.inject(this);

        String intentAction = intent.getAction();

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
                        // TODO: retrieve contact data based on phone number

                        // launch the re-schedule activity
                        schedulingService.sendNotificationAfterCall(context, new CallDetails(phoneNumber, callStartedTime, callStopTime));
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
}
