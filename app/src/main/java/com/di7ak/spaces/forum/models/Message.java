package com.di7ak.spaces.forum.models;

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    public static final int TYPE_MY         = 0;
    public static final int TYPE_RECEIVED   = 1;
    public static final int TYPE_SYSTEM     = 2;
    public static final int TYPE_SENDING    = 3;
    
    public String text;
    public String user;
    public long time;
    public String avatar;
    public boolean read;
    public boolean talk;
    public int type;
    public int nid;
    public int userId;
    public int contact;
    public int talkId;
    public List<TileAttach> tileAttaches;
    
    public void from(JSONObject data) {
        try {
            time = data.getLong("date") * 1000;
            text = data.has("text") ? data.getString("text") : "";
            read = !data.has("not_read");
            nid = data.getInt("nid");
            
            JSONObject contact = data.getJSONObject("contact");
            talk = contact.has("talk");
            if(talk) talkId = contact.getInt("talk_id");
            this.contact = contact.getInt("nid");

            if (data.has("system")) type = Message.TYPE_SYSTEM;
            else if (data.has("received")) type = Message.TYPE_RECEIVED;
            else type = Message.TYPE_MY;
            
            if (type != Message.TYPE_SYSTEM) {
                JSONObject widget = contact.getJSONObject("widget");
                userId = widget.getJSONObject("onlineStatus").getInt("id");
                user = widget.getJSONObject("siteLink").getString("user_name");
                if(!contact.isNull("avatar")) {
                    avatar = contact.getJSONObject("avatar").getString("previewURL");
                }
            }
        } catch(JSONException e) {
            android.util.Log.e("lol", "", e);
        }
    }
}
