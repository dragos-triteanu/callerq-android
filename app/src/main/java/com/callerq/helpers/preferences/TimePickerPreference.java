package com.callerq.helpers.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimePickerPreference extends DialogPreference {

    private Calendar calendar;
    private Calendar defaultCalendar;
    private TimePicker picker = null;

    public TimePickerPreference(Context ctxt) {
        this(ctxt, null);
    }

    public TimePickerPreference(Context ctxt, AttributeSet attrs) {
        this(ctxt, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public TimePickerPreference(Context ctxt, AttributeSet attrs, int defStyle) {
        super(ctxt, attrs, defStyle);

        setPositiveButtonText("Set time");
        setNegativeButtonText("Cancel");
        calendar = Calendar.getInstance();
        defaultCalendar = Calendar.getInstance();
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        picker.setIs24HourView(true);
        return (picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        switch (getKey()) {
            case "pref_morning_time":
                defaultCalendar.set(Calendar.HOUR_OF_DAY, 8);
                break;
            case "pref_afternoon_time":
                defaultCalendar.set(Calendar.HOUR_OF_DAY, 14);
                break;
            case "pref_evening_time":
                defaultCalendar.set(Calendar.HOUR_OF_DAY, 20);
                break;
        }
        defaultCalendar.set(Calendar.MINUTE, 0);

        picker.setCurrentHour(defaultCalendar.get(Calendar.HOUR_OF_DAY));
        picker.setCurrentMinute(defaultCalendar.get(Calendar.MINUTE));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {

            Calendar validateCalendar = Calendar.getInstance();
            validateCalendar.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
            validateCalendar.set(Calendar.MINUTE, picker.getCurrentMinute());

            defaultCalendar.add(Calendar.HOUR_OF_DAY, -3);
            if (validateCalendar.get(Calendar.HOUR_OF_DAY) < defaultCalendar.get(Calendar.HOUR_OF_DAY)) {
                Toast.makeText(getContext(), "There can be at most a 3 hours difference from the default time", Toast.LENGTH_LONG).show();
                return;
            }

            defaultCalendar.add(Calendar.HOUR_OF_DAY, 5);
            if (validateCalendar.get(Calendar.HOUR_OF_DAY) > defaultCalendar.get(Calendar.HOUR_OF_DAY)) {
                Toast.makeText(getContext(), "There can be at most a 3 hours difference from the default time", Toast.LENGTH_LONG).show();
                return;
            }

            calendar.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
            calendar.set(Calendar.MINUTE, picker.getCurrentMinute());

            setSummary(getSummary());
            if (callChangeListener(calendar.getTimeInMillis())) {
                persistLong(calendar.getTimeInMillis());
                notifyChanged();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if (restoreValue) {
            if (defaultValue == null) {
                calendar.setTimeInMillis(getPersistedLong(System.currentTimeMillis()));
            } else {
                calendar.setTimeInMillis(Long.parseLong(getPersistedString((String) defaultValue)));
            }
        } else {
            if (defaultValue == null) {
                calendar.setTimeInMillis(System.currentTimeMillis());
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(defaultValue.toString()));
                calendar.set(Calendar.MINUTE, 0);
            }
        }

        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        if (calendar == null) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return simpleDateFormat.format(calendar.getTimeInMillis());
    }
}