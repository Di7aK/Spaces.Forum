package com.di7ak.spaces.forum.fragments;

import android.view.*;
import java.util.*;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.adapters.CommAdapter;
import com.di7ak.spaces.forum.api.Comm;
import com.rey.material.widget.ListView;

public class CommFragment extends Fragment {
	ListView lvComms;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parrent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.comm_fragment, parrent, false);
		lvComms = (ListView)v.findViewById(R.id.lv_comms);
		List<Comm> comms = new ArrayList<Comm>();
		Comm comm = new Comm();
		comm.name = "testing";
		comms.add(comm);
		comm = new Comm();
		comm.name = "help";
		comms.add(comm);
		CommAdapter adapter = new CommAdapter(getContext(), comms);
		lvComms.setAdapter(adapter);
		
		return v;
	}
	
	
}
