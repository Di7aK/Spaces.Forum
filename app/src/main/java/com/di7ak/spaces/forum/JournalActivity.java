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
import com.di7ak.spaces.forum.api.Forum;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.fragments.ForumFragment;
import com.di7ak.spaces.forum.fragments.JournalFragment;
import java.util.ArrayList;
import java.util.List;

public class JournalActivity extends AppCompatActivity implements 
        Authenticator.OnResult,
ViewPager.OnPageChangeListener {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private JournalFragment newRecords, allRecords;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    onBackPressed();
                }
            });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        viewPager.setOnPageChangeListener(this);
        
        Authenticator.getSession(this, this);
    }

    @Override
    public void onAuthenticatorResult(Session session) {
        if(session == null) finish();
        else {
            this.session = session;
            
            newRecords = new JournalFragment(session, 2);
            allRecords = new JournalFragment(session, 1);
            
            setupViewPager(viewPager);
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(newRecords, "Новые");
        adapter.addFragment(allRecords, "Все");
        viewPager.setAdapter(adapter);
    }
    
    @Override
    public void onPageScrolled(int p1, float p2, int p3) {

    }

    @Override
    public void onPageSelected(int page) {
        if(page == 0) newRecords.onSelected();
        else allRecords.onSelected();
    }

    @Override
    public void onPageScrollStateChanged(int p1) {

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
