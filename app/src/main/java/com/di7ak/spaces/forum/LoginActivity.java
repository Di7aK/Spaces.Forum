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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
	public static final String EXTRA_TOKEN_TYPE = "ru.spaces.EXTRA_TOKEN_TYPE";

	Toolbar toolbar;
	EditText eLogin, ePassword;
	Button btnLogin;
	CoordinatorLayout rootView;

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
		Snackbar bar = Snackbar.make(rootView, "Авторизация", Snackbar.LENGTH_INDEFINITE);
		btnLogin.setEnabled(false);
		eLogin.setEnabled(false);
		ePassword.setEnabled(false);
		canceled = false;
		/*
		bar.setAction("Отмена", new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					canceled = true;
					btnLogin.setEnabled(true);
					eLogin.setEnabled(true);
					ePassword.setEnabled(true);
				}
			});
			*/
		Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) bar.getView();

/*		TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
		textView.setVisibility(View.INVISIBLE);
*/
		View snackView = getLayoutInflater().inflate(R.layout.progress_snackbar, layout, false);
		ProgressView pv = (ProgressView)snackView.findViewById(R.id.progress_pv_circular_determinate);
		pv.start();

/*		TextView textViewTop = (TextView) snackView.findViewById(R.id.text);
		textViewTop.setText("Авторизация");
		textViewTop.setTextColor(Color.WHITE);
*/
		layout.addView(snackView);
		bar.show();
		new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Session session = Auth.login(login, password);
						if (canceled) return;
						AccountManager am = AccountManager.get(getApplicationContext());
						Account account = new Account(session.login, "ru.spaces");
						am.addAccountExplicitly(account, password, new Bundle());
						finish();
					} catch (SpacesException e) {
						final String message = e.getMessage();
						final int code = e.code;
						runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Snackbar bar = Snackbar.make(rootView, message, Snackbar.LENGTH_INDEFINITE);
									if (code < 0) {
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

}
