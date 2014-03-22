package com.jimg.scoutingapp.pojos;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jim on 2/17/14.
 */
public class TeamPojo {
    @SerializedName("TeamId")
    public Integer teamId;

    @SerializedName("Location")
    public String location;

    @SerializedName("Nickname")
    public String nickname;

    @SerializedName("Conference")
    public String conference;

    @SerializedName("Division")
    public String division;
}
