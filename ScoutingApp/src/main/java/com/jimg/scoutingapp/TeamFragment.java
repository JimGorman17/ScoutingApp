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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
* Created by Jim on 2/16/14.
*/
public class TeamFragment extends Fragment {
    private MainActivity mMainActivity;
    private Handler mPlayerHandler;
    private ListView mPlayersListView;

    private static final String TITLE_TAG = "Title";
    private static final String PLAYER_TREEMAP_TAG = "PlayerTreeMap";

    private String mTitle;
    private TreeMap<String, PlayerPojo> mPlayerTreeMap;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(TITLE_TAG, mTitle);
        outState.putSerializable(PLAYER_TREEMAP_TAG, mPlayerTreeMap);
        super.onSaveInstanceState(outState);
    }

    public TeamFragment() {
        // Required empty public constructor
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
                mPlayerTreeMap = Player.convertArrayListToTreeMap(rawPlayerList);
                mMainActivity.mPlayerTreeMap.put(getTeamId(), mPlayerTreeMap);
                PopulatePlayersListView(mMainActivity.mPlayerTreeMap.get(getTeamId()));
                mMainActivity.mProgressDialog.dismiss();
            }
        };

        View rootView = inflater.inflate(R.layout.fragment_team, container, false);
        final TextView teamPageTitleTextView = (TextView)rootView.findViewById(R.id.teamPageTitleTextView);
        mPlayersListView = (ListView)rootView.findViewById(R.id.playersListView);

        mPlayersListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                HashMap<String, String> playerHashMap = (HashMap<String, String>)mPlayersListView.getItemAtPosition(position);
                mMainActivity.ReplaceFragmentWithPlayer(playerHashMap);
            }
        });

        if (savedInstanceState == null) {
            mTitle = mMainActivity.mTeamNamesTreeMap.get(getTeamId());
            teamPageTitleTextView.setText(mTitle);

            if (mMainActivity.mPlayerTreeMap.get(getTeamId()) == null) {
                getPlayers(getTeamId());
            }
            else {
                mPlayerTreeMap = mMainActivity.mPlayerTreeMap.get(getTeamId());
                PopulatePlayersListView(mPlayerTreeMap);
            }
        }
        else {
            mTitle = savedInstanceState.getString(TITLE_TAG);
            mPlayerTreeMap = (TreeMap<String, PlayerPojo>)savedInstanceState.getSerializable(PLAYER_TREEMAP_TAG);

            teamPageTitleTextView.setText(mTitle);
            PopulatePlayersListView(mPlayerTreeMap);
        }

        return rootView;
    }

    private void PopulatePlayersListView(TreeMap<String, PlayerPojo> playerPojoTreeMap) {
        if (playerPojoTreeMap == null) {
            throw new NullPointerException("playerPojoTreeMap cannot be null");
        }

        SimpleAdapter simpleAdapter = Player.convertTreeMapToSimpleAdapter(mMainActivity, playerPojoTreeMap);
        mPlayersListView.setAdapter(simpleAdapter);
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
