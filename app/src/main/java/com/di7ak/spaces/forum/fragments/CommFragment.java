package com.di7ak.spaces.forum.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.listeners.RecyclerItemClickListener;
import com.dexafree.materialList.view.MaterialListView;
import com.di7ak.spaces.forum.ForumActivity;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.Settings;
import com.di7ak.spaces.forum.api.Communities;
import com.di7ak.spaces.forum.api.CommunityData;
import com.di7ak.spaces.forum.api.CommunityListData;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.widget.ProgressBar;

import java.util.List;

import jp.wasabeef.recyclerview.animators.ScaleInAnimator;

public class CommFragment extends Fragment implements RecyclerItemClickListener.OnItemClickListener {
	MaterialListView mListView;
	Session session;
	CommunityListData comms;
	ProgressBar bar;
	int type;
	int retryCount = 0;

	public CommFragment(Session session, int type) {
		super();
		this.session = session;
		this.type = type;
	}

    boolean selected = false;
    
    public void onSelected() {
        selected = true;
        if (getActivity() != null && comms == null) loadComm();
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        bar = new ProgressBar(activity);
        if(selected) loadComm();
    }

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parrent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.comm_fragment, parrent, false);
		mListView = (MaterialListView) v.findViewById(R.id.material_listview);

		mListView.setItemAnimator(new ScaleInAnimator());
        mListView.getItemAnimator().setAddDuration(300);
        mListView.getItemAnimator().setRemoveDuration(300);
		mListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
				@Override
				public final void onScrolled(RecyclerView recyclerView, int dx, int dy) {
					if (!recyclerView.canScrollVertically(1)) {
						if (!bar.isShown() && comms != null && comms.pagination.currentPage < comms.pagination.lastPage) {
							comms.pagination.currentPage ++;
							loadComm();
						}
					}
				}
			});
		mListView.addOnItemTouchListener(this);
		if (comms != null) showComms(comms.communities);
		return v;
	}

	@Override
	public void onItemClick(@NonNull Card card, int position) {
		Intent intent = new Intent(getContext(), ForumActivity.class);
		intent.putExtra("comm", comms.communities.get(position).toString());
        Bundle args = new Bundle();
        args.putString("tab", "new");
        args.putInt("page", 1);
        intent.putExtra("args", args);
		startActivity(intent);
	}

	@Override
	public void onItemLongClick(@NonNull Card card, int position) {
		
	}

	public void loadComm() {
		bar.showProgress("Получение списка");

		new Thread(new Runnable() {

				@Override
				public void run() {
					try {
                        if(comms == null) {
                            comms = Communities.get(session, type, 1);
                            showComms(comms.communities);
                        } else {
                            CommunityListData result = Communities.get(session, type, comms.pagination.currentPage);
                            comms.communities.addAll(result.communities);
                            comms.pagination = result.pagination;
                            showComms(result.communities);
                        }
						retryCount = 0;
                        bar.hide();
					} catch (SpacesException e) {
						final String message = e.getMessage();
						final int code = e.code;
						if (code == -1 && retryCount < Settings.maxRetryCount) {
							retryCount ++;
							try {
								Thread.sleep(100);
							} catch (InterruptedException ie) {}
							loadComm();
						} else {
							retryCount = 0;
                            bar.showError(message, "Повторить", new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        loadComm();
                                    }
                                });
						}
					}
				}
			}).start();

	}

	public void showComms(final List<CommunityData> comms) {
		if (getActivity() == null) return;
		getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					for (CommunityData comm : comms) {
						Card card = new Card.Builder(getContext())
							.withProvider(new CardProvider())
							.setLayout(R.layout.comm_item)
							.setTitle(comm.name)
							.setDescription(createDescription(comm))
							.setDrawable(comm.avatar.previewUrl)
							.endConfig()
							.build();

						mListView.getAdapter().add(mListView.getAdapter().getItemCount(), card, false);
					}
				}
			});
	}

	private String createDescription(CommunityData comm) {
        String result = "";
		if(comm.forumCount > 0) result += "Форум: " + comm.forumCount +"\n";
        if(comm.blogCount > 0) result += "Блог: " + comm.blogCount;
        return result;
	}

}
