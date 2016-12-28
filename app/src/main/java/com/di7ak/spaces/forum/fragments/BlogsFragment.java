package com.di7ak.spaces.forum.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.di7ak.spaces.forum.BlogActivity;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.Request;
import com.di7ak.spaces.forum.api.RequestListener;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.interfaces.OnPageSelectedListener;
import com.di7ak.spaces.forum.util.SpImageGetter;
import com.di7ak.spaces.forum.widget.AvatarView;
import com.di7ak.spaces.forum.widget.ChannelView;
import com.di7ak.spaces.forum.widget.FileAttachmentsView;
import com.di7ak.spaces.forum.widget.ImagedTextView;
import com.di7ak.spaces.forum.widget.PaginationView;
import com.di7ak.spaces.forum.widget.PictureAttachmentsView;
import com.di7ak.spaces.forum.widget.ProgressBar;
import com.di7ak.spaces.forum.widget.UserView;
import com.di7ak.spaces.forum.widget.VotingView;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.res.Configuration;

public class BlogsFragment extends Fragment implements NestedScrollView.OnScrollChangeListener,
OnPageSelectedListener, 
RequestListener,
View.OnClickListener {
    private NestedScrollView mScrollView;
    private LinearLayout mList;
    private Session mSession;
    private ProgressBar mBar;
    private List<Integer> mShowing;
    private PaginationView mPagination;
    private boolean mSelected = false;
    private Uri mData;
    
    public BlogsFragment() {
        super();
    }

    public void setData(Uri data, Session session) {
        mData = data;
        mSession = session;
    }

    @Override
    public void onSelected() {
        mSelected = true;
        if (getActivity() != null && mShowing.size() == 0) {
            loadBlogs();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mBar = new ProgressBar(activity);
        mPagination = new PaginationView(activity);
        mShowing = new ArrayList<Integer>();

        if (mData != null && mSelected && mShowing.size() == 0) {
            loadBlogs();
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parrent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.forum_fragment, parrent, false);

        mList = (LinearLayout) v.findViewById(R.id.topic_list);
        mScrollView = (NestedScrollView) v.findViewById(R.id.scroll_view);
        mScrollView.setOnScrollChangeListener(this);

        return v;
    }

    @Override
    public void onScrollChange(NestedScrollView v, int x, int y, int p4, int p5) {
        if (x + 50 > (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
            if (mData != null && !mBar.isShown() && mPagination.hasNextPage()) {
                loadBlogs();
            }
        }
    }

    private void loadBlogs() {
        mBar.showProgress("Загрузка данных");
        new Request(mData).executeWithListener(this);
    }

    @Override
    public void onSuccess(JSONObject json) {
        mBar.hide();
        try {
            if (json.has("paginationWidget") && json.get("paginationWidget") instanceof JSONObject) {
                JSONObject pagination = json.getJSONObject("paginationWidget");
                mPagination.setupData(pagination);
            }
            if (json.has("topicWidget")) {
                JSONObject topics = json.getJSONObject("topicWidget");
                addTopics(topics);
            }
        } catch (JSONException e) {}
    }

    private void addTopics(JSONObject json) {
        try {
            if (json.has("topicModels")) {
                JSONArray topics = json.getJSONArray("topicModels");
                for (int i = 0; i < topics.length(); i ++) {
                    JSONObject topic = topics.getJSONObject(i);
                    addTopic(topic);
                }
            }
        } catch (JSONException e) {

        }
    }

    private void addTopic(JSONObject json) {
        try {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            int id = json.getJSONObject("model").getInt("topic_id");
            if (mShowing.contains(id)) return;
            mShowing.add(id);
            if (json.has("properties")) {
                JSONObject prop = json.getJSONObject("properties");
                View v = inflater.inflate(R.layout.blog_item, mList, false);
                if (prop.has("time")) {
                    String text = prop.getString("time");
                    if(text.startsWith("в ")) text = "Сегодня " + text;
                    ImagedTextView date = (ImagedTextView) v.findViewById(R.id.date);
                    date.setIcon(R.drawable.ic_access_time_black_18dp);
                    date.setText(text.toUpperCase());
                    if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        date.invert();
                    }
                }
                if (prop.has("userWidget")) {
                    JSONObject user = prop.getJSONObject("userWidget");
                    UserView author = (UserView) v.findViewById(R.id.author);
                    author.setupData(user);
                }
                if (prop.has("avatar")) {
                    JSONObject avatar = prop.getJSONObject("avatar");
                    AvatarView avatarView = (AvatarView) v.findViewById(R.id.avatar);
                    avatarView.setupData(avatar);
                }
                if (prop.has("channel_name")) {
                    ChannelView channel = (ChannelView) v.findViewById(R.id.channel);
                    channel.setupData(prop);
                    if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        channel.invert();
                    }
                }
                if (prop.has("header")) {
                    String header = prop.getString("header");
                    TextView title = (TextView) v.findViewById(R.id.title);
                    title.setText(Html.fromHtml(header));
                }
                if (prop.has("subject")) {
                    String subject = prop.getString("subject");
                    TextView text = (TextView) v.findViewById(R.id.text);
                    text.setText(Html.fromHtml(subject, new SpImageGetter(text), null));
                }
                //header attachments
                if (prop.has("MainAttachWidget")) {
                    JSONObject mainAttachWidgets = prop.getJSONObject("MainAttachWidget");
                    PictureAttachmentsView pictureAttachments = (PictureAttachmentsView) v.findViewById(R.id.picture_attachments);
                    
                    if (mainAttachWidgets.has("pictureWidgets")) {
                        JSONArray pictureWidgets = mainAttachWidgets.getJSONArray("pictureWidgets");
                        pictureAttachments.setupData(pictureWidgets);
                    }
                    if (mainAttachWidgets.has("attachWidgets")) {
                        JSONArray pictureWidgets = mainAttachWidgets.getJSONArray("attachWidgets");
                        pictureAttachments.setupData(pictureWidgets);
                    }
                }
                //footer attachments
                if (prop.has("ElseAttachWidget")) {
                    JSONObject mainAttachWidgets = prop.getJSONObject("ElseAttachWidget");
                    if (mainAttachWidgets.has("attachWidgets")) {
                        JSONArray attachWidgets = mainAttachWidgets.getJSONArray("attachWidgets");
                        FileAttachmentsView fileAttachments = (FileAttachmentsView) v.findViewById(R.id.file_attachments);
                        
                        fileAttachments.setupData(attachWidgets);
                    }
                    if (mainAttachWidgets.has("musicInlineWidget")) {
                        JSONArray attachWidgets = mainAttachWidgets.getJSONArray("musicInlineWidget");
                        FileAttachmentsView audioAttachments = (FileAttachmentsView) v.findViewById(R.id.audio_attachments);
                        audioAttachments.setupData(attachWidgets);
                    }
                }
                if(prop.has("views")) {
                    String views = prop.getString("views");
                    ImagedTextView viewsView = (ImagedTextView) v.findViewById(R.id.views);
                    viewsView.setIcon(R.drawable.ic_remove_red_eye_black_18dp);
                    viewsView.setText(views);
                }
                if(prop.has("actionBar")) {
                    JSONObject data = prop.getJSONObject("actionBar");
                    if (data.has("widgets")) {
                        JSONObject widgets = data.getJSONObject("widgets");
                        VotingView voting = (VotingView) v.findViewById(R.id.voting);
                        voting.setupData(widgets, mSession);
                    }
                }
                if(prop.has("read_href")) {
                    String href = prop.getString("read_href");
                    View layout = v.findViewById(R.id.layout);
                    layout.setTag(href);
                    layout.setOnClickListener(this);
                }
                mList.addView(v);
            }
        } catch (JSONException e) {

        }
    }

    @Override
    public void onError(SpacesException e) {
        mBar.showError(e.getMessage(), "Повторить", this);
    }

    @Override
    public void onClick(View v) {
        if(v.getTag() != null) {
            Intent intent = new Intent(getActivity(), BlogActivity.class);
            intent.setData(Uri.parse((String)v.getTag()));
            intent.setAction(Intent.ACTION_VIEW);
            getActivity().startActivity(intent);
        } else loadBlogs();
    }
}
