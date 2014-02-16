package com.jimg.scoutingapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class MainActivity extends ActionBarActivity {
    ProgressDialog pd;
    private Menu mMenu = null;
    private Handler mMenuHandler = null;
    private static final String TEAM_ID_EXTRA = "TeamId";
    private TreeMap<Integer, String> mTeamTreeMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mMenuHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle reply = msg.getData();
                ArrayList<Tuple<Integer, String, String>> rawLeague = (ArrayList<Tuple<Integer, String, String>>)reply.get(Constants.retrievedEntityExtra);
                mTeamTreeMap = Team.convertRawLeagueToTeamTreeMap(rawLeague);
                TreeMap<String, List<Pair<Integer, String>>> league = Team.convertRawLeagueToDivisions(rawLeague);
                Integer i = 0, j = Menu.FIRST;
                for (String key : league.keySet()) {
                    SubMenu subMenu = mMenu.addSubMenu(key);
                    for (Pair<Integer, String> team : league.get(key)) {
                        subMenu.add(i, team.first, j, team.second);
                        j++;
                    }
                    i++;
                }
                pd.dismiss();
            }
        };

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new HomeScreenFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenu();
        return true;
    }

    private void getMenu() {
        LogHelper.ProcessAndThreadId("MainActivity.getMenu");

        pd=ProgressDialog.show(MainActivity.this,"",getString(R.string.please_wait_message),false);
        Intent serviceIntent = new Intent(this, OnDemandJsonFetchWorker.class);
        serviceIntent.putExtra(Constants.entityToRetrieveExtra, Constants.Entities.Team);
        serviceIntent.putExtra(Constants.messengerExtra, new Messenger(mMenuHandler));
        startService(serviceIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        ReplaceFragment(itemId);
        return super.onOptionsItemSelected(item);
    }

    private void ReplaceFragment(int itemId) {
        if (itemId <= 0) {
            return;
        }

        FragmentManager fm = getFragmentManager();
        Fragment fragment;

        fragment = itemId == android.R.id.home ? new HomeScreenFragment() : TeamFragment.newInstance(itemId);

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, fragment);
        ft.setTransition(
                FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    public static class HomeScreenFragment extends Fragment {

        public HomeScreenFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    public static class TeamFragment extends Fragment {

        public TeamFragment() {
        }

        public static TeamFragment newInstance(int teamId) {
            TeamFragment teamFragment = new TeamFragment();

            Bundle bundle = new Bundle();
            bundle.putInt(TEAM_ID_EXTRA, teamId);
            teamFragment.setArguments(bundle);

            return teamFragment;
        }
        public int getTeamId() {
            return getArguments().getInt(TEAM_ID_EXTRA, 0);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_team, container, false);

            final TextView teamPageTitleTextView = (TextView)rootView.findViewById(R.id.teamPageTitleTextView);
            teamPageTitleTextView.setText(((MainActivity)getActivity()).mTeamTreeMap.get(getTeamId()));

            return rootView;
        }
    }
}
