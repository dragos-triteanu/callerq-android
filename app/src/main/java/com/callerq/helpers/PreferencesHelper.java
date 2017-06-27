package com.callerq.helpers;

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

    private static final String TAG = "PreferencesHelper";
    private static final String PREF_TERMS_ACCEPTED = "termsAccepted";

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static SharedPreferences.Editor getSharedPreferencesEditor(Context context) {
        return getSharedPreferences(context).edit();
    }

    public static void setTermsAccepted (Context context, Boolean termsAgreed) {
        getSharedPreferencesEditor(context).putBoolean(PREF_TERMS_ACCEPTED, termsAgreed).commit();
    }

    public static Boolean getTermsAccepted (Context context) {
        return getSharedPreferences(context).getBoolean(PREF_TERMS_ACCEPTED, false);
    }
}
