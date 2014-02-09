package com.jimg.scoutingapp;

/**
 * Created by Jim on 2/9/14.
 */
public class Constants {
    // public static final String restServiceUrlBase = "http://ip.jsontest.com/"; Just for a sanity check.
    public static final String restServiceUrlBase = "http://192.168.1.5/Scouting.RestService/api/"; // TODO: The host should probably be a configuration setting, not a hard coded string.
    public static final String getJson = "?format=json";

    public static final String entityToRetrieveExtra = "entity";
    public static final String retrievedEntityExtra = "entityResults";
    public static final String messengerExtra = "messenger";

    public enum Entities {
        Team
    }
}
