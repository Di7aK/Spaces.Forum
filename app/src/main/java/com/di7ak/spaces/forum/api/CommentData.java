package com.di7ak.spaces.forum.api;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    public String id;
    public UserData user;
    public boolean userBlocked;
    public boolean userFemale;
    public VotingData voting;
    public List<AttachData> attaches;
    public JSONArray fileAttachments;
    
    public static CommentData fromJson(JSONObject json) throws SpacesException {
        CommentData result = new CommentData();
        result.attaches = new ArrayList<AttachData>();
        try {
            if(json.has("attaches_widgets") && !json.isNull("attaches_widgets")) {
                JSONObject attaches = json.getJSONObject("attaches_widgets");
                if(attaches.has("tile_items")) {
                    JSONArray items = attaches.getJSONArray("tile_items");
                    for(int i = 0; i < items.length(); i ++) {
                        result.attaches.add(AttachData.fromJson(items.getJSONObject(i)));
                    }
                }
                
                if (attaches.has("list_items")) {
                    result.fileAttachments = attaches.getJSONArray("list_items");
                    
                }
            }
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
            if(json.has("comment_id")) result.id = json.getString("comment_id");
        } catch(JSONException e ) {
            android.util.Log.e("lol", "comment: " + e.toString(), e);
            throw new SpacesException(-2);
        }
        return result;
    }
}
