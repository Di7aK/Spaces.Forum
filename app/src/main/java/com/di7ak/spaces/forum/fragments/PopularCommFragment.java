package com.di7ak.spaces.forum.fragments;

import android.view.*;
import com.dexafree.materialList.card.*;
import com.di7ak.spaces.forum.*;
import com.di7ak.spaces.forum.api.*;
import com.rey.material.widget.*;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import com.dexafree.materialList.view.MaterialListView;
import java.util.List;
import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.ScaleInAnimator;
import android.support.v7.widget.RecyclerView;

public class PopularCommFragment extends Fragment {
	MaterialListView mListView;
	Session session;
	List<Comm> comms;
	Snackbar bar;
	int currentPage = 1;
	int pages = 1;

	public PopularCommFragment() {
		super();
		comms = new ArrayList<Comm>();
	}

	public void setSession(Session session) {
		this.session = session;
		if (getActivity() != null) loadPopularComm();
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
						if(!bar.isShown() && currentPage < pages) {
						currentPage ++;
						loadPopularComm();
						}
					}
				}
			});
		showComms(comms);
		if (session != null && comms.size() == 0) loadPopularComm();
		return v;
	}

	public void loadPopularComm() {
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
						CommResult result = Comm.getPopular(session, currentPage);
						comms.addAll(result.comms);
						pages = result.pages;
						showComms(result.comms);
					} catch (SpacesException e) {
						final String message = e.getMessage();
						final int code = e.code;
						getActivity().runOnUiThread(new Runnable() {

								@Override
								public void run() {
									bar.dismiss();
									bar = Snackbar.make(getActivity().getWindow().getDecorView(), message, Snackbar.LENGTH_INDEFINITE);
									if (code == -1) {
										bar.setAction("Повторить", new View.OnClickListener() {

												@Override
												public void onClick(View v) {
													loadPopularComm();
												}
											});
									}
									bar.show();
								}
							});
					}
				}
			}).start();

	}

	public void showComms(final List<Comm> comms) {
		getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (bar != null) bar.dismiss();
					List<Card> cards = new ArrayList<Card>();
					for (Comm comm : comms) {
						Card card = new Card.Builder(getContext())
							//.setDismissible()
							.withProvider(new CardProvider())
							.setLayout(R.layout.comm_item)
							.setTitle(comm.name)
							.setDescription(comm.description)
							.setDrawable(comm.avatar)
							.endConfig()
							.build();

						mListView.getAdapter().add(mListView.getAdapter().getItemCount(), card, false);
						//cards.add(card);
					}
					//mListView.getAdapter().addAll(cards);
				}
			});
	}

}
