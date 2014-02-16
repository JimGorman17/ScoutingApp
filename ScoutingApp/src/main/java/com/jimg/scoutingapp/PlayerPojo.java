package com.jimg.scoutingapp;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jim on 2/16/14.
 */
public class PlayerPojo {
    @SerializedName("PlayerId")
    Integer playerId;

    @SerializedName("Position")
    String position;

    @SerializedName("Number")
    String number;

    @SerializedName("FirstName")
    String firstName;

    @SerializedName("LastName")
    String lastName;

    @SerializedName("Status")
    String status;

    @SerializedName("TeamAbbreviation")
    String abbreviation;
}
