package com.jimg.scoutingapp.pojos;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jim on 4/15/2014.
 */
public class TopUserPojo {

    @SerializedName("PictureUrl")
    public String pictureUrl;

    @SerializedName("UserInfo")
    public String userInfo;

    @SerializedName("Count")
    public Integer commentCount;

}
