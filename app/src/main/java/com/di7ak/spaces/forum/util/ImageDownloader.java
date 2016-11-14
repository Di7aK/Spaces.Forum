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

public class ImageDownloader {
    private Context mContext;
    
    public ImageDownloader(Context context) {
        mContext = context;
    }

    public void downloadImage(final String iUrl, final String hash, final ImageView into, final OnProgressListener listener) {
        File file = new File(mContext.getExternalCacheDir(), hash);
        if (file.exists()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                
                if(into != null) into.setImageBitmap(bitmap);
                if(listener != null) {
                    listener.onProgress(1, 1);
                    listener.onSuccess(bitmap);
                }
            } catch (FileNotFoundException e) {}
        } else {
            DownloadManager.download(iUrl, new DownloadManager.DownloadListener() {

                    @Override
                    public void onProgress(int downloaded, int total) {
                        if(listener != null) listener.onProgress(downloaded, total);
                    }

                    @Override
                    public void onSuccess(File file) {
                        android.util.Log.d("lol", "error " + iUrl);
                        try {
                            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                            if(listener != null) listener.onSuccess(bitmap);
                            if(into != null) into.setImageBitmap(bitmap);
                        } catch (FileNotFoundException e) {}
                    }

                    @Override
                    public void onError() {
                        downloadImage(iUrl, hash, into, listener);
                        //if(listener != null) listener.onError();
                    }
                }, file);
        }
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
    
    public interface OnProgressListener {
        public void onProgress(int current, int total);
        
        public void onError();
        
        public void onSuccess(Bitmap bitmap);
    }
}
