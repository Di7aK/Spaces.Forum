package com.di7ak.spaces.forum;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.Request;
import com.di7ak.spaces.forum.api.RequestListener;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.util.SpImageGetter;
import com.di7ak.spaces.forum.widget.AddCommentView;
import com.di7ak.spaces.forum.widget.AvatarView;
import com.di7ak.spaces.forum.widget.ChannelView;
import com.di7ak.spaces.forum.widget.CommentsView;
import com.di7ak.spaces.forum.widget.FileAttachmentsView;
import com.di7ak.spaces.forum.widget.ImagedTextView;
import com.di7ak.spaces.forum.widget.PictureAttachmentsView;
import com.di7ak.spaces.forum.widget.ProgressBar;
import com.di7ak.spaces.forum.widget.VotingView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BlogActivity extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener, 
        Authenticator.OnResult,
        RequestListener {
    private static final int PERCENTAGE_TO_SHOW_IMAGE = 20;
    
    protected CollapsingToolbarLayout mCollapsingToolbar;
    private int mMaxScrollSize;
    private boolean mIsImageHidden;
    private AppBarLayout appbar;
    protected Session session;
    protected JSONObject blog;
    protected Uri uri;
    protected ProgressBar bar;
    protected TextView mAuthor;
    protected TextView mText;
    protected AvatarView mAvatar;
    protected VotingView mVoting;
    protected PictureAttachmentsView mPictureAttachments;
    protected FileAttachmentsView mFileAttachments;
    protected FileAttachmentsView mAudioAttachments;
    protected CommentsView mComments;
    protected View mHead;
    protected View mBody;
    protected AddCommentView mCommentPanel;
    protected ChannelView mChannel;
    protected ImagedTextView mDate;
    protected ImagedTextView mViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blog);

        mHead = findViewById(R.id.head);
        mHead.setVisibility(View.INVISIBLE);
        mBody = findViewById(R.id.body);
        mBody.setVisibility(View.INVISIBLE);
        mAuthor = (TextView)findViewById(R.id.user_name);
        mText = (TextView)findViewById(R.id.text);
        mAvatar = (AvatarView)findViewById(R.id.avatar);
        mVoting = (VotingView)findViewById(R.id.voting);
        mPictureAttachments = (PictureAttachmentsView)findViewById(R.id.picture_attachments);
        mFileAttachments = (FileAttachmentsView)findViewById(R.id.file_attachments);
        mAudioAttachments = (FileAttachmentsView)findViewById(R.id.audio_attachments);
        mComments = (CommentsView) findViewById(R.id.comments);
        mCommentPanel = (AddCommentView) findViewById(R.id.comment_panel);
        mChannel = (ChannelView) findViewById(R.id.channel);
        mDate = (ImagedTextView) findViewById(R.id.date);
        mViews = (ImagedTextView) findViewById(R.id.views);
        
        mCommentPanel.setVisibility(View.INVISIBLE);
        mComments.setCommentPanel(mCommentPanel);
        mCollapsingToolbar = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    onBackPressed();
                }
            });

        appbar = (AppBarLayout) findViewById(R.id.appbar);
        appbar.addOnOffsetChangedListener(this);
        
        
        
        ((NestedScrollView)findViewById(R.id.nested_scroll_view)).setOnScrollChangeListener(mComments);

        bar = new ProgressBar(this);

        Authenticator.getSession(this, this);
    }

    @Override
    public void onAuthenticatorResult(Session session) {
        if (session == null) finish();
        else {
            this.session = session;
            mCommentPanel.setSession(session);
            uri = getIntent().getData();
            if(uri == null) bar.showError("Ошибка", null, null);
            else readTopic();
        }
    }

    public void readTopic() {
        bar.showProgress("Загрузка данных");
        new Request(uri).executeWithListener(this);
    }

    @Override
    public void onSuccess(JSONObject json) {
        blog = json;
        bar.hide();
        try {
            if(json.has("views")) {
                String views = json.getString("views");
                mViews.setIcon(R.drawable.ic_remove_red_eye_black_18dp);
                mViews.setText(views);
            }
            if (json.has("topicWidget")) {
                JSONObject topicWidget = json.getJSONObject("topicWidget");
                setupTopicData(topicWidget);
            }
            if (json.has("actionBar")) {
                JSONObject actionBar = json.getJSONObject("actionBar");
                setupActionBar(actionBar);
            }
            if (json.has("commentsBlock")) {
                JSONObject commentsBlock = json.getJSONObject("commentsBlock");
                mComments.setupData(commentsBlock, session);
                mComments.setUrl(uri.toString());
            }
        } catch (JSONException e) {

        }
        mHead.setVisibility(View.VISIBLE);
        mBody.setVisibility(View.VISIBLE);
        mCommentPanel.setVisibility(View.VISIBLE);
        ViewCompat.animate(mBody).alphaBy(0).alpha(1).translationYBy(-100).translationY(0).start();
        ViewCompat.animate(mHead).alphaBy(0).alpha(1).translationYBy(100).translationY(0).start();
        ViewCompat.animate(mCommentPanel).alphaBy(0).alpha(1).start();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Application.notificationManager.removeListener(mComments);
    }

    @Override
    public void onResume() {
        super.onResume();
        Application.notificationManager.addListener(mComments);
    }

    public void onError(SpacesException e) {
        bar.showError(e.getMessage(), "ПОВТОРИТЬ", new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    readTopic();
                }
            });
    }

    protected void setupTopicData(JSONObject data) {
        try {
            //author
            if (data.has("username")) {
                String author = data.getString("username");
                mAuthor.setText(author);
            }
            //title
            if (data.has("topicModel")) {
                JSONObject topicModel = data.getJSONObject("topicModel");
                if (topicModel.has("header")) {
                    String title = topicModel.getString("header");
                    if (!TextUtils.isEmpty(title)) mCollapsingToolbar.setTitle(Html.fromHtml(title));
                }
            }
            //text
            if (data.has("subject")) {
                Object subject = data.get("subject");
                String text = new String();
                if (subject instanceof JSONObject) {
                    if (((JSONObject)subject).has("subject")) {
                        text = ((JSONObject)subject).getString("subject");
                    }
                } else text = subject.toString();

                mText.setMovementMethod(LinkMovementMethod.getInstance());
                Spanned sText = Html.fromHtml(text, new SpImageGetter(mText), null);
                mText.setText(sText);
                String line = sText.toString().split("\n")[0];
                int offset = line.length() > 100 ? 100 : line.length();
                String title = line.substring(0, offset);
                CharSequence currentTitle = mCollapsingToolbar.getTitle();
                if(TextUtils.isEmpty(currentTitle)) mCollapsingToolbar.setTitle(title);
            }
            //avatar
            if (data.has("avatar")) {
                JSONObject avatar = data.getJSONObject("avatar");
                mAvatar.setupData(avatar);
            }
            //header attachments
            if (data.has("MainAttachWidget")) {
                JSONObject mainAttachWidgets = data.getJSONObject("MainAttachWidget");
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
            if (data.has("attachWidget")) {
                JSONObject mainAttachWidgets = data.getJSONObject("attachWidget");
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
            if(data.has("time")) {
                String date = data.getString("time");
                if(date.startsWith("в ")) date = "Сегодня " + date;
                mDate.setIcon(R.drawable.ic_access_time_black_18dp);
                mDate.setText(date.toUpperCase());
            }
            //channel
            if(data.has("tagsWidget")) {
                JSONObject tags = data.getJSONObject("tagsWidget");
                mChannel.setupData(tags);
            }
        } catch (JSONException e) {

        }
    }

    public void setupActionBar(JSONObject data) {
        try {
            if (data.has("widgets")) {
                JSONObject widgets = data.getJSONObject("widgets");
                mVoting.setupData(widgets, session);
            }
        } catch (JSONException e) {

        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

        int currentScrollPercentage = (Math.abs(i)) * 100
            / mMaxScrollSize;

        if (currentScrollPercentage >= PERCENTAGE_TO_SHOW_IMAGE) {
            if (!mIsImageHidden) {
                mIsImageHidden = true;

                //ViewCompat.animate(author).translationY(-300).start();
            }
        }

        if (currentScrollPercentage < PERCENTAGE_TO_SHOW_IMAGE) {
            if (mIsImageHidden) {
                mIsImageHidden = false;
                //ViewCompat.animate(author).translationY(0).start();
            }
        }
    }
}
