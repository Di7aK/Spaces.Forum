package com.di7ak.spaces.forum.widget;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import com.di7ak.spaces.forum.util.ImageDownloader;
import org.json.JSONException;
import org.json.JSONObject;

public class PictureAttachmentView extends ImageView {
    private Context mContext;

    public PictureAttachmentView(Context context) {
        super(context);
        mContext = context;
    }

    public void setupData(JSONObject attach) {
        try {
            if (attach.has("preview")) {
                JSONObject preview = attach.getJSONObject("preview");

                float density = mContext.getResources().getDisplayMetrics().density;

                LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
                setLayoutParams(lParams);
                int padding = (int)(5 * density);
                setPadding(padding, padding, padding, padding);


                if (preview.has("previewURL")) {
                    String url = preview.getString("previewURL");
                    Uri uri = Uri.parse(url);
                    String query = uri.getQuery();
                    url = url.replace(query, "");

                    String hash = ImageDownloader.md5(url);
                    new ImageDownloader(mContext).downloadImage(url, hash, this, null);
                }
                if (preview.has("size")) {
                    JSONObject size = preview.getJSONObject("size");
                    int width = 0, height = 0;
                    if (size.has("width")) {
                        width = (int)(size.getInt("width") * density);
                    }
                    if (size.has("height")) {
                        height = (int)(size.getInt("height") * density);
                    }
                    if (width != 0 && height != 0) {
                        lParams = new LinearLayout.LayoutParams(width, height);
                        setLayoutParams(lParams);
                    }
                }
            } 
        } catch (JSONException e) {}
    }
}
