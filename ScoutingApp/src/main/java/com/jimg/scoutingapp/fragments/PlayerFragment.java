package com.jimg.scoutingapp.fragments;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.jimg.scoutingapp.Constants;
import com.jimg.scoutingapp.MainActivity;
import com.jimg.scoutingapp.R;
import com.jimg.scoutingapp.helpers.ErrorHelpers;
import com.jimg.scoutingapp.helpers.LogHelpers;
import com.jimg.scoutingapp.pojos.CommentViewPojo;
import com.jimg.scoutingapp.pojos.PlayerPojo;
import com.jimg.scoutingapp.utilityclasses.LazyAdapterForCommentViewPojo;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerFragment extends Fragment {
    private MainActivity mMainActivity;

    private ArrayList<CommentViewPojo> mCommentList;
    private ListView mCommentListView;
    private EditText mEditText;
    private HashMap<String, String> mPlayerHashMap;

    public PlayerFragment() {
        // Required empty public constructor
    }

    public static PlayerFragment newInstance(String title, HashMap<String, String> playerHashMap) {
        PlayerFragment playerFragment = new PlayerFragment();

        Bundle bundle = new Bundle();
        bundle.putString(Constants.titleExtra, title);
        bundle.putSerializable(Constants.playerHashMapExtra, playerHashMap);
        playerFragment.setArguments(bundle);

        return playerFragment;
    }

    private String getTitle() {
        return getArguments().getString(Constants.titleExtra);
    }

    private HashMap<String, String> getPlayerHashMap() {
        return (HashMap<String, String>) getArguments().getSerializable(Constants.playerHashMapExtra);
    }

    private static class Response {
        @SerializedName("Comments")
        public ArrayList<CommentViewPojo> comments;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainActivity = (MainActivity) getActivity();
        final View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        mPlayerHashMap = getPlayerHashMap();
        mEditText = (EditText) rootView.findViewById(R.id.playerPageEditText);
        mCommentListView = (ListView) rootView.findViewById(R.id.playerPageListView);

        if (mCommentList == null) {
            getComments(Integer.parseInt(mPlayerHashMap.get(PlayerPojo.TAG_PLAYER_ID)));
        } else {
            PopulateCommentsListView(mCommentList);
        }

        final TextView playerPageTeamTextView = (TextView) rootView.findViewById(R.id.playerPageTeamTextView);
        playerPageTeamTextView.setText(getTitle());

        final View playerInfoPlayerRow = rootView.findViewById(R.id.playerInfoPlayerRow);

        final TextView playerNumberTextView = (TextView) playerInfoPlayerRow.findViewById(R.id.columnNumber);
        playerNumberTextView.setText(mPlayerHashMap.get(PlayerPojo.TAG_NUMBER));

        final TextView playerNameTextView = (TextView) playerInfoPlayerRow.findViewById(R.id.columnName);
        playerNameTextView.setText(mPlayerHashMap.get(PlayerPojo.TAG_FORMATTED_NAME));

        final TextView playerPositionTextView = (TextView) playerInfoPlayerRow.findViewById(R.id.columnPosition);
        playerPositionTextView.setText(mPlayerHashMap.get(PlayerPojo.TAG_POSITION));

        final TextView playerStatusTextView = (TextView) playerInfoPlayerRow.findViewById(R.id.columnStatus);
        playerStatusTextView.setText(mPlayerHashMap.get(PlayerPojo.TAG_STATUS));

        final LinearLayout playerPageCommentButtonControls = (LinearLayout) rootView.findViewById(R.id.playerPageCommentButtonControls);

        final TextView pleaseSignInTextView = (TextView) rootView.findViewById(R.id.pleaseSignInTextView);

        if (mMainActivity.mSignInStatus == Constants.SignInStatus.SignedOut) {
            pleaseSignInTextView.setVisibility(View.VISIBLE);
            mEditText.setVisibility(View.GONE);
            playerPageCommentButtonControls.setVisibility(View.GONE);
        } else {
            pleaseSignInTextView.setVisibility(View.GONE);
            final ImageButton clearButton = (ImageButton) rootView.findViewById(R.id.playerPageClearButton);
            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mEditText.setText("");
                    ((LazyAdapterForCommentViewPojo) mCommentListView.getAdapter()).cancelEdit();
                }
            });
            final ImageButton submitButton = (ImageButton) rootView.findViewById(R.id.playerPageSubmitButton);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postComment(((LazyAdapterForCommentViewPojo) mCommentListView.getAdapter()).mCurrentlySelectedCommentId, Integer.parseInt(mPlayerHashMap.get(PlayerPojo.TAG_PLAYER_ID)), mEditText.getText().toString());
                }
            });
            final TextView playerPageCommentLengthWarning = (TextView) rootView.findViewById(R.id.playerPageCommentLengthWarning);
            watcher(mEditText, clearButton, submitButton, playerPageCommentLengthWarning);
        }

        return rootView;
    }

    private void getComments(int playerId) {
        LogHelpers.ProcessAndThreadId("PlayerFragment.getComments");

        JsonObject json = new JsonObject();
        json.addProperty(Constants.playerIdExtra, playerId);
        json.addProperty(Constants.authTokenExtra, mMainActivity.mAuthToken);

        mMainActivity.mProgressDialog = ProgressDialog.show(mMainActivity, "", getString(R.string.please_wait_loading_comments), false);
        Ion.with(mMainActivity, Constants.restServiceUrlBase + "Comment/GetAllByPlayerId?" + Constants.getJson)
                .progressDialog(mMainActivity.mProgressDialog)
                .setJsonObjectBody(json)
                .as(new TypeToken<Response>() {
                })
                .setCallback(new FutureCallback<Response>() {
                    @Override
                    public void onCompleted(Exception e, Response result) {
                        if (e != null || result.comments == null) {
                            ErrorHelpers.handleError(getString(R.string.failure_to_load_message), e.getMessage(), ErrorHelpers.getStackTraceAsString(e), mMainActivity);
                            mMainActivity.goBack();
                        } else {
                            mCommentList = result.comments;
                            PopulateCommentsListView(mCommentList);
                        }
                        mMainActivity.DismissProgressDialog();
                    }
                });
    }

    private void PopulateCommentsListView(ArrayList<CommentViewPojo> commentViewPojoList) {
        if (commentViewPojoList == null) {
            throw new NullPointerException("commentViewPojoList cannot be null");
        }

        LazyAdapterForCommentViewPojo lazyAdapter = new LazyAdapterForCommentViewPojo(mMainActivity, commentViewPojoList);
        mCommentListView.setAdapter(lazyAdapter);
    }

    private void postComment(int commentId, int playerId, String comment) {
        LogHelpers.ProcessAndThreadId("PlayerFragment.postComment");

        JsonObject json = new JsonObject();
        json.addProperty(Constants.commentIdExtra, commentId);
        json.addProperty(Constants.authTokenExtra, mMainActivity.mAuthToken);
        json.addProperty(Constants.playerIdExtra, playerId);
        json.addProperty(Constants.commentExtra, comment);

        mMainActivity.mProgressDialog = ProgressDialog.show(mMainActivity, "", getString(R.string.please_wait_posting_comment), false);
        Ion.with(mMainActivity, Constants.restServiceUrlBase + "Comment/Save?" + Constants.getJson)
                .progressDialog(mMainActivity.mProgressDialog)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            ErrorHelpers.handleError(getString(R.string.failure_to_post_comment), e.getMessage(), ErrorHelpers.getStackTraceAsString(e), mMainActivity);
                        } else {
                            mEditText.setText("");
                        }
                        mMainActivity.DismissProgressDialog();

                        if (e == null) {
                            getComments(Integer.parseInt(mPlayerHashMap.get(PlayerPojo.TAG_PLAYER_ID)));
                        }
                    }
                });
    }

    private void watcher(final EditText editText, final ImageButton clearButton, final ImageButton submitButton, final TextView playerPageCommentLengthWarning) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                ToggleCommentControls(editText, clearButton, submitButton, playerPageCommentLengthWarning);
            }
        });
        ToggleCommentControls(editText, clearButton, submitButton, playerPageCommentLengthWarning);
    }

    private void ToggleCommentControls(EditText editText, ImageButton clearButton, ImageButton submitButton, TextView playerPageCommentLengthWarning) {
        int editTextLength = editText.length();
        if (editTextLength == 0) {
            clearButton.setEnabled(false);
            submitButton.setEnabled(false);
            playerPageCommentLengthWarning.setVisibility(View.INVISIBLE);
        } else if (Integer.parseInt(getString(R.string.comment_length)) <= editTextLength) {
            clearButton.setEnabled(true);
            submitButton.setEnabled(false);
            playerPageCommentLengthWarning.setVisibility(View.VISIBLE);
        } else {
            clearButton.setEnabled(true);
            submitButton.setEnabled(true);
            playerPageCommentLengthWarning.setVisibility(View.INVISIBLE);
        }
    }
}
