package com.di7ak.spaces.forum;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.util.ImageDownloader;
import com.rey.material.widget.ProgressView;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GalleryActivity extends Activity {
    ImageDownloader mImageLoader;
    
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        mImageLoader = new ImageDownloader(this);
        
        Bundle extra = getIntent().getExtras();
        String source = extra.getString("attachments");
        int current = extra.getInt("current");
        try {
            JSONObject json = new JSONObject(source);
            JSONArray attachments = json.getJSONArray("attachments");
            View currentView = fromJson(attachments.getJSONObject(current));
            setContentView(currentView);
        } catch (JSONException e) {}
    }
    
    private View fromJson(JSONObject json) {
        LayoutInflater li = getLayoutInflater();
        View view = li.inflate(R.layout.gallery_item, null);
        ImageViewTouch imageView = (ImageViewTouch) view.findViewById(R.id.image);
        final ProgressView progress = (ProgressView) view.findViewById(R.id.progress);
        try {
            if(json.has("player")) {
                JSONObject player = json.getJSONObject("player");
                
            }
            if(json.has("preview")) {
                JSONObject preview = json.getJSONObject("preview");
                if(preview.has("downloadLink")) {
                    String downloadLink = preview.getString("downloadLink");
                    progress.start();
                    progress.setProgress(0.01f);
                    mImageLoader.downloadImage(downloadLink, imageView, new ImageDownloader.OnProgressListener() {

                            @Override
                            public void onProgress(int current, int total) {
                                if(current < total) {
                                    float p = 1f / (float)total * (float)current;
                                    progress.setProgress(p);
                                } else progress.stop();
                            }
                    });
                }
                if(preview.has("filename")) {
                    
                }
            }
        } catch(JSONException e) {
            
        }
        return view;
    }
    
}
