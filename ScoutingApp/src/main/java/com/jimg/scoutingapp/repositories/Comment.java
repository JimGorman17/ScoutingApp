package com.jimg.scoutingapp.repositories;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.jimg.scoutingapp.helpers.LogHelpers;
import com.jimg.scoutingapp.pojos.CommentViewPojo;
import com.jimg.scoutingapp.Constants;
import com.jimg.scoutingapp.helpers.UrlHelpers;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by Jim on 3/12/14.
 */
public class Comment {
    private class Response {
        @SerializedName("Comments")
        ArrayList<CommentViewPojo> comments;
    }

    public String getAllByPlayerIdUrl() {
        return Constants.restServiceUrlBase + "Comment/GetAllByPlayerId?PlayerId={0}&" + Constants.getJson;
    }

    public void getAllByPlayerId(Messenger messenger, int playerId) throws Exception {
        LogHelpers.ProcessAndThreadId("CommentsByPlayerId.getAll");

        String url = getAllByPlayerIdUrl().replace("{0}", Integer.toString(playerId));
        String json = UrlHelpers.readUrl(url);

        if (json == null) {
            throw new JSONException(String.format("Failed to Get JSON from %s.", url));
        }

        Gson gson = new Gson();
        Response response = gson.fromJson(json, Response.class);

        Bundle data = new Bundle();
        data.putSerializable(Constants.retrievedEntityExtra, response.comments);
        Message message = Message.obtain();
        message.setData(data);
        messenger.send(message);
    }

}
