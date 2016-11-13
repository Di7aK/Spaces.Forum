package com.di7ak.spaces.forum.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.listeners.RecyclerItemClickListener;
import com.dexafree.materialList.view.MaterialListView;
import com.di7ak.spaces.forum.ForumActivity;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.Forum;
import com.di7ak.spaces.forum.api.ForumCategoryData;
import com.di7ak.spaces.forum.api.SpacesException;
import com.rey.material.widget.ProgressView;
import java.util.ArrayList;
import java.util.List;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;
import org.json.JSONObject;
import org.json.JSONException;

public class ForumsFragment extends Fragment implements RecyclerItemClickListener.OnItemClickListener {
    MaterialListView mListView;
    List<ForumCategoryData> categories;
    Snackbar bar;
    int retryCount = 0;
    int maxRetryCount = 2;

    public ForumsFragment() {
        super();
        categories = new ArrayList<ForumCategoryData>();
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(selected && categories.size() == 0) {
            loadCategories();
        }
    }

    boolean selected;
    public void onSelected() {
        if (getActivity() != null && categories.size() == 0) {
            loadCategories();
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parrent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.comm_fragment, parrent, false);
        mListView = (MaterialListView) v.findViewById(R.id.material_listview);

        mListView.setItemAnimator(new ScaleInAnimator());
        mListView.getItemAnimator().setAddDuration(300);
        mListView.getItemAnimator().setRemoveDuration(300);
        
        mListView.addOnItemTouchListener(this);
        if (categories.size() != 0) showCategories();
        return v;
    }

    @Override
    public void onItemClick(@NonNull Card card, int position) {
        Intent intent = new Intent(getContext(), ForumActivity.class);
        intent.putExtra("name", categories.get(position).name);
        JSONObject json = new JSONObject();
        try {
            json.put("name", categories.get(position).name);
            json.put("forum_url", categories.get(position).url);
            intent.putExtra("comm", json.toString());
            Bundle args = new Bundle();
            args.putString("tab", "category");
            args.putString("type", "general");
            intent.putExtra("args", args);
            startActivity(intent);
        } catch (JSONException e) {}
    }

    @Override
    public void onItemLongClick(@NonNull Card card, int position) {
        //Log.d("LONG_CLICK", "" + card.getTag());
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
                        categories = Forum.getCategories();
                        showCategories();
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

    public void showCategories() {
        getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (bar != null) bar.dismiss();
                    for (ForumCategoryData data : categories) {
                        Card card = new Card.Builder(getContext())
                            .withProvider(new CardProvider())
                            .setLayout(R.layout.comm_item)
                            .setTitle(data.name)
                            .setDescription(data.description)
                            .setDrawable(R.drawable.ic_forum)
                            .endConfig()
                            .build();

                        mListView.getAdapter().add(mListView.getAdapter().getItemCount(), card, false);
                    }
                }
            });
    }

}
