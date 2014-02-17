package com.jimg.scoutingapp;

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
    Integer teamId;

    @SerializedName("Location")
    String location;

    @SerializedName("Nickname")
    String nickname;

    @SerializedName("Conference")
    String conference;

    @SerializedName("Division")
    String division;
}
