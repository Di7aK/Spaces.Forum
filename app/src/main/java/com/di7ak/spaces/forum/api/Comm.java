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
	public String name, avatar, cid;
	public int count;

	public Comm(String name, String avatar, String cid, int count) {
		this.name = name;
		this.avatar = avatar;
		this.cid = cid;
		this.count = count;
	}

	public static List<Comm> get(Session session, int page) throws SpacesException {
		List<Comm> commList = new ArrayList<Comm>();
		StringBuilder url = new StringBuilder()
			.append("http://spaces.ru/comm/")
			.append("?List=").append("1")
			.append("&P=").append(Integer.toString(page))
			.append("&sid=").append(Uri.encode(session.sid));
			
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
			/*int pages = page;
			try {
				pages = json.getJSONObject("pagination").getInt("last_page");
			} catch(JSONException e){}*/
			JSONArray comms = json.getJSONArray("comms_list");
			Comm comm;
			JSONObject commObject;
			for(int i = 0; i < comms.length(); i ++) {
				commObject = comms.getJSONObject(i);
				if(!commObject.has("forum_url")) continue;
				Uri forumUri = Uri.parse(commObject.getString("forum_url"));
				comm = new Comm(commObject.getString("name"),
								commObject.getJSONObject("logo_widget").getString("previewURL"),
								forumUri.getQueryParameter("cid"),
								commObject.getJSONObject("counters").getInt("forum"));
				commList.add(comm);
			}
		} catch (IOException e) {
			throw new SpacesException(-1);
		} catch (JSONException e) {
			throw new SpacesException(-2);
		}
		return commList;
	}

}

