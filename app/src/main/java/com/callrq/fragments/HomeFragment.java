package com.callrq.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.callrq.R;

public class HomeFragment extends Fragment {

    private Context context;
    private View view;

    public HomeFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.context = inflater.getContext();
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notificationsOn = prefs.getBoolean("pref_application_status", true);

        TextView introAdvice = view.findViewById(R.id.introAdvice);
        if (notificationsOn) {
            introAdvice.setText(R.string.intro_advice_notif_on);
        } else {
            introAdvice.setText(R.string.intro_advice_notif_off);
        }
        super.onResume();
    }
}
