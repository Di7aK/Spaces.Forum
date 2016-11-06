package com.di7ak.spaces.forum.widget;

import android.content.Context;
import android.util.AttributeSet;
import org.json.JSONException;
import org.json.JSONObject;

public class PaginationView extends LinearLayout {
    private OnPageChangedListener mListener;
    private Context mContext;
    private int mCurrentPage;
    private int mLastPage;
    private int mItemsOnPage;
    private String mNextPage;
    private String mPreviousPage;
    private String mUrl;
    private String mPageParam;
    
    public PaginationView(Context context) {
        super(context);
        init(context);
    }
    
    public PaginationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public void init(Context context) {
        mContext = context;
    }
    
    public void show() {
        
    }
    
    public void hide() {
        
    }
    
    public void setupData(JSONObject data) {
        try {
            if(data.has("page_param")) {
                mPageParam = data.getString("page_param");
            }
            if(data.has("current_page")) {
                mCurrentPage = data.getInt("current_page");
            }
            if(data.has("last_page")) {
                mLastPage = data.getInt("last_page");
            }
            if(data.has("items_on_page")) {
                mItemsOnPage = data.getInt("items_on_page");
            }
            if(data.has("next_link_url")) {
                mNextPage = data.getString("next_link_url");
            } else mNextPage = null;
            if(data.has("prev_link_url")) {
                mPreviousPage = data.getString("prev_link_url");
            } else mPreviousPage = null;
            if(data.has("url")) {
                mUrl = data.getString("url");
            }
        } catch(JSONException e) {
            
        }
    }
    
    public void setOnPageChangedListener(OnPageChangedListener listener) {
        mListener = listener;
    }
    
    public int getCurrentPage() {
        return mCurrentPage;
    }
    
    public boolean hasNextPage() {
        return mNextPage != null;
    }
    
    public boolean hasPreviousPage() {
        return mPreviousPage != null;
    }
    
    public String getNextPageUrl() {
        return mNextPage;
    }
    
    public String getPreviousPageUrl() {
        return mPreviousPage;
    }
    
    public interface OnPageChangedListener {
        
        public void onPageChanged(String url);
    }
}
