package com.di7ak.spaces.forum.fragments;

import android.view.*;
import com.dexafree.materialList.card.*;
import com.di7ak.spaces.forum.*;
import com.di7ak.spaces.forum.api.*;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.widget.LinearLayout;
import com.dexafree.materialList.view.MaterialListView;
import java.util.List;
import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.ScaleInAnimator;
import android.support.v7.widget.RecyclerView;
import java.util.TimerTask;
import com.dexafree.materialList.listeners.RecyclerItemClickListener;
import android.support.annotation.NonNull;
import android.content.Intent;
import android.support.v7.widget.CardView;
import com.rey.material.widget.ProgressView;
import android.widget.TextView;
import android.widget.ScrollView;

public class ForumFragment extends Fragment implements View.OnScrollChangeListener {
	LinearLayout topicList;
	CardView cardView;
	ScrollView scrollView;
	Session session;
	List<Topic> topics;
	Snackbar bar;
	Comm comm;
	int currentPage = 1;
	int pages = 1;
	int type;
	int retryCount = 0;
	int maxRetryCount = 2;

	public ForumFragment(Session session, Comm comm, int type) {
		super();
		topics = new ArrayList<Topic>();
		this.session = session;
		this.comm = comm;
		this.type = type;
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parrent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.forum_fragment, parrent, false);
		topicList = (LinearLayout) v.findViewById(R.id.topic_list);
		cardView = (CardView) v.findViewById(R.id.card_view);
		scrollView = (ScrollView) v.findViewById(R.id.scroll_view);
		cardView.setVisibility(View.INVISIBLE);
		
		scrollView.setOnScrollChangeListener(this);
		
		if (topics.size() == 0) loadTopics();
		else showTopics(topics);
		return v;
	}
	
	@Override
	public void onScrollChange(View p1, int p2, int p3, int p4, int p5) {
		if(scrollView.canScrollVertically(1)) {
			if (!bar.isShown() && currentPage < pages) {
				currentPage ++;
				loadTopics();
			}
		}
	}

	public void loadTopics() {
		bar = Snackbar.make(getActivity().getWindow().getDecorView(), "Получение списка", Snackbar.LENGTH_INDEFINITE);

		Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) bar.getView();
		View snackView = getActivity().getLayoutInflater().inflate(R.layout.progress_snackbar, layout, false);
		ProgressView pv = (ProgressView)snackView.findViewById(R.id.progress_pv_circular_determinate);
		pv.start();
		layout.addView(snackView, 0);

		bar.show();

		new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						ForumResult result = Forum.getTopics(session, comm, currentPage, type);
						topics.addAll(result.topics);
						pages = result.lastPage;
						showTopics(result.topics);
						retryCount = 0;
					} catch (SpacesException e) {
						final String message = e.getMessage();
						final int code = e.code;
						if (code == -1 && retryCount < maxRetryCount) {
							retryCount ++;
							try {
								Thread.sleep(100);
							} catch (InterruptedException ie) {}
							loadTopics();
						} else {
							retryCount = 0;
							getActivity().runOnUiThread(new Runnable() {

									@Override
									public void run() {
										bar.dismiss();
										bar = Snackbar.make(getActivity().getWindow().getDecorView(), message, Snackbar.LENGTH_INDEFINITE);
										if (code == -1) {
											bar.setAction("Повторить", new View.OnClickListener() {

													@Override
													public void onClick(View v) {
														loadTopics();
													}
												});
										}
										bar.show();
									}
								});
						}
					}
				}
			}).start();

	}

	public void showTopics(final List<Topic> topics) {
		if (getActivity() == null) return;
		getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (bar != null) bar.dismiss();
					if(cardView.getVisibility() == View.INVISIBLE) cardView.setVisibility(View.VISIBLE);
					LayoutInflater li = getActivity().getLayoutInflater();
					View v;
					for (Topic topic : topics) {
						v = li.inflate(R.layout.topic_item, null);
						((TextView)v.findViewById(R.id.subject)).setText(topic.subject);
						((TextView)v.findViewById(R.id.description)).setText(createDescription(topic));
						topicList.addView(v);
					}
				}
			});
	}

	private String createDescription(Topic topic) {
		StringBuilder build = new StringBuilder();
		build.append("Автор: ").append(topic.topicUser)
			.append(", создано: ").append(topic.date)
			.append(", последний комментарий: ").append(topic.lastUser)
			.append(" ").append(topic.lastCommentDate);
		return build.toString();
	}
	
}
