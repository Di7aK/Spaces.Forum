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
import com.di7ak.spaces.forum.fragments.PopularCommFragment;

public class MainActivity extends AppCompatActivity implements Authenticator.OnResult {
	
	private Toolbar toolbar;
	private TabLayout tabLayout;
	private ViewPager viewPager;
	private CommFragment myComm;
	private PopularCommFragment popularComm;
	private Session session;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		setupViewPager(viewPager);
		
		tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);
		
		Authenticator.getSession(this, this);
	}
	
	@Override
	public void onAuthenticatorResult(Session session) {
		if(session == null) finish();
		else {
			this.session = session;
			myComm.setSession(session);
			popularComm.setSession(session);
		}
	}
	
	private void setupViewPager(ViewPager viewPager) {
		ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
		myComm = new CommFragment();
		popularComm = new PopularCommFragment();
		adapter.addFragment(myComm, "Мои Сообщества");
		adapter.addFragment(popularComm, "Популярные");
		adapter.addFragment(new CommFragment(), "Категории");
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
