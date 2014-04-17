package com.jimg.scoutingapp.pojos;

import android.content.Context;
import android.widget.SimpleAdapter;

import com.google.gson.annotations.SerializedName;
import com.jimg.scoutingapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jim on 4/15/2014.
 */
public class TeamCommentPojo {
    public static final String TAG_TEAM_ID = "TeamId";
    public static final String TAG_TEAM = "Team";
    public static final String TAG_COUNT = "Count";
    public static final String TAG_LAST_POST_DATE = "LastPostDate";

    @SerializedName("TeamId")
    public Integer TeamId;

    @SerializedName("Team")
    public String Team;

    @SerializedName("Count")
    public Integer Count;

    @SerializedName("LastPostDateString")
    public String LastPostDateString;

    public static SimpleAdapter convertArrayListToSimpleAdapter(Context context, ArrayList<TeamCommentPojo> teamCommentPojos) {
        List<HashMap<String, String>> teamCommentHashMap = new ArrayList<HashMap<String, String>>();
        for (TeamCommentPojo teamCommentPojo : teamCommentPojos) {
            teamCommentHashMap.add(createTeamCommentMap(teamCommentPojo));
        }
        return new SimpleAdapter(context, teamCommentHashMap, R.layout.all_teams_list_row, new String[]{TAG_TEAM, TAG_COUNT, TAG_LAST_POST_DATE}, new int[]{R.id.columnTeamName, R.id.columnCommentCount, R.id.columnLastPostDate});
    }

    private static HashMap<String, String> createTeamCommentMap(TeamCommentPojo teamCommentPojo) {
        HashMap<String, String> teamComment = new HashMap<String, String>();

        teamComment.put(TAG_TEAM_ID, Integer.toString(teamCommentPojo.TeamId));
        teamComment.put(TAG_TEAM, teamCommentPojo.Team);
        teamComment.put(TAG_COUNT, Integer.toString(teamCommentPojo.Count));
        teamComment.put(TAG_LAST_POST_DATE, teamCommentPojo.LastPostDateString);

        return teamComment;
    }
}
