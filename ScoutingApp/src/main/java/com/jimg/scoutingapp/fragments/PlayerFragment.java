package com.jimg.scoutingapp.fragments;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
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

import com.jimg.scoutingapp.Constants;
import com.jimg.scoutingapp.MainActivity;
import com.jimg.scoutingapp.R;
import com.jimg.scoutingapp.helpers.ErrorHelpers;
import com.jimg.scoutingapp.helpers.LogHelpers;
import com.jimg.scoutingapp.intentservices.GetJsonIntentService;
import com.jimg.scoutingapp.intentservices.PostPlayerCommentIntentService;
import com.jimg.scoutingapp.pojos.CommentViewPojo;
import com.jimg.scoutingapp.pojos.PlayerPojo;
import com.jimg.scoutingapp.utilityclasses.LazyAdapterForCommentViewPojo;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerFragment extends Fragment {
    private MainActivity mMainActivity;
    private Handler mCommentPostHandler;

    private ArrayList<CommentViewPojo> mCommentList;
    private Handler mCommentListHandler;
    private ListView mCommentListView;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainActivity = (MainActivity) getActivity();
        final View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        final HashMap<String, String> playerHashMap = getPlayerHashMap();
        final EditText editText = (EditText) rootView.findViewById(R.id.playerPageEditText);
        mCommentListView = (ListView) rootView.findViewById(R.id.playerPageListView);

        mCommentPostHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle reply = msg.getData();
                String errorMessage = reply.getString(Constants.errorMessageExtra);

                if (errorMessage != null) {
                    ErrorHelpers.handleError(getString(R.string.failure_to_post_comment), errorMessage, reply.getString(Constants.stackTraceExtra), mMainActivity);
                } else {
                    editText.setText("");
                }
                mMainActivity.DismissProgressDialog();

                if (errorMessage == null) {
                    getComments(Integer.parseInt(playerHashMap.get(PlayerPojo.TAG_PLAYER_ID)));
                }
            }
        };

        mCommentListHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle reply = msg.getData();
                String errorMessage = reply.getString(Constants.errorMessageExtra);

                if (errorMessage != null) {
                    ErrorHelpers.handleError(getString(R.string.failure_to_load_message), errorMessage, reply.getString(Constants.stackTraceExtra), mMainActivity);
                    mMainActivity.goBack();
                } else {
                    mCommentList =(ArrayList<CommentViewPojo>) reply.get(Constants.retrievedEntityExtra);
                    PopulateCommentsListView(mCommentList);
                }
                mMainActivity.DismissProgressDialog();
            }
        };

        if (mCommentList == null) {
            getComments(Integer.parseInt(playerHashMap.get(PlayerPojo.TAG_PLAYER_ID)));
        } else {
            PopulateCommentsListView(mCommentList);
        }

        final TextView playerPageTeamTextView = (TextView) rootView.findViewById(R.id.playerPageTeamTextView);
        playerPageTeamTextView.setText(getTitle());

        final View playerInfoPlayerRow = rootView.findViewById(R.id.playerInfoPlayerRow);

        final TextView playerNumberTextView = (TextView) playerInfoPlayerRow.findViewById(R.id.columnNumber);
        playerNumberTextView.setText(playerHashMap.get(PlayerPojo.TAG_NUMBER));

        final TextView playerNameTextView = (TextView) playerInfoPlayerRow.findViewById(R.id.columnName);
        playerNameTextView.setText(playerHashMap.get(PlayerPojo.TAG_FORMATTED_NAME));

        final TextView playerPositionTextView = (TextView) playerInfoPlayerRow.findViewById(R.id.columnPosition);
        playerPositionTextView.setText(playerHashMap.get(PlayerPojo.TAG_POSITION));

        final TextView playerStatusTextView = (TextView) playerInfoPlayerRow.findViewById(R.id.columnStatus);
        playerStatusTextView.setText(playerHashMap.get(PlayerPojo.TAG_STATUS));

        final LinearLayout playerPageCommentButtonControls = (LinearLayout) rootView.findViewById(R.id.playerPageCommentButtonControls);

        final TextView pleaseSignInTextView = (TextView) rootView.findViewById(R.id.pleaseSignInTextView);

        if (mMainActivity.mSignInStatus == Constants.SignInStatus.SignedOut) {
            pleaseSignInTextView.setVisibility(View.VISIBLE);
            editText.setVisibility(View.GONE);
            playerPageCommentButtonControls.setVisibility(View.GONE);
        } else {
            pleaseSignInTextView.setVisibility(View.GONE);
            final ImageButton clearButton = (ImageButton) rootView.findViewById(R.id.playerPageClearButton);
            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editText.setText("");
                }
            });
            final ImageButton submitButton = (ImageButton) rootView.findViewById(R.id.playerPageSubmitButton);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postComment(Integer.parseInt(playerHashMap.get(PlayerPojo.TAG_PLAYER_ID)), editText.getText().toString());
                }
            });
            final TextView playerPageCommentLengthWarning = (TextView) rootView.findViewById(R.id.playerPageCommentLengthWarning);
            watcher(editText, clearButton, submitButton, playerPageCommentLengthWarning);
        }

        return rootView;
    }

    private void getComments(int playerId) {
        LogHelpers.ProcessAndThreadId("PlayerFragment.getComments");

        mMainActivity.mProgressDialog = ProgressDialog.show(mMainActivity, "", getString(R.string.please_wait_loading_comments), false);
        Intent serviceIntent = new Intent(mMainActivity, GetJsonIntentService.class);
        serviceIntent.putExtra(Constants.entityToRetrieveExtra, Constants.Entities.CommentsByPlayerId);
        serviceIntent.putExtra(Constants.playerIdExtra, playerId);
        serviceIntent.putExtra(Constants.messengerExtra, new Messenger(mCommentListHandler));
        mMainActivity.startService(serviceIntent);
    }

    private void PopulateCommentsListView(ArrayList<CommentViewPojo> commentViewPojoList) {
        if (commentViewPojoList == null) {
            throw new NullPointerException("commentViewPojoList cannot be null");
        }

        LazyAdapterForCommentViewPojo lazyAdapter = new LazyAdapterForCommentViewPojo(mMainActivity, commentViewPojoList);
        mCommentListView.setAdapter(lazyAdapter);
    }

    private void postComment(int playerId, String comment) {
        LogHelpers.ProcessAndThreadId("PlayerFragment.postComment");

        mMainActivity.mProgressDialog = ProgressDialog.show(mMainActivity, "", getString(R.string.please_wait_posting_comment), false);

        Intent serviceIntent = new Intent(mMainActivity, PostPlayerCommentIntentService.class);
        serviceIntent.putExtra(Constants.messengerExtra, new Messenger(mCommentPostHandler));
        serviceIntent.putExtra(Constants.authTokenExtra, mMainActivity.mAuthToken);
        serviceIntent.putExtra(Constants.playerIdExtra, playerId);
        serviceIntent.putExtra(Constants.commentExtra, comment);

        mMainActivity.startService(serviceIntent);
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
