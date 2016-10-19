package com.di7ak.spaces.forum;

import android.accounts.*;
import android.support.design.widget.*;
import com.di7ak.spaces.forum.api.*;
import com.rey.material.widget.*;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import com.di7ak.spaces.forum.R;
import android.content.Intent;

public class TopicActivity extends AppCompatActivity {
	Toolbar toolbar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.topic);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		
	}
	
}
