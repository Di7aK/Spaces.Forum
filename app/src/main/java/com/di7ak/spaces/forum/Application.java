package com.di7ak.spaces.forum;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.util.DBHelper;
import com.rey.material.app.ThemeManager;
import java.net.URISyntaxException;

public class Application extends Application {
	private static String sSessionId;
    private static NotificationManager sNotificationManager;
    private static Context sContext;
    private static String sChannel;
    private static DBHelper sDBHelper;
    private static SQLiteDatabase sDb;
    private static Session sSession;
    private static MessageService sMessageService;

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
    
    public static SQLiteDatabase getDatabase(Context context) {
        if(sDb == null) {
            sDBHelper = new DBHelper(context);
            sDb = sDBHelper.getWritableDatabase();
        }
        return sDb;
    }
    
    public static MessageService getMessageService(Context context) {
        if(sMessageService == null) {
            sMessageService = new MessageService(context);
        }
        return sMessageService;
    }
    
    public static String getSessionId() {
        return sSessionId;
    }
    
    public static Session getSession() {
        return sSession;
    }
    
    public static void setSession(Session session) {
        sSession = session;
    }
}

