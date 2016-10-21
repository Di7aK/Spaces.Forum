package com.di7ak.spaces.forum.api;

import org.json.JSONObject;
import org.json.JSONException;

public class CommentData {
    public boolean admin;
    public boolean hidden;
    public AttachData avatar;
    public String commentId;
    public int commentType;
    public String date;
    public String hiddenByUser;
    public boolean hiddenByVoting;
    public String replyCommentId;
    public String replyCommentText;
    public String replyUserName;
    public String text;
    public UserData user;
    public boolean userBlocked;
    public boolean userFemale;
    public VotingData voting;
    
    public static CommentData fromJson(JSONObject json) throws SpacesException {
        CommentData result = new CommentData();
        try {
            if(json.has("admin")) {
                result.admin = json.getInt("admin") == 1;
            }
            if(json.has("avatar") && !json.isNull("avatar")) {
                result.avatar = AttachData.fromJson(json.getJSONObject("avatar"));
            }
            if(json.has("comment_id")) result.commentId = json.getString("comment_id");
            if(json.has("comment_type")) result.commentType = json.getInt("comment_type");
            if(json.has("date")) result.date = json.getString("date");
            if(json.has("hidden")) {
                result.hidden = json.getInt("hidden") == 1;
            }
            if(json.has("hidden_by_user_name")) result.hiddenByUser = json.getString("hidden_by_user_name");
            if(json.has("hidden_by_voting")) {
                result.hiddenByVoting = json.getInt("hidden_by_voting") == 1;
            }
            if(json.has("reply_comment_id")) result.replyCommentId = json.getString("reply_comment_id");
            if(json.has("reply_comment_text")) result.replyCommentText = json.getString("reply_comment_text");
            if(json.has("reply_user_name")) result.replyUserName = json.getString("reply_user_name");
            if(json.has("text")) result.text = json.getString("text");
            if(json.has("user")) {
                result.user = UserData.fromJson(json.getJSONObject("user"));
            }
            if(json.has("user_blocked")) {
                result.userBlocked = json.getInt("user_blocked") == 1;
            }
            if(json.has("user_female")) {
                result.userFemale = json.getInt("user_female") == 1;
            }
            if(json.has("voting")) {
                result.voting = VotingData.fromJson(json.getJSONObject("voting"));
            }
        } catch(JSONException e ) {
            android.util.Log.e("lol", "comment: " + e.toString(), e);
            throw new SpacesException(-2);
        }
        return result;
    }
}
