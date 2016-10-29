package com.di7ak.spaces.forum.widget;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.ViewerActivity;
import com.di7ak.spaces.forum.api.AttachData;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class PictureAttach implements View.OnClickListener {
    ImageView view;
    Activity activity;
    Picasso picasso;
    List<String> names;
    List<String> urls;
    String current;

    public PictureAttach(AttachData data, List<String> names, List<String> urls, int index, Activity activity, Picasso picasso) {
        this.names = names;
        this.urls = urls;
        this.activity = activity;
        this.picasso = picasso;

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
            current = data.downloadLink;
    }



    @Override
    public void onClick(View v) {
        Intent intent = new Intent(activity, ViewerActivity.class);
        intent.putExtra("urls", (ArrayList<String>)urls);
        intent.putExtra("names", (ArrayList<String>)names);
        intent.putExtra("current", current);
        activity.startActivity(intent);
    }

    public View getView() {
        return view;
    }
}
