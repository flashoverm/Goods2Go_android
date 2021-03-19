package org.goods2go.android.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import org.goods2go.android.Goods2GoApplication;
import org.goods2go.android.R;
import org.goods2go.android.network.NetworkClient;
import org.goods2go.android.ui.activity.NetworkActivity;

public abstract class NetworkFragment extends Fragment {

    protected View progress;
    protected View content;

    protected NetworkClient networkClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.networkClient = Goods2GoApplication.getNetworkClient(getActivity());
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

    protected void showResult(int stringId){
        ((NetworkActivity)getActivity()).showResult(stringId);
    }

    protected void showLowerLevelFragment(Fragment fragment, boolean keepFragment){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if(keepFragment){
            //Keep fragment and hide it
            transaction.add(R.id.content, fragment);
            transaction.hide(this);
        } else {
            //Replace fragment and delete entries
            transaction.replace(R.id.content, fragment);
        }
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
