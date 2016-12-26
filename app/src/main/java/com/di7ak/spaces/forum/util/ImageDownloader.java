package com.di7ak.spaces.forum.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageDownloader implements DownloadManager.DownloadListener {
    private static Map<String, List<Task>> sTasks = new HashMap<String, List<Task>>();
    private static Map<String, Boolean> sRunning = new HashMap<String, Boolean>();

    private Context mContext;
    private String mUrl;
    private String mHash;
    private Task mTask;

    public ImageDownloader(Context context) {
        mContext = context;
    }

    public ImageDownloader from(String url) {
        mUrl = url;
        return this;
    }

    public ImageDownloader into(ImageView into) {
        mTask.into = into;
        return this;
    }

    public ImageDownloader hash(String hash) {
        mHash = hash;
        mTask = new Task();
        if (!sTasks.containsKey(hash)) {
            sTasks.put(hash, new ArrayList<Task>());
        }
        sTasks.get(hash).add(mTask);
        return this;
    }

    public ImageDownloader with(OnProgressListener listener) {
        mTask.listener = listener;
        return this;
    }

    public void execute() {
        if (sRunning.containsKey(mHash)) {
            if (sRunning.get(mHash)) return;
        }
        sRunning.put(mHash, true);
        check();
    }

    private void check() {
        File file = new File(mContext.getExternalCacheDir(), mHash);
        if (file.exists()) {
            fromFile(file);
        } else load(file);
    }

    private void fromFile(File file) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            success(bitmap);
        } catch (Exception e) {
            load(file);
        }
    }

    private void load(File file) {
        DownloadManager.download(mUrl, this, file);
    }

    private void success(Bitmap bitmap) {
        List<Task> tasks = sTasks.get(mHash);
        for (Task task : tasks) {
            if (task.into != null) task.into.setImageBitmap(bitmap);
            if (task.listener != null) {
                task.listener.onProgress(1, 1);
                task.listener.onSuccess(bitmap);
            }
        }
        sTasks.remove(mHash);
        sRunning.remove(mHash);
    }

    @Override
    public void onProgress(int downloaded, int total) {
        List<Task> tasks = sTasks.get(mHash);
        for (Task task : tasks) {
            if (task.listener != null) {
                task.listener.onProgress(downloaded, total);
            }
        }
    }

    @Override
    public void onSuccess(File file) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            success(bitmap);
        } catch (Exception e) {
            //
        }
    }

    @Override
    public void onError() {
        check();
    }

    public static String md5(String s) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
	}

    public class Task {
        public OnProgressListener listener;
        public ImageView into;
    }

    public interface OnProgressListener {
        public void onProgress(int current, int total);

        public void onError();

        public void onSuccess(Bitmap bitmap);
    }
}
