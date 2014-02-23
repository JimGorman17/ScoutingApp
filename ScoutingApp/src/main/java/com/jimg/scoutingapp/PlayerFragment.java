package com.jimg.scoutingapp;



import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;

public class PlayerFragment extends Fragment {
    private MainActivity mMainActivity;

    private static final String TITLE_TAG = "Title";
    private String mTitle;

    public PlayerFragment() {
        // Required empty public constructor
    }

    public static PlayerFragment newInstance(HashMap<String, String> playerHashMap) {
        PlayerFragment playerFragment = new PlayerFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.playerHashMapExtra, playerHashMap);
        playerFragment.setArguments(bundle);

        return playerFragment;
    }

    private HashMap<String, String> getPlayerHashMap() {
        return (HashMap<String, String>)getArguments().getSerializable(Constants.playerHashMapExtra);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainActivity = (MainActivity)getActivity();
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

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

        return rootView;
    }
}
