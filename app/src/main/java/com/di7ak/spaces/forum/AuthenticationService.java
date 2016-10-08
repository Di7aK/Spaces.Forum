package com.di7ak.spaces.forum;

import android.app.*;
import android.os.*;
import android.content.*;

public class AuthenticationService extends Service {
	private Authenticator mAuth;

    @Override
    public void onCreate() {
        mAuth = new Authenticator(this);
    }
	
	@Override
	public IBinder onBind(Intent p1) {
		return mAuth.getIBinder();
	}
}
