package com.di7ak.spaces.forum.api;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TopicListData {
    public List<PreviewTopicData> topics;
    public PaginationData pagination;
    
    public static TopicListData fromJson(JSONObject json) throws SpacesException {
        TopicListData data = new TopicListData();
        data.topics = new ArrayList<PreviewTopicData>();
        try {
            if(json.has("topicWidget")) {
                JSONObject topicWidget = json.getJSONObject("topicWidget");
                if(topicWidget.has("topics")) {
                    JSONArray topics = topicWidget.getJSONArray("topics");
                    for(int i = 0; i < topics.length(); i ++) {
                        JSONObject topic = topics.getJSONObject(i);
                        PreviewTopicData topicData = PreviewTopicData.fromJson(topic);
                        data.topics.add(topicData);
                    }
                }
            }
            if(json.has("pagnationWidget") && !json.isNull("paginationWidget")) {
                JSONObject paginationWidget = json.getJSONObject("paginationWidget");
                data.pagination = PaginationData.fromJson(paginationWidget);
            } else {
                data.pagination = new PaginationData();
                data.pagination.currentPage = 1;
                data.pagination.lastPage = 1;
                data.pagination.itemsOnPage = data.topics.size();
            }
        } catch(JSONException e) {
            throw new SpacesException(-2);
        }
        return data;
    }
}
