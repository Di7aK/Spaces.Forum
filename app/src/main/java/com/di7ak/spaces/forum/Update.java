package com.di7ak.spaces.forum;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import com.di7ak.spaces.forum.api.Forum;
import com.di7ak.spaces.forum.api.Request;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.api.RequestListener;
import org.json.JSONObject;
import org.json.JSONException;
import android.content.pm.PackageManager.NameNotFoundException;


public class Update {

    public static void check(final Context context) {


        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            final int versionCode = pInfo.versionCode;
            Request request = new Request(Uri.parse("http://spaces.ru/forums/?id=19476328"));
            request.executeWithListener(new RequestListener() {

                    @Override
                    public void onSuccess(JSONObject json) {
                        if (json.has("topicWidget")) {
                            try {
                                JSONObject topic = json.getJSONObject("topicWidget");
                                if (topic.has("body")) {
                                    Object subject = topic.get("body");
                                    String text = new String();
                                    if (subject instanceof JSONObject) {
                                        if (((JSONObject)subject).has("subject")) {
                                            text = ((JSONObject)subject).getString("subject");
                                        }
                                    } else text = subject.toString();


                                    String[] versions = Html.fromHtml(text).toString().split(";");
                                    int latestVersion = 0;
                                    String url = "";
                                    for (String version : versions) {
                                        try {
                                            String[] data = version.trim().split(" ");
                                            int code = Integer.valueOf(data[0]);
                                            String id = data[1];
                                            if (latestVersion < code) {
                                                latestVersion = code;
                                                url = "http://spaces.ru/forums/?id=" + id;
                                            }
                                        } catch (Exception e) {}
                                    }
                                    if (latestVersion > versionCode) {
                                        Intent intent = new Intent(context, TopicActivity.class);
                                        intent.setAction(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse(url));
                                        PendingIntent pintent = PendingIntent.getActivity(context,
                                                                                          0, intent,
                                                                                          PendingIntent.FLAG_CANCEL_CURRENT);
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                                        builder.setContentIntent(pintent)
                                            .setSmallIcon(R.drawable.ic_launcher)
                                            .setContentTitle("Доступно обновление")
                                            .setAutoCancel(true)
                                            .setContentText("Текущая версия: " + versionCode + ", последняя версия: " + latestVersion);

                                        Notification notification = builder.build();

                                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);        
                                        notificationManager.notify(9, notification);
                                    }
                                } 
                            } catch (JSONException e) {}

                        }
                    }

                    @Override
                    public void onError(SpacesException e) {

                    }
                });
        } catch (PackageManager.NameNotFoundException e) {}

    }
}
