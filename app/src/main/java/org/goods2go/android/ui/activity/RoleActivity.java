package org.goods2go.android.ui.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.goods2go.models.Shipment;
import com.goods2go.models.User;

import org.goods2go.android.Goods2GoApplication;
import org.goods2go.android.R;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.fragment.ShipmentFragment;
import org.goods2go.android.util.StringLookup;

import static org.goods2go.android.notification.MessageReceivedListener.OPEN_ID;
import static org.goods2go.android.notification.MessageReceivedListener.OPEN_SHIPMENT;
import static org.goods2go.android.notification.MessageReceivedListener.OPEN_TYPE;

public abstract class RoleActivity extends NetworkActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawer;
    protected NavigationView navigationView;

    private GetShipmentTask getShipmentTask;

    private Snackbar logoutConfirmation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    protected NavigationView initNavigationDrawer(int resIdDrawer, int resIdNavigation){
        drawer = findViewById(resIdDrawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(resIdNavigation);
        navigationView.setNavigationItemSelectedListener(this);
        onNavigationItemSelected(navigationView.getMenu().getItem(0));

        User currentUser = Goods2GoApplication.getCurrentUser(this);
        if(drawer != null && currentUser != null){
            TextView displayName = navigationView.getHeaderView(0).findViewById(R.id.displayName);
            displayName.setText(currentUser.getDisplayNameOrMail());
            TextView role = navigationView.getHeaderView(0).findViewById(R.id.role);
            role.setText(StringLookup.getIdFrom(currentUser.getRole()));
        }
        return navigationView;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.nav_logout) {
            showLogoutConfirmation();
            return true;
        } else {
            return onRoleNavigationItemSelected(item);
        }
    }

    protected Fragment switchToFragment(Class fragmentClass, MenuItem item){
        try {
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.content, fragment);
            transaction.commit();
            item.setChecked(true);
            setTitle(item.getTitle());
            return fragment;
        } catch (IllegalAccessException | InstantiationException e) {
            showResult(R.string.error_fragment);
            e.printStackTrace();
            return null;
        }
    }

    public abstract boolean onRoleNavigationItemSelected(MenuItem item);

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int count = getFragmentManager().getBackStackEntryCount();
            if (count == 0) {
                if(logoutConfirmation == null || !logoutConfirmation.isShown()){
                    showLogoutConfirmation();
                } else {
                    //Close app
                    super.onBackPressed();
                }
            } else {
                //Go back to fragment in stack
                super.onBackPressed();
            }
        }
    }

    public void showLogoutConfirmation(){
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        logoutConfirmation = Snackbar.make(rootView, R.string.text_logout_confirm, Snackbar.LENGTH_LONG)
                .setAction(R.string.text_logout, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Goods2GoApplication.logout(RoleActivity.this);
                    }
                });
        logoutConfirmation.show();
    }

    protected void handleNotificationIntent(Intent intent){
        int type = intent.getIntExtra(OPEN_TYPE, -1);
        long id = intent.getLongExtra(OPEN_ID, -1L);

        if(type == OPEN_SHIPMENT && id != -1){
            showProgress(true);
            getShipmentTask =  new GetShipmentTask();
            getShipmentTask.execute(id);
        }
    }

    private class GetShipmentTask extends NetworkTask<Long, Void, Shipment> {

        @Override
        protected Shipment runInBackground(Long[] params) throws NetworkException {
            long id = params[0];
            return networkClient.getShipment(id);
        }

        @Override
        protected void onPostExecute(Shipment result) {
            getShipmentTask = null;
            showProgress(false);

            if(networkException != null){
                networkException.handleException(RoleActivity.this);
                return;
            }

            ShipmentFragment fragment = (ShipmentFragment)switchToFragment(
                    ShipmentFragment.class, navigationView.getMenu().getItem(0));
            fragment.setShipment(result);
        }
    }
}
