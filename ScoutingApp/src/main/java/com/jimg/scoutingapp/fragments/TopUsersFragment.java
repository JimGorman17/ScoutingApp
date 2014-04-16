package com.jimg.scoutingapp.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.jimg.scoutingapp.Constants;
import com.jimg.scoutingapp.MainActivity;
import com.jimg.scoutingapp.R;
import com.jimg.scoutingapp.adapters.TopUserViewAdapter;
import com.jimg.scoutingapp.helpers.ErrorHelpers;
import com.jimg.scoutingapp.helpers.LogHelpers;
import com.jimg.scoutingapp.pojos.TopUserPojo;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import icepick.Icepick;
import icepick.Icicle;

/**
 * Created by Jim on 4/3/2014.
 */
public class TopUsersFragment extends Fragment {
    private MainActivity mMainActivity;
    @Icicle ArrayList<TopUserPojo> mTopUserPojos;

    // region Handles to UI Widgets
    @InjectView(R.id.topUsersListView) ListView mTopUsersListView;
    @InjectView(R.id.topUsersTableLayout) TableLayout mTopUsersTableLayout;
    @InjectView(R.id.noTopUsersTextView) TextView mNoTopUsersTextView;
    // endregion

    public TopUsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainActivity = (MainActivity) getActivity();
        View rootView = inflater.inflate(R.layout.fragment_top_users, container, false);
        ButterKnife.inject(this, rootView);
        Icepick.restoreInstanceState(this, savedInstanceState);

        if (mTopUserPojos == null) {
            getTopUsers();
        }
        else {
            populateTopUsersListView(mTopUserPojos);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    private void getTopUsers() {
        LogHelpers.ProcessAndThreadId("TopUsersFragment.getTopUsers");

        String getTopUsersUrl = Constants.restServiceUrlBase + "Comment/GetTotalsByUser?NumberOfUsers={0}&" + Constants.getJson;

        mMainActivity.mProgressDialog = ProgressDialog.show(mMainActivity, "", getString(R.string.please_wait_loading_top_users), false);
        Ion.with(mMainActivity, getTopUsersUrl.replace("{0}", mMainActivity.getString(R.string.top_users_report_number_of_users)))
                .progressDialog(mMainActivity.mProgressDialog)
                .as(new TypeToken<ArrayList<TopUserPojo>>() {
                })
                .setCallback(new FutureCallback<ArrayList<TopUserPojo>>() {
                    @Override
                    public void onCompleted(Exception e, ArrayList<TopUserPojo> result) {
                        if (e != null) {
                            ErrorHelpers.handleError(getString(R.string.failure_to_load_message), e.getMessage(), ErrorHelpers.getStackTraceAsString(e), mMainActivity);
                            mMainActivity.goBack();
                        }
                        else {
                            populateTopUsersListView(result);
                        }
                        mMainActivity.dismissProgressDialog();
                    }
                });
    }

    private void populateTopUsersListView(ArrayList<TopUserPojo> topUserPojos) {
        if (topUserPojos == null) {
            throw new NullPointerException("topUserPojos cannot be null.");
        }

        if (0 < topUserPojos.size()) {
            TopUserViewAdapter arrayAdapter = new TopUserViewAdapter(mMainActivity, topUserPojos);
            mTopUsersListView.setAdapter(arrayAdapter);
        } else {
            mTopUsersTableLayout.setVisibility(View.GONE);
            mNoTopUsersTextView.setVisibility(View.VISIBLE);
        }
    }
}
