package com.di7ak.spaces.forum.fragments;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.di7ak.spaces.forum.DialogsActivity;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.adapters.MessageAdapter;
import com.di7ak.spaces.forum.api.Request;
import com.di7ak.spaces.forum.api.RequestListener;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.models.Message;
import com.di7ak.spaces.forum.util.SpImageGetter;
import com.di7ak.spaces.forum.widget.AvatarView;
import com.rey.material.widget.FloatingActionButton;
import com.rey.material.widget.ProgressView;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DialogFragment extends Fragment implements RequestListener, View.OnClickListener {
    private EditText mMsgBox;
    private ListView mMsgList;
    private View mTitleView;
    private View mDropDownTitleView;
    private FloatingActionButton mBtnSend;
    private Session mSession;
    private MessageAdapter mMsgAdapter;
    private List<Message> mSending;
    private List<Integer> mLastReceivedMsgs;
    private SQLiteDatabase mDb;
    private boolean mRefreshing;
    private boolean mTalk;
    private TypingTask mTypingTask;
    private OnNewMessage mListener;
    private OnDialogCreated mOnDialogCreated;
    private Timer mTimer;
    private String mMembers;
    private String mAddr;
    private String mAvatar;
    private int mLastMsgId;
    private int mLastReceivedMsgId;
    private int mUserId;
    private boolean mPaused = true;

    public int contactId;
    public int talkId;

    public DialogFragment(Session session, int contact, SQLiteDatabase db, OnDialogCreated onCreated) {
        super();

        mSession = session;
        contactId = contact;
        mDb = db;
        mOnDialogCreated = onCreated;

        mSending = new ArrayList<Message>();
        mLastReceivedMsgs = new ArrayList<Integer>();
        mTimer = new Timer();
        mTypingTask = new TypingTask();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog, parent, false);
        mMsgBox = (EditText) view.findViewById(R.id.message);
        mMsgList = (ListView) view.findViewById(R.id.messages);
        mBtnSend = (FloatingActionButton) view.findViewById(R.id.fab_send);
        mBtnSend.setOnClickListener(this);
        mMsgBox.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                    typing();
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });

        mMsgAdapter = new MessageAdapter(getActivity());
        mMsgList.setAdapter(mMsgAdapter);
        mMsgList.setDivider(null);

        mTitleView = getActivity().getLayoutInflater().inflate(R.layout.mail_title_item, parent, false);

        mDropDownTitleView = getActivity().getLayoutInflater().inflate(R.layout.mail_drop_down, parent, false);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(
            AbsListView.LayoutParams.MATCH_PARENT,
            AbsListView.LayoutParams.WRAP_CONTENT);
        mDropDownTitleView.setLayoutParams(params);
        final int height = mMsgList.getMeasuredHeight();
        mMsgList.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                private int mPreviousHeight = height;

                @Override
                public void onGlobalLayout() {
                    int newHeight = mMsgList.getMeasuredHeight();
                    if (mPreviousHeight != 0) {
                        int r = mPreviousHeight - newHeight;
                        if (r > 0) {
                            onKeyboardShowing();
                        }
                    }
                    mPreviousHeight = newHeight;
                }
            });


        showFromDb();

        if (mOnDialogCreated != null) mOnDialogCreated.onDialogCreated(this);

        return view;
    }

    public void setOnNewMessageListener(OnNewMessage listener) {
        mListener = listener;
    }

    public void setOnDialogCreatedListener(OnDialogCreated listener) {
        mOnDialogCreated = listener;
    }

    boolean mLoaded;
    MenuItem mUpdateItem;
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_dialog, menu);
        mUpdateItem = menu.getItem(0);
        if (!mLoaded) {
            refresh();
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update:
                mUpdateItem = item;
                refresh();
                return true;
        }
        return false;
    }

    public View getTitleView(View convert, ViewGroup parent) {
        return mTitleView;
    }

    public View getDropDownTitleView(View convert, ViewGroup parent) {
        return mDropDownTitleView;
    }

    public void refresh() {
        if (mRefreshing || mUpdateItem == null) return;
        mRefreshing = true;
        View snackView = getActivity().getLayoutInflater().inflate(R.layout.progress_snackbar, null);
        ProgressView pv = (ProgressView)snackView.findViewById(R.id.progress_pv_circular_determinate);
        pv.start();
        mUpdateItem.setActionView(pv);
        getMessages(1);
    }

    public void stopUpdating() {
        if (mUpdateItem != null) mUpdateItem.setActionView(null);
        mRefreshing = false;
    }

    public void onReceived(int msgId) {
        mLastReceivedMsgs.add(msgId);
        getNewMessages();
    }

    public void onKeyboardShowing() {
        mMsgList.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMsgList.setSelection(mMsgList.getCount());
                    mMsgList.smoothScrollToPosition(mMsgList.getCount());
                }
            }, 100);
    }

    public void onRead() {
        mMsgAdapter.makeAsRead();
        ContentValues cv = new ContentValues();
        cv.put("not_read", 0);
        mDb.update("messages", cv, "contact_id=" + contactId, null);
    }

    public void onTyping(String user) {
        mTimer.cancel();
        mTimer = new Timer();
        mTypingTask = new TypingTask();
        mTimer.schedule(mTypingTask, 6000);
        if (mTalk) setSubtitle(user + " печатает");
        else setSubtitle("печатает");
    }

    private long mLastTyping;
    public void typing() {
        long time = System.currentTimeMillis();
        if (time - mLastTyping < 4000) return;
        mLastTyping = time;
        StringBuilder args = new StringBuilder();
        args.append("method=").append("typing")
            .append("&Contact=").append(Integer.toString(contactId))
            .append("&no_notify=").append(Integer.toString(1))
            .append("&message=").append(mMsgBox.getText().toString())
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
                    typing();
                }
            });
    }

    public void setUser(String text) {
        mAddr = text;
        ((TextView)mTitleView.findViewById(R.id.name)).setText(text);
        ((TextView)mDropDownTitleView.findViewById(R.id.name)).setText(text);
    }

    public void setSubtitle(String text) {
        ((TextView)mTitleView.findViewById(R.id.description)).setText(text);
    }

    public void setDropDownSubtitle(String text) {
        TextView textV = (TextView)mDropDownTitleView.findViewById(R.id.description);
        Spanned sText = Html.fromHtml(text, new SpImageGetter(textV), null);
        textV.setText(sText);
    }

    public void setAvatar(String url) {
        mAvatar = url;
        ((AvatarView)mTitleView.findViewById(R.id.avatar)).setUrl(url);
        ((AvatarView)mDropDownTitleView.findViewById(R.id.avatar)).setUrl(url);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab_send) {
            Message message = new Message();
            message.text = mMsgBox.getText().toString();
            message.read = false;
            message.talk = mTalk;
            message.time = "отправка";
            message.avatar = mSession.avatar;
            message.user = mSession.login;
            message.type = Message.TYPE_MY;
            mSending.add(message);
            boolean bottom = mMsgList.getLastVisiblePosition() == mMsgList.getAdapter().getCount() - 1;

            mMsgAdapter.appendMessage(message);
            mMsgAdapter.notifyDataSetChanged();
            if (bottom) mMsgList.setSelection(mMsgList.getCount() - 1);

            mMsgBox.setText("");
            if (mSending.size() == 1) sendMessages();
        }
    }

    int mRetryCount;
    public void sendMessages() {
        if (mSending.size() == 0) return;
        final Message message = mSending.get(0);
        StringBuilder args = new StringBuilder();
        args.append("method=").append("sendMessage")
            .append("&Contact=").append(Integer.toString(contactId))
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
                        mMsgAdapter.removeMessage(message);
                        mMsgAdapter.appendMessage(message);
                        mMsgAdapter.notifyDataSetChanged();

                        Cursor cursor;
                        cursor = mDb.query("messages", null, "msg_id = ?", new String[]{Integer.toString(message.nid)}, null, null, null);
                        if (cursor.getCount() == 0) {
                            ContentValues cv = new ContentValues();
                            cv.put("msg_id", message.nid);
                            cv.put("contact_id", contactId);
                            cv.put("type", message.type);
                            cv.put("date", message.time);
                            cv.put("message", message.text);
                            cv.put("user_id", mSession.nid);
                            cv.put("talk", mTalk ? 1 : 0);
                            cv.put("not_read", message.read ? 0 : 1);
                            mDb.insert("messages", null, cv);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                    }
                    sendMessages();
                }
            });
    }

    public void getMessages(int page) {
        Uri uri = Uri.parse("http://spaces.ru/mail/message_list/?Contact=" + contactId + "&sid=" + mSession.sid);
        Request request = new Request(uri);
        request.executeWithListener(this);
        mLoaded = true;
    }

    @Override
    public void onSuccess(JSONObject json) {
        stopUpdating();
        try {
            JSONObject contact = json.getJSONObject("contact_info");
            contactId = contact.getInt("nid");
            mTalk = contact.has("talk");
            // mLastReceivedMsgId = contact.getInt("last_received_msg_id");
            if (contact.has("user_id")) mUserId = contact.getInt("user_id");
            if (mTalk) {
                mMembers = contact.getString("members_cnt");
                talkId = contact.getInt("talk_id");

                setSubtitle(mMembers);
            } 
            if (contact.has("avatar")) {
                setAvatar(contact.getJSONObject("avatar").getString("previewURL"));
            } else if (contact.has("widget")) {
                JSONObject widget = contact.getJSONObject("widget");
                setAvatar(widget.getJSONObject("avatar").getString("previewURL"));
                if (widget.has("lastVisit")) {
                    String lastVisit = widget.getString("lastVisit");
                    if (TextUtils.isEmpty(lastVisit)) setSubtitle("онлайн");
                    else setSubtitle("был в сети " + lastVisit);
                }
            }
            if (json.has("new_msg_form")) {
                if (json.getJSONObject("new_msg_form").isNull("action")) {
                    if (mMsgBox.isFocusable()) {
                        mMsgBox.setEnabled(false);
                        mMsgBox.setFocusableInTouchMode(false);
                        mMsgBox.setFocusable(false);
                        mBtnSend.setEnabled(false);
                    }
                }
            } 
            setUser(contact.getString("text_addr"));

            Cursor cursor;
            ContentValues cv;

            cursor = mDb.query("contacts", null, "contact_id = ?", new String[]{Integer.toString(contactId)}, null, null, null);
            if (cursor.getCount() == 0) {
                cv = new ContentValues();
                cv.put("name", mAddr);
                cv.put("user_id", mUserId);
                cv.put("talk_id", talkId);
                cv.put("contact_id", contactId);
                cv.put("avatar", mAvatar);
                mDb.insert("contacts", null, cv);
            }

            boolean bottom = mMsgList.getLastVisiblePosition() == mMsgList.getAdapter().getCount() - 1;
            JSONArray messages = json.getJSONArray("msg_list");
            handleMessages(messages);
            mMsgAdapter.notifyDataSetChanged();
            if (bottom) mMsgList.setSelection(mMsgList.getCount() - 1);
        } catch (JSONException e) {
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(SpacesException e) {
        if(getActivity() != null) {
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
            getMessages(1);
        }
    }

    boolean mUpdatingLast;
    private void getNewMessages() {
        if (mUpdatingLast) return;
        mUpdatingLast = true;
        String msgIds = "";
        for (int id : mLastReceivedMsgs)
            msgIds += id + ",";

        String post =     "sid=" + mSession.sid
            + "&MeSsages=" + msgIds
            + "&Pag=0"
            + "&method=getMessagesByIds"
            + "&Contact=" + contactId;
        Request request = new Request(Uri.parse("http://spaces.ru/neoapi/mail/"));
        request.setPost(post);
        request.executeWithListener(new RequestListener() {

                @Override
                public void onSuccess(JSONObject json) {
                    if (json.has("messages")) {
                        try {
                            JSONObject messages = json.getJSONObject("messages");
                            JSONArray result = new JSONArray();
                            List<Integer> toRemove = new ArrayList<Integer>();
                            for (int id : mLastReceivedMsgs) {
                                if (messages.has(Integer.toString(id))) {
                                    result.put(messages.getJSONObject(Integer.toString(id)));
                                    toRemove.add(id);
                                }
                            }
                            mLastReceivedMsgs.removeAll(toRemove);
                            boolean bottom = mMsgList.getLastVisiblePosition() == mMsgList.getAdapter().getCount() - 1;
                            handleMessages(result);
                            if (bottom) mMsgList.setSelection(mMsgList.getCount() - 1);
                            mUpdatingLast = false;
                            if (mLastReceivedMsgs.size() > 0) getNewMessages();
                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onError(SpacesException e) {
                    Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                    mUpdatingLast = false;
                    getNewMessages();
                }


            });
    }

    @Override
    public void onResume() {
        super.onResume();
        mPaused = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
    }

    public void handleMessages(JSONArray messages) throws JSONException {
        if (getActivity() == null) return;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
        for (int i = messages.length() - 1; i >= 0; i --) {
            JSONObject data = messages.getJSONObject(i);
            Message message = new Message();
            message.time = data.getString("human_date");
            message.text = data.has("text") ? data.getString("text") : "";
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
                    cv.put("talk_id", talkId);
                    cv.put("contact_id", "0");
                    cv.put("avatar", message.avatar);
                    mDb.insert("contacts", null, cv);
                }
            }
            cursor = mDb.query("messages", null, "msg_id = ?", new String[]{Integer.toString(message.nid)}, null, null, null);
            if (cursor.getCount() == 0) {
                cv = new ContentValues();
                cv.put("msg_id", message.nid);
                cv.put("contact_id", contactId);
                cv.put("type", message.type);
                cv.put("date", message.time);
                cv.put("message", message.text);
                cv.put("user_id", message.userId);
                cv.put("talk", mTalk ? 1 : 0);
                cv.put("not_read", message.read ? 0 : 1);
                mDb.insert("messages", null, cv);
                if (message.nid <= mLastMsgId) continue;
                mLastMsgId = message.nid;
                mMsgAdapter.appendMessage(message);
                setDropDownSubtitle(message.text);
                if (mListener != null) mListener.onNewMessage(contactId);
                if (mPaused) {
                    notificationManager.cancel(2);
                    Intent intent = new Intent(getActivity(), DialogsActivity.class);
                    intent.setData(Uri.parse("http://spaces.ru/mail/message_list/?Contact=" + contactId));
                    PendingIntent pintent = PendingIntent.getActivity(getActivity(),
                                                                      0, intent,
                                                                      PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity());
                    String from = mAddr;
                    if (mTalk && message.user != null) from += ": " + message.user;
                    builder.setContentIntent(pintent)
                        .setSmallIcon(R.drawable.ic_launcher)

                        .setContentTitle(from)
                        .setAutoCancel(true)
                        .setContentText(Html.fromHtml(message.text).toString());

                    Notification notification = builder.build();

                    notificationManager.notify(2, notification);

                }
            } else {
                cursor.moveToFirst();
                int iRead = cursor.getColumnIndex("not_read");
                int read = cursor.getInt(iRead);
                if (read == 1 && message.read) {
                    mMsgAdapter.makeAsRead();
                    ContentValues cv2 = new ContentValues();
                    cv2.put("not_read", 0);
                    mDb.update("messages", cv2, "msg_id=" + message.nid, null);
                }
            }
        }
    }


    public void showFromDb() {
        Cursor contact = mDb.query("contacts", null, "contact_id = ?", new String[]{Integer.toString(contactId)}, null, null, null);
        if (contact.getCount() != 0) {
            contact.moveToFirst();
            int iName = contact.getColumnIndex("name");
            int iAvatar = contact.getColumnIndex("avatar");
            setUser(contact.getString(iName));
            setAvatar(contact.getString(iAvatar));
        }
        Cursor cursor = mDb.query("messages", null, "contact_id = ?", new String[]{Integer.toString(contactId)}, null, null, null);
        if (cursor.getCount() != 0) {
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
                    contact = mDb.query("contacts", null, "user_id = ?", new String[]{Integer.toString(message.userId)}, null, null, null);
                    if (contact.getCount() != 0) {
                        contact.moveToFirst();
                        int iName = contact.getColumnIndex("name");
                        int iAvatar = contact.getColumnIndex("avatar");
                        message.user = contact.getString(iName);
                        message.avatar = contact.getString(iAvatar);
                    }
                    if (message.nid >= mLastMsgId) mLastMsgId = message.nid;
                    mMsgAdapter.appendMessage(message);
                    setDropDownSubtitle(message.text);
                } while (cursor.moveToNext());
                mMsgAdapter.notifyDataSetChanged();

                mMsgList.setSelection(mMsgList.getCount() - 1);

            }
        }
    }

    private class TypingTask extends TimerTask {

        @Override
        public void run() {
            Activity activity = getActivity();
            if (activity == null) return;
            activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mTalk) setSubtitle(mMembers);
                        else setSubtitle("онлайн");
                    }
                });
        }
    } 

    public interface OnNewMessage {
        public void onNewMessage(int contact);
    }

    public interface OnDialogCreated {
        public void onDialogCreated(DialogFragment dialog);
    }
}
