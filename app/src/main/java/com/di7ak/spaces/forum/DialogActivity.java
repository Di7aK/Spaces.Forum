package com.di7ak.spaces.forum;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.adapters.MessageAdapter;
import com.di7ak.spaces.forum.api.Request;
import com.di7ak.spaces.forum.api.RequestListener;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.models.Message;
import com.di7ak.spaces.forum.util.DBHelper;
import com.rey.material.widget.FloatingActionButton;
import com.rey.material.widget.ProgressView;
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
    int mUserId;
    String mAddr;
    Uri mUri;
    boolean mTalk;
    String mMembers;
    String mAvatar;
    int mLastMessageId;
    DBHelper mDBHelper;
    SQLiteDatabase mDb;
    int mLastReceivedMsgId;
    List<String> mLastReceivedMsgs;

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
        mLastReceivedMsgs = new ArrayList<String>();
        mDBHelper = new DBHelper(this);
        mDb = mDBHelper.getWritableDatabase();
        mAdapter = new MessageAdapter(this);
        mMessageList.setAdapter(mAdapter);
        mMessageList.setDivider(null);

        Authenticator.getSession(this, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dialog, menu);
        mUpdateItem = menu.getItem(0);
        customUpdate();
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update:
                mUpdateItem = item;
                customUpdate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    boolean mPaused;
    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
        //Application.notificationManager.removeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPaused = false;
        //Application.notificationManager.addListener(this);
        //customUpdate();
    }

    boolean mUpdating;
    MenuItem mUpdateItem;
    public void customUpdate() {
        if (mUpdating || mUpdateItem == null) return;
        mUpdating = true;
        View snackView = getLayoutInflater().inflate(R.layout.progress_snackbar, null);
        ProgressView pv = (ProgressView)snackView.findViewById(R.id.progress_pv_circular_determinate);
        pv.start();

        mUpdateItem.setActionView(pv);
        getMessages(mUri);
    }

    public void stopUpdating() {
        mUpdateItem.setActionView(null);
        mUpdating = false;
    }

    int lastAct;
    @Override
    public boolean onNewNotification(JSONObject message) {
        // android.util.Log.d("lol", message.toString());
        try {
            if (message.has("text")) {
                JSONObject text = message.getJSONObject("text");
                if (text.has("act")) {
                    int act = text.getInt("act");
                    if (act == 24) {//typing
                        int id;
                        if (text.has("talk_id")) id = text.getInt("talk_id");
                        else id = text.getInt("contact_id");

                        if (id == mContact || id == mTalkId) {
                            if (mTalk) {
                                setSub(text.getString("user") + " печатает");
                            } else setSub("печатает");
                            new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(5000);
                                        } catch (InterruptedException e) {}
                                        if (mTalk) setSub(mMembers);
                                        else setSub("онлайн");
                                    }
                                }).start();
                            return true;
                        }
                    }
                    if (act == 1) {//new message
                        int nid = text.getJSONObject("data").getJSONObject("contact").getInt("nid");
                        if (nid == mContact) {
                            mLastReceivedMsgs.add(text.getJSONObject("data").getString("nid"));
                            getNewMessages();
                            return !mPaused;
                        }
                    }
                    if (act == 2) {//read
                        int nid = text.getJSONObject("data").getInt("nid");
                        if (nid == mContact && lastAct != 18) {
                            runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAdapter.makeAsRead();
                                        ContentValues cv = new ContentValues();
                                        cv.put("not_read", 0);
                                        mDb.update("messages", cv, "contact_id=" + mContact, null);
                                    }
                                });
                            return true;
                        }
                    }
                    lastAct = act;
                }
            }
        } catch (JSONException e) {
            android.util.Log.e("lal", "", e);
        }
        return false;
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
            mContact = Integer.valueOf(mUri.getQueryParameter("Contact"));
            mMessageBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View p1, boolean p2) {
                        if (p2) {
                            mMessageList.setSelection(mMessageList.getCount() - 1);

                        }
                    }


                });
            Application.notificationManager.addListener(this);

            showFromDb();
            customUpdate();

            mMessageBox.setEnabled(true);
            mMessageBox.setFocusableInTouchMode(true);
            mMessageBox.setFocusable(true);
            mBtnSend.setEnabled(true);
        }
	}

    private void getMessages(Uri uri) {
        if (uri == null) return;
        Request request = new Request(uri);
        request.executeWithListener(this);

    }

    boolean mUpdatingLast;
    private void getNewMessages() {
        if (mUpdatingLast) return;
        mUpdatingLast = true;
        String msgIds = "";
        for (String id : mLastReceivedMsgs)
            msgIds += id + ",";

        String post =     "sid=" + mSession.sid
            + "&MeSsages=" + msgIds
            + "&Pag=0"
            + "&method=getMessagesByIds"
            + "&Contact=" + mContact;
        Request request = new Request(Uri.parse("http://spaces.ru/neoapi/mail/"));
        request.setPost(post);
        request.executeWithListener(new RequestListener() {

                @Override
                public void onSuccess(JSONObject json) {
                    if (json.has("messages")) {
                        try {
                            JSONObject messages = json.getJSONObject("messages");
                            JSONArray result = new JSONArray();
                            List<String> toRemove = new ArrayList<String>();
                            for (String id : mLastReceivedMsgs) {
                                if (messages.has(id)) {
                                    result.put(messages.getJSONObject(id));
                                    toRemove.add(id);
                                }
                            }
                            mLastReceivedMsgs.removeAll(toRemove);
                            boolean bottom = mMessageList.getLastVisiblePosition() == mMessageList.getAdapter().getCount() - 1;
                            handleMessages(result);
                            if (bottom) mMessageList.setSelection(mMessageList.getCount() - 1);
                            mUpdatingLast = false;
                            if (mLastReceivedMsgs.size() > 0) getNewMessages();
                        } catch (JSONException e) {
                            mMessageBox.setText(e.getMessage());
                        }
                    }
                }

                @Override
                public void onError(SpacesException e) {
                    Toast.makeText(DialogActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    mUpdatingLast = false;
                    getNewMessages();
                }


            });
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
        boolean bottom = mMessageList.getLastVisiblePosition() == mMessageList.getAdapter().getCount() - 1;

        mAdapter.appendMessage(message);
        mAdapter.notifyDataSetChanged();
        if (bottom) mMessageList.setSelection(mMessageList.getCount() - 1);

        mMessageBox.setText("");
        if (mSending.size() == 1) sendMessages();
    }

    int mRetryCount;
    public void sendMessages() {
        if (mSending.size() == 0) return;
        final Message message = mSending.get(0);
        StringBuilder args = new StringBuilder();
        args.append("method=").append("sendMessage")
            .append("&Contact=").append(Integer.toString(mContact))
            .append("&sid=").append(Uri.encode(mSession.sid))
            .append("&CK=").append(Uri.encode(mSession.ck))
            .append("&texttT=").append(Uri.encode(message.text));
        Request request = new Request(Uri.parse("http://spaces.ru/neoapi/mail/"));
        request.setPost(args.toString());
        request.executeWithListener(new RequestListener() {

                @Override
                public void onSuccess(JSONObject json) {

                    try {
                        JSONObject data = json.getJSONObject("message");
                        message.time = data.getString("human_date");
                        message.text = data.getString("text");
                        message.read = !data.has("not_read");
                        message.nid = data.getInt("nid");
                        mAdapter.removeMessage(message);
                        mAdapter.appendMessage(message);
                        mAdapter.notifyDataSetChanged();

                        Cursor cursor;
                        cursor = mDb.query("messages", null, "msg_id = ?", new String[]{Integer.toString(message.nid)}, null, null, null);
                        if (cursor.getCount() == 0) {
                            ContentValues cv = new ContentValues();
                            cv.put("msg_id", message.nid);
                            cv.put("contact_id", mContact);
                            cv.put("type", message.type);
                            cv.put("date", message.time);
                            cv.put("message", message.text);
                            cv.put("user_id", mSession.nid);
                            cv.put("talk", mTalk ? 1 : 0);
                            cv.put("not_read", message.read ? 0 : 1);
                            mDb.insert("messages", null, cv);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(DialogActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }

                    mSending.remove(message);
                    mRetryCount = 0;
                    sendMessages();
                }

                @Override
                public void onError(SpacesException e) {
                    if (mRetryCount < 3) {
                        mRetryCount ++;
                        mSending.remove(message);
                    } else {
                        mRetryCount = 0;
                        Toast.makeText(DialogActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                    sendMessages();
                }
            });
    }

    @Override
    public void onSuccess(JSONObject json) { 
        try {
            stopUpdating();
            JSONObject contact = json.getJSONObject("contact_info");
            mContact = contact.getInt("nid");
            mTalk = contact.has("talk");
            mLastReceivedMsgId = contact.getInt("last_received_msg_id");
            if (contact.has("user_id")) mUserId = contact.getInt("user_id");
            if (mTalk) {
                mMembers = contact.getString("members_cnt");
                mTalkId = contact.getInt("talk_id");

                setSub(mMembers);
            } 
            if (contact.has("avatar")) {
                mAvatar = contact.getJSONObject("avatar").getString("previewURL");
            } else if (contact.has("widget")) {
                JSONObject widget = contact.getJSONObject("widget");
                mAvatar = widget.getJSONObject("avatar").getString("previewURL");
                if (widget.has("lastVisit")) {
                    String lastVisit = widget.getString("lastVisit");
                    if (TextUtils.isEmpty(lastVisit)) setSub("онлайн");
                    else setSub("был в сети " + lastVisit);
                }
            }
            if (json.has("new_msg_form")) {
                if (json.getJSONObject("new_msg_form").isNull("action")) {
                    if (mMessageBox.isFocusable()) {
                        mMessageBox.setEnabled(false);
                        mMessageBox.setFocusableInTouchMode(false);
                        mMessageBox.setFocusable(false);
                        mBtnSend.setEnabled(false);
                    }
                }
            } 
            mAddr = contact.getString("text_addr");
            setTitle(mAddr);

            Cursor cursor;
            ContentValues cv;

            cursor = mDb.query("contacts", null, "contact_id = ?", new String[]{Integer.toString(mContact)}, null, null, null);
            if (cursor.getCount() == 0) {
                cv = new ContentValues();
                cv.put("name", mAddr);
                cv.put("user_id", mUserId);
                cv.put("talk_id", mTalkId);
                cv.put("contact_id", mContact);
                cv.put("avatar", mAvatar);
                mDb.insert("contacts", null, cv);
            }

            boolean bottom = mMessageList.getLastVisiblePosition() == mMessageList.getAdapter().getCount() - 1;
            JSONArray messages = json.getJSONArray("msg_list");
            handleMessages(messages);
            mAdapter.notifyDataSetChanged();
            if (bottom) mMessageList.setSelection(mMessageList.getCount() - 1);
        } catch (JSONException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void handleMessages(JSONArray messages) throws JSONException {
        for (int i = messages.length() - 1; i >= 0; i --) {
            JSONObject data = messages.getJSONObject(i);
            Message message = new Message();
            message.time = data.getString("human_date");
            message.text = data.getString("text");
            message.read = !data.has("not_read");
            message.nid = data.getInt("nid");

            if (data.has("system")) message.type = Message.TYPE_SYSTEM;
            else if (data.has("received")) message.type = Message.TYPE_RECEIVED;
            else message.type = Message.TYPE_MY;

            if (message.type != Message.TYPE_SYSTEM) {
                if (mTalk) {
                    JSONObject contact = data.getJSONObject("contact");
                    message.avatar = contact.getJSONObject("avatar").getString("previewURL");
                    message.user = contact.getJSONObject("widget").getJSONObject("siteLink").getString("user_name");
                    message.userId = contact.getJSONObject("widget").getJSONObject("onlineStatus").getInt("id");
                } else {
                    if (message.type == Message.TYPE_MY) {
                        message.avatar = mSession.avatar;
                        message.userId = mSession.nid;
                    } else {
                        message.avatar = mAvatar;
                        message.userId = mUserId;
                    }
                }
            }
            message.talk = mTalk;
            Cursor cursor;
            ContentValues cv;
            if (mTalk) {
                cursor = mDb.query("contacts", null, "user_id = ?", new String[]{Integer.toString(message.userId)}, null, null, null);
                if (cursor.getCount() == 0) {
                    cv = new ContentValues();
                    cv.put("name", message.user);
                    cv.put("user_id", message.userId);
                    cv.put("talk_id", mTalkId);
                    cv.put("contact_id", "0");
                    cv.put("avatar", message.avatar);
                    mDb.insert("contacts", null, cv);
                }
            }
            cursor = mDb.query("messages", null, "msg_id = ?", new String[]{Integer.toString(message.nid)}, null, null, null);
            if (cursor.getCount() == 0) {
                cv = new ContentValues();
                cv.put("msg_id", message.nid);
                cv.put("contact_id", mContact);
                cv.put("type", message.type);
                cv.put("date", message.time);
                cv.put("message", message.text);
                cv.put("user_id", message.userId);
                cv.put("talk", mTalk ? 1 : 0);
                cv.put("not_read", message.read ? 0 : 1);
                mDb.insert("messages", null, cv);
                if (message.nid <= mLastMessageId) continue;
                mLastMessageId = message.nid;
                mAdapter.appendMessage(message);
            } else {
                cursor.moveToFirst();
                int iRead = cursor.getColumnIndex("not_read");
                int read = cursor.getInt(iRead);
                if (read == 1 && message.read) {
                    mAdapter.makeAsRead();
                    ContentValues cv2 = new ContentValues();
                    cv2.put("not_read", 0);
                    mDb.update("messages", cv2, "msg_id=" + message.nid, null);
                }
            }
        }
    }

    public void showFromDb() {
        Cursor cursor = mDb.query("messages", null, "contact_id = ?", new String[]{Integer.toString(mContact)}, null, null, null);
        if (cursor.getCount() != 0) {
            boolean bottom = mMessageList.getLastVisiblePosition() == mMessageList.getAdapter().getCount() - 1;
            int iMsgId = cursor.getColumnIndex("msg_id");
            int iType = cursor.getColumnIndex("type");
            int iDate = cursor.getColumnIndex("date");
            int iMessage = cursor.getColumnIndex("message");
            int iUserId = cursor.getColumnIndex("user_id");
            int iRead = cursor.getColumnIndex("not_read");
            int iTalk = cursor.getColumnIndex("talk");
            if (cursor.moveToFirst()) {
                do {
                    Message message = new Message();
                    message.nid = cursor.getInt(iMsgId);
                    message.type = cursor.getInt(iType);
                    message.time = cursor.getString(iDate);
                    message.text = cursor.getString(iMessage);
                    message.userId = cursor.getInt(iUserId);
                    message.read = cursor.getInt(iRead) == 0;
                    message.talk = cursor.getInt(iTalk) == 1;
                    Cursor contact = mDb.query("contacts", null, "user_id = ?", new String[]{Integer.toString(message.userId)}, null, null, null);
                    if (contact.getCount() != 0) {
                        contact.moveToFirst();
                        int iName = contact.getColumnIndex("name");
                        int iAvatar = contact.getColumnIndex("avatar");
                        message.user = contact.getString(iName);
                        message.avatar = contact.getString(iAvatar);
                    }
                    if (message.nid >= mLastMessageId) mLastMessageId = message.nid;
                    mAdapter.appendMessage(message);
                } while (cursor.moveToNext());
                mAdapter.notifyDataSetChanged();

                if (bottom) mMessageList.setSelection(mMessageList.getCount() - 1);
            }
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
