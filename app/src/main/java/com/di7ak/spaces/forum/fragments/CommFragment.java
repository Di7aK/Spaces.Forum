package com.di7ak.spaces.forum.fragments;
import android.support.v4.app.*;
import android.os.*;
import android.view.*;
import com.di7ak.spaces.forum.*;

public class CommFragment extends Fragment {
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parrent, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.comm_fragment, parrent, false);
	}
}
