package com.di7ak.spaces.forum.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.AttachData;
import com.di7ak.spaces.forum.api.BlogListData;
import com.di7ak.spaces.forum.api.Blogs;
import com.di7ak.spaces.forum.api.PreviewBlogData;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.interfaces.OnPageSelectedListener;
import com.di7ak.spaces.forum.util.PicassoImageGetter;
import com.di7ak.spaces.forum.widget.PictureAttach;
import com.di7ak.spaces.forum.widget.ProgressBar;
import com.di7ak.spaces.forum.widget.VotingWidget;
import com.rey.material.widget.ProgressView;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;

    public class BlogsFragment extends Fragment implements NestedScrollView.OnScrollChangeListener,
    OnPageSelectedListener {
        LinearLayout blogsList;
        NestedScrollView scrollView;
        Session session;
        List<PreviewBlogData> blogs;
        ProgressBar bar;
        String from;
        int currentPage = 1;
        int pages = 1;
        int type;
        int retryCount = 0;
        int maxRetryCount = 2;

        List<String> attachesNames;
        List<String> attachesUrls;
        Picasso picasso;

        public BlogsFragment(Session session, String from, int type) {
            super();
            blogs = new ArrayList<PreviewBlogData>();
            this.session = session;
            this.from = from;
            this.type = type;

            attachesNames = new ArrayList<String>();
            attachesUrls = new ArrayList<String>();
        }
        
        boolean selected = false;
        @Override
        public void onSelected() {
            selected = true;
            if(getActivity() != null && blogs.size() == 0) {
                loadBlogs();
            }
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            bar = new ProgressBar(activity);
            if (picasso == null) {
                picasso = new Picasso.Builder(activity) 
                    .downloader(new OkHttpDownloader(activity, 100000)) 
                    .build();
            }
            if (selected && blogs.size() == 0) {
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
            blogsList = (LinearLayout) v.findViewById(R.id.topic_list);
            scrollView = (NestedScrollView) v.findViewById(R.id.scroll_view);

            scrollView.setOnScrollChangeListener(this);

            if (blogs.size() != 0) showBlogs(blogs);
            return v;
        }

        @Override
        public void onScrollChange(NestedScrollView v, int p2, int p3, int p4, int p5) {
            if (p3 + 50 > (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                if (!bar.isShown() && currentPage < pages) {
                    currentPage ++;
                    loadBlogs();
                }
            }
        }

        public void loadBlogs() {
            if(!bar.isShown()) bar.showProgress("Получение списка");

            new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            BlogListData result = Blogs.getBlogs(session, from, type, currentPage);
                            blogs.addAll(result.blogs);
                            pages = result.pagination.lastPage;
                            currentPage = result.pagination.currentPage;
                            showBlogs(result.blogs);
                            retryCount = 0;
                            bar.hide();
                        } catch (SpacesException e) {
                            final String message = e.getMessage();
                            final int code = e.code;
                            if (code == -1 && retryCount < maxRetryCount) {
                                retryCount ++;
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ie) {}
                                loadBlogs();
                            } else {
                                retryCount = 0;
                                bar.showError(message, "Повторить", new View.OnClickListener() {

                                        @Override
                                        public void onClick(View v) {
                                            loadBlogs();
                                        }
                                    });
                            }
                        }
                    }
                }).start();

        }

        public void showBlogs(final List<PreviewBlogData> blogs) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        LayoutInflater li = getActivity().getLayoutInflater();
                        View v;
                        for (PreviewBlogData blog : blogs) {
                            try {
                            v = li.inflate(R.layout.blog_item, null);
                            String[] date = blog.date.split("в ");
                            if (date.length == 2) {
                                ((TextView)v.findViewById(R.id.date)).setText(date[0].trim());
                                ((TextView)v.findViewById(R.id.time)).setText(date[1].trim());
                            } else {
                                ((TextView)v.findViewById(R.id.time)).setText(date[0].trim());
                            }

                            ((TextView)v.findViewById(R.id.author)).setText(blog.author);
                            TextView text = (TextView)v.findViewById(R.id.subject);
                            text.setMovementMethod(LinkMovementMethod.getInstance());
                            if (blog.subject != null && !blog.subject.equals("null")) {
                                text.setText(Html.fromHtml(blog.subject, new PicassoImageGetter(text, getResources(), picasso), null));
                            }
                            if(blog.header != null) ((TextView)v.findViewById(R.id.header)).setText(blog.header);
                            
                            
                            if (blog.avatar != null) picasso.load(blog.avatar.previewUrl)
                                    .into((CircleImageView)v.findViewById(R.id.avatar));


                            LinearLayout attachBlock = (LinearLayout)v.findViewById(R.id.attach_block);
                            for (AttachData attach : blog.attaches) {
                                if (attach == null) return;
                                if (attach.fileext.equals("jpg") || attach.fileext.equals("png")) {
                                    attachesNames.add(attach.filename);
                                    attachesUrls.add(attach.downloadLink);
                                    int index = attachesNames.size() - 1;
                                    PictureAttach widget = new PictureAttach(attach, attachesNames, attachesUrls, index, getActivity(), picasso);
                                    attachBlock.addView(widget.getView());
                                }
                            }

                            LinearLayout widgetBlock = (LinearLayout)v.findViewById(R.id.widget_block);

                            if(blog.voting != null) {
                                VotingWidget voting = new VotingWidget(session, getActivity(), blog.voting);
                                widgetBlock.addView(voting.getView());
                            }
                            /*

                            View layout = v.findViewById(R.id.layout);
                            layout.setTag(blog.id);
                            layout.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getContext(), TopicActivity.class);
                                        intent.putExtra("topic_id", (String)v.getTag());

                                        startActivity(intent);
                                    }
                                });
                                */
                            blogsList.addView(v);
                            } catch(Exception e) {
                                android.util.Log.e("lol", "", e);
                            }
                        }

                    }
                });
        }
    }