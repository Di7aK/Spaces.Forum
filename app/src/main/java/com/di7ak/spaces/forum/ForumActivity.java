package com.di7ak.spaces.forum;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.di7ak.spaces.forum.api.Comm;
import com.di7ak.spaces.forum.api.Forum;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.fragments.ForumCategoryFragment;
import com.di7ak.spaces.forum.fragments.ForumFragment;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;

public class ForumActivity extends AppCompatActivity implements 
        Authenticator.OnResult,
        ViewPager.OnPageChangeListener,
        ForumCategoryFragment.OnForumChangeListener {
	private Toolbar toolbar;
	private TabLayout tabLayout;
	private ViewPager viewPager;
	private ForumFragment newTopics, lastTopics;
    private ForumCategoryFragment categoryFragment;
	private Session session;
	private Comm comm;
    private int activeTab;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    onBackPressed();
                }
            });
		
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		tabLayout = (TabLayout) findViewById(R.id.tabs);
		
        viewPager.setOnPageChangeListener(this);
        
		Authenticator.getSession(this, this);
	}
    
    @Override
    public void onForumChange(String id) {
        newTopics.setForum(id);
        lastTopics.setForum(id);
        
        viewPager.setCurrentItem(1);
    }
    
    @Override
    public void onPageScrolled(int p1, float p2, int p3) {

    }
    
    @Override
    public void onBackPressed() {
        if(activeTab == 0) finish();
        else viewPager.setCurrentItem(0);
        super.onBackPressed();
    }

    @Override
    public void onPageSelected(int page) {
        if(page == 1) newTopics.onSelected();
        else if(page == 2) lastTopics.onSelected();
        activeTab = page;
    }

    @Override
    public void onPageScrollStateChanged(int p1) {

    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.journal:
                Intent intent = new Intent(ForumActivity.this, JournalActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
	
	@Override
	public void onAuthenticatorResult(Session session) {
		if(session == null) finish();
		else {
			this.session = session;
			
			Bundle extra = getIntent().getExtras();
			comm = new Comm();
			comm.cid = extra.getString("cid");
			comm.name = extra.getString("name");
			setTitle(comm.name);
			
            categoryFragment = new ForumCategoryFragment(session, comm);
            categoryFragment.setOnForumChangeListener(this);
			newTopics = new ForumFragment(session, comm, Forum.TYPE_NEW);
			lastTopics = new ForumFragment(session, comm, Forum.TYPE_LAST);
			
			setupViewPager(viewPager);
			tabLayout.setupWithViewPager(viewPager);
		}
	}
	
	private void setupViewPager(ViewPager viewPager) {
		ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(categoryFragment, "Разделы");
		adapter.addFragment(newTopics, "Новые");
		adapter.addFragment(lastTopics, "Последние");
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
