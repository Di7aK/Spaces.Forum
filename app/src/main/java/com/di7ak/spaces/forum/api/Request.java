package com.di7ak.spaces.forum.api;

import android.net.Uri;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.DataOutputStream;
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
    private String post;

    public Request(Uri uri) {
        mUri = uri;
        mUseXProxy = true;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public Request disableXProxy() {
        mUseXProxy = false;
        return this;
    }

    @Override
    protected String doInBackground(Void... v) {
        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http");
            builder.authority("spaces.ru");
            builder.appendPath("ajax");
            String ajaxPath = builder.build().toString() + mUri.getPath();
            mUri = Uri.parse(ajaxPath + "?" + mUri.getQuery());
            HttpURLConnection con = (HttpURLConnection) new URL(mUri.toString()).openConnection();
            if (mUseXProxy) con.addRequestProperty("X-proxy", "spaces");
            
            if (post == null) {
                con.setRequestMethod("GET");
            } else {
                con.setRequestMethod("POST");
                con.setDoOutput(true);

                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(post.toString());
                wr.flush();
                wr.close();
            }
            
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
