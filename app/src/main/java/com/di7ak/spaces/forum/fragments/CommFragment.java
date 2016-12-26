package com.di7ak.spaces.forum.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.di7ak.spaces.forum.ForumActivity;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.Request;
import com.di7ak.spaces.forum.api.RequestListener;
import com.di7ak.spaces.forum.api.SpacesException;
import com.di7ak.spaces.forum.util.ImageDownloader;
import com.di7ak.spaces.forum.widget.PaginationView;
import com.di7ak.spaces.forum.widget.ProgressBar;
import com.rey.material.widget.Button;
import com.rey.material.widget.ProgressView;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommFragment extends Fragment implements 
        RequestListener, View.OnClickListener,
        NestedScrollView.OnScrollChangeListener {
    private Uri mData;
    private ProgressBar mBar;
    private LinearLayout mList;
    private CardView mCard;
    private NestedScrollView mScroll;
    private List<Integer> mShowing;
    private PaginationView mPagination;
    private RelativeLayout mLoadNextIndicator;
    private ProgressView mProgressNext;
    private Button mBtnLoadNext;

    public void setData(Uri uri) {
        mData = uri;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mBar = new ProgressBar(activity);
        mBar.showProgress("Загрузка данных");
        new Request(mData).executeWithListener(this);
    }

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parrent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.comm_fragment, parrent, false);
		mList = (LinearLayout) v.findViewById(R.id.comm_list);
        mCard = (CardView) v.findViewById(R.id.card_view);
        mScroll = (NestedScrollView) v.findViewById(R.id.scroll_view);
        mShowing = new ArrayList<Integer>();
        mCard.setVisibility(View.GONE);
        mPagination = new PaginationView(getActivity());
        mScroll.setOnScrollChangeListener(this);
        
        mLoadNextIndicator = (RelativeLayout) inflater.inflate(R.layout.pagination_load, mList, false);
        mLoadNextIndicator.setBackgroundColor(0xffffffff);
        mProgressNext = (ProgressView) mLoadNextIndicator.findViewById(R.id.pagination_load_indicator);
        mBtnLoadNext = (Button) mLoadNextIndicator.findViewById(R.id.btn_more);
        mBtnLoadNext.setOnClickListener(this);
		return v;
	}

    @Override
    public void onSuccess(JSONObject json) {
        mBar.hide();
        removeLoadNextIndicator();
        try {
            if (json.has("pagination") && json.get("pagination") instanceof JSONObject) {
                JSONObject pagination = json.getJSONObject("pagination");
                mPagination.setupData(pagination);
            }
            if(json.has("comms_list")) {
                JSONArray commsList = json.getJSONArray("comms_list");
                showComms(commsList);
            }
            if(mPagination.hasNextPage()) addLoadNextIndicator();
        } catch (JSONException e) {}
    }

    @Override
    public void onError(SpacesException e) {
        mBar.showError(e.getMessage(), "Повторить", this);
    }

    private void showComms(JSONArray comms) {
        try {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            for(int i = 0; i < comms.length(); i ++) {
                JSONObject comm = comms.getJSONObject(i);
                int id = comm.getInt("id");
                if(mShowing.indexOf(id) != -1) continue;
                View v = inflater.inflate(R.layout.comm_item, mList, false);
                //name
                if(comm.has("name")) {
                    String name = comm.getString("name");
                    TextView nameView = (TextView) v.findViewById(R.id.name);
                    nameView.setText(name);
                }
                //counters 
                if(comm.has("counters")) {
                    JSONObject counters = comm.getJSONObject("counters");
                    int forum = 0;
                    int blog = 0;
                    if(counters.has("forum")) forum = counters.getInt("forum");
                    if(counters.has("blog")) blog = counters.getInt("blog");
                    String desc = createDescription(forum, blog);
                    TextView description = (TextView) v.findViewById(R.id.description);
                    description.setText(desc);
                }
                //avatar
                if(comm.has("logo_widget")) {
                    JSONObject logo = comm.getJSONObject("logo_widget");
                    if(logo.has("previewURL")) {
                        String url = logo.getString("previewURL");
                        CircleImageView into = (CircleImageView) v.findViewById(R.id.avatar);
                        Uri uri = Uri.parse(url);
                        String query = uri.getQuery();
                        url = url.replace(query, "");
                        String hash = ImageDownloader.md5(url);
                        new ImageDownloader(getActivity()).hash(hash).from(url).into(into).execute();
                    }
                }
                mShowing.add(id);
                v.setTag(comm.toString());
                v.setOnClickListener(this);
                mList.addView(v);
            }
        } catch(JSONException e) {
            
        }
        if (mShowing.size() > 0 && mCard.getVisibility() == View.GONE) {
            mCard.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onScrollChange(NestedScrollView v, int x, int y, int p4, int p5) {
        if (y + 50 > (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
            if (!mBar.isShown() && mPagination.hasNextPage()) {
                mData = Uri.parse(mPagination.getNextPageUrl());
                mBtnLoadNext.setVisibility(View.INVISIBLE);
                mProgressNext.start();
                new Request(mData).executeWithListener(this);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.equals(mBtnLoadNext)) {
            mData = Uri.parse(mPagination.getNextPageUrl());
            mBtnLoadNext.setVisibility(View.INVISIBLE);
            mProgressNext.start();
            new Request(mData).executeWithListener(this);
        } else if(v.getTag() != null) {
            Intent intent = new Intent(getActivity(), ForumActivity.class);
            intent.putExtra("comm", (String)v.getTag());
            Bundle args = new Bundle();
            args.putString("tab", "new");
            intent.putExtra("args", args);
            getActivity().startActivity(intent);
        } else new Request(mData).executeWithListener(this);
    }
    
    private void addLoadNextIndicator() {
        mList.addView(mLoadNextIndicator);
        mBtnLoadNext.setVisibility(View.VISIBLE);
    }

    private void removeLoadNextIndicator() {
        if(mList.indexOfChild(mLoadNextIndicator) != -1) mList.removeView(mLoadNextIndicator);
        mProgressNext.stop();
    }
    
    private String createDescription(int forum, int blog) {
        StringBuilder result = new StringBuilder();
        if(forum > 0) result.append("форум +").append(Integer.toString(forum));
        if(blog > 0) {
            if(forum > 0) result.append(", ");
            result.append("блог +").append(Integer.toString(blog));
        }
        return result.toString();
    }
}
