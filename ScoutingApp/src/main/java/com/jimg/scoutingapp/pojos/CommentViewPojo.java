package com.jimg.scoutingapp.pojos;

import android.content.Context;
import android.widget.SimpleAdapter;

import com.google.gson.annotations.SerializedName;
import com.jimg.scoutingapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jim on 3/12/14.
 */
public class CommentViewPojo {
    public static final String TAG_COMMENT_ID = "CommentId";
    public static final String TAG_PLAYER_ID = "PlayerId";
    public static final String TAG_GOOGLE_ID = "GoogleId";
    public static final String TAG_COMMENT_STRING = "CommentString";
    public static final String TAG_DELETED = "Deleted";

    public static final String TAG_USER_ID = "UserId";
    public static final String TAG_USER_DISPLAY_NAME = "DisplayName";
    public static final String TAG_USER_PICTURE_URL = "Picture";

    @SerializedName("CommentId")
    Integer commentId;

    @SerializedName("PlayerId")
    Integer playerId;

    @SerializedName("GoogleId")
    String googleId;

    @SerializedName("CommentString")
    String commentString;

    @SerializedName("Deleted")
    Boolean deleted;

    @SerializedName("UserId")
    Integer userId;

    @SerializedName("DisplayName")
    String displayName;

    @SerializedName("Picture")
    String pictureUrl;

    public static HashMap<String, String> createCommentMap(CommentViewPojo commentViewPojo) {
        HashMap<String, String> comment = new HashMap<String, String>();

        comment.put(TAG_COMMENT_ID, commentViewPojo.commentId.toString());
        comment.put(TAG_PLAYER_ID, commentViewPojo.playerId.toString());
        comment.put(TAG_GOOGLE_ID, commentViewPojo.googleId);
        comment.put(TAG_COMMENT_STRING, commentViewPojo.commentString);
        comment.put(TAG_DELETED, commentViewPojo.deleted.toString());
        comment.put(TAG_USER_ID, commentViewPojo.userId.toString());
        comment.put(TAG_USER_DISPLAY_NAME, commentViewPojo.displayName);
        comment.put(TAG_USER_PICTURE_URL, commentViewPojo.pictureUrl);

        return comment;
    }

    public static SimpleAdapter convertListToSimpleAdapter(Context context, ArrayList<CommentViewPojo> commentViewPojoList) {
        List<HashMap<String, String>> commentHashMap = new ArrayList<HashMap<String, String>>();
        for (CommentViewPojo entry : commentViewPojoList) {
            commentHashMap.add(createCommentMap(entry));
        }
        return new SimpleAdapter(context, commentHashMap, R.layout.comment_list_row, new String[]{TAG_USER_DISPLAY_NAME, TAG_COMMENT_STRING}, new int[]{R.id.columnUserDisplayName, R.id.columnCommentString});
    }
}
