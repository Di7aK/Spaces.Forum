package com.di7ak.spaces.forum.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.Session;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommentsView extends LinearLayout {
    private Context mContext;
    private android.widget.LinearLayout mCommentsList;
    private TextView mCommentsCount;
    private int mCount;

    public CommentsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.comments, this, true);
        mCommentsList = (android.widget.LinearLayout) view.findViewById(R.id.comments_list);
        mCommentsCount = (TextView) view.findViewById(R.id.comments_cnt);
    }

    public void setupData(JSONObject data, Picasso picasso, Session session) {
        try {
            if (data.has("pagination") && !data.isNull("pagination")) {
            }
            if (data.has("commentForm") && !data.isNull("commentForm")) {

            }
            if (data.has("comments")) {
                JSONObject comments = data.getJSONObject("comments");
                if (comments.has("commentsCnt")) {
                    mCount = comments.getInt("commentsCnt");
                    updateCommentsCount();
                }
                if (comments.has("comments_list")) {
                    JSONArray commentsList = comments.getJSONArray("comments_list");
                    for (int i = 0; i < commentsList.length(); i ++) {
                        JSONObject comment = commentsList.getJSONObject(i);
                        CommentView view = new CommentView(mContext);
                        view.setupData(comment, picasso, session);
                        mCommentsList.addView(view);
                    }
                }
            }
        } catch (JSONException e) {

        }
    }
    
    private void updateCommentsCount() {
        mCommentsCount.setText(Integer.toString(mCount));
    }
}
