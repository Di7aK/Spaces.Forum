package com.di7ak.spaces.forum;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import com.di7ak.spaces.forum.api.Request;
import com.di7ak.spaces.forum.api.RequestListener;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.util.DBHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationService extends Service implements NotificationManager.OnNewNotification, 
        RequestListener {
    public static boolean running = false;
    private static long lastChecking;
    DBHelper mDBHelper;
    SQLiteDatabase mDb;

    @Override 
    public IBinder onBind(Intent intent) { 
        return null; 
    } 

    String mailUser;
    @Override
    public boolean onNewNotification(JSONObject message) {
        if(System.currentTimeMillis() - lastChecking > 60 * 60 * 1000) {
            lastChecking = System.currentTimeMillis();
            Update.check(this);
        }
        try {

            if (message.has("text")) {
                JSONObject text = message.getJSONObject("text");
                if (text.has("act")) {
                    int act = text.getInt("act");
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);   
                    if (act == 21) {

                        int count = text.getInt("cnt");
                        if (text.has("type")) {
                            int type = text.getInt("type");
                            if (type == 3) {
                                
                            }
                            if (type == 1) {
                                notificationManager.cancel(1);
                                if (count > 0) {
                                    Intent intent = new Intent(this, JournalActivity.class);
                                    PendingIntent pintent = PendingIntent.getActivity(this,
                                                                                      0, intent,
                                                                                      PendingIntent.FLAG_UPDATE_CURRENT);
                                    showNotification(1, "Форум", "Журнал: " + count, pintent);
                                }
                            }
                        }
                        
                    } else if (act == 1) {
                        JSONObject contact = text.getJSONObject("data").getJSONObject("contact");
                        if(contact.has("user")) mailUser = contact.getString("user");
                        else {
                            android.util.Log.d("lol", text.toString());
                            int contactId = contact.getInt("nid");
                            Cursor c = mDb.query("contacts", null, "contact_id = ?", new String[]{Integer.toString(contactId)}, null, null, null);
                            if (c.getCount() != 0) {
                                c.moveToFirst();
                                int iName = c.getColumnIndex("name");
                                //int iAvatar = contact.getColumnIndex("avatar");
                                mailUser = c.getString(iName);
                                //contact.getString(iAvatar);
                            }
                        }
                        int nid = contact.getInt("nid");
                        
                        notificationManager.cancel(2);
                        Intent intent = new Intent(this, DialogActivity.class);
                        intent.setData(Uri.parse("http://spaces.ru/mail/message_list/?Contact=" + nid));
                                    PendingIntent pintent = PendingIntent.getActivity(this,
                                                                                      0, intent,
                                                                                      PendingIntent.FLAG_CANCEL_CURRENT);
                                    showNotification(2, "Почта", "Новое сообщение" + (mailUser == null ? "" : " от: " + mailUser), pintent);
                    }
                }
            }
        } catch (JSONException e) {
            android.util.Log.d("lol", e.getMessage() + " " + message.toString());
        }
        return false;
    }

    public void showNotification(int id, String title, String text, PendingIntent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(intent)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setAutoCancel(true)
            .setContentText(text);

        Notification notification = builder.build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);        
        notificationManager.notify(id, notification);
    }

    @Override 
    public void onCreate() {
        mDBHelper = new DBHelper(this);
        mDb = mDBHelper.getWritableDatabase();
    } 

    @Override 
    public void onStart(Intent intent, int startid) {
        Application.getNotificationManager().addListener(this);
            lastChecking = System.currentTimeMillis();
            checkNotifications();
            Update.check(this);
    } 
    
    private void checkNotifications() {
        String url = "http://spaces.ru/events/?sid=" + Application.getSessionId();
            
            new Request(Uri.parse(url))
                    .disableXProxy()
                    .executeWithListener(this);
    }
    
    @Override
    public void onSuccess(JSONObject json) {
        try {
            if(json.has("topCounters")) {
                JSONObject counters = json.getJSONObject("topCounters");
                if(counters.has("mail_new")) {
                    int count = counters.getInt("mail_new");
                    if (count > 0) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://spaces.ru/mail/"));
                        PendingIntent pintent = PendingIntent.getActivity(this,
                                                                          0, intent,
                                                                          PendingIntent.FLAG_CANCEL_CURRENT);
                        showNotification(2, "Почта", "Непрочитанные сообщения: " + count, pintent);
                    }
                }
                if(counters.has("journal")) {
                    int count = counters.getInt("journal");
                    if (count > 0) {
                        Intent intent = new Intent(this, JournalActivity.class);
                        PendingIntent pintent = PendingIntent.getActivity(this,
                                                                          0, intent,
                                                                          PendingIntent.FLAG_UPDATE_CURRENT);
                        showNotification(1, "Форум", "Журнал: " + count, pintent);
                    }
                }
            }
        } catch(JSONException e) {
            
        }
    }

    @Override
    public void onError(SpacesException e) {
        checkNotifications();
    }

    @Override 
    public void onDestroy() { 
        running = false;
    } 
}
