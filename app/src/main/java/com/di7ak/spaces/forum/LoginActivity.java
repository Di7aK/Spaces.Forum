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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
	Toolbar toolbar;
	EditText eLogin, ePassword;
	Button btnLogin;
	CoordinatorLayout rootView;
	AccountAuthenticatorResponse mAccountAuthenticatorResponse;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		rootView = (CoordinatorLayout)findViewById(R.id.coordinator);
		eLogin = (EditText)findViewById(R.id.login);
		ePassword = (EditText)findViewById(R.id.password);
		btnLogin = (Button)findViewById(R.id.btn_login);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		btnLogin.setOnClickListener(this);
		
		mAccountAuthenticatorResponse = 
				getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
		
		if(mAccountAuthenticatorResponse != null) {
			mAccountAuthenticatorResponse.onRequestContinued();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.equals(btnLogin)) {
			String login = eLogin.getText().toString().trim();
			String password = ePassword.getText().toString().trim();
			if (TextUtils.isEmpty(login)) {
				Snackbar.make(rootView, "Не указан логин", Snackbar.LENGTH_SHORT).show();
			} else if (TextUtils.isEmpty(password)) {
				Snackbar.make(rootView, "Не указан пароль", Snackbar.LENGTH_SHORT).show();
			} else {
				auth(login, password);
			}
		}
	}

	boolean canceled;
	public void auth(final String login, final String password) {
		btnLogin.setEnabled(false);
		eLogin.setEnabled(false);
		ePassword.setEnabled(false);
		canceled = false;
		
		Snackbar bar = Snackbar.make(rootView, "Авторизация", Snackbar.LENGTH_INDEFINITE);
		
		bar.setAction("Отмена", new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					canceled = true;
					btnLogin.setEnabled(true);
					eLogin.setEnabled(true);
					ePassword.setEnabled(true);
				}
			});
			
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
						Session session = Auth.login(login, password);
						if (canceled) return;
						AccountManager am = AccountManager.get(getApplicationContext());
						Account account = new Account(session.login, Authenticator.ACCOUNT_TYPE);
						am.addAccountExplicitly(account, password, new Bundle());
						Intent res = new Intent();
						res.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
						res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.type);
						res.putExtra(AccountManager.KEY_AUTHTOKEN, session.sid);
						if(mAccountAuthenticatorResponse != null) {
							mAccountAuthenticatorResponse.onResult(res.getExtras());
						}
						setResult(RESULT_OK, res);
						finish();
					} catch (SpacesException e) {
						final String message = e.getMessage();
						final int code = e.code;
						runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Snackbar bar = Snackbar.make(rootView, message, Snackbar.LENGTH_INDEFINITE);
									if (code == -1) {
										bar.setAction("Повторить", new View.OnClickListener() {

												@Override
												public void onClick(View v) {
													auth(login, password);
												}
											});
									}
									bar.show();
									btnLogin.setEnabled(true);
									eLogin.setEnabled(true);
									ePassword.setEnabled(true);
								}
							});
					}
				}
			}).start();
	}
	
	@Override
	public void onBackPressed() {
		if(mAccountAuthenticatorResponse != null) {
			mAccountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED,
													  "canceled");
		}
		finish();
	}

}
