package com.di7ak.spaces.forum.api;

import android.net.Uri;
import android.text.Html;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PreviewTopicData {
    public String id;
    public String user;
    public String date;
    public String lastUser;
    public String lastDate;
    public String subject;
    public int attachCount;
    public int commentsCount;
    public boolean locked;
    public boolean newTopic;
    public List<AttachData> attaches;
    public Uri uri;
    
    public static PreviewTopicData fromJson(JSONObject json) throws SpacesException {
        PreviewTopicData data = new PreviewTopicData();
        data.attaches = new ArrayList<AttachData>();
        try {
            if(json.has("topicLink")) {
                String link = json.getString("topicLink");
                link = Html.fromHtml(link).toString();
                data.uri = Uri.parse(link);
            }
            if (json.has("topicUser")) data.user = json.getString("topicUser");
            if (json.has("date")) data.date = json.getString("date");
            if (json.has("subject")) data.subject = json.getString("subject");
            if (json.has("newTopic")) data.newTopic = json.getInt("newTopic") == 1;
            if (json.has("commentsCnt")) data.commentsCount = json.getInt("commentsCnt");
            if (json.has("lastUser")) data.lastUser = json.getString("lastUser");
            if (json.has("lastCommentDate")) data.lastDate = json.getString("lastCommentDate");
            if (json.has("id")) data.id = json.getString("id");
            if (json.has("AttachCount")) data.attachCount = json.getInt("AttachCount");
            if (json.has("locked")) data.locked = json.getInt("locked") == 1;
            if(json.has("MainAttachWidget")) {
                JSONObject mainAttachWidget = json.getJSONObject("MainAttachWidget");
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
            
        } catch(JSONException e) {
            throw new SpacesException(-2);
        }
        return data;
    }
}
