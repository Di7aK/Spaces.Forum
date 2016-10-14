package com.di7ak.spaces.forum.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.Comm;
import com.di7ak.spaces.forum.api.Forum;
import com.di7ak.spaces.forum.api.ForumResult;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.api.Topic;
import com.rey.material.widget.ProgressView;
import java.util.ArrayList;
import java.util.List;

public class ForumFragment extends Fragment implements NestedScrollView.OnScrollChangeListener {
	LinearLayout topicList;
	CardView cardView;
	NestedScrollView scrollView;
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
		scrollView = (NestedScrollView) v.findViewById(R.id.scroll_view);
		cardView.setVisibility(View.INVISIBLE);
		
		scrollView.setOnScrollChangeListener(this);
		
		if (topics.size() == 0) loadTopics();
		else showTopics(topics);
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
					LayoutInflater li = getActivity().getLayoutInflater();
					View v;
					for (Topic topic : topics) {
						v = li.inflate(R.layout.topic_item, null);
						((TextView)v.findViewById(R.id.subject)).setText(topic.subject);
						((TextView)v.findViewById(R.id.description)).setText(createDescription(topic));
						((TextView)v.findViewById(R.id.comments_cnt)).setText(Integer.toString(topic.commentsCount));
						if(topic.locked) {
							((LinearLayout)v.findViewById(R.id.prop)).addView(li.inflate(R.layout.lock, null));
						}
						topicList.addView(v);
					}
					if(cardView.getVisibility() == View.INVISIBLE) {
						cardView.setVisibility(View.VISIBLE);
						expand(topicList);
					}
				}
			});
	}

	private static final String PATTERN_PORTRAIT = "Автор: %1$s %2$s,  последний: %3$s %4$s";
	private static final String PATTERN_LANDSPACE = "Автор: %1$s, создано: %2$s, последний комментарий: %3$s %4$s";
	
	private String createDescription(Topic topic) {
		String pattern = getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? PATTERN_PORTRAIT : PATTERN_LANDSPACE;
		return String.format(pattern, topic.topicUser, topic.date, topic.lastUser, topic.lastCommentDate);
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
