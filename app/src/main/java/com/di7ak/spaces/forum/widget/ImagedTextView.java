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

public class ImagedTextView extends LinearLayout {
    private Context mContext;
    private ImageView mIcon;
    private TextView mText;
    
    public ImagedTextView(Context context) {
        super(context);
        init(context);
    }
    
    public ImagedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public void init(Context context) {
        mContext = context;
        setVisibility(INVISIBLE);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);
        float density = mContext.getResources().getDisplayMetrics().density;
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        
        mIcon = new ImageView(mContext);
        int w = (int)(density * 28);
        int h = w;
        params = new LayoutParams(w, h);
        mIcon.setLayoutParams(params);
        int padding = (int)(3 * density);
        mIcon.setPadding(padding, padding, padding, padding);
        
        mText = new TextView(mContext);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mText.setLayoutParams(params);
        mText.setPadding(padding, padding, padding, padding);
        int size = (int)(18);
        mText.setTextSize(size);
        mText.setTextColor(0xff000000);
        
        addView(mIcon);
        addView(mText);
    }
    
    public void setText(String text) {
        mText.setText(text);
        setVisibility(VISIBLE);
    }
    
    public void setIcon(int res) {
        mIcon.setImageResource(res);
    }
    
}
