package com.jimg.scoutingapp;

/**
 * Created by Jim on 2/9/14.
 */
public class Constants {
    public static final String restServiceUrlBase = "https://www.scoutingapp.net/Scouting.RestService/api/";
    public static final String getJson = "format=json";

    public static final int FLAGGED_COMMENTS_REPORT_ID = 101;
    public static final String FLAGGED_COMMENTS_REPORT_TITLE = "Flagged Comments";

    public static final int ALL_TEAMS_REPORT_ID = 102;
    public static final String ALL_TEAMS_REPORT_TITLE = "All Teams";

    public static final int TOP_USERS_REPORT_ID = 103;
    public static final String TOP_USERS_REPORT_TITLE = "Top Users";

    public static final String teamIdExtra = "TeamId";
    public static final String titleExtra = "Title";
    public static final String playerHashMapExtra = "PlayerHashMap";

    public static final String commentIdExtra = "CommentId";
    public static final String authTokenExtra = "AuthToken";
    public static final String playerIdExtra = "PlayerId";
    public static final String commentExtra = "CommentString";
    public static final String deleteExtra = "Delete";
    public static final String flaggedExtra = "Flagged";
    public static final String handleFlagsExtra = "HandleFlags";

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
}
