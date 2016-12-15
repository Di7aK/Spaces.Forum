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
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.util.SpImageGetter;
import com.di7ak.spaces.forum.widget.AvatarView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageAdapter extends BaseAdapter {
    private JSONArray mMessages;
    private JSONObject mJson;
    private Context mContext;
    private Session mSession;

    public MessageAdapter(Context context, JSONObject json, Session session) {
        super();
        mContext = context;
        mJson = json;
        mSession = session;
        try {
            mMessages = json.getJSONArray("msg_list");
        } catch(JSONException e) {
            
        }
    }
    
    public void appendMessage(String text) {
        JSONObject msg = new JSONObject();
        try {
            msg.put("not_read", 1);
            msg.put("text", text);
            msg.put("human_date", "отправка");
            mMessages.put(0, msg);
            notifyDataSetChanged();
        } catch (JSONException e) {}
    }
    
    public void setData(JSONObject json) {
        mJson = json;
        try {
            mMessages = json.getJSONArray("msg_list");
        } catch(JSONException e) {
            
        }
    }

    @Override
    public int getCount() {
        return mMessages.length();
    }

    @Override
    public Object getItem(int i) {
        try {
            return mMessages.get(i);
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    final static int TYPE_SYSTEM = 0;
    final static int TYPE_MY = 1;
    final static int TYPE_RECEIVED = 2;
    final static int TYPE_SENDING = 3;
    @Override
    public View getView(int i, View v, ViewGroup parent) {
        JSONObject message = (JSONObject) getItem(getCount() - i - 1);
        try {
            int type;
            
            if (message.has("system")) type = TYPE_SYSTEM;
            else {
                if(message.has("received")) type = TYPE_RECEIVED;
                else type = TYPE_MY;
            }
            if (type == TYPE_MY) {
                v = LayoutInflater.from(mContext).inflate(R.layout.my_message_item, parent, false);
            } else if (type == TYPE_RECEIVED) {
                v = LayoutInflater.from(mContext).inflate(R.layout.message_item, parent, false);
            } else if (type == TYPE_SYSTEM) {
                v = LayoutInflater.from(mContext).inflate(R.layout.sys_message_item, parent, false);
            }
            
            TextView textV = (TextView) v.findViewById(R.id.text);
            TextView dateV = (TextView) v.findViewById(R.id.date);

            
            String text = message.getString("text");
            textV.setMovementMethod(LinkMovementMethod.getInstance());
            Spanned sText = Html.fromHtml(text, new SpImageGetter(textV), null);
            textV.setText(sText);
            String date = message.getString("human_date");

            if(type != TYPE_SYSTEM) {
                AvatarView avatarV = (AvatarView) v.findViewById(R.id.avatar);
                JSONObject avatar;
                if(message.has("contact") && !message.getJSONObject("contact").isNull("avatar")) {
                    avatar = message.getJSONObject("contact").getJSONObject("avatar");
                } else if(type == TYPE_RECEIVED) {
                    avatar = mJson.getJSONObject("contact_info").getJSONObject("widget").getJSONObject("avatar");
                } else {
                    avatar = new JSONObject();
                    avatar.put("previewURL", "" + mSession.avatar);
                }
                avatarV.setupData(avatar);
                if(message.has("contact") && mJson.getJSONObject("contact_info").has("talk")) {
                String contact = message.getJSONObject("contact").getJSONObject("widget").getJSONObject("siteLink").getString("user_name");
                dateV.setText(contact + ", " + date);
                } else dateV.setText(date);
            } else {
                dateV.setText(date);
            }
            if(type == TYPE_MY) {
                boolean notRead = message.has("not_read") && message.getInt("not_read") == 1;
                if (notRead) {
                    v.setBackgroundColor(0x44888888);
                }
            }
            
        } catch (JSONException e) {
            android.util.Log.e("lol", "", e);
        }
        return v;
    }
}
