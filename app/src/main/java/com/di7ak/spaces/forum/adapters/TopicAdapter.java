package com.di7ak.spaces.forum.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.Topic;
import android.widget.TextView;

public class TopicAdapter extends BaseAdapter {
	Topic topic;
	Activity activity;
	LayoutInflater inflater;
	
	public TopicAdapter(Activity activity, Topic topic) {
		this.activity = activity;
		this.topic = topic;
		inflater = activity.getLayoutInflater();
	}
	
	@Override
	public int getCount() {
		return 1;
	}

	@Override
	public Object getItem(int p1) {
		return topic;
	}

	@Override
	public long getItemId(int p1) {
		return 0;
	}

	@Override
	public View getView(int index, View p2, ViewGroup p3) {
		View view = null;
		if(isHeader(index)) {
			view = inflater.inflate(R.layout.topic_head, null);
			((TextView)view.findViewById(R.id.author)).setText(topic.topicUser);
			((TextView)view.findViewById(R.id.title)).setText(topic.subject);
			((TextView)view.findViewById(R.id.text)).setText(topic.text);
			((TextView)view.findViewById(R.id.date)).setText(topic.date);
		}
		return view;
	}
	
	private boolean isHeader(int index) {
		return index == 0;
	}
	
}
