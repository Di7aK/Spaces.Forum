package com.di7ak.spaces.forum.api;

import com.di7ak.spaces.forum.api.AttachData;
import com.di7ak.spaces.forum.api.CommentData;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.text.TextUtils;

public class BlogData {
    public List<CommentData> comments;
    public List<AttachData> attaches;
    public PaginationData pagination;
    public AttachData avatar;
    public UserData user;
    public VotingData voting;
    public String header;
    public String subject;
    public String username;
    public String date;
    public String editDate;
    public String editUser;
    public String id;
    public int commentsCnt;
    public boolean commentFormEnabled;
    
    private JSONObject source;

    public BlogData(JSONObject json) throws SpacesException {
        source = json;
        attaches = new ArrayList<AttachData>();
        comments = new ArrayList<CommentData>();
        try {
            if (json.has("code")) {
                int code = json.getInt("code");
                if (code == 0) {
                    if (json.has("topicWidget")) {
                        JSONObject topicWidget = json.getJSONObject("topicWidget");
                        if(topicWidget.has("userWidget")) {
                            JSONObject userWidget = topicWidget.getJSONObject("userWidget");
                            user = UserData.fromJson(userWidget);
                        }
                        if (topicWidget.has("time")) date = topicWidget.getString("time");
                        if (topicWidget.has("MainAttachWidget")) {
                            JSONObject mainAttachWidgets = topicWidget.getJSONObject("MainAttachWidget");
                            if (mainAttachWidgets.has("attachWidgets")) {
                                JSONArray attachWidgets = mainAttachWidgets.getJSONArray("attachWidgets");
                                for (int i = 0; i < attachWidgets.length(); i ++) {
                                    JSONObject attach = attachWidgets.getJSONObject(i);
                                    if(attach.has("attach")) {
                                        attach = attach.getJSONObject("attach");
                                    }
                                    attaches.add(AttachData.fromJson(attach));
                                }
                            }
                            if (mainAttachWidgets.has("pictureWidgets")) {
                                JSONArray attachWidgets = mainAttachWidgets.getJSONArray("pictureWidgets");
                                for (int i = 0; i < attachWidgets.length(); i ++) {
                                    JSONObject attach = attachWidgets.getJSONObject(i);
                                    if(attach.has("attach")) {
                                        attach = attach.getJSONObject("attach");
                                    }
                                    attaches.add(AttachData.fromJson(attach));
                                }
                            }
                        }
                        if (topicWidget.has("attachWidget")) {
                            JSONObject mainAttachWidgets = topicWidget.getJSONObject("attachWidget");
                            
                            if (mainAttachWidgets.has("attachWidgets")) {
                                JSONArray attachWidgets = mainAttachWidgets.getJSONArray("attachWidgets");
                                for (int i = 0; i < attachWidgets.length(); i ++) {
                                    JSONObject attach = attachWidgets.getJSONObject(i);
                                    if(attach.has("attach")) {
                                        attach = attach.getJSONObject("attach");
                                    }
                                    attaches.add(AttachData.fromJson(attach));
                                }
                            }
                            if (mainAttachWidgets.has("musicFullWidget")) {
                                JSONArray attachWidgets = mainAttachWidgets.getJSONArray("musicFullWidget");
                                for (int i = 0; i < attachWidgets.length(); i ++) {
                                    JSONObject attach = attachWidgets.getJSONObject(i);
                                    if(attach.has("attach")) {
                                        attach = attach.getJSONObject("attach");
                                    }
                                    attaches.add(AttachData.fromJson(attach));
                                }
                            }
                        }
                        if (topicWidget.has("avatar")) {
                            avatar = AttachData.fromJson(topicWidget.getJSONObject("avatar"));
                        }
                        if (topicWidget.has("comments")) {
                            commentsCnt = topicWidget.getInt("comments");
                        }
                        if (topicWidget.has("username")) {
                            username = topicWidget.getString("username");
                        }
                        if (topicWidget.has("subject")) {
                            Object subject = topicWidget.get("subject");
                            if(subject instanceof JSONObject) {
                            if (((JSONObject)subject).has("subject")) {
                                this.subject = ((JSONObject)subject).getString("subject");
                            }
                            } else this.subject = subject.toString();
                        }
                        if (topicWidget.has("topicModel")) {
                            JSONObject topicModel = topicWidget.getJSONObject("topicModel");
                            if (topicModel.has("topic_id")) {
                                id = topicModel.getString("topic_id");
                            }
                            if (topicModel.has("header")) {
                                header = topicModel.getString("header");
                                if(TextUtils.isEmpty(header)) header = subject;
                            }
                        }
                    }
                    if(json.has("actionBar")) {
                        JSONObject actionBar = json.getJSONObject("actionBar");
                        if(actionBar.has("widgets")) {
                            JSONObject widgets = actionBar.getJSONObject("widgets");
                            voting = new VotingData();
                            if(widgets.has("like")) {
                                JSONObject like = widgets.getJSONObject("like");
                                if(like.has("URL") && !like.isNull("URL")) voting.likeUrl = like.getString("URL");
                                if(like.has("count")) voting.likes = like.getInt("count");
                                if(like.has("oid")) voting.objectId = like.getInt("oid");
                                if(like.has("ot")) voting.objectType = like.getInt("ot");
                            }
                            if(widgets.has("dislike")) {
                                JSONObject dislike = widgets.getJSONObject("dislike");
                                if(dislike.has("URL") && !dislike.isNull("URL")) voting.dislikeUrl = dislike.getString("URL");
                                if(dislike.has("count")) voting.dislikes = dislike.getInt("count");
                                if(dislike.has("oid")) voting.objectId = dislike.getInt("oid");
                                if(dislike.has("ot")) voting.objectType = dislike.getInt("ot");
                            }
                        }
                    }
                    
                    if (json.has("commentsBlock")) {
                        JSONObject commentsBlock = json.getJSONObject("commentsBlock");
                        Object pagination;
                        if (commentsBlock.has("pagination") && (pagination = commentsBlock.get("pagination")) instanceof JSONObject) {
                            
                            this.pagination = PaginationData.fromJson((JSONObject)pagination);
                        } else {
                            this.pagination = new PaginationData();
                            this.pagination.currentPage = 1;
                            this.pagination.lastPage = 1;
                        }
                        commentFormEnabled = true;//(commentsBlock.has("commentForm") && !commentsBlock.isNull("commentForm"));
                            
                        if (commentsBlock.has("comments")) {
                            JSONObject comments = commentsBlock.getJSONObject("comments");
                            if(comments.has("commentsCnt")) commentsCnt = comments.getInt("commentsCnt");

                            if (comments.has("comments_list")) {
                                JSONArray commentsList = comments.getJSONArray("comments_list");
                                for (int i = 0; i < commentsList.length(); i ++) {
                                    this.comments.add(CommentData.fromJson(commentsList.getJSONObject(i)));
                                }
                            }
                        }
                    }
                } else throw new SpacesException(code);
                if(subject == null) subject = header;
            }
        } catch (JSONException e) {
            android.util.Log.e("lol", "", e);
            throw new SpacesException(-2);
        }
    }
}
