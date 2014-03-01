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

    public TeamFragment() {
        // Required empty public constructor
    }

    public static TeamFragment newInstance(String title, int teamId) {
        TeamFragment teamFragment = new TeamFragment();

        Bundle bundle = new Bundle();
        bundle.putString(Constants.titleExtra, title);
        bundle.putInt(Constants.teamIdExtra, teamId);
        teamFragment.setArguments(bundle);

        return teamFragment;
    }
    private String getTitle() {
        return getArguments().getString(Constants.titleExtra);
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
                String errorMessage = reply.getString(Constants.errorMessageExtra);

                if (errorMessage != null && !errorMessage.isEmpty()) {
                    ErrorHelpers.displayErrorMessage(mMainActivity, reply, errorMessage, getString(R.string.failure_to_load_message));
                }
                else {
                    ArrayList<PlayerPojo> rawPlayerList = (ArrayList<PlayerPojo>)reply.get(Constants.retrievedEntityExtra);
                    mMainActivity.mPlayerTreeMap.put(getTeamId(), Player.convertArrayListToTreeMap(rawPlayerList));
                    PopulatePlayersListView(mMainActivity.mPlayerTreeMap.get(getTeamId()));
                }
                mMainActivity.mProgressDialog.dismiss();
            }
        };

        View rootView = inflater.inflate(R.layout.fragment_team, container, false);
        final TextView teamPageTitleTextView = (TextView)rootView.findViewById(R.id.teamPageTitleTextView);
        teamPageTitleTextView.setText(getTitle());

        mPlayersListView = (ListView)rootView.findViewById(R.id.playersListView);

        mPlayersListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                HashMap<String, String> playerHashMap = (HashMap<String, String>)mPlayersListView.getItemAtPosition(position);
                mMainActivity.ReplaceFragmentWithPlayer(getTitle(), playerHashMap);
            }
        });

        TreeMap<String, PlayerPojo> playerPojoTreeMap = mMainActivity.mPlayerTreeMap.get(getTeamId());
        if (playerPojoTreeMap == null) {
            getPlayers(getTeamId());
        }
        else {
            PopulatePlayersListView(playerPojoTreeMap);
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
