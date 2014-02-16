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
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class MainActivity extends ActionBarActivity {
    ProgressDialog pd;
    private Menu mMenu = null;
    private Handler mMenuHandler = null;
    private static final String TEAM_ID_EXTRA = "TeamId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMenuHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle reply = msg.getData();
                ArrayList<Tuple<Integer, String, String>> rawLeague = (ArrayList<Tuple<Integer, String, String>>)reply.get(Constants.retrievedEntityExtra);
                TreeMap<String, List<Pair<Integer, String>>> league = Team.convertTupleListToTreeMap(rawLeague);
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
                    .add(R.id.container, new PlaceholderFragment())
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

        pd=ProgressDialog.show(MainActivity.this,"","Please Wait",false);
        Intent serviceIntent = new Intent(this, OnDemandJsonFetchWorker.class);
        serviceIntent.putExtra(Constants.entityToRetrieveExtra, Constants.Entities.Team);
        serviceIntent.putExtra(Constants.messengerExtra, new Messenger(mMenuHandler));
        startService(serviceIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemId = item.getItemId();

        if (0 < itemId) {
            ReplaceFragment(itemId);
        }

        if (itemId == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ReplaceFragment(int teamId) {
        FragmentManager fm = getFragmentManager();
        TeamFragment teamFragment = TeamFragment.newInstance(teamId);

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, teamFragment);
        ft.setTransition(
                FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
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
            teamPageTitleTextView.setText(Integer.toString(getTeamId()));

            return rootView;
        }
    }
}
