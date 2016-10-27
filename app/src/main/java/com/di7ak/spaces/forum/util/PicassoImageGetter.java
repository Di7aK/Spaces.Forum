package com.di7ak.spaces.forum.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import android.view.Gravity;

public class PicassoImageGetter implements Html.ImageGetter {

final Resources resources;
final Picasso pablo;
final TextView textView;

public PicassoImageGetter(final TextView textView, final Resources resources, final Picasso pablo) {
    this.textView  = textView;
    this.resources = resources;
    this.pablo     = pablo;
}

@Override public Drawable getDrawable(final String source) {
    final BitmapDrawablePlaceHolder result = new BitmapDrawablePlaceHolder();

    new AsyncTask<Void, Void, Bitmap>() {

        @Override
        protected Bitmap doInBackground(final Void... meh) {
            try {
                
                return pablo.load("http:" + source).get();
            } catch (Exception e) {
                
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            try {
                float density = resources.getDisplayMetrics().density;
                int w = (int)(bitmap.getWidth() * density);
                int h = (int)(bitmap.getHeight() * density);
                BitmapDrawable drawable = new BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, w, h, true));
                
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                //drawable.setGravity(Gravity.CENTER_VERTICAL);
                
                result.setDrawable(drawable);
                
                result.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                textView.setText(textView.getText());
                //textView.invalidate();//doesn't work correctly...
            } catch (Exception e) {
                /* nom nom nom*/
            }
        }

    }.execute((Void) null);

    return result;
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
