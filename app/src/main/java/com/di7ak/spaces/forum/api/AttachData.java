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
	
	public static AttachData fromJson(JSONObject item) throws JSONException {
		AttachData result = new AttachData();
		if(item.has("attach")) {
			JSONObject attach = item.getJSONObject("attach");
			if(attach.has("lightLink")) result.lightLink = attach.getString("lightLink");
			if(attach.has("inTopic")) result.inTopic = attach.getInt("inTopic") == 1;
			if(attach.has("type")) result.type = attach.getInt("type");
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
		}
		return result;
	}
}
