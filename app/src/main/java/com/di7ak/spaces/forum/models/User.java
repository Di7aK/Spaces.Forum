package com.di7ak.spaces.forum.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONException;
import org.json.JSONObject;

public class User {
    public String name;
    public String avatar;
    public int id;

    public void from(JSONObject contact) {
        try {
            JSONObject widget = contact.getJSONObject("widget");
            id = widget.getJSONObject("onlineStatus").getInt("id");
            name = widget.getJSONObject("siteLink").getString("user_name");
            if (!contact.isNull("avatar")) {
                avatar = contact.getJSONObject("avatar").getString("previewURL");
            }
        } catch (JSONException e) {

        }
    }

    public void from(Cursor cursor) {
        int iName = cursor.getColumnIndex("name");
        int iId = cursor.getColumnIndex("user_id");
        int iAvatar = cursor.getColumnIndex("avatar");
        name = cursor.getString(iName);
        id = cursor.getInt(iId);
        avatar = cursor.getString(iAvatar);
    }

    public void put(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("user_id", id);
        if (avatar != null) cv.put("avatar", avatar);
        Cursor cursor = db.query("users", null, "user_id = ?", new String[]{Integer.toString(id)}, null, null, null);
        if (cursor.getCount() == 0) {
            db.insert("users", null, cv);
        } else {
            db.update("users", cv, "user_id=" + id, null);
        }
    }
}
