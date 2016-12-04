package com.di7ak.spaces.forum.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import com.di7ak.spaces.forum.GalleryActivity;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.util.ImageDownloader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PictureAttachmentsView extends LinearLayout implements View.OnClickListener {
    private android.widget.LinearLayout mAttachBlock;
    private Context mContext;
    private JSONObject mSource;
    private JSONArray mAttachments;

    public PictureAttachmentsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        LayoutInflater li = LayoutInflater.from(context);

        View view = li.inflate(R.layout.pictures_attachments, this, true);

        mAttachBlock = (android.widget.LinearLayout)view.findViewById(R.id.attach_block);
        
        mSource = new JSONObject();
        mAttachments = new JSONArray();
        try {
            mSource.put("attachments", mAttachments);
        } catch (JSONException e) {}
    }

    public void setupData(JSONArray data) {
        try {
            for (int i = 0; i < data.length(); i ++) {
                JSONObject attach = data.getJSONObject(i);
                if (attach.has("attach")) {
                    attach = attach.getJSONObject("attach");
                }
                mAttachments.put(attach);
                if(attach.getInt("type") == 7) {
                    PictureAttachmentView item = new PictureAttachmentView(mContext);
                    item.setupData(attach);
                    mAttachBlock.addView(item);
                    item.setOnClickListener(this);
                    item.setTag(mAttachments.length() - 1);
                } else {
                    VideoAttachmentView item = new VideoAttachmentView(mContext);
                    item.setupData(attach);
                    mAttachBlock.addView(item);
                }
            }
        } catch (JSONException e) {

        }
    }
    
    public JSONArray getAttachments() {
        return mAttachments;
    }
    
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(mContext, GalleryActivity.class);
        intent.putExtra("attachments", mSource.toString());
        intent.putExtra("current", (Integer)v.getTag());
        mContext.startActivity(intent);
    }
}
