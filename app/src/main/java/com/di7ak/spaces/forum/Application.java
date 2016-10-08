package com.di7ak.spaces.forum;

import android.app.Application;

import com.rey.material.app.ThemeManager;

public class Application extends Application{

    @Override 
	public void onCreate() {
        super.onCreate();
        ThemeManager.init(this, 1, 0, null);
    }
}

