package com.di7ak.spaces.forum;

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
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.adapters.ContactAdapter;
import com.di7ak.spaces.forum.adapters.DialogAdapter;
import com.di7ak.spaces.forum.api.Request;
import com.di7ak.spaces.forum.api.RequestListener;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.fragments.DialogFragment;
import com.di7ak.spaces.forum.util.DBHelper;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class DialogsActivity extends AppCompatActivity implements 
Authenticator.OnResult,
NotificationManager.OnNewNotification,
ActionBar.OnNavigationListener,
ViewPager.OnPageChangeListener,
DialogFragment.OnNewMessage,
DialogFragment.OnDialogCreated {
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
    List<Integer> mNewCnt;
    TextView mCurrentTitle;

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

        mNewCnt = new ArrayList<Integer>();
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
    public void onNewMessage(int contact) {
        for(int i = 0; i < mAdapter.getCount(); i ++) {
            DialogFragment dialog = (DialogFragment) mAdapter.getItem(i);
            if(dialog.contactId == contact) {
                int current = mViewPager.getCurrentItem();
                if(i != current) {
                    int n = mNewCnt.get(i);
                    mNewCnt.set(i, n + 1);
                    
                    mCurrentTitle.setVisibility(View.VISIBLE);
                    mCurrentTitle.setText(Integer.toString(getNewMessages()));
                    
                    TextView v = (TextView)dialog.getDropDownTitleView(null, null).findViewById(R.id.new_cnt);
                    v.setVisibility(View.VISIBLE);
                    v.setText(Integer.toString(mNewCnt.get(i)));
                } else markAsRead(contact);
            }
        }
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
        setItem(i);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
    
    @Override
    public void onDialogCreated(DialogFragment dialog) {
        android.util.Log.d("lol", "created " + dialog.contactId);
        
        mNewCnt.add(0);
        mContactAdapter.add(dialog);
        mContactAdapter.notifyDataSetChanged();
        dialog.setOnNewMessageListener(this);
        setItem(mAdapter.indexOf(dialog.contactId));
    }
    
    public void setItem(int i) {
        mViewPager.setCurrentItem(i);
        mActionBar.setSelectedNavigationItem(i);
        if(mCurrentTitle != null) {
            mCurrentTitle.setVisibility(View.GONE);
        }
        DialogFragment selected = (DialogFragment)mAdapter.getItem(i);
        mCurrentTitle = (TextView) selected.getTitleView(null, null).findViewById(R.id.new_cnt);
                    
        mNewCnt.set(i, 0);
        markAsRead(selected.contactId);
        int newCnt = getNewMessages();
        mCurrentTitle.setText(Integer.toString(newCnt));
        mCurrentTitle.setVisibility(newCnt > 0 ? View.VISIBLE : View.GONE);
        
        TextView v = (TextView) selected.getDropDownTitleView(null, null).findViewById(R.id.new_cnt);
        v.setVisibility(View.GONE);
        
        invalidateOptionsMenu();
        
        
    }
    
    private void markAsRead(final int contact) {
        StringBuilder args = new StringBuilder();
        args.append("method=").append("markContactsAsRead")
            .append("&CoNtacts=").append(Integer.toString(contact))
            .append("&sid=").append(Uri.encode(mSession.sid))
            .append("&CK=").append(Uri.encode(mSession.ck));
        Request request = new Request(Uri.parse("http://spaces.ru/neoapi/mail/"));
        request.setPost(args.toString());
        request.executeWithListener(new RequestListener() {

                @Override
                public void onSuccess(JSONObject json) {

                    
                }

                @Override
                public void onError(SpacesException e) {
                    markAsRead(contact);
                }
            });
    }
    
    private int getNewMessages() {
        int total = 0;
        for(int cnt : mNewCnt) total += cnt;
        return total;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(mSession == null) {
            mIntent = intent;
            return;
        }
        android.util.Log.d("lol", "on new intent");
        try {
        Uri uri = intent.getData();
        int contact = Integer.valueOf(uri.getQueryParameter("Contact"));
        int idx = mAdapter.indexOf(contact);
        if (idx == -1) {
            DialogFragment dialog = new DialogFragment(mSession, contact, mDb, this);
            mAdapter.appendDialog(dialog);
            mAdapter.notifyDataSetChanged();
            android.util.Log.d("lol", "create dialog " + contact);
        } else setItem(idx);
        } catch(Exception e) {
            android.util.Log.e("lol", "", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mAdapter.getCount() == 0) return false;
        int idx = mViewPager.getCurrentItem();
        return ((DialogFragment)mAdapter.getItem(idx)).onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mAdapter.getCount() == 0) return false;
        int idx = mViewPager.getCurrentItem();
        return ((DialogFragment)mAdapter.getItem(idx)).onOptionsItemSelected(item);
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
