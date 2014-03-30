package com.jimg.scoutingapp.repositories;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.jimg.scoutingapp.Constants;
import com.jimg.scoutingapp.helpers.LogHelpers;
import com.jimg.scoutingapp.helpers.UrlHelpers;
import com.jimg.scoutingapp.pojos.TeamPojo;
import com.jimg.scoutingapp.pojos.TeamTriplet;
import com.jimg.scoutingapp.utilityclasses.Pair;

import org.json.JSONException;

import java.util.ArrayList;
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
        LogHelpers.ProcessAndThreadId("Team.getAll");

        String getAllTeamsUrl = getAllUrl();
        String json = UrlHelpers.readUrl(getAllTeamsUrl);

        if (json == null) {
            throw new JSONException(String.format("Failed to Get JSON from %s.", getAllTeamsUrl));
        }

        Gson gson = new Gson();
        Response response = gson.fromJson(json, Response.class);

        ArrayList<TeamTriplet> results = new ArrayList<TeamTriplet>();
        for (TeamPojo team : response.teams) {
            TeamTriplet teamToReturn = new TeamTriplet(team.teamId, team.location + " " + team.nickname, team.conference + " " + team.division);
            results.add(teamToReturn);
        }

        Bundle data = new Bundle();
        data.putSerializable(Constants.retrievedEntityExtra, results);
        Message message = Message.obtain();
        message.setData(data);
        messenger.send(message);
    }

    public static TreeMap<String, ArrayList<Pair<Integer, String>>> convertRawLeagueToDivisions(ArrayList<TeamTriplet> inputTeams) {
        TreeMap<String, ArrayList<Pair<Integer, String>>> outputTreeMap = new TreeMap<String, ArrayList<Pair<Integer, String>>>(String.CASE_INSENSITIVE_ORDER);
        for (TeamTriplet team : inputTeams) {
            String key = team.division;
            if (outputTreeMap.get(key) == null) {
                outputTreeMap.put(key, new ArrayList<Pair<Integer, String>>());
            }

            Pair<Integer, String> teamToReturn = new Pair<Integer, String>(team.id, team.name);
            outputTreeMap.get(key).add(teamToReturn);
        }

        return outputTreeMap;
    }

    public static TreeMap<Integer, String> convertRawLeagueToTeamTreeMap(ArrayList<TeamTriplet> inputTeams) {
        TreeMap<Integer, String> outputTreeMap = new TreeMap<Integer, String>();
        for (TeamTriplet team : inputTeams) {
            outputTreeMap.put(team.id, team.name);
        }

        return outputTreeMap;
    }
}
