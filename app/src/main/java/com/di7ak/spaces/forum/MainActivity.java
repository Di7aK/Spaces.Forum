package com.di7ak.spaces.forum;

import android.accounts.*;
import android.support.v4.app.*;
import java.util.*;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.fragments.CommFragment;
import java.io.IOException;
import com.di7ak.spaces.forum.api.Comm;

public class MainActivity extends AppCompatActivity implements Authenticator.OnResult {
	
	private Toolbar toolbar;
	private TabLayout tabLayout;
	private ViewPager viewPager;
	private CommFragment myComm, popularComm;
	private Session session;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		tabLayout = (TabLayout) findViewById(R.id.tabs);
		
		Authenticator.getSession(this, this);
	}
	
	@Override
	public void onAuthenticatorResult(Session session) {
		if(session == null) finish();
		else {
			this.session = session;
			myComm = new CommFragment(session, Comm.TYPE_MYCOMM);
			popularComm = new CommFragment(session, Comm.TYPE_POPULAR);
			setupViewPager(viewPager);
			tabLayout.setupWithViewPager(viewPager);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	private void setupViewPager(ViewPager viewPager) {
		ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
		adapter.addFragment(myComm, "Мои Сообщества");
		adapter.addFragment(popularComm, "Популярные");
		viewPager.setAdapter(adapter);
	}
	
	class ViewPagerAdapter extends FragmentPagerAdapter {
		private final List<Fragment> mFragmentList = new ArrayList<>();
		private final List<String> mFragmentTitleList = new ArrayList<>();
		
		public ViewPagerAdapter(FragmentManager manager) {
			super(manager);
		}
		
		@Override
		public Fragment getItem(int position) {
			return mFragmentList.get(position);
		}
		
		@Override
		public int getCount() {
			return mFragmentList.size();
		}
		
		public void addFragment(Fragment fragment, String title) {
			mFragmentList.add(fragment);
			mFragmentTitleList.add(title);
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return mFragmentTitleList.get(position);
		}
		
	}
}
