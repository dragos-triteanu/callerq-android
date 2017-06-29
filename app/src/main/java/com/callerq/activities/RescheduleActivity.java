package com.callerq.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.callerq.R;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RescheduleActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    @Nullable
    @BindView(R.id.titleText)
    TextView titleText;

    @Nullable
    @BindView(R.id.companyText)
    TextView companyText;

    @Nullable
    @BindView(R.id.notesText)
    TextView notesText;

    @Nullable
    @BindView(R.id.buttonSubmit)
    Button buttonSubmit;

    @Nullable
    @BindView(R.id.buttonClose)
    Button buttonClose;

    @Nullable
    @BindView(R.id.dateInput)
    BetterSpinner dateInput;

    @Nullable
    @BindView(R.id.timeInput)
    BetterSpinner timeInput;

    private ViewGroup mContainerView;
    private ViewGroup formView;

    private ArrayAdapter<String> dateArrayAdapter;
    private Calendar setCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reschedule);
        mContainerView = (ViewGroup) findViewById(R.id.rescheduleContainer);

        formView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.reschedule_form, mContainerView, false);
        mContainerView.addView(formView);
        ButterKnife.bind(this);

        setCalendar = Calendar.getInstance();
        initiateDateInput();
        initiateTimeInput();
    }

    public void onSubmit(View view) {

    }

    public void onClose(View view) {
        ViewGroup canceledView = (ViewGroup) LayoutInflater.from(RescheduleActivity.this).inflate(
                R.layout.reschedule_canceled, mContainerView, false);
        mContainerView.removeView(formView);
        mContainerView.addView(canceledView);

        ButterKnife.bind(RescheduleActivity.this);
    }

    private void initiateDateInput() {
        String[] dateArray = {"Today", "Tomorrow", "Pick a date..."};
        dateArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, dateArray);

        assert dateInput != null;
        dateInput.setAdapter(dateArrayAdapter);
        dateInput.setText(dateArray[0]);

        dateInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        setCalendar.set(Calendar.DAY_OF_YEAR, Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
                        break;
                    case 1:
                        setCalendar.set(Calendar.DAY_OF_YEAR, Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
                        setCalendar.add(Calendar.DAY_OF_YEAR, 1);
                        break;
                    case 2:
                        DatePickerDialog datePickerDialog = new DatePickerDialog(
                                RescheduleActivity.this, RescheduleActivity.this,
                                setCalendar.get(Calendar.YEAR),
                                setCalendar.get(Calendar.MONTH),
                                setCalendar.get(Calendar.DAY_OF_MONTH));
                        datePickerDialog.show();
                        break;
                }
                dateInput.clearFocus();
                dateInput.requestFocus();
            }
        });
    }

    private void initiateTimeInput() {
        String[] timeArray = {"Morning", "Afternoon", "Evening", "Set the time..."};
        final ArrayAdapter<String> timeArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, timeArray);

        assert timeInput != null;
        timeInput.setAdapter(timeArrayAdapter);
        timeInput.setText(timeArray[0]);
        setCalendar.set(Calendar.HOUR_OF_DAY, 8);
        setCalendar.set(Calendar.MINUTE, 0);

        timeInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        setCalendar.set(Calendar.HOUR_OF_DAY, 8);
                        setCalendar.set(Calendar.MINUTE, 0);
                        break;
                    case 1:
                        setCalendar.set(Calendar.HOUR_OF_DAY, 13);
                        setCalendar.set(Calendar.MINUTE, 0);
                        break;
                    case 2:
                        setCalendar.set(Calendar.HOUR_OF_DAY, 18);
                        setCalendar.set(Calendar.MINUTE, 0);
                        break;
                    case 3:
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                RescheduleActivity.this, RescheduleActivity.this, setCalendar.get(Calendar.HOUR_OF_DAY), setCalendar.get(Calendar.MINUTE), true);
                        timePickerDialog.show();
                        break;
                }
                timeInput.clearFocus();
                timeInput.requestFocus();
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

            if (setCalendar.get(Calendar.DAY_OF_YEAR) == today) {
                dateInput.setText(dateArrayAdapter.getItem(0));
            } else if (setCalendar.get(Calendar.DAY_OF_YEAR) == tomorrow) {
                dateInput.setText(dateArrayAdapter.getItem(1));
            } else {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, MMMM dd", Locale.US);
                dateInput.setText(simpleDateFormat.format(setCalendar.getTime()));
            }

            dateInput.clearFocus();
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
            timeInput.clearFocus();
            timeInput.requestFocus();
        }
    }
}
