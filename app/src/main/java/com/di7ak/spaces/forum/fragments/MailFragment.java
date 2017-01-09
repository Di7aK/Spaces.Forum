package com.di7ak.spaces.forum.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.di7ak.spaces.forum.Application;
import com.di7ak.spaces.forum.DialogsActivity;
import com.di7ak.spaces.forum.MessageService;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.Request;
import com.di7ak.spaces.forum.api.RequestListener;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.models.Message;
import com.di7ak.spaces.forum.util.SpImageGetter;
import com.di7ak.spaces.forum.util.Time;
import com.di7ak.spaces.forum.widget.AvatarView;
import com.di7ak.spaces.forum.widget.PaginationView;
import com.di7ak.spaces.forum.widget.ProgressBar;
import com.rey.material.widget.Button;
import com.rey.material.widget.ProgressView;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MailFragment extends Fragment implements 
RequestListener, View.OnClickListener,
NestedScrollView.OnScrollChangeListener,
MessageService.MessageListener {
    private Uri mData;
    private ProgressBar mBar;
    private LinearLayout mList;
    private CardView mCard;
    private NestedScrollView mScroll;
    private List<Integer> mShowing;
    private PaginationView mPagination;
    private RelativeLayout mLoadNextIndicator;
    private ProgressView mProgressNext;
    private Button mBtnLoadNext;
    public boolean paused;

    public void setData(Uri uri) {
        mData = uri;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mBar = new ProgressBar(activity);
        mBar.showProgress("Загрузка данных");
        new Request(mData).executeWithListener(this);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Application.getMessageService(getActivity()).addListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parrent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.comm_fragment, parrent, false);
        mList = (LinearLayout) v.findViewById(R.id.comm_list);
        mCard = (CardView) v.findViewById(R.id.card_view);
        mScroll = (NestedScrollView) v.findViewById(R.id.scroll_view);
        mShowing = new ArrayList<Integer>();
        mCard.setVisibility(View.GONE);
        mPagination = new PaginationView(getActivity());
        mScroll.setOnScrollChangeListener(this);

        mLoadNextIndicator = (RelativeLayout) inflater.inflate(R.layout.pagination_load, mList, false);
        mLoadNextIndicator.setBackgroundColor(0xffffffff);
        mProgressNext = (ProgressView) mLoadNextIndicator.findViewById(R.id.pagination_load_indicator);
        mBtnLoadNext = (Button) mLoadNextIndicator.findViewById(R.id.btn_more);
        mBtnLoadNext.setOnClickListener(this);
        return v;
    }

    @Override
    public void onSuccess(JSONObject json) {
        mBar.hide();
        removeLoadNextIndicator();
        try {
            if (json.has("pagination") && json.get("pagination") instanceof JSONObject) {
                JSONObject pagination = json.getJSONObject("pagination");
                mPagination.setupData(pagination);
            }
            if (json.has("form")) {
                JSONArray contacts = json.getJSONObject("form").getJSONArray("contacts");
                showContacts(contacts);
            }
            if (mPagination.hasNextPage()) addLoadNextIndicator();
        } catch (JSONException e) {}
    }

    @Override
    public void onError(SpacesException e) {
        mBar.showError(e.getMessage(), "Повторить", this);
    }

    private void showContacts(JSONArray contacts) {
        try {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            for (int i = 0; i < contacts.length(); i ++) {
                JSONObject contact = contacts.getJSONObject(i);
                int id = contact.getInt("nid");
                if (mShowing.indexOf(id) != -1) continue;
                View v = inflater.inflate(R.layout.mail_item, mList, false);
                v.setId(id);
                //name
                if (contact.has("text_addr")) {
                    String name = contact.getString("text_addr");
                    TextView nameView = (TextView) v.findViewById(R.id.name);
                    nameView.setText(name);
                }
                //last message
                if (contact.has("last_message_widget")) {
                    JSONObject last = contact.getJSONObject("last_message_widget");
                    if (last.has("text")) {
                        String text = last.getString("text");
                        TextView description = (TextView) v.findViewById(R.id.description);
                        description.setText(Html.fromHtml(text, new SpImageGetter(description), null));
                        if (last.has("not_read")) description.setBackgroundColor(0x44888888);
                    }
                    if (last.has("date")) {
                        String text = Time.toString(last.getLong("date") * 1000);
                        TextView date = (TextView) v.findViewById(R.id.date);
                        date.setText(text);
                    }
                }
                //avatar
                if (contact.has("avatar")) {
                    JSONObject logo = contact.getJSONObject("avatar");
                    AvatarView into = (AvatarView) v.findViewById(R.id.avatar);
                    into.setupData(logo);
                }
                //link
                if (contact.has("msg_list_link")) {
                    String link = contact.getString("msg_list_link");
                    if (contact.has("last_message_id")) {
                        int lastMsgId = contact.getInt("last_message_id");
                        link += "&last_msg_id=" + lastMsgId;
                    }
                    v.setTag(link);
                    v.setOnClickListener(this);
                }
                //new count
                if (contact.has("new_cnt")) {
                    String cnt = contact.getString("new_cnt");
                    TextView newCnt = (TextView) v.findViewById(R.id.new_cnt);
                    newCnt.setText(cnt);
                    if (cnt.equals("0")) newCnt.setVisibility(View.GONE);
                }
                mShowing.add(id);

                mList.addView(v);
            }
        } catch (JSONException e) {

        }
        if (mShowing.size() > 0 && mCard.getVisibility() == View.GONE) {
            mCard.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onScrollChange(NestedScrollView v, int x, int y, int p4, int p5) {
        if (y + 50 > (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
            if (!mBar.isShown() && mPagination.hasNextPage()) {
                mData = Uri.parse(mPagination.getNextPageUrl());
                mBtnLoadNext.setVisibility(View.INVISIBLE);
                mProgressNext.start();
                new Request(mData).executeWithListener(this);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mBtnLoadNext)) {
            mData = Uri.parse(mPagination.getNextPageUrl());
            mBtnLoadNext.setVisibility(View.INVISIBLE);
            mProgressNext.start();
            new Request(mData).executeWithListener(this);
        } else if (v.getTag() != null) {
            Intent intent = new Intent();
            intent.setData(Uri.parse((String)v.getTag()));
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClass(getContext(), DialogsActivity.class);
            //intent.setFlags(Intent.FLAG_FROM_BACKGROUND);
            getActivity().startActivity(intent);
            
        } else new Request(mData).executeWithListener(this);
    }

    private void addLoadNextIndicator() {
        mList.addView(mLoadNextIndicator);
        mBtnLoadNext.setVisibility(View.VISIBLE);
    }

    private void removeLoadNextIndicator() {
        if (mList.indexOfChild(mLoadNextIndicator) != -1) mList.removeView(mLoadNextIndicator);
        mProgressNext.stop();
    }

    @Override
    public boolean onNewMessage(Message message) {
        View v = mList.findViewById(message.contact);
        if (v != null) {
            //text
            TextView description = (TextView) v.findViewById(R.id.description);
            description.setText(Html.fromHtml(message.text, new SpImageGetter(description), null)); 
            //time
            TextView date = (TextView) v.findViewById(R.id.date);
            date.setText(Time.toString(message.time));
            //count
            TextView newCnt = (TextView) v.findViewById(R.id.new_cnt);
            int cnt = Integer.valueOf(newCnt.getText().toString());
            cnt ++;
            newCnt.setText(Integer.toString(cnt));
            newCnt.setVisibility(View.VISIBLE);
            //to top
            mList.removeView(v);
            mList.addView(v, 0);
            return !paused;
        }
        return false;
    }

    @Override
    public void onSuccess(Message message) {
        View v = mList.findViewById(message.contact);
        if (v != null) {
            //text
            TextView description = (TextView) v.findViewById(R.id.description);
            description.setText(Html.fromHtml(message.text, new SpImageGetter(description), null)); 
            description.setBackgroundColor(0x44888888);
            //time
            TextView date = (TextView) v.findViewById(R.id.date);
            date.setText(Time.toString(message.time));
            //to top
            mList.removeView(v);
            mList.addView(v, 0);
        }
    }

    @Override
    public void onTyping(int contact, String user) {
        
    }

    @Override
    public void onRead(final int contact) {
        Activity activity = getActivity();
        if(activity == null) return;
        activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    View v = mList.findViewById(contact);
                    if (v != null) {
                        //count
                        TextView newCnt = (TextView) v.findViewById(R.id.new_cnt);
                        newCnt.setText(Integer.toString(0));
                        newCnt.setVisibility(View.GONE);
                        //background
                        TextView description = (TextView) v.findViewById(R.id.description);
                        description.setBackgroundColor(0x00ffffff);
                    }
                }
            });
    }

    @Override
    public void onError(Message message, SpacesException exception) {

    }
}
