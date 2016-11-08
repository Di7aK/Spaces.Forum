package com.di7ak.spaces.forum.api;

import android.net.Uri;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;

public class Request extends AsyncTask<Void, Void, String> {
    private Uri mUri;
    private RequestListener mListener;
    private boolean mUseXProxy;

    public Request(Uri uri) {
        mUri = uri;
        mUseXProxy = true;
    }
    
    public Request disableXProxy() {
        mUseXProxy = false;
        return this;
    }

    @Override
    protected String doInBackground(Void... v) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(mUri.toString()).openConnection();

            con.setRequestMethod("GET");
            if(mUseXProxy) con.addRequestProperty("X-proxy", "spaces");

            BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        JSONObject json = null;
        SpacesException exception = null;
        if (result == null) exception = new SpacesException(-1);
        else {
            try {
                json = new JSONObject(result);
                if (json.has("code")) {
                    int code = json.getInt("code");
                    if (code != 0) exception = new SpacesException(code);
                }
            } catch (JSONException e) {
                exception = new SpacesException(-2);
            }
        }
        if (json == null) mListener.onError(exception);
        else mListener.onSuccess(json);
    }
    
    public void executeWithListener(RequestListener listener) {
        mListener = listener;
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
