package com.di7ak.spaces.forum.widget;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.Voting;
import com.di7ak.spaces.forum.api.VotingData;
import com.di7ak.spaces.forum.api.SpacesException;

public class VotingWidget implements View.OnClickListener {
    Activity activity;
    Session session;
    VotingData data;
    View view;
    TextView likes;
    TextView dislikes;
    ImageView imageLike;
    ImageView imageDislike;
    boolean isLike;
    boolean isDislike;
    boolean lastLike;
    boolean lastDislike;
    
    public VotingWidget(Session session, Activity activity, VotingData data) {
        this.session = session;
        this.activity = activity;
        this.data = data;
        LayoutInflater li = activity.getLayoutInflater();
        view = li.inflate(R.layout.voting, null);
        likes = (TextView)view.findViewById(R.id.likes_count);
        dislikes = (TextView)view.findViewById(R.id.dislikes_count);
        imageLike = (ImageView)view.findViewById(R.id.image_like);
        imageDislike = (ImageView)view.findViewById(R.id.image_dislike);
        
        likes.setText(Integer.toString(data.likes));
        dislikes.setText(Integer.toString(data.dislikes));
        
        view.findViewById(R.id.layout_like).setOnClickListener(this);
        view.findViewById(R.id.layout_dislike).setOnClickListener(this);
        
        isLike = data.likeUrl == null;
        isDislike = data.dislikeUrl == null;
        
        updateState(false);
    }
    
    public View getView() {
        return view;
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
    
    public void updateState(boolean vote) {
        if(isLike) {
            imageLike.setColorFilter(0xff66ff66);
            imageDislike.setColorFilter(0xff000000);
            if(vote) {
                data.likes ++;
                if(lastDislike) data.dislikes --;
                vote(0);
            }
            lastLike = true;
            lastDislike = false;
        } else if(isDislike) {
            imageLike.setColorFilter(0xff000000);
            imageDislike.setColorFilter(0xffff6666);
            if(vote) {
                data.dislikes ++;
                if(lastLike) data.likes --;
                vote(1);
            }
            lastDislike = true;
            lastLike = false;
        } else {
            imageLike.setColorFilter(0xff000000);
            imageDislike.setColorFilter(0xff000000);
            if(vote) {
                if(lastLike) data.likes --;
                else if(lastDislike) data.dislikes --;
                vote(-1);
            }
            lastDislike = false;
            lastLike = false;
        }
        likes.setText(Integer.toString(data.likes));
        dislikes.setText(Integer.toString(data.dislikes));
    }
    
    public void vote(final int down) {
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    Voting.vote(session, data.objectType, data.objectId, down);
                } catch (SpacesException e) {}
            }
        }).start();
    }
}
