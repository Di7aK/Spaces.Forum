package com.di7ak.spaces.forum;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.di7ak.spaces.forum.widget.RatioImageView;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class ViewerActivity extends AppCompatActivity {
    
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        String url = extra.getString("download_url");
        setContentView(R.layout.pictures);
        
        View root = getWindow().getDecorView();
        root.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        
        root.setBackgroundColor(0xff000000);
        
        RatioImageView imageView = (RatioImageView)findViewById(R.id.preview);
        
        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        
        imageView.setBackgroundColor(0xff000000);
        
        
        Picasso picasso = new Picasso.Builder(this) 
            .loggingEnabled(BuildConfig.DEBUG) 
            .indicatorsEnabled(BuildConfig.DEBUG) 
            .downloader(new OkHttpDownloader(this, 100000)) 
            .build();
        picasso.load(url).into(imageView);
    }
}
