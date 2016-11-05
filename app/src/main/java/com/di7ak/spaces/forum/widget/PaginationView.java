package com.di7ak.spaces.forum.widget;

import android.content.Context;
import org.json.JSONObject;

public class PaginationView extends LinearLayout {
    private OnPageChangedListener mListener;
    
    public PaginationView(Context context) {
        super(context);
    }
    
    public void show() {
        
    }
    
    public void hide() {
        
    }
    
    public void setupData(JSONObject data) {
        
    }
    
    public void setOnPageChangedListener(OnPageChangedListener listener) {
        mListener = listener;
    }
    
    public boolean hasNextPage() {
        return false;
    }
    
    public boolean hasPreviousPage() {
        return false;
    }
    
    public String getNextPageUrl() {
        return null;
    }
    
    public String getPreviousPageUrl() {
        return null;
    }
    
    public interface OnPageChangedListener {
        
        public void onPageChanged(String url);
    }
}
