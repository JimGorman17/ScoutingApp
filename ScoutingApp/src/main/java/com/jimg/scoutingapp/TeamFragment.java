package com.jimg.scoutingapp;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.TreeMap;

/**
* Created by Jim on 2/16/14.
*/
public class TeamFragment extends Fragment {
    private MainActivity mMainActivity = null;
    private Handler mPlayerHandler = null;

    public TeamFragment() {
    }

    public static TeamFragment newInstance(int teamId) {
        TeamFragment teamFragment = new TeamFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(Constants.teamIdExtra, teamId);
        teamFragment.setArguments(bundle);

        return teamFragment;
    }
    private int getTeamId() {
        return getArguments().getInt(Constants.teamIdExtra, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainActivity = (MainActivity)getActivity();
        mPlayerHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle reply = msg.getData();
                ArrayList<PlayerPojo> rawPlayerList = (ArrayList<PlayerPojo>)reply.get(Constants.retrievedEntityExtra);
                TreeMap<String, PlayerPojo> playerList = Player.convertArrayListToTreeMap(rawPlayerList);
                mMainActivity.mPlayerTreeMap.put(getTeamId(), playerList);
                mMainActivity.mProgressDialog.dismiss();
            }
        };

        View rootView = inflater.inflate(R.layout.fragment_team, container, false);
        TextView teamPageTitleTextView = (TextView)rootView.findViewById(R.id.teamPageTitleTextView);
        teamPageTitleTextView.setText(mMainActivity.mTeamTreeMap.get(getTeamId()));

        if (mMainActivity.mPlayerTreeMap.get(getTeamId()) == null) {
            getPlayers(getTeamId());
        }

        return rootView;
    }

    private void getPlayers(int teamId) {
        LogHelper.ProcessAndThreadId("TeamFragment.getPlayers");

        mMainActivity.mProgressDialog = ProgressDialog.show(mMainActivity, "", getString(R.string.please_wait_loading_players), false);
        Intent serviceIntent = new Intent(mMainActivity, OnDemandJsonFetchWorker.class);
        serviceIntent.putExtra(Constants.entityToRetrieveExtra, Constants.Entities.PlayersByTeamId);
        serviceIntent.putExtra(Constants.teamIdExtra, teamId);
        serviceIntent.putExtra(Constants.messengerExtra, new Messenger(mPlayerHandler));
        mMainActivity.startService(serviceIntent);
    }
}
