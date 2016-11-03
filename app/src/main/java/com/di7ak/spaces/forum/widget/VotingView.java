package com.di7ak.spaces.forum.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.api.Voting;
import org.json.JSONException;
import org.json.JSONObject;

public class VotingView extends LinearLayout implements View.OnClickListener {
    Session session;
    TextView likes;
    TextView dislikes;
    ImageView imageLike;
    ImageView imageDislike;
    String likeUrl;
    String dislikeUrl;
    boolean isLike;
    boolean isDislike;
    boolean lastLike;
    boolean lastDislike;
    int oid;
    int ot;
    int likesCount;
    int dislikesCount;

    public VotingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater li = LayoutInflater.from(context);

        View view = li.inflate(R.layout.voting, null);

        likes = (TextView)view.findViewById(R.id.likes_count);
        dislikes = (TextView)view.findViewById(R.id.dislikes_count);
        imageLike = (ImageView)view.findViewById(R.id.image_like);
        imageDislike = (ImageView)view.findViewById(R.id.image_dislike);

        view.findViewById(R.id.layout_like).setOnClickListener(this);
        view.findViewById(R.id.layout_dislike).setOnClickListener(this);

        addView(view);
    }

    public void setupData(JSONObject data, Session session) {
        this.session = session;
        try {
            if (data.has("like")) {
                JSONObject like = data.getJSONObject("like");
                if (like.has("URL") && !like.isNull("URL")) {
                    likeUrl = like.getString("URL");
                    if(likeUrl.equals("null")) likeUrl = null;
                }
                if (like.has("count")) likesCount = like.getInt("count");
                if (like.has("oid")) oid = like.getInt("oid");
                if (like.has("ot")) ot = like.getInt("ot");
            }
            if (data.has("dislike")) {
                JSONObject dislike = data.getJSONObject("dislike");
                if(dislike.has("URL") && !dislike.isNull("URL")) {
                    dislikeUrl = dislike.getString("URL");
                    if(dislikeUrl.equals("null")) dislikeUrl = null;
                }
                if (dislike.has("count")) dislikesCount = dislike.getInt("count");
            }
            if (data.has("likes_count")) likesCount = data.getInt("likes_count");
            if (data.has("dislikes_count")) dislikesCount = data.getInt("dislikes_count");
            if (data.has("ot")) ot = data.getInt("ot");
            if (data.has("oid")) oid = data.getInt("oid");
            if (data.has("like_URL") && !data.isNull("likeURL")) {
                likeUrl = data.getString("like_URL");
                if(likeUrl.equals("null")) likeUrl = null;
            }
            if (data.has("dislike_URL") && !data.isNull("dislikeURL")) {
                dislikeUrl = data.getString("dislike_URL");
                if(dislikeUrl.equals("null")) dislikeUrl = null;
            }
            
            isLike = likeUrl == null;
            isDislike = dislikeUrl == null;
        } catch (JSONException e) {

        }
        updateState(false);
    }
    
    public void updateState(boolean vote) {
        if(isLike) {
            imageLike.setColorFilter(0xff66ff66);
            imageDislike.setColorFilter(0xff000000);
            if(vote) {
                likesCount ++;
                if(lastDislike) dislikesCount --;
                vote(0);
            }
            lastLike = true;
            lastDislike = false;
        } else if(isDislike) {
            imageLike.setColorFilter(0xff000000);
            imageDislike.setColorFilter(0xffff6666);
            if(vote) {
                dislikesCount ++;
                if(lastLike) likesCount --;
                vote(1);
            }
            lastDislike = true;
            lastLike = false;
        } else {
            imageLike.setColorFilter(0xff000000);
            imageDislike.setColorFilter(0xff000000);
            if(vote) {
                if(lastLike) likesCount --;
                else if(lastDislike) dislikesCount --;
                vote(-1);
            }
            lastDislike = false;
            lastLike = false;
        }
        likes.setText(Integer.toString(likesCount));
        dislikes.setText(Integer.toString(dislikesCount));
    }
    
    public void vote(final int down) {
        new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Voting.vote(session, ot, oid, down);
                    } catch (SpacesException e) {}
                }
            }).start();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.layout_like) {
            isLike = !isLike;
            isDislike = false;
        } else {
            isDislike = !isDislike;
            isLike = false;
        }
        updateState(true);
    }
}
