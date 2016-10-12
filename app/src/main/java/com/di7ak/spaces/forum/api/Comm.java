package com.di7ak.spaces.forum.api;

import java.util.*;
import org.json.*;
import android.net.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Comm {
	public static final int TYPE_MYCOMM = 0;
	public static final int TYPE_POPULAR = 1;
	
	public String name, avatar, cid, description;
	public int count;

	public static CommResult get(Session session, int page, int type) throws SpacesException {
		List<Comm> commList = new ArrayList<Comm>();
		StringBuilder url = new StringBuilder()
			.append("http://spaces.ru/comm/")
			.append("?List=").append(type == TYPE_POPULAR ? "0" : "1")
			.append("&P=").append(Integer.toString(page))
			.append("&sid=").append(Uri.encode(session.sid));
		int pages = page;

		try {
			HttpURLConnection con = (HttpURLConnection) new URL(url.toString()).openConnection();

			con.setRequestMethod("GET");
			con.addRequestProperty("X-proxy", "spaces");
			BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			JSONObject json = new JSONObject(response.toString());
			int code = json.getInt("code");
			if (code != 0) throw new SpacesException(code);
			try {
				pages = json.getJSONObject("pagination").getInt("last_page");
			} catch(JSONException e){}
			JSONArray comms = json.getJSONArray("comms_list");
			Comm comm;
			JSONObject commObject;
			for(int i = 0; i < comms.length(); i ++) {
				commObject = comms.getJSONObject(i);
				comm = new Comm();
				JSONObject temp;
				if(commObject.has("name")) comm.name = commObject.getString("name");
				if(commObject.has("logo_widget") && (temp = commObject.getJSONObject("logo_widget")).has("previewURL")) comm.avatar = temp.getString("previewURL");
				if(commObject.has("counters") && (temp = commObject.getJSONObject("counters")).has("forum")) comm.count = temp.getInt("forum");
				if(commObject.has("users_label") && (temp = commObject.getJSONObject("users_label")).has("text")) comm.description = temp.getString("text");
				if(commObject.has("forum_url")){
					Uri forumUri = Uri.parse(commObject.getString("forum_url"));
					comm.cid = forumUri.getQueryParameter("cid");
				}
				commList.add(comm);
			}
		} catch (IOException e) {
			throw new SpacesException(-1);
		} catch (JSONException e) {
			android.util.Log.d("lol", e.toString());
			throw new SpacesException(-2);
		}
		CommResult result = new CommResult();
		result.comms = commList;
		result.pages = pages;
		result.page = page;
		return result;
	}
	
}

