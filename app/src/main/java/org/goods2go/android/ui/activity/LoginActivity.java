package org.goods2go.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.goods2go.authentication.SessionItem;
import com.goods2go.models.User;
import com.goods2go.models.enums.Role;

import org.goods2go.android.Goods2GoApplication;
import org.goods2go.android.R;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.util.OfflineStorage;

import java.net.HttpURLConnection;

public class LoginActivity extends NetworkActivity{

    private UserLoginTask userLoginTask = null;
    private GetUserDataTask getUserDataTask = null;

    private EditText mailEdit;
    private EditText passwordEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        content = findViewById(R.id.login_form);
        progress = findViewById(R.id.login_progress);
        mailEdit = findViewById(R.id.displayName);
        passwordEdit = findViewById(R.id.password);

        passwordEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        Button signIn = findViewById(R.id.button_sign_in);
        signIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        Button signUp = findViewById(R.id.button_sign_up);
        signUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                LoginActivity.this.startActivity(intent);
            }
        });
        attemptAutoLogin();
    }

    private void attemptAutoLogin(){
        SessionItem sessionItem = OfflineStorage.loadSessionItem(this);
        if(sessionItem != null){
            Goods2GoApplication.login(LoginActivity.this, sessionItem);
            showProgress(true);
            getUserDataTask = new GetUserDataTask();
            getUserDataTask.execute();
        }
    }

    private void attemptLogin() {
        if (userLoginTask != null) {
            return;
        }
        mailEdit.setError(null);
        passwordEdit.setError(null);

        String email = mailEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            passwordEdit.setError(getString(R.string.error_field_required));
            focusView = passwordEdit;
            cancel = true;
        }
        if (TextUtils.isEmpty(email)) {
            mailEdit.setError(getString(R.string.error_field_required));
            focusView = mailEdit;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mailEdit.setError(getString(R.string.error_invalid_email));
            focusView = mailEdit;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            userLoginTask = new UserLoginTask();
            userLoginTask.execute(email, password);
        }
    }

    private class UserLoginTask extends NetworkTask<String, Void, SessionItem> {

        @Override
        protected SessionItem runInBackground(String... params) throws NetworkException {
            String email = params[0];
            String password = params[1];
            //Login and getting session key
            SessionItem sessionItem = networkClient.signIn(email, password);
            if(sessionItem != null){
                Goods2GoApplication.login(LoginActivity.this, sessionItem);
            }
            return sessionItem;
        }

        @Override
        protected void onPostExecute(SessionItem result) {
            userLoginTask = null;

            if(networkException != null){
                showProgress(false);
                if(networkException.httpError == HttpURLConnection.HTTP_UNAUTHORIZED){
                    passwordEdit.setError(getString(R.string.error_incorrect_password));
                    passwordEdit.requestFocus();
                    return;
                }
                networkException.handleException(LoginActivity.this);
            } else if (result != null) {
                getUserDataTask = new GetUserDataTask();
                getUserDataTask.execute();
            } else {
                showProgress(false);
                showResult(R.string.error_unknown);
            }
        }
    }

    private class GetUserDataTask extends NetworkTask<Void, Void, User> {

        @Override
        protected User runInBackground(Void[] voids) throws NetworkException {
            return networkClient.getUser();
        }

        @Override
        protected void onPostExecute(User result) {
            getUserDataTask = null;
            showProgress(false);

            if(networkException != null){
                if(networkException.httpError == HttpURLConnection.HTTP_UNAUTHORIZED){
                    showResult(R.string.error_session_expired);
                    return;
                }
                networkException.handleException(LoginActivity.this);
            } else if (result != null) {
                Goods2GoApplication.setCurrentUser(LoginActivity.this, result);
                Intent intent = new Intent(LoginActivity.this, RoleChooseActivity.class);
                if(result.getRole().equals(Role.DELIVERER) && result.isIdentconfirmed()){
                    intent.putExtra(
                            RoleChooseActivity.IS_DELIVERER_KEY, true);
                }
                LoginActivity.this.startActivity(intent);
                LoginActivity.this.finish();
            } else {
                showResult(R.string.error_unknown);
            }
        }
    }
}

