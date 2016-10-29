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
    ImageView view;
    Activity activity;
    Picasso picasso;
    AttachData data;
    
    public AttachWidget(AttachData data, Activity activity, Picasso picasso) {
        this.data = data;
        this.activity = activity;
        this.picasso = picasso;
        
        if(data != null && data.fileext != null && data.fileext.equals("jpg") ||
            data.fileext.equals("png")) {
            view = new ImageView(activity);
           
            float density = activity.getResources().getDisplayMetrics().density;
            int width = (int)(data.width * density);
            int height = (int)(data.height * density);
            if(width == 0) width = LinearLayout.LayoutParams.WRAP_CONTENT;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    width, height);
            view.setLayoutParams(layoutParams);
            int padding = (int)(5 * density);
            view.setPadding(padding, padding, padding, padding);
            view.setScaleType(ImageView.ScaleType.FIT_XY);
            picasso.load(data.previewUrl)
                .placeholder(R.color.placeholder)
                .into(view);
                
            view.setOnClickListener(this);
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
