package com.di7ak.spaces.forum;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import com.di7ak.spaces.forum.api.Api;
import com.di7ak.spaces.forum.api.Request;
import com.di7ak.spaces.forum.api.RequestListener;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.models.Contact;
import com.di7ak.spaces.forum.models.Message;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageService implements NotificationManager.OnNewNotification {
    private Session mSession;
    private Context mContext;
    private List<MessageListener> mListeners;
    private List<Received> mReceived;
    private List<Message> mSending;
    private SQLiteDatabase mDb;
    private boolean mReceivedProcess;
    private boolean mSendingProcess;
    private NotificationManagerCompat mNotificationManager;

    public MessageService(Context context) {
        mContext = context;
        mSession = Application.getSession();
        mDb = Application.getDatabase(context);
        mListeners = new ArrayList<MessageListener>();
        mReceived = new ArrayList<Received>();
        mSending = new ArrayList<Message>();
        mReceivedProcess = false;
        mNotificationManager = NotificationManagerCompat.from(context);
        Application.getNotificationManager().addListener(this);
    }

    public void sendMessage(Message message) {
        mSending.add(message);
        if (!mSendingProcess) sendMessages();
    }

    private void sendMessages() {
        mSendingProcess = true;
        final Message message = mSending.get(0);
        Request request = Api.sendMessage(message);
        request.executeWithListener(new RequestListener() {

                @Override
                public void onSuccess(JSONObject json) {
                    try {
                        JSONObject data = json.getJSONObject("message");
                        message.from(data);
                    } catch (JSONException e) {
                         for (MessageListener listener : mListeners) {
                            listener.onError(message, new SpacesException(-2));
                        }
                        return;
                    }
                    message.type = Message.TYPE_MY;
                    message.put(mDb);
                    for (MessageListener listener : mListeners) {
                        listener.onSuccess(message);
                    }
                    mSending.remove(message);
                    mSendingProcess = mSending.size() > 0;
                    if (mSendingProcess) sendMessages();
                }

                @Override
                public void onError(SpacesException e) {
                    sendMessages();
                }
            });
    }

    public void addListener(MessageListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(MessageListener listener) {
        mListeners.remove(listener);
    }

    public Contact getContact(int id) {
        Contact contact = new Contact();
        contact.id = id;
        Cursor cursor = mDb.query("contacts", null, "contact_id = ?", new String[]{Integer.toString(id)}, null, null, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            contact.from(cursor);
        }
        return contact;
    }

    public List<Message> getHistory(Contact contact) {
        List<Message> messages = new ArrayList<Message>();
        Cursor cursor = mDb.query("messages", null, "contact_id = ?", new String[]{Integer.toString(contact.id)}, null, null, "date", "30");
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            do {
                Message message = new Message();
                message.from(cursor, mDb);
                messages.add(message);
            } while (cursor.moveToNext());
        }
        return messages;
    }

    public void markAsRead(int contact) {
        ContentValues cv = new ContentValues();
        cv.put("not_read", 0);
        mDb.update("messages", cv, "contact_id=" + contact, null);
    }

    public void notifyMessage(Message message) {
        mNotificationManager.cancel(2);
        Intent intent = new Intent(mContext, DialogsActivity.class);
        intent.setData(Uri.parse("http://spaces.ru/mail/message_list/?Contact=" + message.contact));
        PendingIntent pintent = PendingIntent.getActivity(mContext,
                                                          0, intent,
                                                          PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        String from = message.user.name;
        if (message.talk && message.user != null) from += ": " + message.user.name;
        builder.setContentIntent(pintent)
            .setSmallIcon(R.drawable.ic_launcher)

            .setContentTitle(from)
            .setAutoCancel(true)
            .setContentText(Html.fromHtml(message.text).toString());

        Notification notification = builder.build();

        mNotificationManager.notify(2, notification);

    }

    private void getReceived() {
        mReceivedProcess = true;
        Received received = mReceived.get(0);
        Request request = Api.getMessageById(received.contact, received.id);
        request.executeWithListener(new RequestListener() {

                @Override
                public void onSuccess(JSONObject json) {
                    if (json.has("messages")) {
                        try {
                            JSONObject messages = json.getJSONObject("messages");
                            Received received = mReceived.get(0);
                            if (messages.has(Integer.toString(received.id))) {
                                Message message = new Message();
                                message.from(messages.getJSONObject(Integer.toString(received.id)));
                                message.put(mDb);
                                boolean noNotify = false;
                                for (MessageListener listener : mListeners) {
                                    if (listener.onNewMessage(message)) noNotify = true;
                                }
                                if (!noNotify) notifyMessage(message);
                            }
                            mReceived.remove(0);
                            mReceivedProcess = mReceived.size() > 0;
                            if (mReceivedProcess) getReceived();
                        } catch (JSONException e) {
                            android.util.Log.e("lol", "", e);
                        }
                    }
                }

                @Override
                public void onError(SpacesException e) {
                    getReceived();
                }
            });
    }

    @Override
    public boolean onNewNotification(JSONObject message) {
        try {
            if (message.has("text")) {
                JSONObject text = message.getJSONObject("text");
                if (text.has("act")) {
                    int act = text.getInt("act");
                    if (act == 24) {//typing
                        int id = text.has("talk_id") ? text.getInt("talk_id") : text.getInt("contact_id");
                        String user = text.has("user") ? text.getString("user") : "";
                        for (MessageListener listener : mListeners) {
                            listener.onTyping(id, user);
                        }
                        return true;
                    }
                    if (act == 1) {//new message
                        int contact = text.getJSONObject("data").getJSONObject("contact").getInt("nid");
                        int messageId = text.getJSONObject("data").getInt("nid");
                        mReceived.add(new Received(contact, messageId));
                        if (!mReceivedProcess) getReceived();
                        return true;
                    }
                    if (act == 2) {//read
                        int contact = text.getJSONObject("data").getInt("nid");
                        markAsRead(contact);
                        for (MessageListener listener : mListeners) {
                            listener.onRead(contact);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            android.util.Log.e("lol", "", e);
        }
        return false;
    }

    public interface MessageListener {

        public boolean onNewMessage(Message message);

        public void onSuccess(Message message);

        public void onTyping(int contact, String user);

        public void onRead(int contact);

        public void onError(Message message, SpacesException exception);
    }

    private class Received {
        public int contact;
        public int id;

        public Received(int contact, int id) {
            this.contact = contact;
            this.id = id;
        }
    }
}
