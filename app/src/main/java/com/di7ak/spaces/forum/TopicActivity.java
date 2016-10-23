package com.di7ak.spaces.forum;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.TopicActivity;
import com.di7ak.spaces.forum.api.Comment;
import com.di7ak.spaces.forum.api.CommentData;
import com.di7ak.spaces.forum.api.Forum;
import com.di7ak.spaces.forum.api.PaginationData;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.api.TopicData;
import com.di7ak.spaces.forum.util.PicassoImageGetter;
import com.rey.material.widget.FloatingActionButton;
import com.rey.material.widget.Button;
import com.rey.material.widget.ProgressView;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import java.util.List;

public class TopicActivity extends AppCompatActivity
implements AppBarLayout.OnOffsetChangedListener, 
Authenticator.OnResult, 
NestedScrollView.OnScrollChangeListener,
View.OnClickListener {
    private static final int PERCENTAGE_TO_SHOW_IMAGE = 20;
    private View author;
    private int mMaxScrollSize;
    private boolean mIsImageHidden;
    Session session;
    TopicData topic;
    int retryCount = 0;
    int maxRetryCount = 3;
    String id;
	Snackbar bar;
    Picasso picasso;
    View content;
    FloatingActionButton fab;
    View commentBlock;
    EditText commentBox;
    FloatingActionButton btnSend;
    boolean showing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic);

        author = findViewById(R.id.author);
        content = findViewById(R.id.content);
        btnSend = (FloatingActionButton)findViewById(R.id.fab_send);
        btnSend.setOnClickListener(this);
        commentBox = (EditText)findViewById(R.id.comment);
        fab = (FloatingActionButton)findViewById(R.id.fab);
        commentBlock = findViewById(R.id.comment_block);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    onBackPressed();
                }
            });

        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);
        appbar.addOnOffsetChangedListener(this);

        picasso = new Picasso.Builder(this) 
            .loggingEnabled(BuildConfig.DEBUG) 
            .indicatorsEnabled(BuildConfig.DEBUG) 
            .downloader(new OkHttpDownloader(this, 100000)) 
            .build();

        hideTopic();

        ((NestedScrollView)findViewById(R.id.nested_scroll_view)).setOnScrollChangeListener(this);

        Authenticator.getSession(this, this);

        fab.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    showing = !showing;
                    if(showing) showCommentBlock();
                    else hideCommentBlock();
                    fab.setLineMorphingState((fab.getLineMorphingState() + 1) % 2, true);
                }
            });
    }

    @Override
    public void onAuthenticatorResult(Session session) {
        if (session == null) finish();
        else {
            this.session = session;

            Bundle extra = getIntent().getExtras();
            topic = new TopicData();
            id = extra.getString("topic_id");
            topic.pagination = new PaginationData();
            topic.pagination.currentPage = 1;

            getTopic();
        }
    }

    @Override
    public void onClick(View v) {
        btnSend.setLineMorphingState(1, true);
        btnSend.setEnabled(false);
        new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Comment.send(session, commentBox.getText().toString(), 51, id);
                    } catch (SpacesException e) {}

                    runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                hideCommentBlock();
                                showFab();
                                fab.setLineMorphingState(0, true);
                                btnSend.setLineMorphingState(0, true);
                                showing = false;
                                btnSend.setEnabled(true);
                            }
                        });
                }
            }).start();
    }

    @Override
    public void onScrollChange(NestedScrollView v, int p2, int p3, int p4, int p5) {
        if (topic.pagination != null && p3 + 50 > (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
            if (!bar.isShown() && topic.pagination.currentPage < topic.pagination.lastPage) {
                topic.pagination.currentPage ++;
                getTopic();
            }
        }
	}

    private void showFab() {
        ViewCompat.animate(fab).translationY(0).start();
    }

    private void hideFab() {
        ViewCompat.animate(fab).translationY(fab.getHeight() + 16).start();
    }

    private void showCommentBlock() {
        ViewCompat.animate(commentBlock).translationY(0).start();
        ViewCompat.animate(fab).translationY(- commentBlock.getHeight()).start();
    }

    private void hideCommentBlock() {
        ViewCompat.animate(commentBlock).translationY(commentBlock.getHeight()).start();
        ViewCompat.animate(fab).translationY(0).start();
    }

    private void getTopic() {
        bar = Snackbar.make(getWindow().getDecorView(), "Получение топика", Snackbar.LENGTH_INDEFINITE);
        runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    hideCommentBlock();
                    hideFab();
                }
            });
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) bar.getView();
        View snackView = getLayoutInflater().inflate(R.layout.progress_snackbar, layout, false);
        ProgressView pv = (ProgressView)snackView.findViewById(R.id.progress_pv_circular_determinate);
        pv.start();
        layout.addView(snackView, 0);

        bar.show();

        new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        topic = Forum.getTopic(session, id, topic.pagination.currentPage);
                        runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    bar.dismiss();
                                    if (topic.pagination.currentPage == 1) showTopic();
                                    showComments(topic.comments);
                                    showFab();
                                }
                            });
                    } catch (SpacesException e) {
                        final String message = e.getMessage();
                        final int code = e.code;
                        if (code == -1 && retryCount < maxRetryCount) {
                            retryCount ++;
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ie) {}
                            getTopic();
                        } else {
                            retryCount = 0;
                            TopicActivity.this.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        bar.dismiss();
                                        bar = Snackbar.make(TopicActivity.this.getWindow().getDecorView(), message, Snackbar.LENGTH_INDEFINITE);
                                        if (code == -1) {
                                            bar.setAction("Повторить", new View.OnClickListener() {

                                                    @Override
                                                    public void onClick(View v) {
                                                        getTopic();
                                                    }
                                                });
                                        }
                                        bar.show();
                                    }
                                });
                        }
                    }
                }
            }).start();
	}

    public void showTopic() {
        ((CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar)).setTitle(Html.fromHtml(topic.subject));
        ((TextView)findViewById(R.id.user_name)).setText(topic.user.name);
        TextView body = (TextView)findViewById(R.id.body);
        body.setText(Html.fromHtml(topic.body, new PicassoImageGetter(body, getResources(), picasso), null));

        picasso.load(topic.avatar.previewUrl.replace("41.40", "81.80"))
            .into((CircleImageView)findViewById(R.id.user_avatar));
        ((TextView)findViewById(R.id.comments_cnt)).setText(Integer.toString(topic.commentsCnt));

        author.setVisibility(View.VISIBLE);
        content.setVisibility(View.VISIBLE);
        commentBlock.setVisibility(View.VISIBLE);
        fab.setVisibility(View.VISIBLE);
        ViewCompat.animate(author).translationY(0).alpha(1).start();
        ViewCompat.animate(content).translationY(0).alpha(1).start();
    }

    View decoration = null;
    public void showComments(List<CommentData> comments) {
        LayoutInflater li = getLayoutInflater();
        LinearLayout commentsList = (LinearLayout)findViewById(R.id.comments_list);
        for (CommentData comment : topic.comments) {
            if (decoration != null) commentsList.addView(decoration);

            View v = li.inflate(R.layout.comment_item, null);
            ((TextView)v.findViewById(R.id.author)).setText(comment.user.name);
            TextView text = (TextView)v.findViewById(R.id.text);
            text.setText(Html.fromHtml(comment.text, new PicassoImageGetter(text, getResources(), picasso), null));
            ((TextView)v.findViewById(R.id.date)).setText(comment.date);
            if(comment.replyUserName != null && !comment.replyUserName.equals("null")) {
                View reply = li.inflate(R.layout.reply, null);
                ((TextView)reply.findViewById(R.id.reply_to)).setText(comment.replyUserName);
                ((LinearLayout)v.findViewById(R.id.comment_block_left)).addView(reply, 1);
            }
            if (comment.avatar != null) picasso.load(comment.avatar.previewUrl)
                    .into((CircleImageView)v.findViewById(R.id.avatar));

            commentsList.addView(v);

            decoration = new View(this);
            decoration.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
            decoration.setBackgroundColor(0xffdddddd);
        }
    }

    public void hideTopic() {
        author.setVisibility(View.INVISIBLE);
        content.setVisibility(View.INVISIBLE);
        commentBlock.setVisibility(View.INVISIBLE);
        fab.setVisibility(View.INVISIBLE);
        ViewCompat.animate(author).translationY(-50).alpha(0).start();
        ViewCompat.animate(content).translationY(50).alpha(0).start();
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
