package com.di7ak.spaces.forum.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import org.json.JSONException;
import org.json.JSONObject;

public class UserView extends TextView implements View.OnClickListener {
    private Context mContext;
    private String mLink;
    
    public UserView(Context context) {
        super(context);
        init(context);
    }
    
    public UserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        mContext = context;
    }
    
    public void setupData(JSONObject data) {
        try {
            if(data.has("siteLink")) {
                JSONObject siteLink = data.getJSONObject("siteLink");
                if(siteLink.has("mysite_link")) {
                    mLink = siteLink.getString("mysite_link");
                    setOnClickListener(this);
                }
                if(siteLink.has("user_name")) {
                    String userName = siteLink.getString("user_name");
                    setText(userName);
                }
            }
            if(data.has("onlineStatus")) {
                JSONObject onlineStatus = data.getJSONObject("onlineStatus");
                if(onlineStatus.has("is_online")) {
                    if(onlineStatus.getInt("is_online") != 0) {
                        Drawable d = mContext.getResources().getDrawable(R.drawable.bg_circle);
                        setCompoundDrawables(null, null, d, null);
                    }
                }
            }
        } catch(JSONException e) {}
    }
    
    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(mLink));
        mContext.startActivity(intent);
    }
}
