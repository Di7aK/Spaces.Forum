package com.di7ak.spaces.forum.widget;

import android.content.Context;
import android.util.AttributeSet;
import com.di7ak.spaces.forum.util.ImageDownloader;
import de.hdodenhof.circleimageview.CircleImageView;
import org.json.JSONException;
import org.json.JSONObject;

public class AvatarView extends CircleImageView {
    
    public AvatarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setupData(JSONObject data) {
        try {
            if (data.has("previewURL")) {
                String url = data.getString("previewURL");
                setUrl(url);
            }
        } catch (JSONException e) {

        }
    }
    
    public void setUrl(String url) {
        if(url == null) return;
        url = url.replace("41.40", "81.80");
        String hash = ImageDownloader.md5(url);
        new ImageDownloader(getContext()).downloadImage(url, hash, this, null);
    }
}
