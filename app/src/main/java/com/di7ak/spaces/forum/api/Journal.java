package com.di7ak.spaces.forum.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class Journal {
    
    public static JournalResult getRecords(Session session, int type, int page) throws SpacesException {
        JournalResult result = new JournalResult();
        result.records = new ArrayList<JournalRecord>();
        StringBuilder url = new StringBuilder()
            .append("http://spaces.ru/journal/")
            .append("?S=").append(Integer.toString(type))
            .append("&P=").append(Integer.toString(page))
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
            
            
            JSONObject json = new JSONObject(response.toString());
            int code = json.getInt("code");
            
            if (code != 0) throw new SpacesException(code);
            
            if(json.has("info")) {
                JSONObject info = json.getJSONObject("info");
                
                JSONArray recs = info.getJSONArray("records");
                
                for(int i = 0; i < recs.length(); i ++) {
                    result.records.add(JournalRecord.fromJson(recs.getJSONObject(i)));
                }
            }
            if (json.has("pagination") && !json.isNull("pagination")) {
                result.pagination = PaginationData.fromJson(json.getJSONObject("pagination"));
            } else {
                result.pagination = new PaginationData();
                result.pagination.currentPage = 1;
                result.pagination.lastPage = 1;
            }
        } catch (IOException e) {
            throw new SpacesException(-1);
        } catch (JSONException e) {
            throw new SpacesException(-2);
		}
        return result;
    }
}
