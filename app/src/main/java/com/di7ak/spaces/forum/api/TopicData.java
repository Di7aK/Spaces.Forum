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
    public int commentsCnt;

    public static TopicData fromJson(JSONObject json) throws SpacesException {
        TopicData result = new TopicData();
        result.attaches = new ArrayList<AttachData>();
        result.comments = new ArrayList<CommentData>();
        try {
            if (json.has("code")) {
                int code = json.getInt("code");
                if (code == 0) {
                    if (json.has("topicWidget")) {
                        
                        JSONObject topicWidget = json.getJSONObject("topicWidget");
                        if (topicWidget.has("id")) result.id = topicWidget.getString("id");
                        if (topicWidget.has("subject")) result.subject = topicWidget.getString("subject");
                        if (topicWidget.has("date")) result.date = topicWidget.getString("date");
                        if (topicWidget.has("body")) {
                            Object body = topicWidget.get("body");
                            if(body instanceof JSONObject) {
                                if (((JSONObject)body).has("subject")) result.body = ((JSONObject)body).getString("subject");
                            } else result.body = body.toString();
                        }
                        if (topicWidget.has("infoDate")) result.editDate = topicWidget.getString("infoDate");
                        if (topicWidget.has("infoUser")) {
                            JSONObject infoUser = topicWidget.getJSONObject("infoUser");
                            if (infoUser.has("name")) result.editUser = infoUser.getString("name");
                        }
                        if (topicWidget.has("user")) result.user = UserData.fromJson(topicWidget.getJSONObject("user"));
                        if (topicWidget.has("attachWidgets")) {

                            JSONObject mainAttachWidgets = topicWidget.getJSONObject("attachWidgets");
                            //android.util.Log.d("lol", "glob " + mainAttachWidgets.toString());
                        }
                        if (topicWidget.has("mainAttachWidgets")) {
                            
                            JSONObject mainAttachWidgets = topicWidget.getJSONObject("mainAttachWidgets");
                            //android.util.Log.d("lol", "main: " + mainAttachWidgets.toString());
                            
                            if (mainAttachWidgets.has("attachWidgets")) {
                                JSONArray attachWidgets = mainAttachWidgets.getJSONArray("attachWidgets");
                                for (int i = 0; i < attachWidgets.length(); i ++) {
                                    JSONObject attach = attachWidgets.getJSONObject(i);
                                    if(attach.has("attach")) {
                                        attach = attach.getJSONObject("attach");
                                    }
                                    result.attaches.add(AttachData.fromJson(attach));
                                }
                            }
                            if (mainAttachWidgets.has("pictureWidgets")) {
                                JSONArray attachWidgets = mainAttachWidgets.getJSONArray("pictureWidgets");
                                for (int i = 0; i < attachWidgets.length(); i ++) {
                                    JSONObject attach = attachWidgets.getJSONObject(i);
                                    if(attach.has("attach")) {
                                        attach = attach.getJSONObject("attach");
                                    }
                                    result.attaches.add(AttachData.fromJson(attach));
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
                    if (json.has("commentsBlock")) {
                        JSONObject commentsBlock = json.getJSONObject("commentsBlock");
                        if (commentsBlock.has("pagination") && !commentsBlock.isNull("pagination")) {
                            result.pagination = PaginationData.fromJson(commentsBlock.getJSONObject("pagination"));
                        } else {
                            result.pagination = new PaginationData();
                            result.pagination.currentPage = 1;
                            result.pagination.lastPage = 1;
                        }
                        if (commentsBlock.has("comments")) {
                            JSONObject comments = commentsBlock.getJSONObject("comments");
                            if(comments.has("commentsCnt")) result.commentsCnt = comments.getInt("commentsCnt");
                            
                            if (comments.has("comments_list")) {
                                JSONArray commentsList = comments.getJSONArray("comments_list");
                                for (int i = 0; i < commentsList.length(); i ++) {
                                    result.comments.add(CommentData.fromJson(commentsList.getJSONObject(i)));
                                }
                            }
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
