package com.di7ak.spaces.forum.widget;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.ViewerActivity;
import com.di7ak.spaces.forum.api.AttachData;
import com.squareup.picasso.Picasso;

public class AttachWidget implements View.OnClickListener {
    View view;
    Activity activity;
    Picasso picasso;
    AttachData data;
    
    public AttachWidget(AttachData data, Activity activity, Picasso picasso) {
        this.data = data;
        this.activity = activity;
        this.picasso = picasso;
        LayoutInflater li = activity.getLayoutInflater();
        if(data.fileext.equals("jpg") ||
            data.fileext.equals("png")) {
            view = li.inflate(R.layout.pictures, null);
            picasso.load(data.previewUrl)
                .into((RatioImageView)view.findViewById(R.id.preview));
            view.findViewById(R.id.preview).setOnClickListener(this);
        }
        
    }
    
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(activity, ViewerActivity.class);
        intent.putExtra("download_url", data.downloadLink);
        activity.startActivity(intent);
    }
    
    public View getView() {
        return view;
    }
}
