package com.jimg.scoutingapp.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.jimg.scoutingapp.Constants;
import com.jimg.scoutingapp.MainActivity;
import com.jimg.scoutingapp.R;
import com.jimg.scoutingapp.helpers.ErrorHelpers;
import com.jimg.scoutingapp.helpers.LogHelpers;
import com.jimg.scoutingapp.pojos.TeamCommentPojo;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import icepick.Icepick;
import icepick.Icicle;

/**
 * Created by Jim on 4/3/2014.
 */
public class AllTeamsFragment extends Fragment {
    private MainActivity mMainActivity;
    @Icicle ArrayList<TeamCommentPojo> mTeamCommentPojos;

    // region Handles to UI widgets
    @InjectView(R.id.allTeamsListView) ListView mAllTeamsListView;
    @InjectView(R.id.allTeamsCommentsColumnHeaderTextView) TextView commentsColumnHeader;
    @InjectView(R.id.allTeamsLastPostColumnHeaderTextView) TextView lastPostColumnHeader;
    // endregion

    public AllTeamsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainActivity = (MainActivity) getActivity();

        View rootView = inflater.inflate(R.layout.fragment_all_teams, container, false);
        ButterKnife.inject(this, rootView);
        Icepick.restoreInstanceState(this, savedInstanceState);

        commentsColumnHeader.setText(Html.fromHtml("<small>Comments</small>"));
        lastPostColumnHeader.setText(Html.fromHtml("<small>Last Post</small>"));
        mAllTeamsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> teamComment = (HashMap<String, String>) mAllTeamsListView.getItemAtPosition(position);
                mMainActivity.onOptionsItemSelected(mMainActivity.mMenu.findItem(Integer.parseInt(teamComment.get(TeamCommentPojo.TAG_TEAM_ID))));
            }
        });

        if (mTeamCommentPojos == null) {
            getTeamComments();
        }
        else {
            populateTeamCommentsListView(mTeamCommentPojos);
        }

        return rootView;
    }

    private void getTeamComments() {
        LogHelpers.ProcessAndThreadId("AllTeamFragment.getTeamComments");

        String getTeamCommentsUrl = Constants.restServiceUrlBase + "Comment/GetTotalsByTeam?" + Constants.getJson;
        mMainActivity.mProgressDialog = ProgressDialog.show(mMainActivity, "", getString(R.string.please_wait_loading_all_teams_comments), false);
        Ion.with(mMainActivity, getTeamCommentsUrl)
                .progressDialog(mMainActivity.mProgressDialog)
                .as(new TypeToken<ArrayList<TeamCommentPojo>>(){})
                .setCallback(new FutureCallback<ArrayList<TeamCommentPojo>>() {
                    @Override
                    public void onCompleted(Exception e, ArrayList<TeamCommentPojo> result) {
                        if (e != null) {
                            ErrorHelpers.handleError(getString(R.string.failure_to_load_message), e.getMessage(), ErrorHelpers.getStackTraceAsString(e), mMainActivity);
                            mMainActivity.goBack();
                        }
                        else {
                            populateTeamCommentsListView(result);
                        }
                        mMainActivity.dismissProgressDialog();
                    }
                });
    }

    private void populateTeamCommentsListView(ArrayList<TeamCommentPojo> teamCommentPojos) {
        if (teamCommentPojos == null) {
            throw new NullPointerException("teamCommentPojos cannot be null.");
        }

        SimpleAdapter simpleAdapter = TeamCommentPojo.convertArrayListToSimpleAdapter(mMainActivity, teamCommentPojos);
        mAllTeamsListView.setAdapter(simpleAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }
}
