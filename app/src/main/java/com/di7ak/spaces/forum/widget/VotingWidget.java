package com.di7ak.spaces.forum.widget;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.VotingData;

public class VotingWidget {
    Activity activity;
    VotingData data;
    View view;
    
    public VotingWidget(Activity activity, VotingData data) {
        this.activity = activity;
        this.data = data;
        LayoutInflater li = activity.getLayoutInflater();
        view = li.inflate(R.layout.voting, null);
    }
    
    public View getView() {
        return view;
    }
}
