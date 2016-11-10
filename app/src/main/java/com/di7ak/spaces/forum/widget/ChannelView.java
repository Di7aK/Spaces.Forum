package com.di7ak.spaces.forum.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import org.json.JSONException;
import org.json.JSONObject;

public class ChannelView extends LinearLayout implements View.OnClickListener {
    private Context mContext;
    private TextView mName;
    private String mUrl;
    
    public ChannelView(Context context) {
        super(context);
        init(context);
    }
    
    public ChannelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public void init(Context context) {
        mContext = context;
        setVisibility(GONE);
        float density = mContext.getResources().getDisplayMetrics().density;
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        
        ImageView icon = new ImageView(mContext);
        icon.setImageResource(R.drawable.ic_rss_feed_black_18dp);
        int w = (int)(density * 28);
        int h = w;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(w, h);
        icon.setLayoutParams(params);
        int padding = (int)(3 * density);
        icon.setPadding(padding, padding, padding, padding);
        
        mName = new TextView(mContext);
        mName.setPadding(padding, padding, padding, padding);
        int size = (int)(density * 18);
        mName.setTextSize(size);
        mName.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
        
        addView(mName);
        addView(icon);
    }
    
    public void setupData(JSONObject json) {
        try {
            if(json.has("channel_name")) {
                String name = json.getString("channel_name");
                mName.setText(name);
                setVisibility(VISIBLE);
            }
            if(json.has("channel_link")) {
                mUrl = json.getString("channel_link");
                setOnClickListener(this);
            }
        } catch(JSONException e) {}
    }
    
    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(mUrl));
        mContext.startActivity(intent);
    }
}
