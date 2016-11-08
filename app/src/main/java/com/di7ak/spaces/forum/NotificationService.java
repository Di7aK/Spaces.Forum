package com.di7ak.spaces.forum;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import com.di7ak.spaces.forum.api.Request;
import org.json.JSONException;
import org.json.JSONObject;
import com.di7ak.spaces.forum.api.RequestListener;
import com.di7ak.spaces.forum.api.SpacesException;

public class NotificationService extends Service implements NotificationManager.OnNewNotification, 
        RequestListener {
    public static boolean running = false;
    private static String channel;
    private static long lastChecking;
    private static String mSid;

    @Override 
    public IBinder onBind(Intent intent) { 
        return null; 
    } 

    String mailUser;
    @Override
    public void onNewNotification(JSONObject message) {
        android.util.Log.d("lol", message.toString());
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
                                notificationManager.cancel(2);
                                if (count > 0) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://spaces.ru/mail/"));
                                    PendingIntent pintent = PendingIntent.getActivity(this,
                                                                                      0, intent,
                                                                                      PendingIntent.FLAG_CANCEL_CURRENT);
                                    showNotification(2, "Почта (" + count + ")", "Новое сообщение от: " + mailUser, pintent);
                                }
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
                        mailUser = contact.getString("user");
                        
                    }
                }
            }
        } catch (JSONException e) {

        }
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
    } 

    @Override 
    public void onStart(Intent intent, int startid) {
        AccountManager am = AccountManager.get(this);
        Account[] accounts = am.getAccountsByType(Authenticator.ACCOUNT_TYPE);
        if (accounts.length != 0) {
            channel = am.getUserData(accounts[0], "channel");
            running = true;
            try {
                Application.notificationManager = new NotificationManager(channel);
                Application.notificationManager.addListener(this);
            } catch (Exception e) {
                running = false;
            }
            lastChecking = System.currentTimeMillis();
            
            mSid = am.peekAuthToken(accounts[0], Authenticator.TOKEN_FULL_ACCESS);
            
            checkNotifications();
            
            Update.check(this);
        }
    } 
    
    private void checkNotifications() {
        String url = "http://spaces.ru/ajax/events/?sid=" + mSid;
            
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
