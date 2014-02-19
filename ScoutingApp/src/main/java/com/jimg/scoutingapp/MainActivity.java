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
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class MainActivity extends ActionBarActivity {
    public ProgressDialog mProgressDialog;
    private Menu mMenu;
    private Handler mMenuHandler;

    private static final String TEAM_NAMES_TAG = "TeamNames";
    private static final String PLAYER_TREEMAP_TAG = "PlayerTreeMap";
    private static final String MENU_TAG = "Menu";

    public TreeMap<Integer, String> mTeamNamesTreeMap;
    public TreeMap<Integer, TreeMap<String, PlayerPojo>> mPlayerTreeMap;
    private TreeMap<String,List<Pair<Integer,String>>> mTeamTreeMapForMenu;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(TEAM_NAMES_TAG, mTeamNamesTreeMap);
        outState.putSerializable(PLAYER_TREEMAP_TAG, mPlayerTreeMap);
        outState.putSerializable(MENU_TAG, mTeamTreeMapForMenu);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTeamNamesTreeMap = (TreeMap<Integer, String>)savedInstanceState.getSerializable(TEAM_NAMES_TAG);
        mPlayerTreeMap = (TreeMap<Integer, TreeMap<String, PlayerPojo>>)savedInstanceState.getSerializable(PLAYER_TREEMAP_TAG);
        mTeamTreeMapForMenu = (TreeMap<String,List<Pair<Integer,String>>>)savedInstanceState.getSerializable(MENU_TAG);
    }

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
                ArrayList<Triplet<Integer, String, String>> rawLeague = (ArrayList<Triplet<Integer, String, String>>)reply.get(Constants.retrievedEntityExtra);
                mTeamNamesTreeMap = Team.convertRawLeagueToTeamTreeMap(rawLeague);
                mPlayerTreeMap = new TreeMap<Integer, TreeMap<String, PlayerPojo>>();
                mTeamTreeMapForMenu = Team.convertRawLeagueToDivisions(rawLeague);
                PopulateMenu();
                mProgressDialog.dismiss();
            }
        };

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new HomeScreenFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (1 < fm.getBackStackEntryCount()) {
            fm.popBackStackImmediate();
        }
        else {
            super.onBackPressed();
        }
    }

    private void PopulateMenu() {
        Integer i = 0, j = Menu.FIRST;
        for (String key : mTeamTreeMapForMenu.keySet()) {
            SubMenu subMenu = mMenu.addSubMenu(key);
            for (Pair<Integer, String> team : mTeamTreeMapForMenu.get(key)) {
                subMenu.add(i, team.first, j, team.second);
                j++;
            }
            i++;
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

        if (mTeamTreeMapForMenu == null) {
            mProgressDialog = ProgressDialog.show(MainActivity.this,"",getString(R.string.please_wait_message),false);
            Intent serviceIntent = new Intent(this, OnDemandJsonFetchWorker.class);
            serviceIntent.putExtra(Constants.entityToRetrieveExtra, Constants.Entities.Team);
            serviceIntent.putExtra(Constants.messengerExtra, new Messenger(mMenuHandler));
            startService(serviceIntent);
        }
        else {
            PopulateMenu();
        }
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

        fm.beginTransaction()
                .replace(R.id.container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null)
                .commit();
    }
}
