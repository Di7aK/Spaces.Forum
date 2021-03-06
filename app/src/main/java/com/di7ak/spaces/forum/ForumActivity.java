package com.di7ak.spaces.forum;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.di7ak.spaces.forum.api.CommunityData;
import com.di7ak.spaces.forum.api.Forum;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.fragments.BlogsFragment;
import com.di7ak.spaces.forum.fragments.ForumCategoryFragment;
import com.di7ak.spaces.forum.fragments.ForumFragment;
import com.di7ak.spaces.forum.interfaces.OnPageSelectedListener;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class ForumActivity extends AppCompatActivity implements 
        Authenticator.OnResult,
        ViewPager.OnPageChangeListener,
        ForumCategoryFragment.OnForumChangeListener {
	private Toolbar toolbar;
	private TabLayout tabLayout;
	private ViewPager viewPager;
	private ForumFragment newTopics, lastTopics;
    private ForumCategoryFragment categoryFragment;
    private BlogsFragment blogsFragment;
	private Session session;
	private CommunityData comm;
    private Bundle args;
    private ViewPagerAdapter adapter;
    String type;
    
    public ForumActivity() {
        super();
    }
	
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
    
    boolean forumChanged = false;
    @Override
    public void onForumChange(String id) {
        if(!sortTabsAdded) {
            addSortTabs();
            tabLayout.setupWithViewPager(viewPager);
        }
        
        newTopics.setForum(id);
        lastTopics.setForum(id);
        
        forumChanged = true;
        
        //show new topics
        viewPager.setCurrentItem(adapter.getItemPosition(newTopics));
    }
    
    @Override
    public void onBackPressed() {
        if(forumChanged) {
            forumChanged = false;
            //show forums
            viewPager.setCurrentItem(adapter.getItemPosition(categoryFragment));
        } else finish();
    }
    
    @Override
    public void onPageScrolled(int p1, float p2, int p3) {

    }

    @Override
    public void onPageSelected(int page) {
        OnPageSelectedListener listener = (OnPageSelectedListener)adapter.getItem(page);
        listener.onSelected();
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
			String commSource = extra.getString("comm");
            JSONObject commData;
			try {
                
                commData = new JSONObject(commSource);
                comm = new CommunityData(commData);
            } catch (SpacesException e) {
                finish();
                return;
            } catch (JSONException e) {
                finish();
                return;
            }
			setTitle(comm.name);
            
            args = extra.getBundle("args");
			
			adapter = new ViewPagerAdapter(getSupportFragmentManager());
            try {
			if(commData.has("diary_url")) {
                String url = commData.getString("diary_url");
                blogsFragment = new BlogsFragment();
                blogsFragment.setData(Uri.parse(url), session);
                adapter.addFragment(blogsFragment, "Блоги");
            }
            } catch(JSONException e) {}

            if(comm.forumEnabled) {
                categoryFragment = new ForumCategoryFragment(session, comm.cid, comm.id != null);
                adapter.addFragment(categoryFragment, "Разделы");
            }
            
            if(comm.id != null) addSortTabs();
            
            viewPager.setAdapter(adapter);
            
			tabLayout.setupWithViewPager(viewPager);
            
            if(args != null) {
                String tab = args.getString("tab");
                if(tab != null) {
                    int item = 0;
                    if(tab.equals("new")) item = adapter.getItemPosition(newTopics);
                    if(tab.equals("category")) item = adapter.getItemPosition(categoryFragment);
                    if(tab.equals("last")) item = adapter.getItemPosition(lastTopics);
                    if(tab.equals("blog")) item = adapter.getItemPosition(blogsFragment);
                    if(item == -1) item = 0;
                    if(item == 0) onPageSelected(item);
                    viewPager.setCurrentItem(item);
                }
            }
            
            categoryFragment.setOnForumChangeListener(this);
		}
	}
    
    boolean sortTabsAdded = false;
    private void addSortTabs() {
        newTopics = new ForumFragment(session, comm, Forum.TYPE_NEW);
        lastTopics = new ForumFragment(session, comm, Forum.TYPE_LAST);
        adapter.addFragment(newTopics, "Новые");
        adapter.addFragment(lastTopics, "Последние");
        adapter.notifyDataSetChanged();
        sortTabsAdded = true;
    }
	
	class ViewPagerAdapter extends FragmentPagerAdapter {
		private final List<Fragment> mFragmentList = new ArrayList<>();
		private final List<String> mFragmentTitleList = new ArrayList<>();
		
		public ViewPagerAdapter(FragmentManager manager) {
			super(manager);
		}
        
        @Override
        public int getItemPosition(Object item) {
            return mFragmentList.indexOf(item);
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
