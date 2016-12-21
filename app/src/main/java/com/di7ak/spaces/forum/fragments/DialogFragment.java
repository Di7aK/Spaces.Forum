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
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private Timer mTimer;
    private String mMembers;
    private String mAddr;
    private String mAvatar;
    private int mLastMsgId;
    private int mUserId;
    private boolean mPaused = true;
    
    public int contactId;
    public int talkId;
    
    public DialogFragment(Session session, int contact, SQLiteDatabase db) {
        super();
        
        mSession = session;
        contactId = contact;
        mDb = db;
        
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
        
        /*mMsgBox.setEnabled(false);
        mMsgBox.setFocusable(false);
        mBtnSend.setEnabled(false);*/
        
        mMsgAdapter = new MessageAdapter(getActivity());
        mMsgList.setAdapter(mMsgAdapter);
        mMsgList.setDivider(null);
        
        mTitleView = getActivity().getLayoutInflater().inflate(R.layout.mail_title_item, parent, false);
        
        mDropDownTitleView = getActivity().getLayoutInflater().inflate(R.layout.mail_drop_down, parent, false);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(
            AbsListView.LayoutParams.MATCH_PARENT,
            AbsListView.LayoutParams.WRAP_CONTENT);
        mDropDownTitleView.setLayoutParams(params);
        
        showFromDb();
        
        return view;
    }
    
    public View getTitleView(View convert, ViewGroup parent) {
        return mTitleView;
    }
    
    public View getDropDownTitleView(View convert, ViewGroup parent) {
        return mDropDownTitleView;
    }
    
    public void refresh() {
        if(mRefreshing) return;
        mRefreshing = true;
    }
    
    
    
    public void onReceived(int msgId) {
        mLastReceivedMsgs.add(msgId);
        getNewMessages();
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
        mTimer.schedule(mTypingTask, 6000);
        if(mTalk) setSubtitle(user + " печатает");
        else setSubtitle("печатает");
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
        if(v.getId() == R.id.fab_send) {
            
        }
    }
    
    @Override
    public void onSuccess(JSONObject json) {
        
    }
    
    @Override
    public void onError(SpacesException e) {
        
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
    
    public void handleMessages(JSONArray messages) throws JSONException {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
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
                if (mPaused) {
                    notificationManager.cancel(2);
                    Intent intent = new Intent(getActivity(), DialogsActivity.class);
                    intent.setData(Uri.parse("http://spaces.ru/mail/message_list/?Contact=" + contactId));
                    PendingIntent pintent = PendingIntent.getActivity(getActivity(),
                                                                      0, intent,
                                                                      PendingIntent.FLAG_IMMUTABLE);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity());
                    String from = mAddr;
                    if (mTalk) from += ": " + message.user;
                    builder.setContentIntent(pintent)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(from)
                        .setAutoCancel(true)
                        .setContentText(message.text);

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
                setSubtitle("онлайн");
            }
        }
    }
    
    private class TypingTask extends TimerTask {
        
        @Override
        public void run() {
            if(mTalk) setSubtitle(mMembers);
            else setSubtitle("онлайн");
        }
    }
}
