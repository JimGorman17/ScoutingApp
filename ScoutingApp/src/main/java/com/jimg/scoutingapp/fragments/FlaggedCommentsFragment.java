package com.jimg.scoutingapp.fragments;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.jimg.scoutingapp.Constants;
import com.jimg.scoutingapp.MainActivity;
import com.jimg.scoutingapp.R;
import com.jimg.scoutingapp.adapters.FlaggedCommentViewAdapter;
import com.jimg.scoutingapp.helpers.ErrorHelpers;
import com.jimg.scoutingapp.helpers.LogHelpers;
import com.jimg.scoutingapp.pojos.FlaggedCommentPojo;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import icepick.Icicle;

public class FlaggedCommentsFragment extends Fragment {
    private MainActivity mMainActivity;
    @Icicle ArrayList<FlaggedCommentPojo> mFlaggedCommentPojos;

    // region Handles to UI Widgets
    @InjectView(R.id.flaggedCommentsListView) ListView mFlaggedCommentsListView;
    // endregion

    public FlaggedCommentsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainActivity = (MainActivity) getActivity();
        View rootView = inflater.inflate(R.layout.fragment_flagged_comments, container, false);
        ButterKnife.inject(this, rootView);

        if (mFlaggedCommentPojos == null) {
            getFlaggedComments();
        }
        else {
            populateFlaggedCommentsListView(mFlaggedCommentPojos);
        }

        return rootView;
    }

    private void getFlaggedComments() {
        LogHelpers.ProcessAndThreadId("FlaggedCommentsFragment.getFlaggedComments");

        JsonObject json = new JsonObject();
        json.addProperty(Constants.authTokenExtra, mMainActivity.mAuthToken);

        mMainActivity.mProgressDialog = ProgressDialog.show(mMainActivity, "", getString(R.string.please_wait_loading_flagged_comments), false);
        Ion.with(mMainActivity, Constants.restServiceUrlBase + "FlaggedComment/GetCommentsForModerator?" + Constants.getJson)
                .progressDialog(mMainActivity.mProgressDialog)
                .setJsonObjectBody(json)
                .as(new TypeToken<ArrayList<FlaggedCommentPojo>>() {
                })
                .setCallback(new FutureCallback<ArrayList<FlaggedCommentPojo>>() {
                    @Override
                    public void onCompleted(Exception e, ArrayList<FlaggedCommentPojo> result) {
                        if (e != null) {
                            ErrorHelpers.handleError(getString(R.string.failure_to_load_message), e.getMessage(), ErrorHelpers.getStackTraceAsString(e), mMainActivity);
                            mMainActivity.goBack();
                        }
                        else {
                            populateFlaggedCommentsListView(result);
                        }
                        mMainActivity.dismissProgressDialog();
                    }
                });
    }

    private void populateFlaggedCommentsListView(ArrayList<FlaggedCommentPojo> flaggedCommentArrayList) {
        if (flaggedCommentArrayList == null) {
            throw new NullPointerException("flaggedCommentArrayList cannot be null.");
        }

        FlaggedCommentViewAdapter arrayAdapter = new FlaggedCommentViewAdapter (mMainActivity, flaggedCommentArrayList);
        mFlaggedCommentsListView.setAdapter(arrayAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
