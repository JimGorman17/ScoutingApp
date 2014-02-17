package com.jimg.scoutingapp;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.SimpleAdapter;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Player {
    private class Response {
        @SerializedName("Players")
        ArrayList<PlayerPojo> players;
    }

    public String getAllByTeamUrl() {
        return Constants.restServiceUrlBase + "Player/GetAllByTeamId?TeamId={0}&" + Constants.getJson;
    }

    public void getAllByTeamId(Messenger messenger, int teamId) {
        try {
            LogHelper.ProcessAndThreadId("PlayersByTeamId.getAll");

            String url = getAllByTeamUrl().replace("{0}", Integer.toString(teamId));
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
        for (PlayerPojo player : playerList) {
            outputTreeMap.put(player.lastName + ", " + player.firstName, player);
        }

        return outputTreeMap;
    }

    public static SimpleAdapter convertTreeMapToSimpleAdapter(Context context, TreeMap<String, PlayerPojo> playerTreeMap) {
        List<HashMap<String, String>> playerHashMap = new ArrayList<HashMap<String, String>>();
        for (Map.Entry<String, PlayerPojo> entry : playerTreeMap.entrySet()) {
            playerHashMap.add(PlayerPojo.createPlayerMap(entry.getValue()));
        }
        return new SimpleAdapter(context, playerHashMap, R.layout.player_list_row, new String[]{PlayerPojo.TAG_NUMBER, PlayerPojo.TAG_FORMATTED_NAME, PlayerPojo.TAG_POSITION, PlayerPojo.TAG_STATUS}, new int[]{R.id.columnNumber, R.id.columnName, R.id.columnPosition, R.id.columnStatus} );
    }
}
