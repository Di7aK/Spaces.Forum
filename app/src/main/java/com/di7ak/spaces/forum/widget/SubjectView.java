package com.di7ak.spaces.forum.widget;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;
import com.di7ak.spaces.forum.util.SpImageGetter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SubjectView extends LinearLayout {
    private static final String START_ATTACH_TAG = "<script type=\"spaces/file\">";
    private static final String END_TAG = "</script>";

    private Context mContext;
    private AttributeSet mAttrs;
    private PictureAttachmentsView mAttachments;

    public SubjectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mAttrs = attrs;
        setOrientation(LinearLayout.VERTICAL);
        LayoutParams lParams = new LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT);
        setLayoutParams(lParams);
    }
    
    public void setAttachments(PictureAttachmentsView attachments) {
        mAttachments = attachments;
    }

    public void setText(String subject) {
        int start;
        int offset = 0;
        boolean found;
        while (true) {
            start = subject.indexOf(START_ATTACH_TAG, offset);
            if (!(found = !(start == -1))) {
                start = subject.length();
            }
            
            String text = subject.substring(offset, start);
            TextView tv = new TextView(mContext, mAttrs);
            tv.setTextSize(20);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            Spanned sText = Html.fromHtml(text, new SpImageGetter(tv), null);
            tv.setText(sText);
            addView(tv);
            
            if (found) {
                start += START_ATTACH_TAG.length();
                offset = subject.indexOf(END_TAG, start);
                if (offset == -1) break;
                String tag = subject.substring(start, offset);
                offset += END_TAG.length();

                try {
                    JSONObject jo = new JSONObject(tag);
                    jo = jo.getJSONObject("attach");
                    if(jo.getInt("type") == 7) {
                        PictureAttachmentView pa = new PictureAttachmentView(mContext);
                        pa.setupData(jo);
                        addView(pa);
                        pa.setOnClickListener(mAttachments);
                        mAttachments.getAttachments().put(jo);
                        pa.setTag(mAttachments.getAttachments().length());
                    } else if(jo.getInt("type") == 5 || jo.getInt("type") == 6) {
                        FileAttachView view = new FileAttachView(mContext);
                        view.setupData(jo);
                        addView(view);
                    } else if(jo.getInt("type") == 25) {
                        VideoAttachmentView view = new VideoAttachmentView(mContext);
                        view.setupData(jo);
                        addView(view);
                    } else {
                        tv = new TextView(mContext, mAttrs);
                        tv.setTextSize(20);
                        tv.setMovementMethod(LinkMovementMethod.getInstance());
                        sText = Html.fromHtml(tag, new SpImageGetter(tv), null);
                        tv.setText(sText);
                        addView(tv);
                    }
                } catch(JSONException e) {}
            } else break;
        }
    }

}
