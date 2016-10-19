package com.di7ak.spaces.forum.api;

import org.json.JSONObject;
import org.json.JSONException;

public class AttachData {
	public static final int TYPE_FILEDIR 	= 1;
	public static final int TYPE_MUSICDIR 	= 2;
	public static final int TYPE_PICDIRDIR 	= 3;
	public static final int TYPE_FILE 		= 5;
	public static final int TYPE_MUSIC 		= 6;
	public static final int TYPE_PICTURE 	= 7;
	public static final int TYPE_VIDEODIR 	= 24;
	public static final int TYPE_VIDEO 		= 25;
	
    public String   url;
	public String 	lightLink;
	public String	previewUrl;
	public String	downloadLink;
	public String	showLink;
	public String	filename;
	public String	description;
	public String	resolution;
	public String	fileext;
	
	public boolean 	inTopic;
	
	public int 		type;
    public int      width;
    public int      height;
	
	public static AttachData fromJson(JSONObject attach) throws SpacesException {
		AttachData result = new AttachData();
        try {
			if(attach.has("lightLink")) result.lightLink = attach.getString("lightLink");
			if(attach.has("inTopic")) result.inTopic = attach.getInt("inTopic") == 1;
			if(attach.has("type")) result.type = attach.getInt("type");
            if(attach.has("previewURL")) result.previewUrl = attach.getString("previewURL");
            if(attach.has("URL")) result.url = attach.getString("URL");
			if(attach.has("size")) {
                JSONObject size = attach.getJSONObject("size");
                if(size.has("width")) result.width = size.getInt("width");
                if(size.has("height")) result.height = size.getInt("height");
            }
			if(attach.has("preview")) {
				JSONObject preview = attach.getJSONObject("preview");
				if(preview.has("previewURL")) result.previewUrl = preview.getString("previewURL");
				if(preview.has("downloadLink")) result.downloadLink = preview.getString("downloadLink");
				if(preview.has("showLink")) result.showLink = preview.getString("showLink");
				if(preview.has("filename")) result.filename = preview.getString("filename");
				if(preview.has("description")) result.description = preview.getString("description");
				if(preview.has("resolution")) result.resolution = preview.getString("resolution");
				if(preview.has("fileext")) result.previewUrl = preview.getString("fileext");
			}
        } catch(JSONException e) {
            android.util.Log.e("lol", "attach: " + e.toString(), e);
            
            throw new SpacesException(-2);
        }
		return result;
	}
}
