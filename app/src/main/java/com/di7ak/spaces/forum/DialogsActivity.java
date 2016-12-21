package com.di7ak.spaces.forum;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.adapters.ContactAdapter;
import com.di7ak.spaces.forum.adapters.DialogAdapter;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.fragments.DialogFragment;
import com.di7ak.spaces.forum.util.DBHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class DialogsActivity extends AppCompatActivity implements 
Authenticator.OnResult,
NotificationManager.OnNewNotification,
ActionBar.OnNavigationListener,
ViewPager.OnPageChangeListener {
    Toolbar toolbar;
    DBHelper mDBHelper;
    SQLiteDatabase mDb;
    Session mSession;
    boolean mPaused;
    ViewPager mViewPager;
    DialogAdapter mAdapter;
    ContactAdapter mContactAdapter;
    ActionBar mActionBar;
    Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("lol", "on create");
        setContentView(R.layout.dialogs);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    onBackPressed();
                }
            });

        mDBHelper = new DBHelper(this);
        mDb = mDBHelper.getWritableDatabase();
        mAdapter = new DialogAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(this);
        mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        mContactAdapter = new ContactAdapter();
        mActionBar.setListNavigationCallbacks(mContactAdapter, this);

        Authenticator.getSession(this, this);
    }

    @Override
    public boolean onNavigationItemSelected(int idx, long p) {
        mViewPager.setCurrentItem(idx);
        return false;
    }

    @Override
    public void onPageScrolled(int i, float p, int p2) {

    }

    @Override
    public void onPageSelected(int i) {
        mActionBar.setSelectedNavigationItem(i);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(mSession == null) {
            mIntent = intent;
            return;
        }
        Uri uri = intent.getData();
        int contact = Integer.valueOf(uri.getQueryParameter("Contact"));
        int idx = mAdapter.indexOf(contact);
        if (idx == -1) {
            DialogFragment dialog = new DialogFragment(mSession, contact, mDb);
            idx = mAdapter.appendDialog(dialog);
            mAdapter.notifyDataSetChanged();
            mContactAdapter.add(dialog);
            mContactAdapter.notifyDataSetChanged();
        }
        mViewPager.setCurrentItem(idx);
        mActionBar.setSelectedNavigationItem(idx);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dialog, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        android.util.Log.d("lol", "on pause");
        mPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.d("lol", "on resume");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(2);
        mPaused = false;
    } 
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Application.getNotificationManager().removeListener(this);
    }

    @Override
    public boolean onNewNotification(JSONObject message) {

        try {
            if (message.has("text")) {
                JSONObject text = message.getJSONObject("text");
                if (text.has("act")) {
                    int act = text.getInt("act");
                    if (act == 24) {//typing
                        int id;
                        if (text.has("talk_id")) id = text.getInt("talk_id");
                        else id = text.getInt("contact_id");
                        for (int i = 0; i < mAdapter.getCount(); i ++) {
                            DialogFragment dialog = (DialogFragment) mAdapter.getItem(i);
                            if (id == dialog.contactId || id == dialog.talkId) {
                                final String user = text.has("user") ? text.getString("user") : "";
                                final DialogFragment currentDialog = dialog;
                                runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            currentDialog.onTyping(user);
                                        }
                                    }
                                );

                                return true;
                            }
                        }
                    }
                    if (act == 1) {//new message
                        int nid = text.getJSONObject("data").getJSONObject("contact").getInt("nid");
                        for (int i = 0; i < mAdapter.getCount(); i ++) {
                            DialogFragment dialog = (DialogFragment) mAdapter.getItem(i);
                            if (nid == dialog.contactId) {
                                final int mid = text.getJSONObject("data").getInt("nid");
                                final DialogFragment currentDialog = dialog;
                                runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            currentDialog.onReceived(mid);
                                        }
                                    }
                                );
                                return true;
                            }
                        }
                    }
                    if (act == 2) {//read
                        int nid = text.getJSONObject("data").getInt("nid");
                        for (int i = 0; i < mAdapter.getCount(); i ++) {
                            DialogFragment dialog = (DialogFragment) mAdapter.getItem(i);
                            if (nid == dialog.contactId) {
                                final DialogFragment currentDialog = dialog;
                                runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            currentDialog.onRead();
                                        }
                                    }
                                );
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            android.util.Log.e("lal", "", e);
        }
        return false;
    }

    @Override
    public void onAuthenticatorResult(Session session) {
        if (session == null) finish();
        else {
            mSession = session;

            Intent intent = mIntent == null ? getIntent() : mIntent;
            onNewIntent(intent);

            Application.getNotificationManager().addListener(this);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
