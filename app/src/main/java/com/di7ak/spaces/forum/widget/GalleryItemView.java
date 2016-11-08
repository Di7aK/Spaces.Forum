package com.di7ak.spaces.forum.widget;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.util.ImageDownloader;
import com.rey.material.widget.ProgressView;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import org.json.JSONException;
import org.json.JSONObject;

public class GalleryItemView extends LinearLayout implements ImageDownloader.OnProgressListener {
    private Context mContext;
    private ImageViewTouch mImageView;
    private ProgressView mProgress;
    private String mDownloadLink;

    public GalleryItemView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.gallery_item, null);
        mImageView = (ImageViewTouch) view.findViewById(R.id.image);
        mProgress = (ProgressView) view.findViewById(R.id.progress);
        addView(view);
    }

    public void setupData(JSONObject json) {
        try {
            if (json.has("player")) {
                JSONObject player = json.getJSONObject("player");

            }
            if (json.has("preview")) {
                JSONObject preview = json.getJSONObject("preview");
                if (preview.has("downloadLink")) {
                    mDownloadLink = preview.getString("downloadLink");
                    load();
                }
                if (preview.has("filename")) {

                }
            }
        } catch (JSONException e) {

        }
    }

    private void load() {
        mProgress.start();
        mProgress.setProgress(0.01f);
        Uri uri = Uri.parse(mDownloadLink);
        String[] segments = uri.getPath().split("/");
        String np = mDownloadLink.replace(segments[3], "time");
        String hash = ImageDownloader.md5(np);
        new ImageDownloader(mContext).downloadImage(mDownloadLink, hash, mImageView, this);
    }

    @Override
    public void onProgress(int current, int total) {
        if (current < total) {
            float p = 1f / (float)total * (float)current;
            mProgress.setProgress(p);
        } else mProgress.stop();
    }
    
    @Override
    public void onError() {
        load();
    }
}
