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

	public static ForumResult getTopics(Session session, Comm comm, int page, int type) throws SpacesException {
		ForumResult result = new ForumResult();
		result.topics = new ArrayList<PreviewTopicData>();
		StringBuilder url = new StringBuilder()
			.append("http://spaces.ru/xhr/forums/")
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

			JSONObject json = new JSONObject(parseJsonString(response.toString()));
			int code = json.getInt("code");
			if (code != 0) throw new SpacesException(code);
			JSONObject temp;
			if (json.has("paginationWidget")) {
				temp = json.getJSONObject("paginationWidget");
				result.currentPage = temp.getInt("current_page");
				result.lastPage = temp.getInt("last_page");
			} else {
				result.currentPage = page;
				result.lastPage = page;
			}
			if (json.has("topicWidget")) {
				temp = json.getJSONObject("topicWidget");
				JSONArray topics = temp.getJSONArray("topics");
				PreviewTopicData topic;
				for (int i = 0; i < topics.length(); i ++) {
					temp = topics.getJSONObject(i);
					topic = new PreviewTopicData();
					if (temp.has("topicUser")) topic.user = temp.getString("topicUser");
					if (temp.has("date")) topic.date = temp.getString("date");
					if (temp.has("subject")) topic.subject = temp.getString("subject");
					if (temp.has("newTopic")) topic.newTopic = temp.getInt("newTopic") == 1;
					if (temp.has("commentsCnt")) topic.commentsCount = temp.getInt("commentsCnt");
					if (temp.has("lastUser")) topic.lastUser = temp.getString("lastUser");
					if (temp.has("lastCommentDate")) topic.lastDate = temp.getString("lastCommentDate");
					if (temp.has("id")) topic.id = temp.getString("id");
					if (temp.has("AttachCount")) topic.attachCount = temp.getInt("AttachCount");
					if (temp.has("locked")) topic.locked = temp.getInt("locked") == 1;
					result.topics.add(topic);
				}
			}
		} catch (IOException e) {
			throw new SpacesException(-1);
		} catch (JSONException e) {
			throw new SpacesException(-2);
		}
		return result;
	}
	
	public static TopicData getTopic(Session session, String topicId, int page) throws SpacesException {
		TopicData result = new TopicData();
		StringBuilder url = new StringBuilder()
			.append("http://spaces.ru/xhr/forums/")
			.append("?id=").append(topicId)
			.append("&p=").append(Integer.toString(page))
			.append("&sid=").append(session.sid);

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

			JSONObject json = new JSONObject(parseJsonString(response.toString()));
			result = TopicData.fromJson(json);
		} catch (IOException e) {
			throw new SpacesException(-1);
		} catch (JSONException e) {
			throw new SpacesException(-2);
		}
		return result;
	}

	private static String parseJsonString(String from) {
		int start = from.indexOf("data(");
		start += 5;
		int offset = from.lastIndexOf(")</");
		return from.substring(start, offset);
	}
    
}
