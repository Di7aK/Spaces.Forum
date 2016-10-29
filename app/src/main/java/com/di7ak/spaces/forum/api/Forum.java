package com.di7ak.spaces.forum.api;
import java.util.ArrayList;
import java.util.List;
import android.net.Uri;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

public class Forum {
	public static final int TYPE_NEW = 6;
	public static final int TYPE_LAST = 5;
	public static final int TYPE_POPULAR = 7;

	public static TopicListData getTopics(Session session, Comm comm, int page, int type) throws SpacesException {
		StringBuilder url = new StringBuilder()
			.append("http://spaces.ru/ajax/forums/")
			.append("?com_cat_id=").append(comm.cid)
			.append("&last=").append(Integer.toString(type))
			.append("&tp=").append(Integer.toString(page))
			.append("&sid=").append(session.sid);

		try {
			HttpURLConnection con = (HttpURLConnection) new URL(url.toString()).openConnection();

			con.setRequestMethod("GET");
			con.addRequestProperty("X-proxy", "spaces");
			con.setRequestProperty("User-Agent", "android_app");
			con.setRequestProperty("Cookie", "json=1");
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
			return TopicListData.fromJson(json);
		} catch (IOException e) {
			throw new SpacesException(-1);
		} catch (JSONException e) {
			throw new SpacesException(-2);
		}
	}
	
	public static TopicData getTopic(Session session, String topicId, int page) throws SpacesException {
		TopicData result = new TopicData();
		StringBuilder url = new StringBuilder()
			.append("http://spaces.ru/ajax/forums/")
			.append("?id=").append(topicId)
			.append("&p=").append(Integer.toString(page))
			.append("&sid=").append(session.sid);

		try {
			HttpURLConnection con = (HttpURLConnection) new URL(url.toString()).openConnection();

			con.setRequestMethod("GET");
			con.addRequestProperty("X-proxy", "spaces");
			con.addRequestProperty("Cookie", "beta=1");
			BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
            
			JSONObject json = new JSONObject(response.toString());
			result = TopicData.fromJson(json);
		} catch (IOException e) {
			throw new SpacesException(-1);
		} catch (JSONException e) {
			throw new SpacesException(-2);
		}
		return result;
	}
    
}
