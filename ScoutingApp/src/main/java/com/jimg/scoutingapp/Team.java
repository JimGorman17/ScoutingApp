package com.jimg.scoutingapp;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by Jim on 2/9/14.
 */
public class Team {
    private class Response {
        @SerializedName("Teams")
        ArrayList<TeamPojo> teams;
    }

    public String getAllUrl() {
        return Constants.restServiceUrlBase + "Team/GetAll?" + Constants.getJson;
    }

    public void getAll(Messenger messenger) throws Exception {
        LogHelper.ProcessAndThreadId("Team.getAll");

        String getAllTeamsUrl = getAllUrl();
        String json = UrlHelpers.readUrl(getAllTeamsUrl);

        if (json == null) {
            throw new JSONException(String.format("Failed to Get JSON from %s.", getAllTeamsUrl));
        }

        Gson gson = new Gson();
        Response response = gson.fromJson(json, Response.class);

        ArrayList<Triplet<Integer, String, String>> results = new ArrayList<Triplet<Integer, String, String>>();
        for (TeamPojo team : response.teams) {
            Triplet<Integer, String, String> teamToReturn = new Triplet<Integer, String, String>(team.teamId, team.location + " " + team.nickname, team.conference + " " + team.division);
            results.add(teamToReturn);
        }

        Bundle data = new Bundle();
        data.putSerializable(Constants.retrievedEntityExtra, results);
        Message message = Message.obtain();
        message.setData(data);
        messenger.send(message);
    }

    public static TreeMap<String, List<Pair<Integer, String>>> convertRawLeagueToDivisions(ArrayList<Triplet<Integer, String, String>> inputTeams) {
        TreeMap<String, List<Pair<Integer, String>>> outputTreeMap = new TreeMap<String, List<Pair<Integer, String>>>(String.CASE_INSENSITIVE_ORDER);
        for (Triplet<Integer, String, String> team : inputTeams) {
            String key = team.z;
            if (outputTreeMap.get(key) == null) {
                outputTreeMap.put(key, new ArrayList<Pair<Integer, String>>());
            }

            Pair<Integer, String> teamToReturn = new Pair<Integer, String>(team.x, team.y);
            outputTreeMap.get(key).add(teamToReturn);
        }

        return outputTreeMap;
    }

    public static TreeMap<Integer, String> convertRawLeagueToTeamTreeMap(ArrayList<Triplet<Integer, String, String>> inputTeams) {
        TreeMap<Integer, String> outputTreeMap = new TreeMap<Integer, String>();
        for (Triplet<Integer, String, String> team : inputTeams) {
            outputTreeMap.put(team.x, team.y);
        }

        return outputTreeMap;
    }
}
