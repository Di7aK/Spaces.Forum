package com.di7ak.spaces.forum.fragments;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.di7ak.spaces.forum.Application;
import com.di7ak.spaces.forum.MessageService;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.adapters.MessageAdapter;
import com.di7ak.spaces.forum.api.Request;
import com.di7ak.spaces.forum.api.RequestListener;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.models.Contact;
import com.di7ak.spaces.forum.models.Message;
import com.di7ak.spaces.forum.models.User;
import com.di7ak.spaces.forum.util.SpImageGetter;
import com.di7ak.spaces.forum.widget.AvatarView;
import com.rey.material.widget.FloatingActionButton;
import com.rey.material.widget.ProgressView;
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
    private SQLiteDatabase mDb;
    private boolean mRefreshing;
    private OnDialogCreated mOnDialogCreated;
    private MessageService mMessageService;
    private boolean mPaused = true;
    private boolean mUpdating;

    public Contact contact;
    
    public DialogFragment() {
        super();
    }

    public DialogFragment(int id, int lastMsgId, OnDialogCreated onCreated) {
        super();

        mMessageService = Application.getMessageService(getActivity());
        contact = mMessageService.getContact(id);
        contact.lastMsgId = lastMsgId;
        mOnDialogCreated = onCreated;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        mSession = Application.getSession();
        mDb = Application.getDatabase(getActivity());
        if(mMessageService == null) mMessageService = Application.getMessageService(getActivity());
        if(bundle != null && bundle.containsKey("contact")) {
            int id = bundle.getInt("contact");
            contact = mMessageService.getContact(id);
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("contact", contact.id);
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

        setContact(contact);
        showFromHistory();

        int lastMsgId = 0;
        if(mMsgAdapter.getCount() > 0) {
            Message last = (Message)mMsgAdapter.getItem(mMsgAdapter.getCount() - 1);
            setDropDownSubtitle(last.text);
            lastMsgId = last.nid;
        }
        if(contact.lastMsgId == 0 || lastMsgId == 0 || lastMsgId < contact.lastMsgId) {
            startUpdating();
            getMessages(1);
        }
        
        if (mOnDialogCreated != null) mOnDialogCreated.onDialogCreated(this);

        return view;
    }
    
    public void showFromHistory() {
        List<Message> messages = mMessageService.getHistory(contact);
        for(Message message : messages) {
            mMsgAdapter.appendMessage(message);
        }
        mMsgAdapter.notifyDataSetChanged();
        mMsgList.setSelection(mMsgList.getCount() - 1);
    }

    boolean mLoaded;
    MenuItem mUpdateItem;
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_dialog, menu);
        mUpdateItem = menu.getItem(0);
        if (mUpdating) {
            startUpdating();
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update:
                mUpdateItem = item;
                startUpdating();
                getMessages(1);
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

    public void startUpdating() {
        if (mUpdateItem == null) return;
        View snackView = getActivity().getLayoutInflater().inflate(R.layout.progress_snackbar, null);
        ProgressView pv = (ProgressView)snackView.findViewById(R.id.progress_pv_circular_determinate);
        pv.start();
        mUpdateItem.setActionView(pv);
    }

    public void stopUpdating() {
        if (mUpdateItem != null) mUpdateItem.setActionView(null);
        mRefreshing = false;
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
    }
    
    public void onNewMessage(Message message, int cnt) {
        setDropDownSubtitle(message.text);
        updateCnt(cnt);
    }
    
    public void updateCnt(int cnt) {
        TextView newCnt = (TextView) mDropDownTitleView.findViewById(R.id.new_cnt);
        newCnt.setText(Integer.toString(cnt));
        newCnt.setVisibility(cnt > 0 ? View.VISIBLE : View.GONE);
    }

    public void onTyping(String user) {
        if (contact.talkId != 0) setSubtitle(user + " печатает", 6000);
        else setSubtitle("печатает", 6000);
    }

    private long mLastTyping;
    public void typing() {
        long time = System.currentTimeMillis();
        if (time - mLastTyping < 4000) return;
        mLastTyping = time;
        StringBuilder args = new StringBuilder();
        args.append("method=").append("typing")
            .append("&Contact=").append(Integer.toString(contact.id))
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
        ((TextView)mTitleView.findViewById(R.id.name)).setText(text);
        ((TextView)mDropDownTitleView.findViewById(R.id.name)).setText(text);
    }

    String staticTitle;
    public void setSubtitle(String text, long time) {
        final TextView title = (TextView)mTitleView.findViewById(R.id.description);
        title.setText(text);
        if(time > 0) {
            title.postDelayed(new Runnable() {
                @Override
                public void run() {
                    title.setText(staticTitle);
                }
            }, time);
        } else staticTitle = text;
    }

    public void setDropDownSubtitle(String text) {
        TextView textV = (TextView)mDropDownTitleView.findViewById(R.id.description);
        Spanned sText = Html.fromHtml(text, new SpImageGetter(textV), null);
        textV.setText(sText);
    }

    public void setAvatar(String url) {
        ((AvatarView)mTitleView.findViewById(R.id.avatar)).setUrl(url);
        ((AvatarView)mDropDownTitleView.findViewById(R.id.avatar)).setUrl(url);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab_send) {
            sendMessage();
        }
    }

    public void sendMessage() {
        Message message = new Message();
            message.text = mMsgBox.getText().toString();
            message.read = false;
            message.talk = contact.talkId != 0;
            message.contact = contact.id;
            message.time = System.currentTimeMillis();
            message.user = new User();
            message.user.avatar = mSession.avatar;
            message.user.name = mSession.login;
            message.user.id = mSession.nid;
            message.type = Message.TYPE_SENDING;
            
            boolean bottom = mMsgList.getLastVisiblePosition() == mMsgList.getAdapter().getCount() - 1;

            mMsgAdapter.appendMessage(message);
            mMsgAdapter.notifyDataSetChanged();
            if (bottom) mMsgList.setSelection(mMsgList.getCount() - 1);

            mMsgBox.setText("");
            
            mMessageService.sendMessage(message);
    }
    
    public void getMessages(int page) {
        mUpdating = true;
        Uri uri = Uri.parse("http://spaces.ru/mail/message_list/?Contact=" + contact.id + "&sid=" + mSession.sid);
        Request request = new Request(uri);
        request.executeWithListener(this);
    }
    
    public void onSuccess(Message message) {
        setDropDownSubtitle(message.text);
    }
    
    public void onError(Message message) {
        
    }

    @Override
    public void onSuccess(JSONObject json) {
        stopUpdating();
        mUpdating = false;
        mDb.delete("messages", "contact_id=" + contact.id, null);
        try {
            JSONObject contactInfo = json.getJSONObject("contact_info");
            contact.from(contactInfo);
            contact.put(mDb);
            
            setContact(contact);

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
            
            boolean bottom = mMsgList.getLastVisiblePosition() == mMsgList.getAdapter().getCount() - 1;
            JSONArray messages = json.getJSONArray("msg_list");
            handleMessages(messages);
            mMsgAdapter.notifyDataSetChanged();
            if (bottom) mMsgList.setSelection(mMsgList.getCount() - 1);
        } catch (JSONException e) {
            mMsgBox.setText(json.toString());
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(SpacesException e) {
        if(getActivity() != null) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            getMessages(1);
        }
    }
    
    public void setContact(Contact contact) {
        setUser(contact.name);
        setAvatar(contact.avatar);
        if(contact.talkId != 0) setSubtitle(TextUtils.isEmpty(contact.members) ? "беседа" : contact.members, 0);
        else if (TextUtils.isEmpty(contact.lastVisit)) setSubtitle("контакт", 0);
        else setSubtitle("был в сети " + contact.lastVisit, 0);
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
        for (int i = messages.length() - 1; i >= 0; i --) {
            JSONObject data = messages.getJSONObject(i);
            Message message = new Message();
            message.from(data);
            message.put(mDb);
            mMsgAdapter.appendMessage(message);
            setDropDownSubtitle(message.text);
        }
    }

    public interface OnDialogCreated {
        public void onDialogCreated(DialogFragment dialog);
    }
}
