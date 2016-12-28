package com.di7ak.spaces.forum.models;

import org.json.JSONException;
import org.json.JSONObject;

public class TileAttach {
    public int type;
    public int id;
    public int previewW;
    public int previewH;
    public int group;
    public String name;
    public String previewUrl;
    public String downloadUrl;
    public String fileExt;
    
    public void from(JSONObject data) {
        try {
            type = data.getInt("type");
            name = data.getString("filename");
            id = data.getInt("nid");
            previewW = data.getInt("previewW");
            previewH = data.getInt("previewH");
            JSONObject preview = data.getJSONObject("preview");
            previewUrl = preview.getString("previewURL");
            downloadUrl = preview.getString("downloadLink");
            fileExt = preview.getString("fileext");
            group = preview.getInt("group");
        } catch(JSONException e) {
            
        }
    }
}
