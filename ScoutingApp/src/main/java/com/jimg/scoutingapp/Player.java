package com.jimg.scoutingapp;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;

import java.util.List;

public class Player implements Entity {
    private class Response {
        @SerializedName("Players")
        List<PlayerPojo> players;
    }

    @Override
    public String getAllUrl() {
        return Constants.restServiceUrlBase + "Team/GetAllByTeamId?TeamId={0}" + Constants.getJson;
    }

    @Override
    public void getAll(Messenger messenger) {
        try {
            LogHelper.ProcessAndThreadId("Player.getAll");
            String json = UrlHelpers.readUrl(getAllUrl());

            if (json == null) {
                throw new JSONException("Failed to Get JSON from endpoint.");
            }

            Gson gson = new Gson();
            Response response = gson.fromJson(json, Response.class);

            /*
            Bundle data = new Bundle();
            data.putSerializable(Constants.retrievedEntityExtra, response.players);
            Message message = Message.obtain();
            message.setData(data);
            messenger.send(message);
            */
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("getAll for Players", "Error processing players " + e.toString());
        }
    }
}
