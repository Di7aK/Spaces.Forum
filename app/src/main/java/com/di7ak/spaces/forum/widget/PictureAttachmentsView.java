package com.di7ak.spaces.forum.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import com.di7ak.spaces.forum.R;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PictureAttachmentsView extends LinearLayout {
    private android.widget.LinearLayout mAttachBlock;
    private Context mContext;

    public PictureAttachmentsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        LayoutInflater li = LayoutInflater.from(context);

        View view = li.inflate(R.layout.pictures_attachments, null);

        mAttachBlock = (android.widget.LinearLayout)view.findViewById(R.id.attach_block);

        addView(view);
    }

    public void setupData(JSONArray data, Picasso picasso) {
        try {
            for (int i = 0; i < data.length(); i ++) {
                JSONObject attach = data.getJSONObject(i);
                if (attach.has("attach")) {
                    attach = attach.getJSONObject("attach");
                }
                if(attach.has("preview")) {
                    float density = mContext.getResources().getDisplayMetrics().density;
                    
                    ImageView imageView = new ImageView(mContext);
                    LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    imageView.setLayoutParams(lParams);
                    int padding = (int)(5 * density);
                    imageView.setPadding(padding, padding, padding, padding);
                    
                    JSONObject preview = attach.getJSONObject("preview");
                    if(preview.has("previewURL")) {
                        String url = preview.getString("previewURL");
                        picasso.load(url).placeholder(R.color.placeholder).into(imageView);
                    }
                    if(preview.has("size")) {
                        JSONObject size = preview.getJSONObject("size");
                        int width = 0, height = 0;
                        if(size.has("width")) {
                            width = (int)(size.getInt("width") * density);
                        }
                        if(size.has("height")) {
                            height = (int)(size.getInt("height") * density);
                        }
                        if(width != 0 && height != 0) {
                            lParams = new LinearLayout.LayoutParams(width, height);
                            imageView.setLayoutParams(lParams);
                        }
                    }
                    mAttachBlock.addView(imageView);
                }
            }
        } catch (JSONException e) {

        }
    }
}
