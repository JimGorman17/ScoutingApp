package com.jimg.scoutingapp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Jim on 2/16/14.
 */
public class PlayerPojo implements Serializable {
    public static final String TAG_PLAYER_ID = "PlayerId";
    public static final String TAG_POSITION = "Position";
    public static final String TAG_NUMBER = "Number";
    public static final String TAG_FIRST_NAME = "FirstName";
    public static final String TAG_LAST_NAME = "LastName";
    public static final String TAG_FORMATTED_NAME = "FormattedName";
    public static final String TAG_STATUS = "Status";
    public static final String TAG_TEAM_ABBREVIATION = "TeamAbbreviation";

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

    String formattedName() {
        if (firstName.length() == 0) {
            return lastName;
        }
        return 0 < lastName.length() ? lastName + ", " + firstName : firstName;
    }

    public static HashMap<String, String> createPlayerMap(PlayerPojo playerPojo) {
        HashMap<String, String> player = new HashMap<String, String>();

        player.put(TAG_PLAYER_ID,  Integer.toString(playerPojo.playerId));
        player.put(TAG_POSITION, playerPojo.position);
        player.put(TAG_NUMBER, playerPojo.number);
        player.put(TAG_FIRST_NAME, playerPojo.firstName);
        player.put(TAG_LAST_NAME, playerPojo.lastName);
        player.put(TAG_FORMATTED_NAME, playerPojo.formattedName());
        player.put(TAG_STATUS, playerPojo.status);
        player.put(TAG_TEAM_ABBREVIATION, playerPojo.abbreviation);

        return player;
    }
}
