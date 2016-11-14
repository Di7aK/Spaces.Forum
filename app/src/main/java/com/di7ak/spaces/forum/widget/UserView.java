package com.di7ak.spaces.forum.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import org.json.JSONException;
import org.json.JSONObject;

public class UserView extends LinearLayout implements View.OnClickListener {
    private Context mContext;
    private TextView mUserName;
    private ImageView mOnline;
    private String mLink;
    
    public UserView(Context context) {
        super(context);
        mUserName = new TextView(context);
        init(context);
    }
    
    public UserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mUserName = new TextView(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        mContext = context;
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        addView(mUserName);
        
        mOnline = new ImageView(mContext);
        int w = (int)(mUserName.getTextSize() / 2);
        int h = w;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(w, h);
        float density = mContext.getResources().getDisplayMetrics().density;
        params.setMargins((int)(5 * density), 0, 0, 0);
        mOnline.setLayoutParams(params);
        
        addView(mOnline);
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
                    mUserName.setText(userName);
                }
            }
            if(data.has("onlineStatus")) {
                JSONObject onlineStatus = data.getJSONObject("onlineStatus");
                if(onlineStatus.has("is_online")) {
                    if(onlineStatus.getInt("is_online") != 0) {
                        Drawable d = mContext.getResources().getDrawable(R.drawable.bg_circle);
                        mOnline.setImageDrawable(d);
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
    
    public void setText(String text) {
        mUserName.setText(text);
    }
}
