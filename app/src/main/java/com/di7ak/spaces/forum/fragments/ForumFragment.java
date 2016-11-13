package com.di7ak.spaces.forum.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.TopicActivity;
import com.di7ak.spaces.forum.api.CommunityData;
import com.di7ak.spaces.forum.api.Forum;
import com.di7ak.spaces.forum.api.PreviewTopicData;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.api.TopicListData;
import com.di7ak.spaces.forum.interfaces.OnPageSelectedListener;
import com.rey.material.widget.ProgressView;
import java.util.ArrayList;
import java.util.List;

public class ForumFragment extends Fragment implements NestedScrollView.OnScrollChangeListener,
        OnPageSelectedListener {
	LinearLayout topicList;
	NestedScrollView scrollView;
	Session session;
	List<PreviewTopicData> topics;
	Snackbar bar;
	CommunityData comm;
	int currentPage = 1;
	int pages = 1;
	int type;
	int retryCount = 0;
	int maxRetryCount = 2;

	public ForumFragment(Session session, CommunityData comm, int type) {
		super();
		topics = new ArrayList<PreviewTopicData>();
		this.session = session;
		this.comm = comm;
		this.type = type;
	}
    
    
    String forum;
    public void setForum(String forum) {
        this.forum = forum;
        if(topicList != null) topicList.removeAllViews();
        topics = new ArrayList<PreviewTopicData>();
        currentPage = 1;
        pages = 1;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(selected && topics.size() == 0) {
            loadTopics();
        }
    }
    
    boolean selected = false;
    @Override
    public void onSelected() {
        selected = true;
        if(getActivity() != null && topics.size() == 0) {
            loadTopics();
        }
    }

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parrent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.forum_fragment, parrent, false);
		topicList = (LinearLayout) v.findViewById(R.id.topic_list);
		//cardView = (CardView) v.findViewById(R.id.card_view);
		scrollView = (NestedScrollView) v.findViewById(R.id.scroll_view);
		//cardView.setVisibility(View.INVISIBLE);
		
		scrollView.setOnScrollChangeListener(this);
		
		if (topics.size() != 0) showTopics(topics);
		return v;
	}
	
	@Override
	public void onScrollChange(NestedScrollView v, int p2, int p3, int p4, int p5) {
		if(p3 + 50 > (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
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
						TopicListData result = Forum.getTopics(session, comm.cid, currentPage, type, forum);
						topics.addAll(result.topics);
						pages = result.pagination.lastPage;
                        currentPage = result.pagination.currentPage;
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

	public void showTopics(final List<PreviewTopicData> topics) {
		if (getActivity() == null) return;
		getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (bar != null) bar.dismiss();
					LayoutInflater li = getActivity().getLayoutInflater();
					View v;
					for (PreviewTopicData topic : topics) {
						v = li.inflate(R.layout.topic_item, null);
						((TextView)v.findViewById(R.id.subject)).setText(Html.fromHtml(topic.subject));
						((TextView)v.findViewById(R.id.description)).setText(createDescription(topic));
						((TextView)v.findViewById(R.id.comments_cnt)).setText(Integer.toString(topic.commentsCount));
						LinearLayout prop = (LinearLayout)v.findViewById(R.id.prop);
                        float density = getResources().getDisplayMetrics().density;
                        int propW = (int)(density * 26);
                        int propH = (int)(density * 26);
                        int padding = (int)(density * 3);
                        if(topic.locked) {
                            ImageView lock = new ImageView(getActivity());
                            lock.setPadding(padding, padding, padding, padding);
                            lock.setLayoutParams(new LinearLayout.LayoutParams(propW, propH));
                            lock.setImageResource(R.drawable.ic_lock_outline_black_18dp);
							prop.addView(lock);
						}
                        if(topic.attachCount > 0) {
                            ImageView attach = new ImageView(getActivity());
                            attach.setPadding(padding, padding, padding, padding);
                            attach.setLayoutParams(new LinearLayout.LayoutParams(propW, propH));
                            attach.setImageResource(R.drawable.ic_attachment_black_18dp);
                            prop.addView(attach);
						}
						final Uri uri = topic.uri;
						v.findViewById(R.id.layout).setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(getActivity(), TopicActivity.class);
                                intent.setData(uri);
								startActivity(intent);
							}
						});
						topicList.addView(v);
					}
                    
                    expand(topicList);
                    
					/*if(cardView.getVisibility() == View.INVISIBLE) {
						cardView.setVisibility(View.VISIBLE);
						expand(topicList);
					}*/
				}
			});
	}

	private static final String PATTERN_PORTRAIT = "Автор: %1$s %2$s,  последний: %3$s %4$s";
	private static final String PATTERN_LANDSPACE = "Автор: %1$s, создано: %2$s, последний комментарий: %3$s %4$s";
	
	private String createDescription(PreviewTopicData topic) {
		String pattern = getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? PATTERN_PORTRAIT : PATTERN_LANDSPACE;
		return String.format(pattern, topic.user, topic.date, topic.lastUser, topic.lastDate);
	}

public static void expand(final View v) {
    v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    final int targetHeight = v.getMeasuredHeight();

    // Older versions of android (pre API 21) cancel animations for views with a height of 0.
    v.getLayoutParams().height = 1;
    v.setVisibility(View.VISIBLE);
    Animation a = new Animation()
    {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            v.getLayoutParams().height = interpolatedTime == 1
                    ? LayoutParams.WRAP_CONTENT
                    : (int)(targetHeight * interpolatedTime);
            v.requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    };

    // 1dp/ms
    a.setDuration(1000);
    v.startAnimation(a);
}

public static void collapse(final View v) {
    final int initialHeight = v.getMeasuredHeight();

    Animation a = new Animation()
    {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if(interpolatedTime == 1){
                v.setVisibility(View.GONE);
            }else{
                v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                v.requestLayout();
            }
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    };

    // 1dp/ms
    a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
    v.startAnimation(a);
}
	
}
