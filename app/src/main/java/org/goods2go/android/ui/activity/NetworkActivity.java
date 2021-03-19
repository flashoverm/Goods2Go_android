package org.goods2go.android.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.goods2go.android.Goods2GoApplication;
import org.goods2go.android.network.NetworkClient;

public abstract class NetworkActivity extends AppCompatActivity {

    protected View progress;
    protected View content;

    protected NetworkClient networkClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        networkClient = Goods2GoApplication.getNetworkClient(this);
    }

    protected void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        content.setVisibility(show ? View.GONE : View.VISIBLE);
        content.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                content.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        progress.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progress.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    protected boolean isEmailValid(String email) {
        return email.contains("@");
    }

    public void showResult(int stringId){
        Toast.makeText(this, stringId, Toast.LENGTH_LONG).show();
    }

}
