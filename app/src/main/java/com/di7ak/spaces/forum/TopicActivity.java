package com.di7ak.spaces.forum;

import android.accounts.*;
import android.support.design.widget.*;
import com.di7ak.spaces.forum.api.*;
import android.widget.ListView;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import com.di7ak.spaces.forum.R;
import android.content.Intent;
import com.di7ak.spaces.forum.adapters.TopicAdapter;
import com.rey.material.widget.ProgressView;

public class TopicActivity extends AppCompatActivity implements Authenticator.OnResult {
	Toolbar toolbar;
	Session session;
	Topic topic;
	TopicAdapter adapter;
	ListView content;
	int retryCount = 0;
	int maxRetryCount = 3;
	Snackbar bar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.topic);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		content = (ListView)findViewById(R.id.topic_content);
		
		Authenticator.getSession(this, this);
	}
	
	@Override
	public void onAuthenticatorResult(Session session) {
		if(session == null) finish();
		else {
			this.session = session;

			Bundle extra = getIntent().getExtras();
			topic = new Topic();
			topic.id = extra.getString("topic_id");

			getTopic();
		}
	}
	
	private void getTopic() {
		bar = Snackbar.make(getWindow().getDecorView(), "Получение топика", Snackbar.LENGTH_INDEFINITE);

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
					topic = Forum.getTopic(session, topic.id, 1);
                    android.util.Log.d("lol", "author " + topic.topicUser + ", avatar " + topic.avatarUrl);
                    runOnUiThread(new Runnable() {
                        
                        @Override
                        public void run() {
					if(adapter == null) {
						adapter = new TopicAdapter(TopicActivity.this, topic);
						content.setAdapter(adapter);
					}
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
	
}
