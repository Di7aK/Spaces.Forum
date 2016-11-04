package com.di7ak.spaces.forum.widget;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.Request;
import com.di7ak.spaces.forum.api.RequestListener;
import com.di7ak.spaces.forum.api.SpacesException;
import com.rey.material.widget.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FileAttachmentsView extends LinearLayout {
    private Context mContext;
    
    public FileAttachmentsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        setOrientation(VERTICAL);
    }

    public void setupData(JSONArray data) {
        LayoutInflater li = LayoutInflater.from(mContext);
        try {
            for (int i = 0; i < data.length(); i ++) {
                JSONObject attach = data.getJSONObject(i);
                FileAttachView view = new FileAttachView(mContext);
                view.setupData(attach);
                addView(view);
            }
        } catch (JSONException e) {
            android.util.Log.e("lol", "", e);
        }
    }
}
