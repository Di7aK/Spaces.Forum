package com.di7ak.spaces.forum;

import android.content.Intent;
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
import com.di7ak.spaces.forum.api.Api;
import com.di7ak.spaces.forum.api.Request;
import com.di7ak.spaces.forum.api.RequestListener;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.fragments.DialogFragment;
import com.di7ak.spaces.forum.models.Message;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class DialogsActivity extends AppCompatActivity implements 
Authenticator.OnResult,
ActionBar.OnNavigationListener,
ViewPager.OnPageChangeListener,
MessageService.MessageListener {
    Toolbar toolbar;
    Session mSession;
    boolean mPaused;
    ViewPager mViewPager;
    DialogAdapter mAdapter;
    ContactAdapter mContactAdapter;
    ActionBar mActionBar;
    Intent mIntent;
    MessageService mMessageService;
    List<Integer> mNewMsgs;
    int mLastIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        mNewMsgs = new ArrayList<Integer>();
        mAdapter = new DialogAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(this);
        mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        mContactAdapter = new ContactAdapter();
        mActionBar.setListNavigationCallbacks(mContactAdapter, this);

        Authenticator.getSession(this, this);
    }

    public DialogFragment getCurrentDialog() {
        int current = mViewPager.getCurrentItem();
        return (DialogFragment)mAdapter.getItem(current);
    }

    public DialogFragment getDialog(int contact) {
        int index = mAdapter.indexOf(contact);
        return (DialogFragment)mAdapter.getItem(index);
    }

    @Override
    public boolean onNavigationItemSelected(int idx, long p) {
        setItem(idx);
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

    public void setItem(int i) {
        TextView newCnt = (TextView) ((DialogFragment)mAdapter.getItem(mLastIndex)).getTitleView(null, null).findViewById(R.id.new_cnt);
        mNewMsgs.set(mLastIndex, 0);
        newCnt.setVisibility(View.GONE);
        newCnt.setText(Integer.toString(0));

        mViewPager.setCurrentItem(i);
        mActionBar.setSelectedNavigationItem(i);

        Request request = Api.markAsRead(getCurrentDialog().contact.id);
        request.executeWithListener(new RequestListener() {

                @Override
                public void onSuccess(JSONObject json) {

                }

                @Override
                public void onError(SpacesException e) {

                }
            });

        newCnt = (TextView) getCurrentDialog().getTitleView(null, null).findViewById(R.id.new_cnt);
        int total = 0;
        for (int count : mNewMsgs) {
            total += count;
        }
        newCnt.setVisibility(total > 0 ? View.VISIBLE : View.GONE);
        newCnt.setText(Integer.toString(total));

        mLastIndex = i;

        getCurrentDialog().updateCnt(0);

        invalidateOptionsMenu();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mSession == null) {
            mIntent = intent;
            return;
        }

        try {
            Uri uri = intent.getData();
            int contact = Integer.valueOf(uri.getQueryParameter("Contact"));
            int lastMsgId = 0;
            try {
                lastMsgId = Integer.valueOf(uri.getQueryParameter("last_msg_id"));
            } catch (Exception e) {}
            int idx = mAdapter.indexOf(contact);
            if (idx == -1) {
                addDialog(contact, true, lastMsgId);
            } else setItem(idx);
        } catch (Exception e) {
            android.util.Log.e("lol", "", e);
        }
    }

    public int addDialog(int contact, final boolean select, int lastMsgId) {
        mNewMsgs.add(0);
        DialogFragment dialog = new DialogFragment(contact, lastMsgId, new DialogFragment.OnDialogCreated() {
                @Override
                public void onDialogCreated(DialogFragment dialog) {
                    mContactAdapter.add(dialog);
                    mContactAdapter.notifyDataSetChanged();
                    if (select) {
                        setItem(mAdapter.indexOf(dialog.contact.id));
                    }
                }
            });
        int idx = mAdapter.appendDialog(dialog);
        mAdapter.notifyDataSetChanged();
        return idx;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mAdapter.getCount() == 0) return false;
        return getCurrentDialog().onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mAdapter.getCount() == 0) return false;
        int idx = mViewPager.getCurrentItem();
        return ((DialogFragment)mAdapter.getItem(idx)).onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(2);
        mPaused = false;
    } 

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMessageService != null) mMessageService.removeListener(this);
    }

    @Override
    public void onAuthenticatorResult(Session session) {
        if (session == null) finish();
        else {
            mSession = session;

            Intent intent = mIntent == null ? getIntent() : mIntent;
            onNewIntent(intent);

            mMessageService = Application.getMessageService(this);
            mMessageService.addListener(this);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onNewMessage(Message message) {

        int index = mAdapter.indexOf(message.contact);
        int current = 0;
        if (index == -1) {
            index = addDialog(message.contact, false, message.nid);
            current = mNewMsgs.get(index);
            current ++;
        } else {
            current = mNewMsgs.get(index);
            current ++;
            ((DialogFragment)mAdapter.getItem(index)).onNewMessage(message, current);
        }
        if (getCurrentDialog().contact.id == message.contact) {
            getCurrentDialog().updateCnt(0);
            Request request = Api.markAsRead(getCurrentDialog().contact.id);
            request.executeWithListener(new RequestListener() {

                    @Override
                    public void onSuccess(JSONObject json) {

                    }

                    @Override
                    public void onError(SpacesException e) {

                    }
                });
        } else {
            mNewMsgs.set(index, current);
            notifyOnTitle(message);
        }

        return false;
    }

    private void notifyOnTitle(Message message) {
        TextView newCnt = (TextView) getCurrentDialog().getTitleView(null, null).findViewById(R.id.new_cnt);
        int total = 0;
        for (int count : mNewMsgs) {
            total += count;
        }
        newCnt.setVisibility(View.VISIBLE);
        newCnt.setText(Integer.toString(total));


    }

    @Override
    public void onSuccess(Message message) {
        getDialog(message.contact).onSuccess(message);
    }

    @Override
    public void onTyping(final int contact, final String user) {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAdapter.indexOf(contact) == -1) return;
                    getDialog(contact).onTyping(user);
                }
            });
    }

    @Override
    public void onRead(final int contact) {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getDialog(contact).onRead();
                }
            });
    }

    @Override
    public void onError(Message message, SpacesException exception) {
        getDialog(message.contact).onError(message);
    }

}
