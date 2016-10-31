package com.di7ak.spaces.forum.api;

import org.json.JSONException;
import org.json.JSONObject;

public class ForumCategoryData {
    public String name;
    public String description;
    public String url;
    
    public static ForumCategoryData fromJson(JSONObject json) throws SpacesException {
        ForumCategoryData data = new ForumCategoryData();
        try {
            if(json.has("catLink")) data.url = json.getString("catLink");
            if(json.has("name")) data.name = json.getString("name");
            if(json.has("description")) data.description = json.getString("description");
        } catch(JSONException e) {
            throw new SpacesException(-2);
        }
        return data;
    }
    
}
