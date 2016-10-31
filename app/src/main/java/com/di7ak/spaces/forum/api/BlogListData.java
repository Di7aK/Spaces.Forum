package com.di7ak.spaces.forum.api;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BlogListData {
    
    public List<PreviewBlogData> blogs;
    public PaginationData pagination;

    public static BlogListData fromJson(JSONObject json) throws SpacesException {
        BlogListData data = new BlogListData();
        data.blogs = new ArrayList<PreviewBlogData>();
        try {
            if(json.has("topicWidget")) {
                JSONObject topicWidget = json.getJSONObject("topicWidget");
                if(topicWidget.has("topicModels")) {
                    JSONArray blogs = topicWidget.getJSONArray("topicModels");
                    for(int i = 0; i < blogs.length(); i ++) {
                        JSONObject topic = blogs.getJSONObject(i);
                        PreviewBlogData blogData = PreviewBlogData.fromJson(topic);
                        data.blogs.add(blogData);
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
                data.pagination.itemsOnPage = data.blogs.size();
            }
        } catch(JSONException e) {
            throw new SpacesException(-2);
        }
        return data;
    }
}
