package com.jimg.scoutingapp.pojos;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jim on 2/17/14.
 */
public class TeamPojo {
    private static final String TAG_TEAM_ID = "TeamId";
    private static final String TAG_LOCATION = "Location";
    private static final String TAG_NICKNAME = "Nickname";
    private static final String TAG_CONFERENCE = "Conference";
    private static final String TAG_DIVISION = "Division";

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
