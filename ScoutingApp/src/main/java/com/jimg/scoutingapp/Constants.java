package com.jimg.scoutingapp;

/**
 * Created by Jim on 2/9/14.
 */
public class Constants {
    // public static final String restServiceUrlBase = "http://ip.jsontest.com/"; Just for a sanity check.
    public static final String restServiceUrlBase = "http://scoutingapp.zapto.org/Scouting.RestService/api/";
    public static final String getJson = "format=json";

    public static final int FAVORITE_TEAM_REPORT_ID = 101;
    public static final String FAVORITE_TEAM_REPORT_TITLE = "Favorite Team";

    public static final int ALL_TEAMS_REPORT_ID = 102;
    public static final String ALL_TEAMS_REPORT_TITLE = "All Teams";

    public static final int ALL_USERS_REPORT_ID = 103;
    public static final String ALL_USERS_REPORT_TITLE = "All Users";

    public static final int MY_STATS_REPORT_ID = 104;
    public static final String MY_STATS_REPORT_TITLE = "My Stats";

    public static final int FLAGGED_COMMENTS_REPORT_ID = 105;
    public static final String FLAGGED_COMMENTS_REPORT_TITLE = "Flagged Comments";

    //region Extras
    public static final String entityToRetrieveExtra = "entity";
    public static final String retrievedEntityExtra = "entityResults";
    public static final String messengerExtra = "messenger";

    public static final String teamIdExtra = "TeamId";
    public static final String titleExtra = "Title";
    public static final String playerHashMapExtra = "PlayerHashMap";

    public static final String commentIdExtra = "CommentId";
    public static final String authTokenExtra = "AuthToken";
    public static final String playerIdExtra = "PlayerId";
    public static final String commentExtra = "CommentString";
    public static final String deleteExtra = "Delete";
    public static final String flaggedExtra = "Flagged";

    public static final String latitudeLongitudeExtra = "LatitudeLongitude";

    public static final String applicationExtra = "Application";
    public static final String phoneIdExtra = "PhoneId";
    public static final String errorMessageExtra = "ErrorMessage";
    public static final String stackTraceExtra = "StackTrace";
    //endregion

    public enum SignInStatus {
        SignedOut(0), SignedIn(1);

        private final int value;

        private SignInStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Entities {
        PlayersByTeamId, GetClosestTeam
    }
}
