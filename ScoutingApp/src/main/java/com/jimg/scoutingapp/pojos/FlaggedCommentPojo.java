package com.jimg.scoutingapp.pojos;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jim on 4/13/2014.
 */
public class FlaggedCommentPojo {

    @SerializedName("CommentId")
    public Integer commentId;

    @SerializedName("Comment")
    public String comment;

    @SerializedName("FormattedComment")
    public String formattedComment;

    @SerializedName("NumberOfFlags")
    public Integer numberOfFlags;

}
