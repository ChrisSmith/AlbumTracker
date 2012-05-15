/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.collegelabs.albumtracker.authenticator;

import org.collegelabs.albumtracker.BuildConfig;
import org.collegelabs.albumtracker.Constants;
import org.collegelabs.albumtracker.R;
import org.collegelabs.albumtracker.content.AlbumProvider;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


/**
 * This class is a mutilation of the AuthenticatorActivity demo class provided in the 
 * samples provided by Google. We don't need to authenticate anything, just add the account
 */
/**
 * Activity which displays login screen to the user.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {
	/** The Intent flag to confirm credentials. */
	public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";

	/** The Intent extra to store password. */
	public static final String PARAM_PASSWORD = "password";

	/** The Intent extra to store username. */
	public static final String PARAM_USERNAME = "username";

	/** The Intent extra to store username. */
	public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

	/** The tag used to log to adb console. */
	private static final String TAG = "AuthenticatorActivity";
	private AccountManager mAccountManager;


	/**
	 * If set we are just checking that the user knows their credentials; this
	 * doesn't cause the user's password or authToken to be changed on the
	 * device.
	 */
	private Boolean mConfirmCredentials = false;

//	private TextView mMessage;

	private String mPassword;

	/** Was the original caller asking for an entirely new account? */
	protected boolean mRequestNewAccount = false;

	private String mUsername;

	private EditText mUsernameEdit;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle icicle) {

		if(BuildConfig.DEBUG) Log.i(TAG, "onCreate(" + icicle + ")");
		super.onCreate(icicle);
		mAccountManager = AccountManager.get(this);
		if(BuildConfig.DEBUG) Log.i(TAG, "loading data from Intent");
		final Intent intent = getIntent();
		mUsername = intent.getStringExtra(PARAM_USERNAME);
		mRequestNewAccount = mUsername == null;
		mConfirmCredentials = intent.getBooleanExtra(PARAM_CONFIRM_CREDENTIALS, false);
		if(BuildConfig.DEBUG) Log.i(TAG, "    request new: " + mRequestNewAccount);
		setContentView(R.layout.activity_login);

//		mMessage = (TextView) findViewById(R.id.message);
		mUsernameEdit = (EditText) findViewById(R.id.username_edit);

		if (!TextUtils.isEmpty(mUsername)) mUsernameEdit.setText(mUsername);
//		mMessage.setText(getMessage());
	}

	/**
	 * Handles onClick event on the Submit button. Sends username/password to
	 * the server for authentication. The button is configured to call
	 * handleLogin() in the layout XML.
	 *
	 * @param view The Submit button for which this method is invoked
	 */
	public void handleLogin(View view) {
		if (mRequestNewAccount) {
			mUsername = mUsernameEdit.getText().toString();
		}
		mPassword = "OK";
		if (TextUtils.isEmpty(mUsername) || TextUtils.isEmpty(mPassword)) {
//			mMessage.setText(getMessage());
		} else {
			onAuthenticationResult("OK");	
		}
	}

	/**
	 * Called when response is received from the server for confirm credentials
	 * request. See onAuthenticationResult(). Sets the
	 * AccountAuthenticatorResult which is sent back to the caller.
	 *
	 * @param result the confirmCredentials result.
	 */
	private void finishConfirmCredentials(boolean result) {
		if(BuildConfig.DEBUG) Log.i(TAG, "finishConfirmCredentials()");
		final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);
		mAccountManager.setPassword(account, mPassword);
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, result);
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK, intent);
		finish();
	}

	/**
	 * Called when response is received from the server for authentication
	 * request. See onAuthenticationResult(). Sets the
	 * AccountAuthenticatorResult which is sent back to the caller. We store the
	 * authToken that's returned from the server as the 'password' for this
	 * account - so we're never storing the user's actual password locally.
	 *
	 * @param result the confirmCredentials result.
	 */
	private void finishLogin(String authToken) {

		if(BuildConfig.DEBUG) Log.i(TAG, "finishLogin()");
		final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);
		if (mRequestNewAccount) {
			mAccountManager.addAccountExplicitly(account, mPassword, null);
			
			//setup the periodic sync
			long seconds = 60 * 60 * 24;
			ContentResolver.setSyncAutomatically(account, AlbumProvider.AUTHORITY, true);
			ContentResolver.addPeriodicSync(account, AlbumProvider.AUTHORITY, new Bundle(), seconds);

			//Force the sync if global sync is disabled
			if(!ContentResolver.getMasterSyncAutomatically()){
				Bundle bundle = new Bundle();
				bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
				ContentResolver.requestSync(account, AlbumProvider.AUTHORITY, bundle);	
			}
			
		} else {
			mAccountManager.setPassword(account, mPassword);
		}
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK, intent);
		finish();
	}

	/**
	 * Called when the authentication process completes (see attemptLogin()).
	 *
	 * @param authToken the authentication token returned by the server, or NULL if
	 *            authentication failed.
	 */
	public void onAuthenticationResult(String authToken) {

		boolean success = ((authToken != null) && (authToken.length() > 0));
		if(BuildConfig.DEBUG) Log.i(TAG, "onAuthenticationResult(" + success + ")");

		if (success) {
			if (!mConfirmCredentials) {
				finishLogin(authToken);
			} else {
				finishConfirmCredentials(success);
			}
		} else {
			if(BuildConfig.DEBUG) Log.e(TAG, "onAuthenticationResult: failed to authenticate");
			if (mRequestNewAccount) {
				// "Please enter a valid username/password.
//				mMessage.setText(getText(R.string.login_activity_loginfail_text_both));
			} else {
				// "Please enter a valid password." (Used when the
				// account is already in the database but the password
				// doesn't work.)
//				mMessage.setText(getText(R.string.login_activity_loginfail_text_pwonly));
			}
		}
	}

	/**
	 * Returns the message to be displayed at the top of the login dialog box.
	 *
	private CharSequence getMessage() {
		getString(R.string.label);
		if (TextUtils.isEmpty(mUsername)) {
			// If no username, then we ask the user to log in using an
			// appropriate service.
			final CharSequence msg = getText(R.string.login_activity_newaccount_text);
			return msg;
		}
		if (TextUtils.isEmpty(mPassword)) {
			// We have an account but no password
			return getText(R.string.login_activity_loginfail_text_pwmissing);
		}
		return null;
	}
	*/
}
