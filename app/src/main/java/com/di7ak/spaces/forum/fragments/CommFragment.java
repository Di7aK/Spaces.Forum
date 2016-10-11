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

public class CommFragment extends Fragment {
	ListView lvComms;
	MaterialListView mListView;
	Session session;
	List<Comm> comms;
	Snackbar bar;
	
	public CommFragment() {
		super();
		comms = new ArrayList<Comm>();
	}
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parrent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.comm_fragment, parrent, false);
		mListView = (MaterialListView) v.findViewById(R.id.material_listview);
		showComms(comms);
		return v;
	}
	

	
	public void loadMyComm(final Session session) {
		this.session = session;
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
					comms.addAll(Comm.get(session, 1));
					showComms(comms);
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
												loadMyComm(session);
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
					if(bar != null) bar.dismiss();
					mListView.scrollToPosition(0);
					for(Comm comm : comms) {
						Card card = new Card.Builder(getContext())
							.setDismissible()
							.withProvider(new CardProvider())
							.setLayout(R.layout.comm_item)
							.setTitle(comm.name)
							.setDescription(Integer.toString(comm.count) + " новых тем")
							.setDrawable(comm.avatar)
							.endConfig()
							.build();

						mListView.getAdapter().add(card);
					}
				}
			});
	}
	
}
