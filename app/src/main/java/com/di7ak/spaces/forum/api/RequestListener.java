package com.di7ak.spaces.forum.api;

import org.json.JSONObject;

public interface RequestListener {
    
    public void onSuccess(JSONObject json);
        
    public void onError(SpacesException e);
}
