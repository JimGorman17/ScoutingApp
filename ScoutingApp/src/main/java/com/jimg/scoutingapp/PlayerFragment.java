package com.jimg.scoutingapp;



import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;

public class PlayerFragment extends Fragment {
    private MainActivity mMainActivity;

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
        return (HashMap<String, String>)getArguments().getSerializable(Constants.playerHashMapExtra);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainActivity = (MainActivity)getActivity();
        final View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        final TextView playerPageTeamTextView = (TextView)rootView.findViewById(R.id.playerPageTeamTextView);
        playerPageTeamTextView.setText(getTitle());

        final View playerInfoPlayerRow = rootView.findViewById(R.id.playerInfoPlayerRow);
        HashMap<String, String> playerHashMap = getPlayerHashMap();

        final TextView playerNumberTextView = (TextView)playerInfoPlayerRow.findViewById(R.id.columnNumber);
        playerNumberTextView.setText(playerHashMap.get(PlayerPojo.TAG_NUMBER));

        final TextView playerNameTextView = (TextView)playerInfoPlayerRow.findViewById(R.id.columnName);
        playerNameTextView.setText(playerHashMap.get(PlayerPojo.TAG_FORMATTED_NAME));

        final TextView playerPositionTextView = (TextView)playerInfoPlayerRow.findViewById(R.id.columnPosition);
        playerPositionTextView.setText(playerHashMap.get(PlayerPojo.TAG_POSITION));

        final TextView playerStatusTextView = (TextView)playerInfoPlayerRow.findViewById(R.id.columnStatus);
        playerStatusTextView.setText(playerHashMap.get(PlayerPojo.TAG_STATUS));

        final EditText editText = (EditText)rootView.findViewById(R.id.playerPageEditText);
        final Button clearButton = (Button)rootView.findViewById(R.id.playerPageClearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
            }
        });
        final Button submitButton = (Button)rootView.findViewById(R.id.playerPageSubmitButton);
        final TextView playerPageCommentLengthWarning = (TextView)rootView.findViewById(R.id.playerPageCommentLengthWarning);
        watcher(editText, clearButton, submitButton, playerPageCommentLengthWarning);

        return rootView;
    }

    private void watcher(final EditText editText, final Button clearButton, final Button submitButton, final TextView playerPageCommentLengthWarning) {
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

    private void ToggleCommentControls(EditText editText, Button clearButton, Button submitButton, TextView playerPageCommentLengthWarning) {
        int editTextLength = editText.length();
        if (editTextLength == 0) {
            clearButton.setEnabled(false);
            submitButton.setEnabled(false);
            playerPageCommentLengthWarning.setVisibility(View.INVISIBLE);
        }
        else if (Integer.parseInt(getString(R.string.comment_length)) <= editTextLength){
            clearButton.setEnabled(true);
            submitButton.setEnabled(false);
            playerPageCommentLengthWarning.setVisibility(View.VISIBLE);
        }
        else {
            clearButton.setEnabled(true);
            submitButton.setEnabled(true);
            playerPageCommentLengthWarning.setVisibility(View.INVISIBLE);
        }
    }
}
