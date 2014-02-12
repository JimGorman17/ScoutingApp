package com.jimg.scoutingapp;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jim on 2/9/14.
 */
public class Team implements Entity {
    private static final String TAG_TEAMS = "Teams";
    private static final String TAG_LOCATION = "Location";
    private static final String TAG_NICKNAME = "Nickname";
    private static final String TAG_ABBREVIATION = "Abbreviation";
    private static final String TAG_CONFERENCE = "Conference";
    private static final String TAG_DIVISION = "Division";

    @Override
    public String getAllUrl() {
        return Constants.restServiceUrlBase + "Team/GetAll" + Constants.getJson;
    }

    @Override
    public void getAll(Messenger messenger) {
        try {
            JSONParser jParser = new JSONParser();
            LogHelper.ProcessAndThreadId("Team.getAll");
            JSONObject json = jParser.getJSONFromUrl(getAllUrl());

            if (json == null) {
                throw new JSONException("Failed to Get JSON from endpoint.");
            }

            JSONArray teams = null;
            teams = json.getJSONArray(TAG_TEAMS);

            ArrayList<Tuple<String, String, String>> results = new ArrayList<Tuple<String, String, String>>();
            for(int i = 0; i < teams.length(); i++){
                JSONObject teamFromJson = teams.getJSONObject(i);

                String location = teamFromJson.getString(TAG_LOCATION);
                String nickname = teamFromJson.getString(TAG_NICKNAME);
                String abbreviation = teamFromJson.getString(TAG_ABBREVIATION);
                String conference = teamFromJson.getString(TAG_CONFERENCE);
                String division = teamFromJson.getString(TAG_DIVISION);

                Tuple<String, String, String> teamToReturn = new Tuple<String, String, String>(abbreviation, location + " " + nickname, conference + " " + division);
                results.add(teamToReturn);
            }
            Bundle data = new Bundle();
            data.putSerializable(Constants.retrievedEntityExtra, results);
            Message message = Message.obtain();
            message.setData(data);
            try {
                messenger.send(message);
            } catch (RemoteException e) {
                Log.e("getAll for Teams", "Error processing teams " + e.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("getAll for Teams", "Error processing teams " + e.toString());
        }
    }

    public static Map<String, List<Pair<String, String>>> convertTupleListToMap(ArrayList<Tuple<String, String, String>> inputTeams) {
        Map<String, List<Pair<String, String>>> outputTeams = new HashMap<String, List<Pair<String, String>>>();
        for(int i = 0; i < inputTeams.size(); i++){
            String key = inputTeams.get(i).z;
            if (outputTeams.get(key) == null) {
                outputTeams.put(key, new ArrayList<Pair<String, String>>());
            }

            Pair<String, String> teamToReturn = new Pair<String, String>(inputTeams.get(i).x, inputTeams.get(i).y);
            outputTeams.get(key).add(teamToReturn);
        }

        return outputTeams;
    }
}
