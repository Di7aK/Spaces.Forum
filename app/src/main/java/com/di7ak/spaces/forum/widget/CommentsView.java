package com.di7ak.spaces.forum.widget;

import android.content.Context;
import android.net.Uri;
import android.support.v4.widget.NestedScrollView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.di7ak.spaces.forum.NotificationManager;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.Request;
import com.di7ak.spaces.forum.api.RequestListener;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.rey.material.widget.Button;
import com.rey.material.widget.ProgressView;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommentsView extends LinearLayout 
        implements NestedScrollView.OnScrollChangeListener, 
        RequestListener,
        View.OnClickListener,
        CommentView.OnButtonClick,
        NotificationManager.OnNewNotification {
    private Context mContext;
    private Session mSession;
    private AddCommentView mCommentPanel;
    private android.widget.LinearLayout mCommentsList;
    private TextView mCommentsCount;
    private RelativeLayout mLoadNextIndicator;
    private ProgressView mProgressNext;
    private RelativeLayout mLoadPrevIndicator;
    private ProgressView mProgressPrev;
    private NestedScrollView mScrollView;
    private Button mBtnLoadPrev;
    private Button mBtnLoadNext;
    private int mCount;
    private PaginationView mPagination;
    private List<Integer> mShowingIds;
    private boolean mLoadNext = true;
    private boolean mLoadPrev = false;
    private String mCurrentDate = "";
    private String mFirstDate = "";
    private String mCurrentUrl;
    private int mFirstPage;
    private int mLastPage;
    private int mObjectId;

    public CommentsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.comments, this, true);
        mCommentsList = (android.widget.LinearLayout) view.findViewById(R.id.comments_list);
        mCommentsCount = (TextView) view.findViewById(R.id.comments_cnt);
        LayoutInflater li = LayoutInflater.from(mContext);
        mLoadNextIndicator = (RelativeLayout) li.inflate(R.layout.pagination_load, mCommentsList, false);
        mProgressNext = (ProgressView) mLoadNextIndicator.findViewById(R.id.pagination_load_indicator);
        mBtnLoadNext = (Button) mLoadNextIndicator.findViewById(R.id.btn_more);
        mLoadPrevIndicator = (RelativeLayout) li.inflate(R.layout.pagination_load, mCommentsList, false);
        mProgressPrev = (ProgressView) mLoadPrevIndicator.findViewById(R.id.pagination_load_indicator);
        mBtnLoadPrev = (Button) mLoadPrevIndicator.findViewById(R.id.btn_more);
        
        mBtnLoadNext.setOnClickListener(this);
        mBtnLoadPrev.setOnClickListener(this);
        
        mPagination = new PaginationView(mContext);
        mShowingIds = new ArrayList<Integer>();
    }
    
    @Override
    public boolean onNewNotification(JSONObject message) {
        try {
            if (message.has("text")) {
                JSONObject text = message.getJSONObject("text");
                if (text.has("act")) {
                    int act = text.getInt("act");
                    if (act == 40) {
                        if(text.has("objectId")) {
                            int objectId = text.getInt("objectId");
                            if(objectId == mObjectId) {
                                updateComments();
                                return true;
                            }
                        }
                    } 
                }
            }
        } catch (JSONException e) {

        }
        return false;
    }
    
    public void setUrl(String url) {
        mCurrentUrl = url;
    }
    
    public void setCommentPanel(AddCommentView view) {
        mCommentPanel = view;
        setPadding(0, 0, 0,
            (int)(mContext.getResources().getDisplayMetrics().density * 30));
    }

    public void setupData(JSONObject data, Session session) {
        mSession = session;
        try {
            
            if (data.has("commentForm") && !data.isNull("commentForm")) {

            }
            if (data.has("comments")) {
                JSONObject comments = data.getJSONObject("comments");
                if (comments.has("comments_cnt")) {
                    mCount = comments.getInt("comments_cnt");
                    updateCommentsCount();
                }
                if(comments.has("type")) {
                    mCommentPanel.setCommentType(comments.getInt("type"));
                }
                if(comments.has("objectId")) {
                    mObjectId = comments.getInt("objectId");
                    mCommentPanel.setObjectId(mObjectId);
                }
                if (comments.has("comments_list")) {
                    JSONArray commentsList = comments.getJSONArray("comments_list");
                    for (int i = 0; i < commentsList.length(); i ++) {
                        JSONObject comment = commentsList.getJSONObject(mLoadNext ? i : (commentsList.length() - i - 1));
                        CommentView view = new CommentView(mContext);
                        view.setupData(comment, session);
                        if(!mShowingIds.contains(view.getCommentId())) {
                            view.setOnButtonClickListener(this);
                            if(mLoadNext) {
                                if(!view.getCommentDate().equals(mCurrentDate)) {
                                    View date = createDateView(view.getCommentDate());
                                    mCommentsList.addView(date);
                                    mCurrentDate = view.getCommentDate();
                                    if(TextUtils.isEmpty(mFirstDate)) mFirstDate = mCurrentDate;
                                }
                                mCommentsList.addView(view);
                            }
                            if(mLoadPrev) {
                                if(!view.getCommentDate().equals(mFirstDate)) {
                                    View date = createDateView(view.getCommentDate());
                                    mCommentsList.addView(date, 0);
                                    
                                }
                                mCommentsList.addView(view, 1);
                                
                            }
                            
                            view.setTag(mPagination.getCurrentPage());
                            mShowingIds.add(view.getCommentId());
                        }
                    }
                }
                if (data.has("pagination") && data.get("pagination") instanceof JSONObject) {
                    mPagination.setupData(data.getJSONObject("pagination"));
                    if(mLoadPrev || mFirstPage == 0) {
                        mFirstPage = mPagination.getCurrentPage();
                        if(mPagination.hasPreviousPage()) addLoadPrevIndicator();
                        mLoadPrev = false;
                    }
                    if(mLoadNext) {
                        mLastPage = mPagination.getCurrentPage();
                        if(mPagination.hasNextPage()) addLoadNextIndicator();
                        mLoadNext = false;
                    }
                }
                
            }
        } catch (JSONException e) {
            android.util.Log.e("lol", "", e);
        }
    }
    
    @Override
    public void onButtonClick(int id, int type) {
        mCommentPanel.setReply(id);
    }
    
    @Override
    public void onClick(View v) {
        if(v.equals(mBtnLoadNext)) readNextComments();
        if(v.equals(mBtnLoadPrev)) readPrevComments();
    }
    
    public void updateComments() {
        String url = mPagination.getCurrentPageUrl();
        if(url == null) url = mCurrentUrl;
        readComments(url);
        
        mLoadNext = true;
        
    }
    
    public void readPrevComments() {
        
        readComments(mPagination.getPreviousPageUrl());
        mBtnLoadPrev.setVisibility(View.INVISIBLE);
        mLoadPrev = true;
        mProgressPrev.start();
    }
    
    public void readNextComments() {
        readComments(mPagination.getNextPageUrl());
        mBtnLoadNext.setVisibility(View.INVISIBLE);
        mLoadNext = true;
        mProgressNext.start();
    }
    
    @Override
    public void onScrollChange(NestedScrollView v, int x, int y, int p4, int p5) {
        mScrollView = v;
        if (y + 50 > (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
            if(!mLoadNext && !mLoadPrev && mPagination.hasNextPage()) {
                readNextComments();
            }
        }
    }
    
    private void addLoadPrevIndicator() {
        mCommentsList.addView(mLoadPrevIndicator, 0);
        mBtnLoadPrev.setVisibility(View.VISIBLE);
    }
    
    private void removeLoadPrevIndicator() {
        mCommentsList.removeView(mLoadPrevIndicator);
        mProgressPrev.stop();
    }
    
    private void addLoadNextIndicator() {
        mCommentsList.addView(mLoadNextIndicator);
        mBtnLoadNext.setVisibility(View.VISIBLE);
    }
    
    private void removeLoadNextIndicator() {
        mCommentsList.removeView(mLoadNextIndicator);
        mProgressNext.stop();
    }
    
    private View createDateView(String date) {
        LayoutInflater li = LayoutInflater.from(mContext);
        View v = li.inflate(R.layout.date, mCommentsList, false);
        ((TextView)v.findViewById(R.id.comments_date)).setText(date);
        return v;
    }
    
    private void readComments(String url) {
        Request request = new Request(Uri.parse(url));
        request.executeWithListener(this);
    }
    
    @Override
    public void onSuccess(JSONObject json) {
        if(mLoadNext) removeLoadNextIndicator();
        if(mLoadPrev) removeLoadPrevIndicator();
        try {
            if(json.has("commentsBlock")) {
                JSONObject commentsBlock = json.getJSONObject("commentsBlock");
                setupData(commentsBlock, mSession);
            }
        } catch(JSONException e) {
            
        }
    }

    @Override
    public void onError(SpacesException e) {
        if(mLoadNext) {
            mBtnLoadNext.setVisibility(View.VISIBLE);
            mProgressNext.stop();
        }
        if(mLoadPrev) {
            mBtnLoadPrev.setVisibility(View.VISIBLE);
            mProgressPrev.stop();
        }
    }
    
    private void updateCommentsCount() {
        mCommentsCount.setText(Integer.toString(mCount));
    }
}
