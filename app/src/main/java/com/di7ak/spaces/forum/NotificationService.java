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
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationService extends Service implements NotificationManager.OnNewNotification { 
    public static boolean running = false;
    private static String channel;

    @Override 
    public IBinder onBind(Intent intent) { 
        return null; 
    } 

    @Override
    public void onNewNotification(JSONObject message) {
        android.util.Log.d("lol", message.toString());
        try {

            if (message.has("text")) {
                JSONObject text = message.getJSONObject("text");
                if (text.has("act")) {
                    int act = text.getInt("act");
                    if (act == 21) {
                        if (text.has("Ot") && text.getInt("Ot") == 8) {
                            int count = text.getInt("cnt");
                            Intent intent = new Intent(this, JournalActivity.class);
                            PendingIntent pintent = PendingIntent.getActivity(this,
                                                                              0, intent,
                                                                              PendingIntent.FLAG_CANCEL_CURRENT);
                            showNotification(1, "Форум", "Журнал: " + count, pintent);
                        }
                    } else if(act == 24) {
                        if(text.has("user")) {
                            String from = text.getString("user");
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://spaces.ru/mail/"));
                            PendingIntent pintent = PendingIntent.getActivity(this,
                                                                              0, intent,
                                                                              PendingIntent.FLAG_CANCEL_CURRENT);
                            showNotification(2, "Почта", "Новое сообщение от: " + from, pintent);
                        }
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
        }
    } 

    @Override 
    public void onDestroy() { 
        android.util.Log.d("lol", "destroy");
        running = false;
    } 
}
