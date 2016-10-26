package com.di7ak.spaces.forum;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {   

    @Override
    public void onReceive(Context context, Intent intent) {
        
            Intent myIntent = new Intent(context, NotificationService.class);
            context.startService(myIntent);
        

    }
}
