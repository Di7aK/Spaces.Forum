package com.di7ak.spaces.forum.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    public static final int TYPE_MY         = 0;
    public static final int TYPE_RECEIVED   = 1;
    public static final int TYPE_SYSTEM     = 2;
    public static final int TYPE_SENDING    = 3;

    public User user;
    public String text;
    public long time;
    public boolean read;
    public boolean talk;
    public int type;
    public int nid;
    public int contact;
    public int talkId;
    public List<TileAttach> tileAttaches;

    public void from(JSONObject data) {
        try {
            if(user == null) user = new User();
            
            time = data.getLong("date") * 1000;
            text = data.has("text") ? data.getString("text") : "";
            read = !data.has("not_read");
            nid = data.getInt("nid");

            if (type != TYPE_SENDING) {
                JSONObject contact = data.getJSONObject("contact");
                talk = contact.has("talk");
                if (talk) talkId = contact.getInt("talk_id");
                this.contact = contact.getInt("nid");

                if (data.has("system")) type = Message.TYPE_SYSTEM;
                else if (data.has("received")) type = Message.TYPE_RECEIVED;
                else type = Message.TYPE_MY;

                if (type != Message.TYPE_SYSTEM) {
                    user.from(contact);
                }
            } else type = TYPE_MY;
        } catch (JSONException e) {
            android.util.Log.e("lol", "", e);
        }
    }
    
    public void from(Cursor cursor, SQLiteDatabase db) {
        int iMsgId = cursor.getColumnIndex("msg_id");
        int iType = cursor.getColumnIndex("type");
        int iDate = cursor.getColumnIndex("date");
        int iMessage = cursor.getColumnIndex("message");
        int iUserId = cursor.getColumnIndex("user_id");
        int iRead = cursor.getColumnIndex("not_read");
        int iTalk = cursor.getColumnIndex("talk");
        
        nid = cursor.getInt(iMsgId);
        type = cursor.getInt(iType);
        time = cursor.getLong(iDate);
        text = cursor.getString(iMessage);
        user = new User();
        user.id = cursor.getInt(iUserId);
        read = cursor.getInt(iRead) == 0;
        talk = cursor.getInt(iTalk) == 1;
        
        cursor = db.query("users", null, "user_id = ?", new String[]{Integer.toString(user.id)}, null, null, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            user.from(cursor);
        }
    }

    public void put(SQLiteDatabase db) {
        ContentValues cv;
        Cursor cursor = db.query("messages", null, "msg_id = ?", new String[]{Integer.toString(nid)}, null, null, null);
        if (cursor.getCount() == 0) {
            cv = new ContentValues();
            cv.put("msg_id", nid);
            cv.put("contact_id", contact);
            cv.put("type", type);
            cv.put("date", time);
            cv.put("message", text);
            cv.put("user_id", user.id);
            cv.put("talk", talk ? 1 : 0);
            cv.put("not_read", read ? 0 : 1);
            db.insert("messages", null, cv);
        }
        user.put(db);
    }
}
