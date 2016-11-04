package com.di7ak.spaces.forum.widget;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.Request;
import com.di7ak.spaces.forum.api.RequestListener;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.util.Animations;
import com.di7ak.spaces.forum.util.DownloadManager;
import com.rey.material.widget.CheckBox;
import com.rey.material.widget.FloatingActionButton;
import com.rey.material.widget.ProgressView;
import java.io.File;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class FileAttachView extends LinearLayout 
implements View.OnClickListener,
RequestListener,
DownloadManager.DownloadListener {
    private static final int STATE_DOWNLOAD = 0;
    private static final int STATE_WAIT = 1;
    private static final int STATE_CANCELL= 2;
    private static final int STATE_ERROR = 3;
    private static final int STATE_OK = 4;

    private static HashMap<String, String> urls = new HashMap<String, String>();
    private Context mContext;
    private FloatingActionButton mFabLoad;
    private android.widget.LinearLayout mProgressState;
    private ProgressView mProgress;
    private TextView mFileName;
    private TextView mFileSize;
    private TextView mDownloaded;
    private TextView mTotal;
    private String mUrl;
    private String mDownloadUrl;
    private String mNid;
    private File mFile;
    private JSONObject mDownloadBox;
    private boolean mOpenOnFinished = false;

    public FileAttachView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public FileAttachView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public void init() {
        LayoutInflater li = LayoutInflater.from(mContext);
        View view = li.inflate(R.layout.file_attach, this, true);
        mFabLoad = (FloatingActionButton) view.findViewById(R.id.fab_load);
        mFileName = (TextView) view.findViewById(R.id.filename);
        mFileSize = (TextView) view.findViewById(R.id.filesize);
        mDownloaded = (TextView) view.findViewById(R.id.downloaded);
        mTotal = (TextView) view.findViewById(R.id.total);
        mProgress = (ProgressView) view.findViewById(R.id.progress);
        mProgressState = (android.widget.LinearLayout) view.findViewById(R.id.progress_state);
        CheckBox cb = (CheckBox) view.findViewById(R.id.open_on_finished);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mOpenOnFinished = isChecked;
                }
            });

        hideDownloadState();
    }

    public void setupData(JSONObject attach) {
        try {
            if (attach.has("attach")) {
                attach = attach.getJSONObject("attach");
            }
            if (attach.has("name")) {
                String name = attach.getString("name");
                mFileName.setText(name);
            }
            if (attach.has("fileName")) {
                String name = attach.getString("fileName");
                mFileName.setText(name);
            }
            if (attach.has("nid")) {
                mNid = attach.getString("nid");
                if (urls.containsKey(mNid)) {
                    String downloadUrl = urls.get(mNid);
                    if (DownloadManager.isDownload(downloadUrl)) {
                        mProgress.setProgress(0f);
                        mProgress.start();
                        mFabLoad.setLineMorphingState(STATE_CANCELL, true);
                        DownloadManager.appendListener(downloadUrl, this);
                    }
                }
            }
            if (attach.has("weight")) {
                String size = attach.getString("weight");
                mFileSize.setText(size);
            }
            if (attach.has("URL")) {
                mUrl = attach.getString("URL");
                mFabLoad.setOnClickListener(this);
            }
            if (attach.has("downloadBox")) {
                mDownloadBox = attach.getJSONObject("downloadBox");
                if (mDownloadBox.has("playURL")) {
                    mDownloadUrl = mDownloadBox.getString("playURL");
                } else if (mDownloadBox.has("downloadURL")) {
                    mDownloadUrl = mDownloadBox.getString("downloadURL");
                }
            }
            if (attach.has("player")) {
                JSONObject player = attach.getJSONObject("player");
                if (player.has("download_url")) {
                    mDownloadUrl = player.getString("download_url");
                }
            }
        } catch (JSONException e) {

        }
    }

    private void open() {
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        String mimeType = getMimeType(mFile.toString());
        newIntent.setDataAndType(Uri.fromFile(mFile), mimeType);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivity(newIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, "No handler for this type of file.", Toast.LENGTH_LONG).show();
        }
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private void showDownloadState() {
        mProgressState.setVisibility(View.VISIBLE);
        Animations.expand(mProgressState, 0, 500);
    }

    private void hideDownloadState() {
        Animations.collapse(mProgressState, 500);
    }

    @Override
    public void onClick(View v) {
        if (mFabLoad.getLineMorphingState() == STATE_DOWNLOAD ||
            mFabLoad.getLineMorphingState() == STATE_ERROR) {
            mFabLoad.setLineMorphingState(STATE_WAIT, true);
            mProgress.setProgress(0f);
            mProgress.start();
            if (mDownloadUrl == null) {
                Request request = new Request(Uri.parse(mUrl));
                request.executeWithListener(this);
            } else startDownload();
        } else if (mFabLoad.getLineMorphingState() == STATE_CANCELL) {
            DownloadManager.cancellDownload(urls.get(mNid));
            urls.remove(mNid);
            mProgress.stop();
            hideDownloadState();
            mFabLoad.setLineMorphingState(STATE_DOWNLOAD, true);
        } else if (mFabLoad.getLineMorphingState() == STATE_OK) {
            urls.remove(mUrl);
            hideDownloadState();
            open();
        }
    }

    @Override
    public void onSuccess(JSONObject json) {
        try {
            if (json.has("info")) {
                JSONObject info = json.getJSONObject("info");
                if (info.has("file_widget")) {
                    JSONObject fileWidget = info.getJSONObject("file_widget");
                    if (fileWidget.has("downloadBox")) {
                        mDownloadBox = fileWidget.getJSONObject("downloadBox");
                        if (mDownloadBox.has("downloadURL")) {
                            mDownloadUrl = mDownloadBox.getString("downloadURL");
                            startDownload();
                        }
                    }
                }
            }
        } catch (JSONException e) {

        }
    }

    private void startDownload() {
        mFabLoad.setLineMorphingState(STATE_CANCELL, true);
        DownloadManager.download(mDownloadUrl, this);
        urls.put(mNid, mDownloadUrl);
        onProgress(0, 0);
        showDownloadState();
    }

    @Override
    public void onError(SpacesException e) {
        mFabLoad.setLineMorphingState(STATE_ERROR, true);
    }

    @Override
    public void onProgress(int downloaded, int total) {
        mDownloaded.setText(Integer.toString(downloaded / 1024) + "кб/");
        mTotal.setText(Integer.toString(total / 1024) + "кб");
        float width = 1f / total * downloaded;
        mProgress.setProgress(width);
    }

    @Override
    public void onSuccess(File file) {
        mFile = file;
        mProgress.stop();
        hideDownloadState();
        mFabLoad.setLineMorphingState(STATE_OK, true);
        if (mOpenOnFinished) open();
    }

    @Override
    public void onError() {
        //mProgress.stop();
        mFabLoad.setLineMorphingState(STATE_ERROR, true);
    }
}
