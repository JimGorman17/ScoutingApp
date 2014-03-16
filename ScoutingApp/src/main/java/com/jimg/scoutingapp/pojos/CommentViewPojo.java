package com.jimg.scoutingapp.pojos;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jim on 3/12/14.
 */
public class CommentViewPojo {

    @SerializedName("CommentId")
    public Integer CommentId;

    @SerializedName("PlayerId")
    public Integer PlayerId;

    @SerializedName("GoogleId")
    public String GoogleId;

    @SerializedName("CommentString")
    public String CommentString;

    @SerializedName("Deleted")
    public Boolean Deleted;

    @SerializedName("UserId")
    public Integer UserId;

    @SerializedName("DisplayName")
    public String DisplayName;

    @SerializedName("Picture")
    public String PictureUrl;
}
