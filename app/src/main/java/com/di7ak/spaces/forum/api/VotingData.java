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

    public static VotingData fromJson(JSONObject source) throws SpacesException {
        VotingData result = new VotingData();
        try {
            if (source.has("likes_count")) result.likes = source.getInt("likes_count");
            if (source.has("dislikes_count")) result.dislikes = source.getInt("dislikes_count");
            if (source.has("ot")) result.objectType = source.getInt("ot");
            if (source.has("oid")) result.objectId = source.getInt("oid");
            //if(source.has("disabled")) result.disabled = source.getInt("disabled") == 1;
            //if(source.has("can_dislike")) result.canDislike = source.getInt("candislike") == 1;
        } catch (JSONException e) {
            android.util.Log.e("lol", "voting: " + e.toString(), e);
            
            throw new SpacesException(-2);
        }
        return result;
    }
}