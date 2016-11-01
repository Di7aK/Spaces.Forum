package com.di7ak.spaces.forum;

import android.content.Intent;
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
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.Communities;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.fragments.CommFragment;
import com.di7ak.spaces.forum.fragments.ForumsFragment;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements 
        Authenticator.OnResult,
        ViewPager.OnPageChangeListener {

	private Toolbar toolbar;
	private TabLayout tabLayout;
	private ViewPager viewPager;
	private CommFragment myComm;
    private ForumsFragment forumsFragment;
	private Session session;
    private Bundle args;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		viewPager = (ViewPager) findViewById(R.id.viewpager);
		tabLayout = (TabLayout) findViewById(R.id.tabs);
        
        viewPager.setOnPageChangeListener(this);

		Authenticator.getSession(this, this);
	}
    
    @Override
    public void onPageScrolled(int p1, float p2, int p3) {

    }

    @Override
    public void onPageSelected(int page) {
        if(page == 0) myComm.onSelected();
        else forumsFragment.onSelected();
    }

    @Override
    public void onPageScrollStateChanged(int p1) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.journal:
                Intent intent = new Intent(MainActivity.this, JournalActivity.class);
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
		if (session == null) finish();
		else {
			this.session = session;
			myComm = new CommFragment(session, Communities.TYPE_MYCOMM);
			forumsFragment = new ForumsFragment();
			setupViewPager(viewPager);
			tabLayout.setupWithViewPager(viewPager);
            if (!NotificationService.running) startService(new Intent(this, NotificationService.class));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private void setupViewPager(ViewPager viewPager) {
		ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
		adapter.addFragment(myComm, "Форумы сообществ");
		adapter.addFragment(forumsFragment, "Общий форум");
		viewPager.setAdapter(adapter);
        myComm.onSelected();
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
