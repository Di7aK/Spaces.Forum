package com.di7ak.spaces.forum;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;

import com.di7ak.spaces.forum.api.Session;
import com.rey.material.app.ThemeManager;

import java.net.URISyntaxException;

public class Application extends Application {
	private static String sSessionId;
    private static NotificationManager sNotificationManager;
    private static Context sContext;
    private static String sChannel;

    @Override 
	public void onCreate() {
        super.onCreate();
        ThemeManager.init(this, 1, 0, null);
		sContext = this;
        
        AccountManager am = AccountManager.get(sContext);
        Account[] accounts = am.getAccountsByType(Authenticator.ACCOUNT_TYPE);
        if (accounts.length != 0) {
            sChannel = am.getUserData(accounts[0], "channel");
            sSessionId = am.peekAuthToken(accounts[0], Authenticator.TOKEN_FULL_ACCESS);
        }
    }
    
    public static NotificationManager getNotificationManager() {
        if(sNotificationManager == null && sChannel != null) {
            try {
                sNotificationManager = new NotificationManager(sChannel);
            } catch (URISyntaxException e) {}
        }
        return sNotificationManager;
    }
    
    public static String getSessionId() {
        return sSessionId;
    }
}

