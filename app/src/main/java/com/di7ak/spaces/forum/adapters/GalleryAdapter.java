package com.di7ak.spaces.forum.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.di7ak.spaces.forum.util.ImageDownloader;
import com.di7ak.spaces.forum.widget.GalleryItemView;
import org.json.JSONArray;
import org.json.JSONException;

public class GalleryAdapter extends PagerAdapter {
    private Context mContext;
    private JSONArray mItems;

    public GalleryAdapter(Context context, JSONArray items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        GalleryItemView view = new GalleryItemView(mContext);
        try {
            view.setupData(mItems.getJSONObject(position));
        } catch (JSONException e) {}
        collection.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return mItems.length();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

}
