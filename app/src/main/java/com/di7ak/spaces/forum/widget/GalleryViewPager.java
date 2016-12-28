package com.di7ak.spaces.forum.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import com.di7ak.spaces.forum.R;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class GalleryViewPager extends ViewPager {

public GalleryViewPager(Context context) {
    super(context);
}

public GalleryViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
}

@Override
protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
    int current = getCurrentItem();
    if(current < getChildCount()) {
        View page = getChildAt(current);
        ImageViewTouch imageView = (ImageViewTouch) page.findViewById(R.id.image);
        return imageView.canScroll();
    } else {
        return super.canScroll(v, checkV, dx, x, y);
    }
}

}
