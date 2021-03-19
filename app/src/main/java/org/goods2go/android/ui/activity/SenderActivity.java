package org.goods2go.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.goods2go.models.PaymentInformation;
import com.goods2go.models.User;
import com.goods2go.models.enums.IdentType;
import com.goods2go.models.enums.Role;

import org.goods2go.android.Goods2GoApplication;
import org.goods2go.android.R;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.notification.NotificationService;
import org.goods2go.android.ui.dialog.BecomeDelivererDialog;
import org.goods2go.android.ui.fragment.sender.ActiveShipmentsFragment;
import org.goods2go.android.ui.fragment.sender.AnnounceFragment;
import org.goods2go.android.ui.fragment.sender.ClosedShipmentsFragment;
import org.goods2go.android.ui.fragment.sender.OpenAnnouncementsFragment;

import static org.goods2go.android.notification.MessageReceivedListener.OPEN_ANNOUNCEMENT_LIST;
import static org.goods2go.android.notification.MessageReceivedListener.OPEN_ANNOUNCEMENT_REQUEST;
import static org.goods2go.android.notification.MessageReceivedListener.OPEN_ID;
import static org.goods2go.android.notification.MessageReceivedListener.OPEN_SHIPMENT;
import static org.goods2go.android.notification.MessageReceivedListener.OPEN_TYPE;

public class SenderActivity extends RoleActivity
        implements BecomeDelivererDialog.BecomeDelivererListener{

    private BecomeDelivererTask becomeDelivererTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        content = findViewById(R.id.content);
        progress = findViewById(R.id.progress);

        NavigationView navigationView = this.initNavigationDrawer(
                R.id.drawer, R.id.navigation);

        User currentUser = Goods2GoApplication.getCurrentUser(this);
        if(currentUser.getRole().equals(Role.DELIVERER)){
            Menu menu = navigationView.getMenu();
            MenuItem switchDeliverer = menu.findItem(R.id.nav_switch_to_deliverer);
            switchDeliverer.setVisible(true);
            MenuItem becomeDeliverer = menu.findItem(R.id.nav_become_deliverer);
            becomeDeliverer.setVisible(false);
        }
        handleNotificationIntent(getIntent());
    }

    @Override
    public boolean onRoleNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_become_deliverer) {
            BecomeDelivererDialog dialog = new BecomeDelivererDialog();
            dialog.show(getFragmentManager(), "BecomeDelivererDialog");
            return false;
        } else if (id == R.id.nav_switch_to_deliverer) {
            Intent intent = new Intent(this, DelivererActivity.class);
            this.startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.nav_account_administration) {
            Intent intent = new Intent(this, AccountAdministrationActivity.class);
            this.startActivity(intent);
            drawer.closeDrawer(GravityCompat.START);
            return false;
        } else {
            Class fragmentClass;
            switch(id){
                case R.id.nav_announce_shipment:
                    fragmentClass = AnnounceFragment.class;
                    break;
                case R.id.nav_open_announcements:
                    fragmentClass = OpenAnnouncementsFragment.class;
                    break;
                case R.id.nav_active_shipments:
                    fragmentClass = ActiveShipmentsFragment.class;
                    break;
                case R.id.nav_closed_shipments:
                    fragmentClass = ClosedShipmentsFragment.class;
                    break;
                default:
                    fragmentClass = AnnounceFragment.class;
            }
            switchToFragment(fragmentClass, item);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBecomeDeliverer(IdentType identType, String identificationNumber,
                                  PaymentInformation paymentInformation) {
        drawer.closeDrawer(GravityCompat.START);
        showProgress(true);

        User update = Goods2GoApplication.getCurrentUser(this);
        update.setIdenttype(identType);
        update.setIdentno(identificationNumber);
        update.setPaymentInformation(paymentInformation);
        becomeDelivererTask = new BecomeDelivererTask();
        becomeDelivererTask.execute(update);
    }

    private class BecomeDelivererTask extends NetworkTask<User, Void, User> {

        @Override
        protected User runInBackground(User... params) throws NetworkException {
            User delivererUser = params[0];
            return networkClient.becomeDeliverer(delivererUser);
        }

        @Override
        protected void onPostExecute(User result) {
            becomeDelivererTask = null;
            showProgress(false);

            if(networkException != null){
                networkException.handleException(SenderActivity.this);
                return;
            }

            if(result != null){
                showResult(R.string.info_request_sent);
            } else {
                showResult(R.string.error_request_sent);
            }
        }
    }

    @Override
    protected void handleNotificationIntent(Intent intent) {
        int type = intent.getIntExtra(OPEN_TYPE, -1);
        long id = intent.getLongExtra(OPEN_ID, -1L);

        if(type != -1 && id != -1){
            switch (type){
                case OPEN_SHIPMENT:
                    super.handleNotificationIntent(intent);
                    break;
                case OPEN_ANNOUNCEMENT_LIST:
                    OpenAnnouncementsFragment fragment;
                    fragment = (OpenAnnouncementsFragment) switchToFragment(
                            OpenAnnouncementsFragment.class,
                            navigationView.getMenu().findItem(R.id.nav_open_announcements));
                    fragment.expandSpecificAnnouncement(id);
                    break;
                case OPEN_ANNOUNCEMENT_REQUEST:
                    Log.e(NotificationService.TAG,
                            "Only DelivererActivity can handle OPEN_ANNOUNCEMENT_REQUEST");
                    break;
            }
        }
    }
}
