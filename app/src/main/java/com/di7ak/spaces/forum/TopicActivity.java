package com.di7ak.spaces.forum;

import android.support.v4.view.ViewCompat;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.util.SpImageGetter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.text.Spanned;

public class TopicActivity extends BlogActivity {
    
    @Override
    protected void setupTopicData(JSONObject data) {
        try {
            //text
            if (data.has("body")) {
                Object subject = data.get("body");
                String text = new String();
                if (subject instanceof JSONObject) {
                    if (((JSONObject)subject).has("subject")) {
                        text = ((JSONObject)subject).getString("subject");
                    }
                } else text = subject.toString();
                mText.setMovementMethod(LinkMovementMethod.getInstance());
                Spanned sText = Html.fromHtml(text, new SpImageGetter(mText), null);
                mText.setText(sText);
            }
            //title
            if (data.has("subject")) {
                String title = data.getString("subject");
                if(!TextUtils.isEmpty(title)) {
                    mCollapsingToolbar.setTitle(Html.fromHtml(title));
                }
            }
            //avatar
            if (data.has("avatar")) {
                JSONObject avatar = data.getJSONObject("avatar");
                mAvatar.setupData(avatar);
            }
            //author
            if(data.has("user")) {
                JSONObject user = data.getJSONObject("user");
                if(user.has("siteLink")) {
                    JSONObject siteLink = user.getJSONObject("siteLink");
                    if(siteLink.has("user_name")) {
                        String name = siteLink.getString("user_name");
                        mAuthor.setText(name);
                    }
                }
            }
            //voting
            if(data.has("actionBar")) {
                JSONObject actionBar = data.getJSONObject("actionBar");
                setupActionBar(actionBar);
            }
            //header attachments
            if (data.has("mainAttachWidgets")) {
                JSONObject mainAttachWidgets = data.getJSONObject("mainAttachWidgets");
                if (mainAttachWidgets.has("pictureWidgets")) {
                    JSONArray pictureWidgets = mainAttachWidgets.getJSONArray("pictureWidgets");
                    mPictureAttachments.setupData(pictureWidgets);
                }
                if (mainAttachWidgets.has("attachWidgets")) {
                    JSONArray pictureWidgets = mainAttachWidgets.getJSONArray("attachWidgets");
                    mPictureAttachments.setupData(pictureWidgets);
                }
            }
            //footer attachments
            if (data.has("attachWidgets")) {
                JSONObject mainAttachWidgets = data.getJSONObject("attachWidgets");
                if (mainAttachWidgets.has("attachWidgets")) {
                    JSONArray attachWidgets = mainAttachWidgets.getJSONArray("attachWidgets");
                    mFileAttachments.setupData(attachWidgets);
                }
                if (mainAttachWidgets.has("musicInlineWidget")) {
                    JSONArray attachWidgets = mainAttachWidgets.getJSONArray("musicInlineWidget");
                    mAudioAttachments.setupData(attachWidgets);
                }
            }
            //date
            if(data.has("date")) {
                String date = data.getString("date");
                if(date.startsWith("в ")) date = "Сегодня " + date;
                mDate.setIcon(R.drawable.ic_access_time_black_18dp);
                mDate.setText(date.toUpperCase());
            }
        } catch (JSONException e) {

        }
    }
}
