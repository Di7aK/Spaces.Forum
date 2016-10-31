package com.di7ak.spaces.forum.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import com.di7ak.spaces.forum.api.Comm;
import com.di7ak.spaces.forum.api.CommResult;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.rey.material.widget.ProgressView;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.ScaleInAnimator;

public class CommFragment extends Fragment implements RecyclerItemClickListener.OnItemClickListener {
	MaterialListView mListView;
	Session session;
	List<Comm> comms;
	Snackbar bar;
	int currentPage = 1;
	int pages = 1;
	int type;
	int retryCount = 0;
	int maxRetryCount = 2;

	public CommFragment(Session session, int type) {
		super();
		comms = new ArrayList<Comm>();
		this.session = session;
		this.type = type;
	}

    public void onSelected() {
        if (comms.size() == 0) {
            loadComm();
        }
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
						if (!bar.isShown() && currentPage < pages) {
							currentPage ++;
							loadComm();
						}
					}
				}
			});
		mListView.addOnItemTouchListener(this);
		if (comms.size() != 0) showComms(comms);
		return v;
	}

	@Override
	public void onItemClick(@NonNull Card card, int position) {
		Intent intent = new Intent(getContext(), ForumActivity.class);
		intent.putExtra("name", comms.get(position).name);
		intent.putExtra("cid", comms.get(position).cid);
        intent.putExtra("default_page", 1);
		startActivity(intent);
	}

	@Override
	public void onItemLongClick(@NonNull Card card, int position) {
		//Log.d("LONG_CLICK", "" + card.getTag());
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (type == Comm.TYPE_MYCOMM) loadComm();
    }

	public void loadComm() {
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
						CommResult result = Comm.get(session, currentPage, type);
						comms.addAll(result.comms);
						pages = result.pages;
						showComms(result.comms);
						retryCount = 0;
					} catch (SpacesException e) {
						final String message = e.getMessage();
						final int code = e.code;
						if (code == -1 && retryCount < maxRetryCount) {
							retryCount ++;
							try {
								Thread.sleep(100);
							} catch (InterruptedException ie) {}
							loadComm();
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
														loadComm();
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

	public void showComms(final List<Comm> comms) {
		if (getActivity() == null) return;
		getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (bar != null) bar.dismiss();
					LayoutInflater li = getActivity().getLayoutInflater();
					for (Comm comm : comms) {
						Card card = new Card.Builder(getContext())
							.withProvider(new CardProvider())
							.setLayout(R.layout.comm_item)
							.setTitle(comm.name)
							.setDescription(comm.description == null ? getCountText(comm.count) : comm.description)
							.setDrawable(comm.avatar)
							.endConfig()
							.build();

						mListView.getAdapter().add(mListView.getAdapter().getItemCount(), card, false);
					}
				}
			});
	}

	private String getCountText(int count) {
		if (count == 0) return "нет новых тем";
		String countString = Integer.toString(count);
		String text = " новых тем";
		if (!(count > 10 && count < 20)) {
			if (countString.endsWith("1")) text = " новая тема";
			else if (countString.endsWith("2") ||
					 countString.endsWith("3") ||
					 countString.endsWith("4")) text = " новые темы";
		}
		return countString + text;
	}

}
