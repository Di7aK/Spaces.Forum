package com.di7ak.spaces.forum.api;

import org.json.JSONException;
import org.json.JSONObject;

public class ForumData {
    public String name;
    public String description;
    public String url;
    public int newCount;
    public int topicCount;
    
    
    public static ForumData fromJson(JSONObject json) throws SpacesException {
        ForumData data = new ForumData();
        try {
            if(json.has("forumLink")) data.url = json.getString("forumLink");
            if(json.has("forumName")) data.name = json.getString("forumName");
            if(json.has("description")) data.description = json.getString("description");
            if(json.has("newTopicsCnt")) data.newCount = json.getInt("newTopicsCnt");
            if(json.has("topicsCnt")) data.topicCount = json.getInt("topicsCnt");
        } catch(JSONException e) {
            throw new SpacesException(-2);
        }
        return data;
    }
    
}
