package com.di7ak.spaces.forum.api;

import android.net.Uri;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;

public class Communities {
    public static final int TYPE_MYCOMM = 0;
	public static final int TYPE_POPULAR = 1;
    
    public static CommunityListData get(Session session, int type, int page) throws SpacesException {
        StringBuilder url = new StringBuilder()
            .append("http://spaces.ru/comm/")
            .append("?List=").append(type == TYPE_POPULAR ? "0" : "1")
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
            return new CommunityListData(json);
        } catch (IOException e) {
            throw new SpacesException(-1);
        } catch (JSONException e) {
            throw new SpacesException(-2);
		}
    }
}
