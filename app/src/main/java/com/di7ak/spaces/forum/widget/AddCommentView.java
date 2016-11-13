package com.di7ak.spaces.forum.widget;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.Comment;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.rey.material.widget.FloatingActionButton;

public class AddCommentView extends LinearLayout implements View.OnClickListener {
    private static final int STATE_SEND = 0;
    private static final int STATE_WAIT = 1;
    private static final int STATE_ERR = 2;
    
    private Session mSession;
    private Context mContext;
    private View mCommentPanel;
    private View mResize;
    private EditText mEditText;
    private FloatingActionButton mSend;
    private int mMinHeight;
    private int mCommentType;
    private int mObjectId;
    private int mReplyId;
    
    public AddCommentView(Context context) {
        super(context);
        init(context);
    }
    
    public AddCommentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mCommentPanel = inflater.inflate(R.layout.comment_add, this, true);
        mEditText = (EditText) mCommentPanel.findViewById(R.id.comment);
        mSend = (FloatingActionButton) mCommentPanel.findViewById(R.id.fab_send);
        mSend.setOnClickListener(this);
        mResize = mCommentPanel.findViewById(R.id.resize);
        
        mMinHeight = (int)(mContext.getResources().getDisplayMetrics().density * 110);
        
        mResize.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mResize.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    hide();
                }
            });
    }
    
    public void setSession(Session session) {
        mSession = session;
    }
    
    @Override
    public void onClick(View v) {
        if(v.equals(mSend)) {
            mSend.setLineMorphingState(STATE_WAIT, true);
            mSend.setEnabled(false);
            mEditText.setEnabled(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Comment.send(mSession, mEditText.getText().toString(), mCommentType, Integer.toString(mObjectId), mReplyId != 0 ? Integer.toString(mReplyId) : null);
                        ((Activity)mContext).runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mSend.setLineMorphingState(STATE_SEND, true);
                                    mSend.setEnabled(true);
                                    mEditText.setEnabled(true);
                                    mEditText.setText("");
                                    hide();
                                }
                            });
                    } catch (SpacesException e) {
                        ((Activity)mContext).runOnUiThread(new Runnable() {
                            
                            @Override
                            public void run() {
                                mSend.setLineMorphingState(STATE_ERR, true);
                                mSend.setEnabled(true);
                                mEditText.setEnabled(true);
                            }
                        });
                    }
                }
            }).start();
        }
    }
    
    public void setReply(int id) {
        mReplyId = id;
        if(getTranslationY() > 0) show();
    }
    
    public void setCommentType(int type) {
        mCommentType = type;
    }
    
    public void setObjectId(int objectId) {
        mObjectId = objectId;
    }

    int mLastDrag;
    int mTX;
    int mTY;
    boolean mDrag = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int eventaction = event.getAction();

        int X = (int) event.getX();
        int Y = (int) event.getY();

        switch (eventaction) {

        case MotionEvent.ACTION_DOWN:
            mTX = X;
            mTY = Y;
            mDrag = Y < mResize.getHeight();
            if(mDrag) mResize.setBackgroundColor(0x33000000);
            break;

        case MotionEvent.ACTION_MOVE:
            Y -= mTY;
            if(mDrag) {
                mLastDrag = Y;
                int height = getHeight() - Y;
                
                if(height > mMinHeight && getTranslationY() == 0) {
                    if(height < mMinHeight) height = mMinHeight;
                    getLayoutParams().height = height;
                } else {
                    if(getTranslationY() + Y > 0) setTranslationY(getTranslationY() + Y);
                    else setTranslationY(0);
                }
                requestLayout();
            }
            break;

        case MotionEvent.ACTION_UP:
            if(mDrag) {
                mDrag = false;
                mResize.setBackgroundColor(0x00000000);
                if(getTranslationY() > 0) {
                    if(getTranslationY() < mMinHeight / 2) show();
                    else hide();
                }
                if(mTX == X && mTY == Y) {
                    if(getTranslationY() == 0) hide();
                    else show();
                }
                if(getTranslationY() == 0) {
                    mEditText.setEnabled(true);
                }
            }
            break;
        }
        //invalidate();
        return true;

    }
    
    public void hide() {
        mEditText.setEnabled(false);
        mReplyId = 0;
        ViewCompat.animate(this).translationY(getHeight() - mResize.getHeight()).start();
    }
    
    public void show() {
        mEditText.setEnabled(true);
        ViewCompat.animate(this).translationY(0).start();
    }
}
