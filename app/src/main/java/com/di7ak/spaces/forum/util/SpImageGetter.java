package com.di7ak.spaces.forum.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.widget.TextView;

public class SpImageGetter implements Html.ImageGetter {
    private TextView mTextView;

    public SpImageGetter(TextView textView) {
        mTextView  = textView;
    }

    @Override 
    public Drawable getDrawable(String source) {
        source = "http:" + source;
        final BitmapDrawablePlaceHolder mResult;
        mResult = new BitmapDrawablePlaceHolder();
        String hash = ImageDownloader.md5(source);
        new ImageDownloader(mTextView.getContext())
            .downloadImage(source, hash, null, new ImageDownloader.OnProgressListener() {

                @Override
                public void onProgress(int current, int total) {

                }

                @Override
                public void onError() {
                    
                }

                @Override
                public void onSuccess(Bitmap bitmap) {
                    try {
                        Resources resources = mTextView.getContext().getResources();
                        float density = resources.getDisplayMetrics().density;
                        int w = (int)(bitmap.getWidth() * density);
                        int h = (int)(bitmap.getHeight() * density);
                        BitmapDrawable drawable = new BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, w, h, true));

                        float height = drawable.getIntrinsicHeight();
                        float width = drawable.getIntrinsicWidth();

                        drawable.setBounds(0, 0, (int)width, (int)height);
                        mResult.setDrawable(drawable);
                        mResult.setBounds(0, 0, (int)width, (int)height);
                        
                        mTextView.setText(mTextView.getText());
                    } catch (Exception e) {
                    }
                }
            });

        return mResult;
    }

    static class BitmapDrawablePlaceHolder extends BitmapDrawable {

        protected Drawable drawable;

        @Override
        public void draw(final Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

    }
}
