package com.di7ak.spaces.forum;

import android.accounts.*;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import com.di7ak.spaces.forum.LoginActivity;
import com.rey.material.app.ThemeManager;
import com.di7ak.spaces.forum.api.Session;

public class Application extends Application {
	public static Session session;

    @Override 
	public void onCreate() {
        super.onCreate();
        ThemeManager.init(this, 1, 0, null);
		
    }
}

