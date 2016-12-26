package com.di7ak.spaces.forum.widget;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.util.ImageDownloader;
import org.json.JSONException;
import org.json.JSONObject;

public class VideoAttachmentView extends RelativeLayout implements View.OnClickListener {
    private Context mContext;
    private ImageView mPreview;
    private TextView mFileName;
    private TextView mDuration;
    private String mDownloadLink;

    public VideoAttachmentView(Context context) {
        super(context);
        init(context);
    }

    public VideoAttachmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        mContext = context;

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);

        mPreview = new ImageView(mContext);
        layoutParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mPreview.setLayoutParams(layoutParams);
        addView(mPreview);

        ImageView play = new ImageView(mContext);
        layoutParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        play.setLayoutParams(layoutParams);
        play.setImageResource(R.drawable.ic_play);
        addView(play);

        float density = mContext.getResources().getDisplayMetrics().density;
        int padding = (int)(3 * density);

        mFileName = new TextView(mContext);
        mFileName.setTextColor(0xffffffff);
        mFileName.setPadding(padding, padding, padding, padding);
        layoutParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        mFileName.setLayoutParams(layoutParams);
        //addView(mFileName);

        mDuration = new TextView(mContext);
        mDuration.setTextColor(0xffffffff);
        mDuration.setBackgroundColor(0x77000000);
        mDuration.setPadding(padding, padding, padding, padding);
        layoutParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        mDuration.setLayoutParams(layoutParams);
        addView(mDuration);
    }

    public void setupData(JSONObject attach) {
        try {
            if (attach.has("player") || attach.has("preview")) {
                JSONObject player = attach.has("player") ? attach.getJSONObject("player") : attach.getJSONObject("preview");
                if (player.has("showLink")) {
                    String previewUrl = player.getString("showLink");
                    String hash = ImageDownloader.md5(previewUrl);
                    new ImageDownloader(getContext()).hash(hash).from(previewUrl).into(mPreview).execute();
                }
                if(player.has("player")) {
                    JSONObject player2 = player.getJSONObject("player");
                    if(player2.has("externalVideo")) {
                        JSONObject ext = player2.getJSONObject("externalVideo");
                        String id = ext.getString("id");
                        mDownloadLink = "https://youtube.com/watch?v=" + id;
                        mPreview.setOnClickListener(this);
                    }
                }
                if (player.has("downloadLink")) {
                    mDownloadLink = player.getString("downloadLink");
                    mPreview.setOnClickListener(this);
                }
                if (player.has("filename")) {
                    String fileName = player.getString("filename");
                    mFileName.setText(fileName);
                }
                if (player.has("fileext")) {
                    //fileExt = player.getString("fileext");
                }
                if (player.has("duration")) {
                    mDuration.setText(player.getString("duration"));
                }
                if (player.has("size")) {
                    JSONObject size = player.getJSONObject("size");
                    int width = 0, height = 0;
                    float density = mContext.getResources().getDisplayMetrics().density;
                    if (size.has("width")) {
                        width = (int)(size.getInt("width") * density);
                    }
                    if (size.has("height")) {
                        height = (int)(size.getInt("height") * density);
                    }
                    if (width != 0 && height != 0) {
                        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams)mPreview.getLayoutParams();
                        lParams.width = width;
                        lParams.height = height;
                        mPreview.setLayoutParams(lParams);
                    }
                }
            }
        } catch (JSONException e) {

        }
    }

    @Override
    public void onClick(View v) {
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        String mimeType = FileAttachView.getMimeType(mDownloadLink);
        newIntent.setDataAndType(Uri.parse(mDownloadLink), mimeType);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivity(newIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, "No handler for this type of file.", Toast.LENGTH_LONG).show();
        }
    }

}
