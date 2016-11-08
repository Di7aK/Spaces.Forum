package com.di7ak.spaces.forum;

import android.app.Activity;
import android.os.Bundle;
import com.di7ak.spaces.forum.widget.GalleryViewPager;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.adapters.GalleryAdapter;
import com.di7ak.spaces.forum.page_transformers.ZoomOutPageTransformer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GalleryActivity extends Activity {
    private GalleryViewPager mPager;
    private GalleryAdapter mAdapter;
    
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.gallery_new);
        
        mPager = (GalleryViewPager) findViewById(R.id.pager);
        //mPager.setPageTransformer(true, new ZoomOutPageTransformer());
        
        Bundle extra = getIntent().getExtras();
        String source = extra.getString("attachments");
        int current = extra.getInt("current");
        try {
            JSONObject json = new JSONObject(source);
            JSONArray attachments = json.getJSONArray("attachments");
            mAdapter = new GalleryAdapter(this, attachments);
            mPager.setAdapter(mAdapter);
            mPager.setCurrentItem(current);
        } catch (JSONException e) {}
    }
    
    
    
}
