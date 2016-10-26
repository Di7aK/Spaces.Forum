package com.di7ak.spaces.forum;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.di7ak.spaces.forum.api.Comm;
import com.di7ak.spaces.forum.api.Forum;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.fragments.ForumFragment;
import com.di7ak.spaces.forum.fragments.JournalFragment;
import java.util.ArrayList;
import java.util.List;

public class JournalActivity extends AppCompatActivity implements Authenticator.OnResult {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private JournalFragment newRecords, allRecords;
    private Session session;
    private Comm comm;

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
