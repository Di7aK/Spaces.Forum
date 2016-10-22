package com.di7ak.spaces.forum.api;

import android.net.Uri;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;

public class Comment {
    
    public static void send(Session session, String text, int type, String id) throws SpacesException {
        StringBuilder args = new StringBuilder();
        args.append("method=").append("add")
            .append("&sid=").append(Uri.encode(session.sid))
            .append("&CK=").append(Uri.encode(session.sid.substring(12, 16)))
            .append("&comment=").append(Uri.encode(text))
            .append("&id=").append(id)
            .append("&Id=").append(id)
            .append("&passed=").append("1")
            .append("&Type=").append(Integer.toString(type));
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("http://spaces.ru/neoapi/comments/").openConnection();

            con.setRequestMethod("POST");
            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(args.toString());
            wr.flush();
            wr.close();

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
            
        } catch (IOException e) {
            throw new SpacesException(-1);
        } catch (JSONException e) {
            throw new SpacesException(-2);
		}
	}
}
