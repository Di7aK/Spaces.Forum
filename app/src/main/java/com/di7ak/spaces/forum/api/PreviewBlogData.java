package com.di7ak.spaces.forum.api;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PreviewBlogData {
    public List<AttachData> attaches;
    public AttachData avatar;
    public VotingData voting;
    public String author;
    public String subject;
    public String header;
    public String date;
    public String channelName;
    public String id;
    public int sharesCount;
    public int views;
    public int attachCount;
    public int commentsCount;
    
    
    public static PreviewBlogData fromJson(JSONObject json) throws SpacesException {
        PreviewBlogData data = new PreviewBlogData();
        data.attaches = new ArrayList<AttachData>();
        try {
            if(json.has("shares_cnt")) data.sharesCount = json.getInt("shares_cnt");
            if(json.has("properties")) {
                JSONObject properties = json.getJSONObject("properties");
                if(properties.has("time")) data.date = properties.getString("time");
                if(properties.has("views")) data.views = properties.getInt("views");
                if(properties.has("AttachCount")) data.attachCount = properties.getInt("AttachCount");
                if(properties.has("subject")) data.subject = properties.getString("subject");
                if(properties.has("channel_name")) data.channelName = properties.getString("channel_name");
                if(properties.has("header")) data.header = properties.getString("header");
                if(properties.has("actionBar")) {
                    JSONObject actionBar = properties.getJSONObject("actionBar");
                    if(actionBar.has("info")) {
                        JSONObject info = actionBar.getJSONObject("info");
                        if(info.has("comments")) {
                            JSONObject comments = info.getJSONObject("comments");
                            if(comments.has("cnt")) data.commentsCount = comments.getInt("cnt");
                        }
                    }
                    if(actionBar.has("widgets")) {
                        JSONObject widgets = actionBar.getJSONObject("widgets");
                        data.voting = new VotingData();
                        if(widgets.has("like")) {
                            JSONObject like = widgets.getJSONObject("like");
                            if(like.has("URL") && !like.isNull("URL")) data.voting.likeUrl = like.getString("URL");
                            if(like.has("count")) data.voting.likes = like.getInt("count");
                            if(like.has("oid")) data.voting.objectId = like.getInt("oid");
                            if(like.has("ot")) data.voting.objectType = like.getInt("ot");
                        }
                        if(widgets.has("dislike")) {
                            JSONObject dislike = widgets.getJSONObject("dislike");
                            if(dislike.has("URL") && !dislike.isNull("URL")) data.voting.dislikeUrl = dislike.getString("URL");
                            if(dislike.has("count")) data.voting.dislikes = dislike.getInt("count");
                            if(dislike.has("oid")) data.voting.objectId = dislike.getInt("oid");
                            if(dislike.has("ot")) data.voting.objectType = dislike.getInt("ot");
                        }
                    }
                    if(properties.has("MainAttachWidget")) {
                        JSONObject mainAttachWidget = properties.getJSONObject("MainAttachWidget");
                        if(mainAttachWidget.has("attachWidgets")) {
                            JSONArray attachWidgets = mainAttachWidget.getJSONArray("attachWidgets");
                            for(int i = 0; i < attachWidgets.length(); i ++) {
                                JSONObject attachData = attachWidgets.getJSONObject(i);
                                attachData = attachData.getJSONObject("attach");
                                AttachData attach = AttachData.fromJson(attachData);
                                data.attaches.add(attach);
                            }
                        }
                        if(mainAttachWidget.has("pictureWidgets")) {
                            JSONArray attachWidgets = mainAttachWidget.getJSONArray("pictureWidgets");
                            for(int i = 0; i < attachWidgets.length(); i ++) {
                                JSONObject attachData = attachWidgets.getJSONObject(i);
                                attachData = attachData.getJSONObject("attach");
                                AttachData attach = AttachData.fromJson(attachData);
                                data.attaches.add(attach);
                            }
                        }
                    }
                    if(properties.has("avatar")) {
                        JSONObject avatar = properties.getJSONObject("avatar");
                        data.avatar = AttachData.fromJson(avatar);
                    }
                    if(properties.has("userWidget")) {
                        JSONObject userWidget = properties.getJSONObject("userWidget");
                        data.author = userWidget.getString("name");
                    }
                    if(properties.has("commWidget")) {
                        JSONObject commWidget = properties.getJSONObject("commWidget");
                        data.author = commWidget.getString("name");
                    }
                }
            }
            if(json.has("model")) {
                JSONObject model = json.getJSONObject("model");
                if(model.has("topic_id")) data.id = model.getString("topic_id");
            }
        } catch(JSONException e) {
            throw new SpacesException(-2);
        }
        return data;
    }
}
