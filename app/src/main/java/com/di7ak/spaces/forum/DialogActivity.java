package com.di7ak.spaces.forum;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.di7ak.spaces.forum.adapters.MessageAdapter;
import com.di7ak.spaces.forum.api.Request;
import com.di7ak.spaces.forum.api.RequestListener;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.models.Message;
import com.rey.material.widget.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DialogActivity extends AppCompatActivity implements 
Authenticator.OnResult,
RequestListener, View.OnClickListener,
NotificationManager.OnNewNotification {
    Toolbar toolbar;
    ListView mMessageList;
    MessageAdapter mAdapter;
    List<Message> mSending;
    Session mSession;
    FloatingActionButton mBtnSend;
    EditText mMessageBox;
    int mContact;
    int mTalkId;
    String mAddr;
    String mAction;
    Uri mUri;
    boolean mTalk;
    String mMembers;
    String mAvatar;
    int mLastMessageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    onBackPressed();
                }
            });

        mMessageList = (ListView) findViewById(R.id.messages);
        mBtnSend = (FloatingActionButton) findViewById(R.id.fab_send);
        mMessageBox = (EditText) findViewById(R.id.message);
        
        mBtnSend.setOnClickListener(this);
        mMessageBox.setEnabled(false);
        mMessageBox.setFocusable(false);
        mBtnSend.setEnabled(false);
        
        mSending = new ArrayList<Message>();
        Authenticator.getSession(this, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Application.notificationManager.removeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Application.notificationManager.addListener(this);
        getMessages(mUri);
    }

    int lastAct;
    @Override
    public void onNewNotification(JSONObject message) {
       // android.util.Log.d("lol", message.toString());
        try {
            if (message.has("text")) {
                JSONObject text = message.getJSONObject("text");
                if (text.has("act")) {
                    int act = text.getInt("act");
                    if (act == 24) {//typing
                        int id;
                        if(text.has("talk_id")) id = text.getInt("talk_id");
                        else id = text.getInt("contact_id");
      
                        if (id == mContact || id == mTalkId) {
                            if(mTalk) {
                                setSub(text.getString("user") + " печатает");
                            } else setSub("печатает");
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException e) {}
                                    if(mTalk) setSub(mMembers);
                                    else setSub("онлайн");
                                }
                            }).start();
                        }
                    }
                    if (act == 1) {//new message
                        int nid = text.getJSONObject("data").getJSONObject("contact").getInt("nid");

                        if (nid == mContact) {
                            getMessages(mUri);
                        }
                    }
                    if(act == 2) {//read
                        int nid = text.getJSONObject("data").getInt("nid");
                        if(nid == mContact && lastAct != 18) {
                            runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                            mAdapter.makeAsRead();
                            }
                            });
                        }
                    }
                    lastAct = act;
                }
            }
        } catch (JSONException e) {
            android.util.Log.e("lal", "", e);
        }
    }

    private void setSub(final String msg) {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getSupportActionBar().setSubtitle(msg);
                }
            });
    }
    
    @Override
    public void onAuthenticatorResult(Session session) {
        if (session == null) finish();
        else {
            mSession = session;
            mUri = getIntent().getData();
            mUri = Uri.parse(mUri.toString() + "&sid=" + session.sid);
            getMessages(mUri);
        }
	}

    private void getMessages(Uri uri) {
        if(uri == null) return;
        Request request = new Request(uri);
        request.executeWithListener(this);
    }

    @Override
    public void onClick(View v) {
        Message message = new Message();
        message.text = mMessageBox.getText().toString();
        message.read = false;
        message.talk = mTalk;
        message.time = "отправка";
        message.avatar = mSession.avatar;
        message.user = mSession.login;
        message.type = Message.TYPE_MY;
        mSending.add(message);
        boolean bottom = mMessageList.getLastVisiblePosition() == mMessageList.getAdapter().getCount() -1;
        
        mAdapter.appendMessage(message);
        mAdapter.notifyDataSetChanged();
        if(bottom) mMessageList.setSelection(mMessageList.getCount() - 1);
        
        mMessageBox.setText("");
        if(mSending.size() == 1) sendMessages();
    }
    
    public void sendMessages() {
        if(mSending.size() == 0) {
            getMessages(mUri);
            return;
        }
        final Message message = mSending.get(0);
        String post =     "sid=" + mSession.sid
                        + "&CK=" + mSession.ck
                        + "&texttT=" + Uri.encode(message.text)
                        + "&cfms=" + Uri.encode("Отправить")
                        + "&Contact=" + mContact;
        Request request = new Request(Uri.parse(mAction));
        request.setPost(post);
        request.executeWithListener(new RequestListener() {

                @Override
                public void onSuccess(JSONObject json) {
                    mAdapter.removeMessage(message);
                    mSending.remove(message);
                    sendMessages();
                }

                @Override
                public void onError(SpacesException e) {
                    Toast.makeText(DialogActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    sendMessages();
                }


            });
    }

    @Override
    public void onSuccess(JSONObject json) { 
        try {
            JSONObject contact = json.getJSONObject("contact_info");
            mContact = contact.getInt("nid");
            mTalk = contact.has("talk");
            if(mTalk) {
                mMembers = contact.getString("members_cnt");
                mTalkId = contact.getInt("talk_id");
                setSub(mMembers);
            } 
            if(contact.has("avatar")) {
                mAvatar = contact.getJSONObject("avatar").getString("previewURL");
            } else if(contact.has("widget")) {
                JSONObject widget = contact.getJSONObject("widget");
                mAvatar = widget.getJSONObject("avatar").getString("previewURL");
                if(widget.has("lastVisit")) {
                    String lastVisit = widget.getString("lastVisit");
                    if(TextUtils.isEmpty(lastVisit)) setSub("онлайн");
                    else setSub("был в сети " + lastVisit);
                }
            }
            if (json.has("new_msg_form")) {
                mAction = json.getJSONObject("new_msg_form").getString("action");
                if (!mMessageBox.isFocusable()) {
                    mMessageBox.setEnabled(true);
                    mMessageBox.setFocusableInTouchMode(true);
                    mMessageBox.setFocusable(true);
                    mBtnSend.setEnabled(true);
                }
            } 
            mAddr = contact.getString("text_addr");
            setTitle(mAddr);
            
            if (mAdapter == null) {
                mAdapter = new MessageAdapter(this);
                mMessageList.setAdapter(mAdapter);
                mMessageList.setDivider(null);
            }
            boolean bottom = mMessageList.getLastVisiblePosition() == mMessageList.getAdapter().getCount() -1;
            JSONArray messages = json.getJSONArray("msg_list");
            handleMessages(messages);
            mAdapter.notifyDataSetChanged();
            if(bottom) mMessageList.setSelection(mMessageList.getCount() - 1);
        } catch (JSONException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
    
    public void handleMessages(JSONArray messages) throws JSONException {
        for(int i = messages.length() - 1; i >=0; i --) {
            JSONObject data = messages.getJSONObject(i);
            Message message = new Message();
            message.time = data.getString("human_date");
            message.text = data.getString("text");
            message.read = !data.has("not_read");
            message.nid = data.getInt("nid");
            if(message.nid <= mLastMessageId) continue;
            mLastMessageId = message.nid;
            if(data.has("system")) message.type = Message.TYPE_SYSTEM;
            else if(data.has("received")) message.type = Message.TYPE_RECEIVED;
            else message.type = Message.TYPE_MY;
            
            if(message.type != Message.TYPE_SYSTEM) {
                if(mTalk) {
                    JSONObject contact = data.getJSONObject("contact");
                    message.avatar = contact.getJSONObject("avatar").getString("previewURL");
                    message.user = contact.getJSONObject("widget").getJSONObject("siteLink").getString("user_name");
                } else {
                    if(message.type == Message.TYPE_MY) {
                        message.avatar = mSession.avatar;
                    } else {
                        message.avatar = mAvatar;
                    }
                }
            }
            
            message.talk = mTalk;
            mAdapter.appendMessage(message);
        }
    }

    @Override
    public void onError(SpacesException e) {
        Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        getMessages(mUri);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
