package com.jimg.scoutingapp.repositories;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.jimg.scoutingapp.Constants;
import com.jimg.scoutingapp.helpers.LogHelpers;
import com.jimg.scoutingapp.helpers.UrlHelpers;
import com.jimg.scoutingapp.utilityclasses.Pair;
import com.jimg.scoutingapp.pojos.TeamPojo;
import com.jimg.scoutingapp.pojos.TeamTriplet;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by Jim on 2/9/14.
 */
public class Team {

    private class ClosestTeamResponse {
        @SerializedName("Team")
        TeamPojo team;
    }

    public String getClosestTeamUrl() {
        return Constants.restServiceUrlBase + "Team/GetClosestTeam?Latitude={0}&Longitude={1}&" + Constants.getJson;
    }

    public void getClosestTeam(Messenger messenger, Pair<Double, Double> latitudeLongitudePair) throws Exception {
        LogHelpers.ProcessAndThreadId("Team.getClosestTeam");

        String getClosestTeamUrl = getClosestTeamUrl().replace("{0}", Double.toString(latitudeLongitudePair.first)).replace("{1}", Double.toString(latitudeLongitudePair.second));
        String json = UrlHelpers.readUrl(getClosestTeamUrl);

        if (json == null) {
            throw new JSONException(String.format("Failed to Get JSON from %s.", getClosestTeamUrl));
        }

        Gson gson = new Gson();
        ClosestTeamResponse response = gson.fromJson(json, ClosestTeamResponse.class);

        Bundle data = new Bundle();
        data.putSerializable(Constants.retrievedEntityExtra, response.team);
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
