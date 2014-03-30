package com.jimg.scoutingapp.pojos;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Jim on 2/17/14.
 */
public class TeamPojo implements Serializable {
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
