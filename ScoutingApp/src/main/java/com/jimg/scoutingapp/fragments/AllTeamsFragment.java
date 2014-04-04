package com.jimg.scoutingapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jimg.scoutingapp.R;

/**
 * Created by Jim on 4/3/2014.
 */
public class AllTeamsFragment extends Fragment {
    public AllTeamsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_all_teams, container, false);
        return rootView;
    }
}
