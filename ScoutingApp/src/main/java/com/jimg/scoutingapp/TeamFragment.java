package com.jimg.scoutingapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
* Created by Jim on 2/16/14.
*/
public class TeamFragment extends Fragment {

    public TeamFragment() {
    }

    public static TeamFragment newInstance(int teamId) {
        TeamFragment teamFragment = new TeamFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(Constants.teamIdExtra, teamId);
        teamFragment.setArguments(bundle);

        return teamFragment;
    }
    public int getTeamId() {
        return getArguments().getInt(Constants.teamIdExtra, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_team, container, false);

        final TextView teamPageTitleTextView = (TextView)rootView.findViewById(R.id.teamPageTitleTextView);
        teamPageTitleTextView.setText(((MainActivity)getActivity()).mTeamTreeMap.get(getTeamId()));

        return rootView;
    }
}
