package com.jimg.scoutingapp.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.jimg.scoutingapp.Constants;
import com.jimg.scoutingapp.MainActivity;
import com.jimg.scoutingapp.R;
import com.jimg.scoutingapp.helpers.ErrorHelpers;
import com.jimg.scoutingapp.helpers.LogHelpers;
import com.jimg.scoutingapp.pojos.PlayerPojo;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by Jim on 2/16/14.
 */
public class TeamFragment extends Fragment {
    private MainActivity mMainActivity;
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
        mMainActivity = (MainActivity) getActivity();

        View rootView = inflater.inflate(R.layout.fragment_team, container, false);
        final TextView teamPageTitleTextView = (TextView) rootView.findViewById(R.id.teamPageTitleTextView);
        teamPageTitleTextView.setText(getTitle());

        mPlayersListView = (ListView) rootView.findViewById(R.id.playersListView);

        mPlayersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> playerHashMap = (HashMap<String, String>) mPlayersListView.getItemAtPosition(position);
                mMainActivity.ReplaceFragmentWithPlayer(getTitle(), playerHashMap);
            }
        });

        TreeMap<String, PlayerPojo> playerPojoTreeMap = mMainActivity.mPlayerTreeMap.get(getTeamId());
        if (playerPojoTreeMap == null) {
            getPlayers(getTeamId());
        } else {
            PopulatePlayersListView(playerPojoTreeMap);
        }

        return rootView;
    }

    private void PopulatePlayersListView(TreeMap<String, PlayerPojo> playerPojoTreeMap) {
        if (playerPojoTreeMap == null) {
            throw new NullPointerException("playerPojoTreeMap cannot be null");
        }

        SimpleAdapter simpleAdapter = PlayerPojo.convertTreeMapToSimpleAdapter(mMainActivity, playerPojoTreeMap);
        mPlayersListView.setAdapter(simpleAdapter);
    }

    private class Response {
        @SerializedName("Players")
        ArrayList<PlayerPojo> players;
    }

    private void getPlayers(int teamId) {
        LogHelpers.ProcessAndThreadId("TeamFragment.getPlayers");

        String getAllByTeamIdUrl = Constants.restServiceUrlBase + "Player/GetAllByTeamId?TeamId={0}&" + Constants.getJson;

        mMainActivity.mProgressDialog = ProgressDialog.show(mMainActivity, "", getString(R.string.please_wait_loading_players), false);
        Ion.with(mMainActivity, getAllByTeamIdUrl.replace("{0}", Integer.toString(teamId)))
                .progressDialog(mMainActivity.mProgressDialog)
                .as(new TypeToken<Response>(){})
                .setCallback(new FutureCallback<Response>() {
                    @Override
                    public void onCompleted(Exception e, Response result) {
                        if (e != null) {
                            ErrorHelpers.handleError(getString(R.string.failure_to_load_message), e.getMessage(), ErrorHelpers.getStackTraceAsString(e), mMainActivity);
                            mMainActivity.goBack();
                        }
                        else {
                            ArrayList<PlayerPojo> rawPlayerList = result.players;
                            mMainActivity.mPlayerTreeMap.put(getTeamId(), PlayerPojo.convertArrayListToTreeMap(rawPlayerList));
                            PopulatePlayersListView(mMainActivity.mPlayerTreeMap.get(getTeamId()));
                        }

                        mMainActivity.dismissProgressDialog();
                    }
                });
    }
}
