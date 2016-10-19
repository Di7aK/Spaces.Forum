package com.di7ak.spaces.forum.api;

import java.util.List;

import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

import java.util.ArrayList;

public class TopicData {
    public List<CommentData> comments;
    public List<AttachData> attaches;
    public PaginationData pagination;
    public AttachData avatar;
    public UserData user;
    public VotingData voting;
    public String body;
    public String subject;
    public String date;
    public String editDate;
    public String editUser;
    public String id;

    public static TopicData fromJson(JSONObject json) throws SpacesException {
        TopicData result = new TopicData();
        result.attaches = new ArrayList<AttachData>();
        try {
            if (json.has("code")) {
                int code = json.getInt("code");
                if (code == 0) {
                    if (json.has("topicWidget")) {
                        JSONObject topicWidget = json.getJSONObject("topicWidget");
                        if (topicWidget.has("subject")) result.subject = topicWidget.getString("subject");
                        if (topicWidget.has("date")) result.subject = topicWidget.getString("date");
                        if (topicWidget.has("body")) {
                            JSONObject body = topicWidget.getJSONObject("body");
                            if (body.has("subject")) result.body = body.getString("subject");
                        }
                        if (topicWidget.has("infoDate")) result.editDate = topicWidget.getString("infoDate");
                        if (topicWidget.has("infoUser")) {
                            JSONObject infoUser = topicWidget.getJSONObject("infoUser");
                            if (infoUser.has("name")) result.editUser = infoUser.getString("name");
                        }
                        if (topicWidget.has("user")) result.user = UserData.fromJson(topicWidget.getJSONObject("user"));
                        if (topicWidget.has("mainAttachWidgets")) {
                            JSONObject mainAttachWidgets = topicWidget.getJSONObject("mainAttachWidgets");
                            if (mainAttachWidgets.has("attachWidgets")) {
                                JSONArray attachWidgets = mainAttachWidgets.getJSONArray("attachWidgets");
                                for (int i = 0; i < attachWidgets.length(); i ++) {
                                    result.attaches.add(AttachData.fromJson(attachWidgets.getJSONObject(i)));
                                }
                            }
                        }
                        if (topicWidget.has("avatar")) {
                            result.avatar = AttachData.fromJson(topicWidget.getJSONObject("avatar"));
                        }
                        if (topicWidget.has("widgets")) {
                            JSONObject widgets = topicWidget.getJSONObject("widgets");
                            if (widgets.has("voting")) {
                                result.voting = VotingData.fromJson(widgets.getJSONObject("voting"));
                            }
                        }
                    }
                    if(json.has("commentsBlock")) {
                        JSONObject commentsBlock = json.getJSONObject("commentsBlock");
                        if(commentsBlock.has("pagination")) {
                            result.pagination = PaginationData.fromJson(commentsBlock.getJSONObject("pagination"));
                        }
                    }
                } else throw new SpacesException(code);
            }
        } catch (JSONException e) {
            android.util.Log.e("lol", "topic: " + e.toString(), e);
            throw new SpacesException(-2);
        }
        return result;
    }
}
