package com.di7ak.spaces.forum.api;

import org.json.JSONObject;
import org.json.JSONException;

public class VotingData {
    public int likes;
    public int dislikes;
    public int objectType;
    public int objectId;

    public boolean canDislike;
    public boolean disabled;
    
    public String likeUrl;
    public String dislikeUrl;
    public String deleteUrl;

    public static VotingData fromJson(JSONObject source) throws SpacesException {
        VotingData result = new VotingData();
        try {
            if (source.has("likes_count")) result.likes = source.getInt("likes_count");
            if (source.has("dislikes_count")) result.dislikes = source.getInt("dislikes_count");
            if (source.has("ot")) result.objectType = source.getInt("ot");
            if (source.has("oid")) result.objectId = source.getInt("oid");
            if (source.has("like_URL")) result.likeUrl = source.getString("like_URL");
            if (source.has("dislike_URL")) result.dislikeUrl = source.getString("dislike_URL");
            if (source.has("delete_URL")) result.deleteUrl = source.getString("delete_URL");
        } catch (JSONException e) {
            throw new SpacesException(-2);
        }
        return result;
    }
}
