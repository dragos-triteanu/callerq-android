package com.callrq.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;

public class PreferencesHelper {

    private static final String TAG = "PreferencesHelper: ";
    private static final String PREF_TERMS_ACCEPTED = "termsAccepted";
    private static final String PREFERENCES_LOGIN_TOKEN = "loginToken";
    private static final String PREFERENCES_ANALYTICS_ENABLED = "analyticsEnabled";
    private static final String PREFERENCES_DATABASE_RETRY_SCHEDULED = "databaseRetryScheduled";
    private static final String PREFERENCES_DATABASE_RETRY_INTERVAL = "databaseRetryInterval";
    private static final String IGNORE_LIST_FILE = "ignoreList";

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static SharedPreferences.Editor getSharedPreferencesEditor(Context context) {
        return getSharedPreferences(context).edit();
    }

    public static void setTermsAccepted(Context context, Boolean termsAgreed) {
        getSharedPreferencesEditor(context).putBoolean(PREF_TERMS_ACCEPTED, termsAgreed).commit();
    }

    public static Boolean getTermsAccepted(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_TERMS_ACCEPTED, false);
    }

    public static void setLoginToken(Context context, String loginToken) {
        getSharedPreferencesEditor(context).putString(PREFERENCES_LOGIN_TOKEN, loginToken).commit();
    }

    public static String getLoginToken(Context context) {
        return getSharedPreferences(context).getString(PREFERENCES_LOGIN_TOKEN, null);
    }

    public static boolean isRetryScheduled(Context context) {
        return getSharedPreferences(context).getBoolean(PREFERENCES_DATABASE_RETRY_SCHEDULED, false);
    }

    public static void setRetryScheduled(Context context, boolean retryScheduled) {
        getSharedPreferencesEditor(context).putBoolean(PREFERENCES_DATABASE_RETRY_SCHEDULED, retryScheduled).commit();
    }

    public static long getRetryInterval(Context context) {
        return getSharedPreferences(context).getLong(PREFERENCES_DATABASE_RETRY_INTERVAL, 0);
    }

    public static void setRetryInterval(Context context, long retryInterval) {
        SharedPreferences.Editor edit = getSharedPreferencesEditor(context);
        edit.putLong(PREFERENCES_DATABASE_RETRY_INTERVAL, retryInterval);
        edit.commit();
    }

    public static void ignorePhoneNumber(Context context, String phoneNumber) {
        FileInputStream fis;
        try {
            fis = context.openFileInput(IGNORE_LIST_FILE);
        } catch (FileNotFoundException e) {
            HashSet<String> ignoreList = new HashSet<>();
            ignoreList.add(phoneNumber);
            ObjectOutputStream oos;
            try {
                oos = new ObjectOutputStream(context.openFileOutput(IGNORE_LIST_FILE, Context.MODE_PRIVATE));
                oos.writeObject(ignoreList);
                oos.close();
            } catch (Exception e1) {
                Log.e(TAG, "Cannot create ignore list file: " + e.getMessage());
            }
            return;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(fis);
            @SuppressWarnings("unchecked")
            HashSet<String> ignoreList = new HashSet<>((HashSet<String>) ois.readObject());
            ignoreList.add(phoneNumber);
            ObjectOutputStream oos = new ObjectOutputStream(context.openFileOutput(IGNORE_LIST_FILE, Context.MODE_PRIVATE));
            oos.writeObject(ignoreList);
            oos.close();
        } catch (Exception e) {
            Log.e(TAG, "Cannot write ignore list to file: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean isPhoneNumberIgnored(Context context, String phoneNumber) {
        HashSet<String> ignoreList = new HashSet<>();
        try {
            FileInputStream fis = context.openFileInput(IGNORE_LIST_FILE);
            ObjectInputStream oin = new ObjectInputStream(fis);
            ignoreList = new HashSet<>((HashSet<String>) oin.readObject());
            oin.close();
        } catch (Exception e) {
            Log.d(TAG, "Cannot read ignore list file: " + e.getMessage());
        }
        return ignoreList.contains(phoneNumber);
    }

    public static void setAnalyiticsEnabled(Context context, Boolean enabled) {
        getSharedPreferencesEditor(context).putBoolean(PREFERENCES_ANALYTICS_ENABLED, enabled).commit();
    }

    public static Boolean getAnalyiticsEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(PREFERENCES_ANALYTICS_ENABLED, false);
    }
}
