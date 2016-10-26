package com.di7ak.spaces.forum.api;

import org.json.JSONException;
import org.json.JSONObject;

public class JournalRecord {
    public String link;
    public String text;
    public String date;
    public String commentUserName;
    public int answer;
    public int commentsCnt;
    public int eventId;
    
    public static JournalRecord fromJson(JSONObject json) throws SpacesException {
        JournalRecord record = new JournalRecord();
        try {
            if(json.has("link")) record.link = json.getString("link");
            if(json.has("text")) record.text = json.getString("text");
            if(json.has("cool_date")) record.date = json.getString("cool_date");
            if(json.has("comment_user_name")) record.commentUserName = json.getString("comment_user_name");
            if(json.has("answer")) record.answer = json.getInt("answer");
            if(json.has("comments_cnt")) record.commentsCnt = json.getInt("comments_cnt");
            if(json.has("event_id")) record.answer = json.getInt("event_id");
        } catch(JSONException e) {
            throw new SpacesException(-2);
        }
        return record;
    }
}
