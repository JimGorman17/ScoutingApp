package com.jimg.scoutingapp;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.TreeMap;

public class Player implements Entity {
    private class Response {
        @SerializedName("Players")
        ArrayList<PlayerPojo> players;
    }

    @Override
    public String getAllUrl() {
        return Constants.restServiceUrlBase + "Player/GetAllByTeamId?TeamId={0}&" + Constants.getJson;
    }

    public void getAllByTeamId(Messenger messenger, int teamId) {
        try {
            LogHelper.ProcessAndThreadId("PlayersByTeamId.getAll");

            String url = getAllUrl().replace("{0}", Integer.toString(teamId));
            String json = UrlHelpers.readUrl(url);

            if (json == null) {
                throw new JSONException("Failed to Get JSON from endpoint.");
            }

            Gson gson = new Gson();
            Response response = gson.fromJson(json, Response.class);

            Bundle data = new Bundle();
            data.putSerializable(Constants.retrievedEntityExtra, response.players);
            Message message = Message.obtain();
            message.setData(data);
            messenger.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("getAll for Players", "Error processing players " + e.toString());
        }
    }

    public static TreeMap<String, PlayerPojo> convertArrayListToTreeMap(ArrayList<PlayerPojo> playerList) {
        TreeMap<String, PlayerPojo> outputTreeMap = new TreeMap<String, PlayerPojo>(String.CASE_INSENSITIVE_ORDER);
        for(int i = 0; i < playerList.size(); i++){
            PlayerPojo player = playerList.get(i);
            outputTreeMap.put(player.lastName + ", " + player.firstName, player);
        }

        return outputTreeMap;
    }
}
