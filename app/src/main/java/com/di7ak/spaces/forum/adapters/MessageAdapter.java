package com.di7ak.spaces.forum.adapters;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.models.Message;
import com.di7ak.spaces.forum.util.SpImageGetter;
import com.di7ak.spaces.forum.widget.AvatarView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.di7ak.spaces.forum.util.Time;

public class MessageAdapter extends BaseAdapter {
    private List<Message> mMessages;
    private Context mContext;

    public MessageAdapter(Context context) {
        super();
        mContext = context;
        mMessages = new ArrayList<Message>();
    }

    public void appendMessage(Message message) {
        mMessages.add(message);
    }

    public void removeMessage(int index) {
        synchronized(mMessages) {
            mMessages.remove(index);
        }
    }

    public void removeMessage(Message message) {
        synchronized(mMessages) {
            mMessages.remove(message);
        }
    }

    public void makeAsRead() {
        for (Message message : mMessages) {
            message.read = true;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMessages.size();
    }

    @Override
    public Object getItem(int i) {
        if(i >= mMessages.size()) i = mMessages.size() -1;
        return mMessages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View v, ViewGroup parent) {
        Message message = (Message) getItem(i);
        if (message.type == Message.TYPE_MY) {
            v = LayoutInflater.from(mContext).inflate(R.layout.my_message_item, parent, false);
        } else if (message.type == Message.TYPE_RECEIVED) {
            v = LayoutInflater.from(mContext).inflate(R.layout.message_item, parent, false);
        } else if (message.type == Message.TYPE_SYSTEM) {
            v = LayoutInflater.from(mContext).inflate(R.layout.sys_message_item, parent, false);
        }

        TextView textV = (TextView) v.findViewById(R.id.text);
        TextView dateV = (TextView) v.findViewById(R.id.date);

        textV.setMovementMethod(LinkMovementMethod.getInstance());
        Spanned sText = Html.fromHtml(message.text, new SpImageGetter(textV), null);
        textV.setText(sText);

        String sDate = Time.toString(message.time);
        
        if (message.type != Message.TYPE_SYSTEM) {
            AvatarView avatarV = (AvatarView) v.findViewById(R.id.avatar);
            avatarV.setUrl(message.avatar);
            if(message.talk) {
                dateV.setText(message.user + ", " + sDate);
            } else dateV.setText(sDate);
        } else {
            dateV.setText(sDate);
        }
        if (!message.read) {
            v.setBackgroundColor(0x44888888);
        }
        return v;
    }
}
