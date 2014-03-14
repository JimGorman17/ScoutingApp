package com.jimg.scoutingapp;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.widget.SimpleAdapter;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        LogHelper.ProcessAndThreadId("CommentsByPlayerId.getAll");

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

    public static SimpleAdapter convertListToSimpleAdapter(Context context, ArrayList<CommentViewPojo> commentViewPojoList) {
        List<HashMap<String, String>> commentHashMap = new ArrayList<HashMap<String, String>>();
        for (CommentViewPojo entry : commentViewPojoList) {
            commentHashMap.add(CommentViewPojo.createCommentMap(entry));
        }
        return new SimpleAdapter(context, commentHashMap, R.layout.comment_list_row, new String[]{CommentViewPojo.TAG_USER_DISPLAY_NAME, CommentViewPojo.TAG_COMMENT_STRING}, new int[]{R.id.columnUserDisplayName, R.id.columnCommentString});
    }
}
