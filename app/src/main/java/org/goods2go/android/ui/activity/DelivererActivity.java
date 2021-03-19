package org.goods2go.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.MenuItem;

import com.goods2go.models.ShipmentAnnouncement;

import org.goods2go.android.R;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.notification.NotificationService;
import org.goods2go.android.ui.fragment.deliverer.ActiveShipmentsFragment;
import org.goods2go.android.ui.fragment.deliverer.ClosedShipmentsFragment;
import org.goods2go.android.ui.fragment.deliverer.EndDeliveryListFragment;
import org.goods2go.android.ui.fragment.deliverer.OpenRequestsFragment;
import org.goods2go.android.ui.fragment.deliverer.SearchAnnouncementFragment;
import org.goods2go.android.ui.fragment.deliverer.SendRequestFragment;
import org.goods2go.android.ui.fragment.deliverer.StartDeliveryListFragment;
import org.goods2go.android.ui.fragment.deliverer.SubscribeShipmentsFragment;
import org.goods2go.android.ui.fragment.deliverer.SubscriptionListFragment;
import org.goods2go.android.util.PermissionHandler;

import static org.goods2go.android.notification.MessageReceivedListener.OPEN_ANNOUNCEMENT_LIST;
import static org.goods2go.android.notification.MessageReceivedListener.OPEN_ANNOUNCEMENT_REQUEST;
import static org.goods2go.android.notification.MessageReceivedListener.OPEN_ID;
import static org.goods2go.android.notification.MessageReceivedListener.OPEN_SHIPMENT;
import static org.goods2go.android.notification.MessageReceivedListener.OPEN_TYPE;
import static org.goods2go.android.ui.activity.RoleChooseActivity.IS_DELIVERER_KEY;
import static org.goods2go.android.util.PermissionHandler.CAMERA_REQUEST;
import static org.goods2go.android.util.PermissionHandler.LOCATION_REQUEST;

public class DelivererActivity extends RoleActivity {

    private GetAnnouncementTask getAnnouncementTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliverer);

        content = findViewById(R.id.content);
        progress = findViewById(R.id.progress);

        NavigationView navigationView = this.initNavigationDrawer(
                R.id.drawer, R.id.navigation);
        handleNotificationIntent(getIntent());
    }

    @Override
    public boolean onRoleNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_switch_to_sender) {
            Intent intent = new Intent(this, SenderActivity.class);
            intent.putExtra(IS_DELIVERER_KEY, true);
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
                case R.id.nav_search_announcement:
                    fragmentClass = SearchAnnouncementFragment.class;
                    break;
                case R.id.nav_shipment_requests:
                    fragmentClass = OpenRequestsFragment.class;
                    break;
                case R.id.nav_start_shipment:
                    fragmentClass = StartDeliveryListFragment.class;
                    break;
                case R.id.nav_active_shipments:
                    fragmentClass = ActiveShipmentsFragment.class;
                    break;
                case R.id.nav_end_shipment:
                    fragmentClass = EndDeliveryListFragment.class;
                    break;
                case R.id.nav_closed_shipments:
                    fragmentClass = ClosedShipmentsFragment.class;
                    break;
                case R.id.nav_subscribe_announcements:
                    fragmentClass = SubscribeShipmentsFragment.class;
                    break;
                case R.id.nav_subscribtions:
                    fragmentClass = SubscriptionListFragment.class;
                    break;

                default:
                    fragmentClass = SearchAnnouncementFragment.class;
            }
            switchToFragment(fragmentClass, item);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
                    Log.e(NotificationService.TAG,
                            "Only SenderActivity can handle OPEN_ANNOUNCEMENT_LIST");
                    break;
                case OPEN_ANNOUNCEMENT_REQUEST:
                    showProgress(true);
                    getAnnouncementTask =  new GetAnnouncementTask();
                    getAnnouncementTask.execute(id);
                    break;
            }
        }
    }

    private class GetAnnouncementTask extends NetworkTask<Long, Void, ShipmentAnnouncement> {

        @Override
        protected ShipmentAnnouncement runInBackground(Long[] params) throws NetworkException {
            long id = params[0];
            return networkClient.getShipmentAnnouncement(id);
        }

        @Override
        protected void onPostExecute(ShipmentAnnouncement result) {
            getAnnouncementTask = null;
            showProgress(false);

            if(networkException != null){
                networkException.handleException(DelivererActivity.this);
                return;
            }

            SendRequestFragment fragment = (SendRequestFragment)switchToFragment(
                    SendRequestFragment.class, navigationView.getMenu().getItem(0));
            fragment.setShipmentAnnouncement(result);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        int code = PermissionHandler.handleRequestResult(requestCode, permissions, grantResults);
        if(code == LOCATION_REQUEST) {
            showResult(R.string.error_location_permission);

        } else if(code == CAMERA_REQUEST) {
            showResult(R.string.error_camera_permission);

        }
    }
}
