package com.jimg.scoutingapp.repositories;

import com.jimg.scoutingapp.pojos.TeamTriplet;
import com.jimg.scoutingapp.utilityclasses.Pair;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by Jim on 2/9/14.
 */
public class Team {

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
