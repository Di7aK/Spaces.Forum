package com.di7ak.spaces.forum;

import android.accounts.*;
import android.os.*;
import android.content.*;
import android.text.*;
import com.di7ak.spaces.forum.api.*;
import org.json.*;
import java.io.*;

public class Authenticator extends AbstractAccountAuthenticator {
	private final Context context;
	
	public Authenticator(Context context) {
		super(context);
		this.context = context;
	}
	
	@Override
	public Bundle editProperties(AccountAuthenticatorResponse p1, String p2) {
		// TODO: Implement this method
		return null;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType,
							 String[] requiredFeatures, Bundle options) {
		final Intent intent = new Intent(context, LoginActivity.class);
		intent.putExtra(LoginActivity.EXTRA_TOKEN_TYPE, accountType);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		final Bundle bundle = new Bundle();
		if (options != null) {
			bundle.putAll(options);
		}
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse p1, Account p2, Bundle p3) throws NetworkErrorException {
		// TODO: Implement this method
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType,
							   Bundle options) throws NetworkErrorException {
		final Bundle result = new Bundle();
		final AccountManager am = AccountManager.get(context.getApplicationContext());
		String authToken = am.peekAuthToken(account, authTokenType);
		if (TextUtils.isEmpty(authToken)) {
			final String password = am.getPassword(account);
			if (!TextUtils.isEmpty(password)) {
				try {
					Session session = Auth.login(account.name, password);
					authToken = session.sid;
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
			intent.putExtra(LoginActivity.EXTRA_TOKEN_TYPE, authTokenType);
			final Bundle bundle = new Bundle();
			bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		}
		return result;
	}

	@Override
	public String getAuthTokenLabel(String p1) {
		// TODO: Implement this method
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse p1, Account p2, String p3, Bundle p4) throws NetworkErrorException {
		// TODO: Implement this method
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse p1, Account p2, String[] p3) throws NetworkErrorException {
		// TODO: Implement this method
		return null;
	}
	
	
}
