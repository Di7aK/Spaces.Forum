package com.di7ak.spaces.forum.api;

import org.json.JSONException;
import org.json.JSONObject;

public class UserData {
    public String imageLink;
    public double rate;
    public boolean online;
    public boolean birthday;
    public long lastTime;
    public String imageOn;
    public String imageOff;
    public String name;
    
    public static UserData fromJson(JSONObject json) throws SpacesException {
        UserData result = new UserData();
        try {
            if(json.has("grant")) {
                JSONObject grant = json.getJSONObject("grant");
                if(grant.has("birthday")) {
                    result.birthday = grant.getInt("birthday") == 1;
                }
                if(grant.has("img_link")) result.imageLink = grant.getString("img_link");
                if(grant.has("user_rate")) result.rate = grant.getDouble("user_rate");
            }
            if(json.has("onlineStatus") && !json.isNull("onlineStatus")) {
                JSONObject onlineStatus = json.getJSONObject("onlineStatus");
                if(onlineStatus.has("is_online")) {
                    result.online = onlineStatus.getInt("is_online") == 1;
                }
                if(onlineStatus.has("last_time")) result.lastTime = onlineStatus.getLong("last_time");
                if(onlineStatus.has("on_img")) result.imageOn = onlineStatus.getString("on_img");
                if(onlineStatus.has("off_img")) result.imageOff = onlineStatus.getString("off_img");
            }
            if(json.has("siteLink") && !json.isNull("siteLink")) {
                Object siteLink = json.get("siteLink");
                if(siteLink instanceof JSONObject) {
                    if(((JSONObject)siteLink).has("user_name")) result.name = ((JSONObject)siteLink).getString("user_name");
                }
            }
            if(json.has("name")) result.name = json.getString("name");
        } catch(JSONException e) {
            android.util.Log.e("lol", "user: " + e.toString(), e);
            
            throw new SpacesException(-2);
        }
        return result;
    }
}
