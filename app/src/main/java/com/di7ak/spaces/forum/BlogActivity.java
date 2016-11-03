package com.di7ak.spaces.forum;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.Request;
import com.di7ak.spaces.forum.api.RequestListener;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.util.PicassoImageGetter;
import com.di7ak.spaces.forum.widget.AvatarView;
import com.di7ak.spaces.forum.widget.CommentsView;
import com.di7ak.spaces.forum.widget.PictureAttachmentsView;
import com.di7ak.spaces.forum.widget.ProgressBar;
import com.di7ak.spaces.forum.widget.VotingView;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BlogActivity extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener, 
        Authenticator.OnResult,
        RequestListener {
    private static final int PERCENTAGE_TO_SHOW_IMAGE = 20;
    private View author;
    private int mMaxScrollSize;
    private boolean mIsImageHidden;
    private Session session;
    private JSONObject blog;
    private Uri uri;
    private ProgressBar bar;
    private Picasso picasso;
    private TextView mAuthor;
    private TextView mText;
    private AvatarView mAvatar;
    private VotingView mVoting;
    private PictureAttachmentsView mPictureAttachments;
    private CommentsView mComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blog);

        author = findViewById(R.id.author);
        mAuthor = (TextView)findViewById(R.id.user_name);
        mText = (TextView)findViewById(R.id.text);
        mAvatar = (AvatarView)findViewById(R.id.avatar);
        mVoting = (VotingView)findViewById(R.id.voting);
        mPictureAttachments = (PictureAttachmentsView)findViewById(R.id.picture_attachments);
        mComments = (CommentsView) findViewById(R.id.comments);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    onBackPressed();
                }
            });

        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);
        appbar.addOnOffsetChangedListener(this);

        picasso = new Picasso.Builder(this) 
            .downloader(new OkHttpDownloader(this, Settings.CACHE_SIZE)) 
            .build();
        bar = new ProgressBar(this);

        Authenticator.getSession(this, this);
    }

    @Override
    public void onAuthenticatorResult(Session session) {
        if (session == null) finish();
        else {
            this.session = session;

            uri = getIntent().getData();
            if (uri == null) {
                bar.showError("Ошибка", null, null);
            } else readTopic();
        }
    }

    private void readTopic() {
        bar.showProgress("Загрузка данных");
        new Request(uri).executeWithListener(this);
    }

    public void onSuccess(JSONObject json) {
        blog = json;
        bar.hide();
        try {
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
                mComments.setupData(commentsBlock, picasso, session);
            }
        } catch (JSONException e) {

        }
    }

    public void onError(SpacesException e) {
        bar.showError(e.getMessage(), "ПОВТОРИТЬ", new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    readTopic();
                }
            });
    }

    private void setupTopicData(JSONObject data) {
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
                    if (!TextUtils.isEmpty(title)) setTitle(title);
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
                mText.setText(Html.fromHtml(text, new PicassoImageGetter(mText, getResources(), picasso), null));
            }
            //avatar
            if (data.has("avatar")) {
                JSONObject avatar = data.getJSONObject("avatar");
                mAvatar.setupData(avatar, picasso);
            }
            //attachments
            if (data.has("MainAttachWidget")) {
                JSONObject mainAttachWidgets = data.getJSONObject("MainAttachWidget");
                if (mainAttachWidgets.has("pictureWidgets")) {
                    JSONArray pictureWidgets = mainAttachWidgets.getJSONArray("pictureWidgets");
                    mPictureAttachments.setupData(pictureWidgets, picasso);
                }
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
    /*
     public void showBlog() {
     ((CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar)).setTitle(Html.fromHtml(blog.header));
     ((TextView)findViewById(R.id.user_name)).setText(blog.username);
     TextView body = (TextView)findViewById(R.id.body);
     body.setMovementMethod(LinkMovementMethod.getInstance());
     body.setText(Html.fromHtml(blog.subject, new PicassoImageGetter(body, getResources(), picasso), null));

     picasso.load(blog.avatar.previewUrl.replace("41.40", "81.80"))
     .into((CircleImageView)findViewById(R.id.user_avatar));

     LinearLayout attachBlock = (LinearLayout)findViewById(R.id.attach_block);
     for(AttachData attach : blog.attaches) {
     if(attach == null || attach.fileext == null) return;
     if(attach.fileext.equals("jpg") || attach.fileext.equals("png")) {
     int index = attachesNames.size() - 1;
     PictureAttach widget = new PictureAttach(attach, attachesNames, attachesUrls, index, this, picasso);
     attachBlock.addView(widget.getView());
     }

     }

     LinearLayout widgets = (LinearLayout)findViewById(R.id.widgets);

     VotingWidget voting = new VotingWidget(session, BlogActivity.this, blog.voting);
     widgets.addView(voting.getView());

     ViewCompat.animate(author).translationY(0).alpha(1).start();
     ViewCompat.animate(content).translationY(0).alpha(1).start();
     }*/

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

        int currentScrollPercentage = (Math.abs(i)) * 100
            / mMaxScrollSize;

        if (currentScrollPercentage >= PERCENTAGE_TO_SHOW_IMAGE) {
            if (!mIsImageHidden) {
                mIsImageHidden = true;

                ViewCompat.animate(author).translationY(-300).start();
            }
        }

        if (currentScrollPercentage < PERCENTAGE_TO_SHOW_IMAGE) {
            if (mIsImageHidden) {
                mIsImageHidden = false;
                ViewCompat.animate(author).translationY(0).start();
            }
        }
    }
}
