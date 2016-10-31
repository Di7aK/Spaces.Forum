package com.di7ak.spaces.forum;

import android.accounts.*;
import android.os.*;
import android.content.*;
import android.text.*;
import com.di7ak.spaces.forum.api.*;
import org.json.*;
import java.io.*;
import android.app.Activity;

public class Authenticator extends AbstractAccountAuthenticator {
	public static final String ACCOUNT_TYPE = "ru.spaces";
	public static final String TOKEN_FULL_ACCESS = "ru.spaces.full_access";

	private final Context context;

	public Authenticator(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse p1, String p2) {
		return null;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType,
							 String[] requiredFeatures, Bundle options) {
		Intent intent = new Intent(context, LoginActivity.class);
		intent.putExtra(TOKEN_FULL_ACCESS, ACCOUNT_TYPE);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		Bundle bundle = new Bundle();
		if (options != null) {
			bundle.putAll(options);
		}
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse p1, Account p2, Bundle p3) throws NetworkErrorException {
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType,
							   Bundle options) throws NetworkErrorException {
		Bundle result = new Bundle();
		AccountManager am = AccountManager.get(context.getApplicationContext());
		String authToken = am.peekAuthToken(account, authTokenType);
        String channelId = am.getUserData(account, "channel");
        if (TextUtils.isEmpty(authToken) || TextUtils.isEmpty(channelId)) {
			final String password = am.getPassword(account);
			if (!TextUtils.isEmpty(password)) {
				try {
                    Session session = Auth.login(account.name, password);
					authToken = session.sid;
                    am.setUserData(account, "channel", session.channel);
                    am.setUserData(account, "ck", session.ck);
                    am.setUserData(account, "login", session.login);
                    am.setUserData(account, "avatar", session.avatar);
				} catch (SpacesException e) {}
			}
		}
		if (!TextUtils.isEmpty(authToken)) {
			result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
			result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
			result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
		} else {
			final Intent intent = new Intent(context, LoginActivity.class);
			intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
			intent.putExtra(TOKEN_FULL_ACCESS, authTokenType);
			result.putParcelable(AccountManager.KEY_INTENT, intent);
		}
		return result;
	}

	@Override
	public String getAuthTokenLabel(String p1) {
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse p1, Account p2, String p3, Bundle p4) throws NetworkErrorException {
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse p1, Account p2, String[] p3) throws NetworkErrorException {
		return null;
	}

	public static void getSession(final Activity activity, final OnResult onResult) {
		final AccountManager am = AccountManager.get(activity);
		final Account[] accounts = am.getAccountsByType(ACCOUNT_TYPE);
        if (accounts.length == 0) {
            am.addAccount(ACCOUNT_TYPE, TOKEN_FULL_ACCESS, null, null, activity,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        getSession(activity, onResult);
                    }
                }, null
			);
        } else {
			am.getAuthToken(accounts[0], TOKEN_FULL_ACCESS, new Bundle(), true,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
							Bundle result = future.getResult();
							Session session = new Session();
                            session.sid = result.getString(AccountManager.KEY_AUTHTOKEN);
                            session.channel = am.getUserData(accounts[0], "channel");
                            session.ck = am.getUserData(accounts[0], "ck");
                            session.login = am.getUserData(accounts[0], "login");
                            session.avatar = am.getUserData(accounts[0], "avatar");
                            onResult.onAuthenticatorResult(session);
                        } catch (Exception e) {
							onResult.onAuthenticatorResult(null);
                        }
                    }
                }, null
			);
		}
	}

	public interface OnResult {
		public void onAuthenticatorResult(Session session);
	}


}
