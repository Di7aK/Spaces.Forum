package com.di7ak.spaces.forum.api;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommunityListData {
    public List<CommunityData> communities;
    public PaginationData pagination;
    
    private JSONObject source;
    
    public CommunityListData(JSONObject json) throws SpacesException {
        source = json;
        communities = new ArrayList<CommunityData>();
        try {
            if(json.has("pagination") && !json.isNull("pagination")) {
                JSONObject paginationObject = json.getJSONObject("pagination");
                pagination = PaginationData.fromJson(paginationObject);
            }
            if(json.has("comms_list") && !json.isNull("comms_list")) {
                JSONArray commsList = json.getJSONArray("comms_list");
                for(int i = 0; i < commsList.length(); i ++) {
                    JSONObject comm = commsList.getJSONObject(i);
                    communities.add(new CommunityData(comm));
                }
            }
        } catch(JSONException e) {
            throw new SpacesException(-2);
        }
    }
    
    @Override
    public String toString() {
        return source.toString();
    }
}
