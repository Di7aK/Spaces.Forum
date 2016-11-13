package com.di7ak.spaces.forum.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;

public class Blogs {
    public static final int TYPE_COMM = 0;
    public static final int TYPE_USER = 1;
    
    public static BlogListData getBlogs(Session session, String from, int type, int page) throws SpacesException {
        StringBuilder url = new StringBuilder()
            .append("http://spaces.ru/diary/view/");
            if(type == TYPE_COMM) {
                url.append("?comm=").append(from);
            } else {
                
            }
             url.append("&P=").append(Integer.toString(page))
            .append("&sid=").append(session.sid)
            .append("&short=").append("0");

        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url.toString()).openConnection();

            con.setRequestMethod("POST");
            con.addRequestProperty("X-proxy", "spaces");
            con.addRequestProperty("Cookie", "beta=1;");
            
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
            return BlogListData.fromJson(json);
        } catch (IOException e) {
            throw new SpacesException(-1);
        } catch (JSONException e) {
            throw new SpacesException(-2);
		}
    }
    
    
}
