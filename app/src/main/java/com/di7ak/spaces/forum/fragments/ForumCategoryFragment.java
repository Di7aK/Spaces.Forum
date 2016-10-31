package com.di7ak.spaces.forum.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.Comm;
import com.di7ak.spaces.forum.api.Forum;
import com.di7ak.spaces.forum.api.ForumCategoryData;
import com.di7ak.spaces.forum.api.ForumData;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.rey.material.widget.ProgressView;
import java.util.ArrayList;
import java.util.List;
import android.net.Uri;

public class ForumCategoryFragment extends Fragment {
    LinearLayout catList;
    Session session;
    List<ForumData> categoryList;
    Snackbar bar;
    Comm comm;
    int retryCount = 0;
    int maxRetryCount = 2;

    public ForumCategoryFragment(Session session, Comm comm) {
        super();
        categoryList = new ArrayList<ForumData>();
        this.session = session;
        this.comm = comm;
    }

    public void onSelected() {
        if(categoryList.size() == 0) {
            loadCategories();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        loadCategories();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parrent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.forum_categories_fragment, parrent, false);
        catList = (LinearLayout) v.findViewById(R.id.categories_list);
        if (categoryList.size() != 0) showCategories(categoryList);
        return v;
    }

    public void loadCategories() {
        bar = Snackbar.make(getActivity().getWindow().getDecorView(), "Получение списка", Snackbar.LENGTH_INDEFINITE);

        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) bar.getView();
        View snackView = getActivity().getLayoutInflater().inflate(R.layout.progress_snackbar, layout, false);
        ProgressView pv = (ProgressView)snackView.findViewById(R.id.progress_pv_circular_determinate);
        pv.start();
        layout.addView(snackView, 0);

        bar.show();

        new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        categoryList = Forum.getForums(session, comm.cid);
                        ForumData all = new ForumData();
                        all.name = "Все";
                        all.description = "Показать со всех разделов";
                        categoryList.add(0, all);
                        showCategories(categoryList);
                        retryCount = 0;
                    } catch (SpacesException e) {
                        final String message = e.getMessage();
                        final int code = e.code;
                        if (code == -1 && retryCount < maxRetryCount) {
                            retryCount ++;
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ie) {}
                            loadCategories();
                        } else {
                            retryCount = 0;
                            getActivity().runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        bar.dismiss();
                                        bar = Snackbar.make(getActivity().getWindow().getDecorView(), message, Snackbar.LENGTH_INDEFINITE);
                                        if (code == -1) {
                                            bar.setAction("Повторить", new View.OnClickListener() {

                                                    @Override
                                                    public void onClick(View v) {
                                                        loadCategories();
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

    public void showCategories(final List<ForumData> categories) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (bar != null) bar.dismiss();
                    LayoutInflater li = getActivity().getLayoutInflater();
                    View v;
                    for (ForumData data : categories) {
                        v = li.inflate(R.layout.forum_item, null);
                        ((TextView)v.findViewById(R.id.name)).setText(Html.fromHtml(data.name));
                        ((TextView)v.findViewById(R.id.description)).setText(data.description);
                        ((TextView)v.findViewById(R.id.topics_cnt)).setText("+" + data.newCount);
                        View layout = v.findViewById(R.id.layout);
                        if(data.url != null) {
                            Uri uri = Uri.parse(data.url);
                            layout.setTag(uri.getQueryParameter("f"));
                        }
                        layout.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    if(listener != null) listener.onForumChange((String)v.getTag());
                                }
                            });
                        catList.addView(v);
                    }
                }
            });
    }
    
    OnForumChangeListener listener;
    
    public void setOnForumChangeListener(OnForumChangeListener listener) {
        this.listener = listener;
    }
    
    public interface OnForumChangeListener {
        public void onForumChange(String id);
    }
}
