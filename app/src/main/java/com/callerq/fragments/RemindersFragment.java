package com.callerq.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.callerq.R;
import com.callerq.helpers.AddressBookHelper;
import com.callerq.helpers.DatabaseHelper;
import com.callerq.models.Reminder;
import com.callerq.services.DatabaseService;

import javax.inject.Inject;
import java.util.ArrayList;

public class RemindersFragment extends Fragment {

    public RemindersFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminders, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DatabaseHelper.getInstance().getReminders(getActivity());
    }
}
