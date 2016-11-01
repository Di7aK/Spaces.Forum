package com.di7ak.spaces.forum.api;

import android.net.Uri;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommunityData {
    public String name;
    public String id;
    public String cid;
    public AttachData avatar;
    public boolean blogEnabled;
    public boolean forumEnabled;
    public int forumCount;
    public int blogCount;
    
    private JSONObject source;

    public CommunityData(JSONObject json) throws SpacesException {
        source = json;
        try {
            if(json.has("name")) name = json.getString("name");
            if(json.has("id")) id = json.getString("id");
            if(json.has("logo_widget")) {
                JSONObject logoWidget = json.getJSONObject("logo_widget");
                avatar = AttachData.fromJson(logoWidget);
            }
            if(json.has("counters")) {
                JSONObject counters = json.getJSONObject("counters");
                if(counters.has("forum")) forumCount = counters.getInt("forum");
                if(counters.has("blog")) blogCount = counters.getInt("blog");
            }
            forumEnabled = (json.has("forum_url") && !json.isNull("forum_url"));
            if(forumEnabled) {
                Uri forumUri = Uri.parse(json.getString("forum_url"));
                cid = forumUri.getQueryParameter("cid");
            }
            blogEnabled = (json.has("diary_url") && !json.isNull("diary_url"));
        } catch(JSONException e) {
            throw new SpacesException(-2);
        }
    }
    
    @Override
    public String toString() {
        return source.toString();
    }
    
}
