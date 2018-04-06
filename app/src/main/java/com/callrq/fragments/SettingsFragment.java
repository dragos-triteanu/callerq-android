package com.callrq.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.callrq.R;

public class SettingsFragment extends Fragment {

    private OnFragmentCompleteListener mListener;

    public SettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListener.onFragmentComplete();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachListener(context);
    }

    // Method is deprecated since API 23 but must be defined because older APIs still call this method instead of the new one
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < 23) {
            onAttachListener(activity);
        }
    }

    private void onAttachListener(Context context) {
        if (context instanceof OnFragmentCompleteListener) {
            mListener = (OnFragmentCompleteListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentCompleteListener {
        void onFragmentComplete();
    }
}
