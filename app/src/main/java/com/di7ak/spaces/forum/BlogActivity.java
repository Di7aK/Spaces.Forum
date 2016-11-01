package com.di7ak.spaces.forum;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.TopicActivity;
import com.di7ak.spaces.forum.api.AttachData;
import com.di7ak.spaces.forum.api.BlogData;
import com.di7ak.spaces.forum.api.Comment;
import com.di7ak.spaces.forum.api.CommentData;
import com.di7ak.spaces.forum.api.Forum;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.util.PicassoImageGetter;
import com.di7ak.spaces.forum.widget.PictureAttach;
import com.di7ak.spaces.forum.widget.ReplyWidget;
import com.di7ak.spaces.forum.widget.VotingWidget;
import com.rey.material.widget.Button;
import com.rey.material.widget.FloatingActionButton;
import com.rey.material.widget.ProgressView;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.di7ak.spaces.forum.api.Blogs;

public class BlogActivity extends AppCompatActivity
implements AppBarLayout.OnOffsetChangedListener, 
Authenticator.OnResult, 
NestedScrollView.OnScrollChangeListener,
View.OnClickListener, NotificationManager.OnNewNotification {
    private static final int PERCENTAGE_TO_SHOW_IMAGE = 20;
    private View author;
    private int mMaxScrollSize;
    private boolean mIsImageHidden;
    Session session;
    BlogData blog;
    int retryCount = 0;
    String id;
    Snackbar bar;
    Picasso picasso;
    View content;
    FloatingActionButton fab;
    View commentBlock;
    EditText commentBox;
    FloatingActionButton btnSend;
    boolean showing = false;
    String replyId = null;
    
    List<String> attachesNames;
    List<String> attachesUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blog);

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
            .downloader(new OkHttpDownloader(this, 100000)) 
            .build();

        hideBlog();

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
            
        attachesNames = new ArrayList<String>();
        attachesUrls = new ArrayList<String>();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.journal:
                Intent intent = new Intent(BlogActivity.this, JournalActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Application.notificationManager.removeListener(this);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Application.notificationManager.addListener(this);
        readNewComments();
    }
    
    @Override
    public void onNewNotification(JSONObject message) {
        android.util.Log.d("lol", message.toString());
        try {

            if (message.has("text")) {
                JSONObject text = message.getJSONObject("text");
                if (text.has("act")) {
                    int act = text.getInt("act");
                    if (act == 40) {
                        readNewComments();
                    } 
                }
            }
        } catch (JSONException e) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onAuthenticatorResult(Session session) {
        if (session == null) finish();
        else {
            this.session = session;

            Uri data = getIntent().getData();
            if(data != null) {
                id = data.getQueryParameter("id");
            } else {
                Bundle extra = getIntent().getExtras();
                if(extra != null) {
                    id = extra.getString("blog_id");
                }
            }
            getBlog();
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
                        Comment.send(session, commentBox.getText().toString(), 54, id, replyId);
                    } catch (SpacesException e) {}

                    runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                hideCommentBlock();
                                showFab();
                                commentBox.setText("");
                                fab.setLineMorphingState(0, true);
                                btnSend.setLineMorphingState(0, true);
                                showing = false;
                                btnSend.setEnabled(true);
                                readNewComments();
                            }
                        });
                }
            }).start();
    }

    int preY;
    @Override
    public void onScrollChange(NestedScrollView v, int p2, int p3, int p4, int p5) {
        if(preY > p3) {
            if(!fabShowing && !showing) showFab();
        } else {
            if(fabShowing && !showing) hideFab();
        }
        preY = p3;
        if(blog != null)
        if (blog.pagination != null && p3 + 50 > (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
            if (!bar.isShown() && blog.pagination.currentPage < blog.pagination.lastPage) {
                blog.pagination.currentPage ++;
                getBlog();
            }
        }
    }

    boolean fabShowing = false;
    private void showFab() {
        if(blog != null && !blog.commentFormEnabled) return;
        ViewCompat.animate(fab).translationY(0).start();
        fabShowing = true;
    }

    private void hideFab() {
        int hidingPos  = fab.getHeight() + (int)(20 * getResources().getDisplayMetrics().density);
        ViewCompat.animate(fab).translationY(hidingPos).start();
        fabShowing = false;
    }

    private void showCommentBlock() {
        commentBox.setEnabled(true);
        ViewCompat.animate(commentBlock).translationY(0).start();
        ViewCompat.animate(fab).translationY(- commentBlock.getHeight()).start();
    }

    private void hideCommentBlock() {
        replyId = null;
        commentBox.setEnabled(false);
        ViewCompat.animate(commentBlock).translationY(commentBlock.getHeight()).start();
        ViewCompat.animate(fab).translationY(0).start();
    }
    
    private void readNewComments() {
        if(blog == null) return;
        new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        blog = Blogs.getBlog(session, id, blog.pagination.currentPage);
                        runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    showComments(blog.comments);
                                }
                            });
                    } catch (SpacesException e) {
                        if (e.code == -1) {
                            
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ie) {}
                            readNewComments();
                        } 
                    }
                }
            }).start();
    }

    boolean blogShown = false;
    private void getBlog() {
        bar = Snackbar.make(getWindow().getDecorView(), "Получение блога", Snackbar.LENGTH_INDEFINITE);
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
                        if(blog == null) {
                            blog = Blogs.getBlog(session, id, 1);
                        } else {
                            blog = Blogs.getBlog(session, id, blog.pagination.currentPage);
                        }
                        runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    bar.dismiss();
                                    if (!blogShown) showBlog();
                                    showComments(blog.comments);
                                    hideCommentBlock();
                                    showFab();
                                }
                            });
                    } catch (SpacesException e) {
                        final String message = e.getMessage();
                        final int code = e.code;
                        if (code == -1 && retryCount < Settings.maxRetryCount) {
                            retryCount ++;
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ie) {}
                            getBlog();
                        } else {
                            retryCount = 0;
                            BlogActivity.this.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        bar.dismiss();
                                        bar = Snackbar.make(BlogActivity.this.getWindow().getDecorView(), message, Snackbar.LENGTH_INDEFINITE);
                                        if (code == -1) {
                                            bar.setAction("Повторить", new View.OnClickListener() {

                                                    @Override
                                                    public void onClick(View v) {
                                                        getBlog();
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

    public void showBlog() {
        blogShown = true;
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
                    attachesNames.add(attach.filename);
                    attachesUrls.add(attach.downloadLink);
                    int index = attachesNames.size() - 1;
                    PictureAttach widget = new PictureAttach(attach, attachesNames, attachesUrls, index, this, picasso);
                    attachBlock.addView(widget.getView());
                }
                
            }
            
        LinearLayout widgets = (LinearLayout)findViewById(R.id.widgets);
        
        VotingWidget voting = new VotingWidget(session, BlogActivity.this, blog.voting);
        widgets.addView(voting.getView());
            
        author.setVisibility(View.VISIBLE);
        content.setVisibility(View.VISIBLE);
        commentBlock.setVisibility(View.VISIBLE);
        fab.setVisibility(View.VISIBLE);
        ViewCompat.animate(author).translationY(0).alpha(1).start();
        ViewCompat.animate(content).translationY(0).alpha(1).start();
    }

    View decoration = null;
    List<String> showingIds = new ArrayList<String>();
    
    public void showComments(List<CommentData> comments) {
        LayoutInflater li = getLayoutInflater();
        ((TextView)findViewById(R.id.comments_cnt)).setText(Integer.toString(blog.commentsCnt));
        LinearLayout commentsList = (LinearLayout)findViewById(R.id.comments_list);
        for (CommentData comment : comments) {
            if(showingIds.indexOf(comment.id) != -1) continue;
            if (decoration != null) commentsList.addView(decoration);

            View v = li.inflate(R.layout.comment_item, null);
            ((TextView)v.findViewById(R.id.author)).setText(comment.user.name);
            TextView text = (TextView)v.findViewById(R.id.text);
            text.setMovementMethod(LinkMovementMethod.getInstance());
            if(comment.text != null && !comment.text.equals("null")) {
                text.setText(Html.fromHtml(comment.text, new PicassoImageGetter(text, getResources(), picasso), null));
            }
            String[] date = comment.date.split("в ");
            if(date.length == 2) {
                ((TextView)v.findViewById(R.id.date)).setText(date[0].trim());
                ((TextView)v.findViewById(R.id.time)).setText(date[1].trim());
            } else {
                ((TextView)v.findViewById(R.id.time)).setText(date[0].trim());
            }
            
            //attaches
            LinearLayout attachBlock = (LinearLayout)v.findViewById(R.id.attach_block);
            for(AttachData attach : comment.attaches) {
                if(attach == null) return;
                if(attach.fileext.equals("jpg") || attach.fileext.equals("png")) {
                    attachesNames.add(attach.filename);
                    attachesUrls.add(attach.downloadLink);
                    int index = attachesNames.size() - 1;
                    PictureAttach widget = new PictureAttach(attach, attachesNames, attachesUrls, index, this, picasso);
                    attachBlock.addView(widget.getView());
                }
            }
            
            LinearLayout buttonBlock = (LinearLayout)v.findViewById(R.id.button_block);
            
            if(blog.commentFormEnabled) {
                View btnResponse = li.inflate(R.layout.btn_response, null);
                Button btnReply = (Button)btnResponse.findViewById(R.id.btn_reply);
                btnReply.setTag(comment.id);
                btnReply.setOnClickListener(new View.OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        replyId = (String)v.getTag();
                        
                        if(!commentBox.isEnabled()) {
                            showing = true;
                            showCommentBlock();
                            fab.setLineMorphingState((fab.getLineMorphingState() + 1) % 2, true);
                        }
                    }
                });
                buttonBlock.addView(btnResponse);
            }
        
            LinearLayout widgetBlock = (LinearLayout)v.findViewById(R.id.widget_block);
            
            
            VotingWidget voting = new VotingWidget(session, BlogActivity.this, comment.voting);
            widgetBlock.addView(voting.getView());
            
            if(comment.replyUserName != null && !comment.replyUserName.equals("null")) {
                View reply = new ReplyWidget(this, comment.replyUserName, comment.replyCommentText, picasso).getView();
                ((LinearLayout)v.findViewById(R.id.comment_block_left)).addView(reply, 1);
            }
            if (comment.avatar != null) picasso.load(comment.avatar.previewUrl)
                    .into((CircleImageView)v.findViewById(R.id.avatar));

            commentsList.addView(v);

            decoration = new View(this);
            decoration.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
            decoration.setBackgroundColor(0xffdddddd);
            
            showingIds.add(comment.id);
        }
    }

    public void hideBlog() {
        author.setVisibility(View.INVISIBLE);
        content.setVisibility(View.INVISIBLE);
        commentBlock.setVisibility(View.INVISIBLE);
        hideCommentBlock();
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
