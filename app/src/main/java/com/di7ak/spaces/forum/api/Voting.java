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

public class Voting {
    
    public static void vote(Session session, int ot, int oid, int down) throws SpacesException {
        StringBuilder args = new StringBuilder();
        args.append("method=").append(down == -1 ? "delete" : "like")
            .append("&sid=").append(Uri.encode(session.sid))
            .append("&CK=").append(Uri.encode(session.ck))
            .append("&Ot=").append(Uri.encode(Integer.toString(ot)))
            .append("&Oid=").append(Uri.encode(Integer.toString(oid)))
            .append("&Down=").append(Uri.encode(Integer.toString(down)));

        try {
            HttpURLConnection con = (HttpURLConnection) new URL("http://spaces.ru/neoapi/voting/").openConnection();

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
android.util.Log.d("lol", response.toString());
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
