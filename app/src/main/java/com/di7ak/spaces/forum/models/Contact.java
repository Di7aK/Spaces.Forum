package com.di7ak.spaces.forum.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONException;
import org.json.JSONObject;

public class Contact {
    public int userId;
    public int talkId;
    public int id;
    public int lastReceivedMsgId;
    public int lastMsgId;
    public String members;
    public String name;
    public String avatar;
    public String lastVisit;
    
    public void from(JSONObject data) {
        try {
            id = data.getInt("nid");
            if(data.has("last_received_msg_id") && !data.isNull("last_received_msg_id")) {
                lastReceivedMsgId = data.getInt("last_received_msg_id");
            }
            if (data.has("user_id")) userId = data.getInt("user_id");
            if (data.has("talk")) {
                members = data.getString("members_cnt");
                talkId = data.getInt("talk_id");
            } 
            if (data.has("avatar")) {
                avatar = data.getJSONObject("avatar").getString("previewURL");
            } else if (data.has("widget")) {
                JSONObject widget = data.getJSONObject("widget");
                avatar = widget.getJSONObject("avatar").getString("previewURL");
                if (widget.has("lastVisit")) {
                    lastVisit = widget.getString("lastVisit");
                }
            }
            name = data.getString("text_addr");
        } catch(JSONException e) {
            android.util.Log.e("lol", "", e);
        }
    }
    
    public void from(Cursor cursor) {
        int iName = cursor.getColumnIndex("name");
        int iUserId = cursor.getColumnIndex("user_id");
        int iTalkId = cursor.getColumnIndex("talk_id");
        int iId = cursor.getColumnIndex("contact_id");
        int iAvatar = cursor.getColumnIndex("avatar");
        name = cursor.getString(iName);
        userId = cursor.getInt(iUserId);
        talkId = cursor.getInt(iTalkId);
        id = cursor.getInt(iId);
        avatar = cursor.getString(iAvatar);
    }

    public void put(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("contact_id", id);
        cv.put("user_id", userId);
        cv.put("talk_id", talkId);
        if (avatar != null) cv.put("avatar", avatar);
        Cursor cursor = db.query("contacts", null, "contact_id = ?", new String[]{Integer.toString(id)}, null, null, null);
        if (cursor.getCount() == 0) {
            db.insert("contacts", null, cv);
        } else {
            db.update("contacts", cv, "contact_id=" + id, null);
        }
    }
}
