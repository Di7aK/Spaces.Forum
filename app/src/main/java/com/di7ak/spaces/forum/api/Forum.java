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

	public static TopicListData getTopics(Session session, String categoryId, int page, int type, String forum) throws SpacesException {
		StringBuilder url = new StringBuilder()
			.append("http://spaces.ru/ajax/forums/");
            if(forum == null) {
			    url.append("?com_cat_id=").append(categoryId);
            } else {
                url.append("?f=").append(forum);
            }
			if((forum != null && type == TYPE_NEW) || forum == null) url.append("&last=").append(Integer.toString(type));
			url.append("&tp=").append(Integer.toString(page))
			.append("&sid=").append(session.sid);

		try {
			HttpURLConnection con = (HttpURLConnection) new URL(url.toString()).openConnection();

			con.setRequestMethod("GET");
			con.addRequestProperty("X-proxy", "spaces");
			con.addRequestProperty("User-Agent", "android_app");
			con.addRequestProperty("Cookie", "json=1; beta=1;");
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
	
	
    public static List<ForumCategoryData> getCategories() throws SpacesException {
        List<ForumCategoryData> result = new ArrayList<ForumCategoryData>();
        StringBuilder url = new StringBuilder()
            .append("http://spaces.ru/ajax/forums/");

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
            if(json.has("cat_list")) {
                JSONArray catList = json.getJSONArray("cat_list");
                for(int i = 0; i < catList.length(); i ++) {
                    JSONObject category = catList.getJSONObject(i);
                    ForumCategoryData data = ForumCategoryData.fromJson(category);
                    result.add(data);
                }
            }
        } catch (IOException e) {
            throw new SpacesException(-1);
        } catch (JSONException e) {
            throw new SpacesException(-2);
		}
        return result;
    }
    
    public static List<ForumData> getForums(Session session, String cid) throws SpacesException {
        List<ForumData> result = new ArrayList<ForumData>();
        StringBuilder url = new StringBuilder()
            .append("http://spaces.ru/ajax/forums/")
            .append("?cid=").append(cid)
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
            if(json.has("forums")) {
                JSONArray forums = json.getJSONArray("forums");
                for(int i = 0; i < forums.length(); i ++) {
                    JSONObject category = forums.getJSONObject(i);
                    ForumData data = ForumData.fromJson(category);
                    result.add(data);
                }
            }
        } catch (IOException e) {
            throw new SpacesException(-1);
        } catch (JSONException e) {
            throw new SpacesException(-2);
        }
        return result;
    }
    
}
