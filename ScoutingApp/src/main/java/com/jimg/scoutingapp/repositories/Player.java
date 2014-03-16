package com.jimg.scoutingapp.repositories;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.jimg.scoutingapp.Constants;
import com.jimg.scoutingapp.helpers.LogHelpers;
import com.jimg.scoutingapp.helpers.UrlHelpers;
import com.jimg.scoutingapp.pojos.PlayerPojo;

import org.json.JSONException;

import java.util.ArrayList;

public class Player {
    private class Response {
        @SerializedName("Players")
        ArrayList<PlayerPojo> players;
    }

    public String getAllByTeamIdUrl() {
        return Constants.restServiceUrlBase + "Player/GetAllByTeamId?TeamId={0}&" + Constants.getJson;
    }

    public void getAllByTeamId(Messenger messenger, int teamId) throws Exception {
        LogHelpers.ProcessAndThreadId("PlayersByTeamId.getAll");

        String url = getAllByTeamIdUrl().replace("{0}", Integer.toString(teamId));
        String json = UrlHelpers.readUrl(url);

        if (json == null) {
            throw new JSONException(String.format("Failed to Get JSON from %s.", url));
        }

        Gson gson = new Gson();
        Response response = gson.fromJson(json, Response.class);

        Bundle data = new Bundle();
        data.putSerializable(Constants.retrievedEntityExtra, response.players);
        Message message = Message.obtain();
        message.setData(data);
        messenger.send(message);
    }

}
