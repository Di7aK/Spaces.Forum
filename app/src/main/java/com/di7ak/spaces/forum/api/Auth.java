package com.di7ak.spaces.forum.api;

import java.net.*;
import java.io.*;
import org.json.*;
import android.net.*;

public class Auth {

	public static Session login(String login, String password) throws SpacesException {
		Session session = new Session();
		StringBuilder args = new StringBuilder();
		args.append("method=").append("login")
			.append("&login=").append(Uri.encode(login))
			.append("&password=").append(Uri.encode(password));

		try {
			HttpURLConnection con = (HttpURLConnection) new URL("http://spaces.ru/api/auth/").openConnection();

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
            
			json = json.getJSONObject("attributes");
			session.sid = json.getString("sid");
			session.ck = json.getString("CK");
			session.login = json.getString("name");
            session.channel = json.getString("channel_id");
            session.avatar = json.getString("avatar");
            session.nid = json.getInt("nid");
		} catch (IOException e) {
			throw new SpacesException(-1);
		} catch (JSONException e) {
            
			throw new SpacesException(-2);
		}
		return session;
	}

}
